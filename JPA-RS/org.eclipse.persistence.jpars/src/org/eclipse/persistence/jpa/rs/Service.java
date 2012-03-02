/*******************************************************************************
 * Copyright (c) 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 		dclarke/tware - initial 
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs;

import static org.eclipse.persistence.jpa.rs.util.StreamingOutputMarshaller.mediaType;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.queries.MapContainerPolicy;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.rs.metadata.DatabaseMetadataStore;
import org.eclipse.persistence.jpa.rs.util.IdHelper;
import org.eclipse.persistence.jpa.rs.util.StreamingOutputMarshaller;
import org.eclipse.persistence.mappings.CollectionMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;

import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;

/**
 * JAX-RS application interface JPA-RS
 * 
 * @author dclarke
 * @since EclipseLink 2.4.0
 */
@Singleton
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Path("/")
public class Service {
	static final Logger logger = Logger.getLogger("AppService");	

    private PersistenceFactory factory;

    public PersistenceFactory getPersistenceFactory() {
        return factory;
    }

   @EJB
    public void setPersistenceFactory(PersistenceFactory factory) {
        this.factory = factory;
    }
    
   @PUT
   @Path("/")
   @Consumes({ MediaType.WILDCARD})
   public Response start(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, InputStream in){
       ResponseBuilder rb = new ResponseBuilderImpl();
       try{
           factory.setMetadataStore(new DatabaseMetadataStore());
           List<String> datasourceValues = hh.getRequestHeader("datasourceName");
           Map<String, Object> properties = new HashMap<String, Object>();
           if (datasourceValues != null && datasourceValues.size() > 0){
               properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, datasourceValues.get(0));
           }
           factory.getMetadataStore().setProperties(properties);
       } catch (Exception e){
           rb.status(Status.NOT_FOUND);
           return rb.build();
       }
       rb.status(Status.CREATED);
       return rb.build();
   }
   
   @GET
   @Path("/")
   @Consumes({ MediaType.WILDCARD})
   public Response getContexts(@Context HttpHeaders hh) {
       ResponseBuilder rb = new ResponseBuilderImpl();
       Set<String> contexts = factory.getPersistenceContextNames();
       StringBuffer buffer = new StringBuffer();
       buffer.append("[");
       Iterator<String> contextIterator = contexts.iterator();
       while (contextIterator.hasNext()){
           buffer.append("\"" + contextIterator.next() + "\"");
           if (contextIterator.hasNext()){
               buffer.append(", ");
           }
       }           
       buffer.append("]");
       rb.status(Status.OK);    
       rb.entity(new StreamingOutputMarshaller(null, buffer.toString(), hh.getAcceptableMediaTypes()));
       return rb.build();
   }
   
    @PUT
    @Path("{context}")
    public Response bootstrap(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, @Context UriInfo uriInfo, InputStream in){
        ResponseBuilder rb = new ResponseBuilderImpl();
        String urlString = getURL(hh);

        PersistenceContext persistenceContext = null;
        boolean replace = false;
        List<String> replaceValues = hh.getRequestHeader("replace");
        if (replaceValues != null && replaceValues.size() > 0){
            replace = Boolean.getBoolean(replaceValues.get(0));
        }
        Map<String, Object> properties = new HashMap<String, Object>();
        try{
            if (urlString != null){
                URL url = new URL(urlString);
                persistenceContext = factory.bootstrapPersistenceContext(persistenceUnit, url, properties, replace);
            } else {
                persistenceContext = factory.bootstrapPersistenceContext(persistenceUnit, in, properties, replace);
            }
       } catch (Exception e){
            e.printStackTrace();
            rb.status(Status.NOT_FOUND);
        }
        if (persistenceContext != null){
            persistenceContext.setBaseURI(uriInfo.getBaseUri());
            rb.status(Status.CREATED);
        }

        return rb.build();
    }
    
    @GET
    @Path("{context}")
    @Consumes({ MediaType.WILDCARD})
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTypes(@PathParam("context") String persistenceUnit, @Context HttpHeaders hh, @Context UriInfo uriInfo) {
        ResponseBuilder rb = new ResponseBuilderImpl();
        PersistenceContext app = get(persistenceUnit, uriInfo.getBaseUri());
        if (app == null){
            rb.status(Status.NOT_FOUND);
        } else {
            Map<Class, ClassDescriptor> descriptors = JpaHelper.getServerSession(app.getEmf()).getDescriptors();
            StringBuffer buffer = new StringBuffer();
            
            buffer.append("[");
            Iterator<Class> contextIterator = descriptors.keySet().iterator();
            while (contextIterator.hasNext()){
                ClassDescriptor descriptor = descriptors.get(contextIterator.next());
                appendDescriptor(buffer, descriptor);
                if (contextIterator.hasNext()){
                    buffer.append(", ");
                }
            }           
            buffer.append("]");
            rb.status(Status.OK);
            rb.entity(new StreamingOutputMarshaller(null , buffer.toString(), hh.getAcceptableMediaTypes()));
        }
        return rb.build();
    }
    
    @DELETE
    @Path("{context}")
    public Response removeContext(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, InputStream in){
        ResponseBuilder rb = new ResponseBuilderImpl();
        factory.closePersistenceContext(persistenceUnit);
        rb.status(Status.OK);
        return rb.build();
    }

    @PUT
    @Path("{context}/subscribe/{name}")
    public Response subscribe(@PathParam("context") String persistenceUnit, @PathParam("name") String name, @Context UriInfo ui) {
        ResponseBuilder rb = new ResponseBuilderImpl();
        System.out.println("Subscribe " + name);
        PersistenceContext app = get(persistenceUnit, ui.getBaseUri());
        if (app == null){
            rb.status(Status.NOT_FOUND);
        }
        app.subscribeToEventNotification(name);

        rb.status(Status.OK);
        return rb.build();
    }
    
    @GET
    @Path("{context}/entity/{type}")
    public Response find(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, @Context UriInfo ui) {
        PersistenceContext app = get(persistenceUnit, ui.getBaseUri());
        Object id = IdHelper.buildId(app, type, ui.getQueryParameters());

        Object entity = app.find(getTenantId(hh), type, id);
        ResponseBuilder rb = new ResponseBuilderImpl();
        if (entity == null) {
            rb.status(Status.NOT_FOUND);
        } else {
            rb.status(Status.OK);
            rb.entity(new StreamingOutputMarshaller(app, entity, hh.getAcceptableMediaTypes()));
        }
        return rb.build();
    }

    @PUT
    @Path("{context}/entity/{type}")
    public Response create(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, @Context UriInfo uriInfo, InputStream in) {
        PersistenceContext app = get(persistenceUnit, uriInfo.getBaseUri());
        Object entity = null;
        try {
            entity = app.unmarshalEntity(type, getTenantId(hh), mediaType(hh.getAcceptableMediaTypes()), in);
        } catch (JAXBException e){
            throw new WebApplicationException(e);
        }
        app.create(getTenantId(hh), entity);

        ResponseBuilder rb = new ResponseBuilderImpl();
        rb.status(Status.CREATED);
        rb.entity(new StreamingOutputMarshaller(app, entity, hh.getAcceptableMediaTypes()));
        return rb.build();
    }

    @POST
    @Path("{context}/entity/{type}")
    public StreamingOutput update(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, @Context UriInfo uriInfo, InputStream in) {
        PersistenceContext app = get(persistenceUnit, uriInfo.getBaseUri());
        String tenantId = getTenantId(hh);
        MediaType contentType = mediaType(hh.getRequestHeader(HttpHeaders.CONTENT_TYPE)); 
        Object entity = null;
        try {
            entity = app.unmarshalEntity(type, tenantId, contentType, in);
        } catch (JAXBException e){
            throw new WebApplicationException(e);
        }
        entity = app.merge(tenantId, entity);
        return new StreamingOutputMarshaller(app, entity, hh.getAcceptableMediaTypes());
    }

    @GET
    @Path("{context}/query")
    public StreamingOutput adhocQuery(@PathParam("context") String persistenceUnit, @Context HttpHeaders hh, @Context UriInfo ui) {
        throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }
    
    @GET
    @Path("{context}/query/{name}")
    public StreamingOutput namedQuery(@PathParam("context") String persistenceUnit, @PathParam("name") String name, @Context HttpHeaders hh, @Context UriInfo ui) {
        long millis = System.currentTimeMillis();
        System.out.println("Start Named Query " + name);
        PersistenceContext app = get(persistenceUnit, ui.getBaseUri());
        Object result = app.query(name, Service.getParameterMap(ui), Service.getHintMap(ui), false);


        System.out.println("Named Query " + name + " Marshalling. time: " + (System.currentTimeMillis() - millis));
        return new StreamingOutputMarshaller(app, result, hh.getAcceptableMediaTypes());
    }
    
    @GET
    @Path("{context}/singleResultQuery/{name}")
    @Produces(MediaType.WILDCARD)
    public StreamingOutput namedQuerySingleResult(@PathParam("context") String persistenceUnit, @PathParam("name") String name, @Context HttpHeaders hh, @Context UriInfo ui) {
        PersistenceContext app = get(persistenceUnit, ui.getBaseUri());
        Object result = app.query(name, Service.getParameterMap(ui), Service.getHintMap(ui), true);
        return new StreamingOutputMarshaller(app, result, hh.getAcceptableMediaTypes());
    }
    
    protected void appendDescriptor(StringBuffer buffer, ClassDescriptor descriptor){
        buffer.append("{\"name\": ");
        buffer.append("\"" + descriptor.getAlias() + "\"");
        buffer.append(", \"type\":\"" + descriptor.getJavaClassName() + "\"");
        if (!descriptor.getMappings().isEmpty()){
            buffer.append(", \"attributes\":[");
            Iterator<DatabaseMapping> mappingIterator = descriptor.getMappings().iterator();
            while (mappingIterator.hasNext()){
                DatabaseMapping mapping = mappingIterator.next();
                appendMapping(buffer, mapping);
                if (mappingIterator.hasNext()){
                    buffer.append(", ");
                }
            }
            buffer.append("]");
        }
        buffer.append("}");
    }
    
    protected void appendMapping(StringBuffer buffer, DatabaseMapping mapping){
        buffer.append("{\"name\": \"" + mapping.getAttributeName() + "\", ");
        String target = null;
        if (mapping.isCollectionMapping()){
            CollectionMapping collectionMapping = (CollectionMapping)mapping;
            String collectionType = collectionMapping.getContainerPolicy().getContainerClassName();
            if (collectionMapping.getContainerPolicy().isMapPolicy()){
                String mapKeyType = ((MapContainerPolicy)collectionMapping.getContainerPolicy()).getKeyType().toString();
                target = collectionType + "<" +  mapKeyType + ", " + collectionMapping.getReferenceClassName() + ">";
            } else {
                target = collectionType + "<" + collectionMapping.getReferenceClassName() + ">";
            }
        } else if (mapping.isForeignReferenceMapping()){
            target = ((ForeignReferenceMapping)mapping).getReferenceClass().getName();
        } else {
            target = mapping.getAttributeClassification().getName();
        }
        buffer.append("\"type\": \"" + target + "\"");
        buffer.append("}");
    }
    
    @PreDestroy
    public void close() {
        factory.close();
    }

    private PersistenceContext get(String persistenceUnit, URI defaultURI) {
        PersistenceContext app = getPersistenceFactory().getPersistenceContext(persistenceUnit);

        if (app == null){
            try{
                DynamicClassLoader dcl = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(PersistenceUnitProperties.CLASSLOADER, dcl);
                EntityManagerFactory factory = Persistence.createEntityManagerFactory(persistenceUnit, properties);
                if (factory != null){
                    app = getPersistenceFactory().bootstrapPersistenceContext(persistenceUnit, factory, defaultURI, true);
                }
            } catch (Exception e){}
        }
        
        if (app == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        return app;
    }
    
    private static Map<String, Object> getHintMap(UriInfo info){
        Map<String, Object> hints = new HashMap<String, Object>();
         for(String key :  info.getQueryParameters().keySet()) { 
            hints.put(key, info.getQueryParameters().getFirst(key));  
        }
        return hints;
    }
    
    /**
     * This method has been temporarily added to allow processing of either query or matrix parameters
     * When the final protocol is worked out, it should be removed or altered.
     * 
     * Here we check for query parameters and if they don't exist, we get the matrix parameters.
     * @param info
     * @return
     */
    private static Map<String, Object> getParameterMap(UriInfo info){
        Map<String, Object> parameters = new HashMap<String, Object>();
        PathSegment pathSegment = info.getPathSegments().get(info.getPathSegments().size() - 1); 
        for(Entry<String, List<String>> entry : pathSegment.getMatrixParameters().entrySet()) { 
            parameters.put(entry.getKey(), entry.getValue().get(0)); 
        }
        return parameters;
    }

    private String getTenantId(HttpHeaders hh) {
        List<String> tenantIdValues = hh.getRequestHeader("tenant-id");
        if (tenantIdValues == null || tenantIdValues.isEmpty()) {
            return null;
        }
        if (tenantIdValues.size() != 1) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        return tenantIdValues.get(0);
    }
    
    private String getURL(HttpHeaders hh){
        List<String> persistenceXmlURLs = hh.getRequestHeader("persistenceXmlURL");
        if (persistenceXmlURLs == null || persistenceXmlURLs.isEmpty()) {
            return null;
        }
        if (persistenceXmlURLs.size() != 1) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        return persistenceXmlURLs.get(0);
    }

}