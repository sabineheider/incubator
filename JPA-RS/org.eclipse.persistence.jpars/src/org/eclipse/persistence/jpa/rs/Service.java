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

import static org.eclipse.persistence.jaxb.JAXBContext.MEDIA_TYPE;
import static org.eclipse.persistence.jpa.rs.util.StreamingOutputMarshaller.mediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.internal.queries.MapContainerPolicy;
import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.rs.metadata.DatabaseMetadataStore;
import org.eclipse.persistence.jpa.rs.metadata.model.Attribute;
import org.eclipse.persistence.jpa.rs.metadata.model.Descriptor;
import org.eclipse.persistence.jpa.rs.metadata.model.Link;
import org.eclipse.persistence.jpa.rs.metadata.model.LinkTemplate;
import org.eclipse.persistence.jpa.rs.metadata.model.Parameter;
import org.eclipse.persistence.jpa.rs.metadata.model.PersistenceUnit;
import org.eclipse.persistence.jpa.rs.metadata.model.Query;
import org.eclipse.persistence.jpa.rs.metadata.model.SessionBeanCall;
import org.eclipse.persistence.jpa.rs.util.IdHelper;
import org.eclipse.persistence.jpa.rs.util.StreamingOutputMarshaller;
import org.eclipse.persistence.mappings.CollectionMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.mappings.foundation.AbstractDirectMapping;
import org.eclipse.persistence.queries.DatabaseQuery;

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
       factory.setMetadataStore(new DatabaseMetadataStore());
       List<String> datasourceValues = hh.getRequestHeader("datasourceName");
       Map<String, Object> properties = new HashMap<String, Object>();
       if (datasourceValues != null && datasourceValues.size() > 0){
           properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, datasourceValues.get(0));
       }
       factory.getMetadataStore().setProperties(properties);
       rb.status(Status.CREATED);
       return rb.build();
   }
   
   @GET
   @Path("/")
   public Response getContexts(@Context HttpHeaders hh, @Context UriInfo uriInfo) throws JAXBException {
       ResponseBuilder rb = new ResponseBuilderImpl();
       Set<String> contexts = factory.getPersistenceContextNames();
       Iterator<String> contextIterator = contexts.iterator();
       List<Link> links = new ArrayList<Link>();
       String mediaType = StreamingOutputMarshaller.mediaType(hh.getAcceptableMediaTypes()).toString();
       while (contextIterator.hasNext()){
           String context = contextIterator.next();
           links.add(new Link(context, mediaType, "\"href\": \"" + uriInfo.getBaseUri() + context + "/metadata\""));
       }
       String result = null;
       result = marshallMetadata(links, mediaType);
       rb.status(Status.OK);
       rb.entity(new StreamingOutputMarshaller(null, result, hh.getAcceptableMediaTypes()));
       return rb.build();
   }
   
   
   @POST
   @Path("/")
   @Produces(MediaType.WILDCARD)
   public Response callSessionBean(@Context HttpHeaders hh, @Context UriInfo ui, InputStream is) throws JAXBException, ClassNotFoundException, NamingException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
       ResponseBuilder rb = new ResponseBuilderImpl();
       SessionBeanCall call = null;
       call = unmarshallSessionBeanCall(is);

       String jndiName = call.getJndiName();
       javax.naming.Context ctx = new InitialContext();
       Object ans=ctx.lookup(jndiName);  
       if (ans == null){
           rb.status(Status.NOT_FOUND);
           return rb.build();
       }
           
       PersistenceContext context = null;
       if (call.getContext() != null){
           context = factory.getPersistenceContext(call.getContext());
           if (context == null){
               rb.status(Status.NOT_FOUND);
               return rb.build();
           }
       }
           
       Class[] parameters = new Class[call.getParameters().size()];
       Object[] args = new Object[call.getParameters().size()];
       int i = 0;
       for (Parameter param: call.getParameters()){
           System.out.println("Got paramter " + param.getValue());
           Class parameterClass = null;
           Object parameterValue = null;
           if (context != null){
               parameterClass = context.getClass(param.getTypeName());
           }
           if (parameterClass != null){
               parameterValue = context.unmarshalEntity(param.getTypeName(), null, hh.getMediaType(), is);
           } else {
               parameterClass = Thread.currentThread().getContextClassLoader().loadClass(param.getTypeName());
               parameterValue = ConversionManager.getDefaultManager().convertObject(param.getValue(), parameterClass);
           }
           parameters[i] = parameterClass;
           args[i] = parameterValue;
           i++;
       }
       Method method = ans.getClass().getMethod(call.getMethodName(), parameters);
       Object returnValue = method.invoke(ans, args);
       rb.status(Status.OK);
       rb.entity(new StreamingOutputMarshaller(null, returnValue, hh.getAcceptableMediaTypes()));
       return rb.build();
   }
   
    @PUT
    @Path("{context}")
    public Response bootstrap(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, @Context UriInfo uriInfo, InputStream in) throws IOException, MalformedURLException{
        ResponseBuilder rb = new ResponseBuilderImpl();
        String urlString = getSingleHeader("persistenceXmlURL", hh);

        PersistenceContext persistenceContext = null;
        boolean replace = false;
        List<String> replaceValues = hh.getRequestHeader("replace");
        if (replaceValues != null && replaceValues.size() > 0){
            replace = Boolean.getBoolean(replaceValues.get(0));
        }
        Map<String, Object> properties = new HashMap<String, Object>();
        if (urlString != null){
            URL url = new URL(urlString);
            persistenceContext = factory.bootstrapPersistenceContext(persistenceUnit, url, properties, replace);
        } else {
            persistenceContext = factory.bootstrapPersistenceContext(persistenceUnit, in, properties, replace);
        }
        if (persistenceContext != null){
            persistenceContext.setBaseURI(uriInfo.getBaseUri());
            rb.status(Status.CREATED);
        }

        return rb.build();
    }
    
    @GET
    @Path("{context}/metadata")
    public Response getTypes(@PathParam("context") String persistenceUnit, @Context HttpHeaders hh, @Context UriInfo uriInfo) {
        ResponseBuilder rb = new ResponseBuilderImpl();
        PersistenceContext app = get(persistenceUnit, uriInfo.getBaseUri());
        if (app == null){
            rb.status(Status.NOT_FOUND);
        } else {
            PersistenceUnit pu = new PersistenceUnit();
            pu.setPersistenceUnitName(persistenceUnit);
            Map<Class, ClassDescriptor> descriptors = JpaHelper.getServerSession(app.getEmf()).getDescriptors();
            String mediaType = StreamingOutputMarshaller.mediaType(hh.getAcceptableMediaTypes()).toString();
            Iterator<Class> contextIterator = descriptors.keySet().iterator();
            while (contextIterator.hasNext()){
                ClassDescriptor descriptor = descriptors.get(contextIterator.next());
                pu.getTypes().add(new Link(descriptor.getAlias(), mediaType, uriInfo.getBaseUri() + persistenceUnit + "/metadata/entity/" + descriptor.getAlias()));
            }           
            String result = null;
            try {
                result = marshallMetadata(pu, mediaType);
            } catch (JAXBException e){
                rb.status(Status.INTERNAL_SERVER_ERROR);
                return rb.build();
            }
            rb.status(Status.OK);
            rb.header("Content-Type", MediaType.APPLICATION_JSON);
            rb.entity(new StreamingOutputMarshaller(null , result, hh.getAcceptableMediaTypes()));
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
        PersistenceContext app = get(persistenceUnit, ui.getBaseUri());
        if (app == null){
            rb.status(Status.NOT_FOUND);
        }
        app.subscribeToEventNotification(name);

        rb.status(Status.OK);
        return rb.build();
    }
    
    @GET
    @Path("{context}/metadata/entity/{descriptorAlias}")
    public Response getDescriptorMetadata(@PathParam("context") String persistenceUnit, @PathParam("descriptorAlias") String descriptorAlias, @Context HttpHeaders hh, @Context UriInfo uriInfo) {
        ResponseBuilder rb = new ResponseBuilderImpl();
        PersistenceContext app = get(persistenceUnit, uriInfo.getBaseUri());
        if (app == null){
            rb.status(Status.NOT_FOUND);
        } else {
            ClassDescriptor descriptor = JpaHelper.getServerSession(app.getEmf()).getDescriptorForAlias(descriptorAlias);
            if (descriptor == null){
                rb.status(Status.NOT_FOUND);
            } else {
                String mediaType = StreamingOutputMarshaller.mediaType(hh.getAcceptableMediaTypes()).toString();
                Descriptor returnDescriptor = buildDescriptor(app, persistenceUnit, descriptor, uriInfo.getBaseUri().toString());
                rb.status(Status.OK);
                String result = null;
                try {
                    result = marshallMetadata(returnDescriptor, mediaType);
                } catch (JAXBException e){
                    rb.status(Status.INTERNAL_SERVER_ERROR);
                    return rb.build();
                }
                rb.status(Status.OK);
                rb.entity(new StreamingOutputMarshaller(null , result, hh.getAcceptableMediaTypes()));
            }
        }
        return rb.build();
    }
    
    @GET
    @Path("{context}/metadata/query/")
    public Response getQueryMetadata(@PathParam("context") String persistenceUnit, @Context HttpHeaders hh, @Context UriInfo uriInfo) {
        ResponseBuilder rb = new ResponseBuilderImpl();
        PersistenceContext app = get(persistenceUnit, uriInfo.getBaseUri());
        if (app == null){
            rb.status(Status.NOT_FOUND);
        } else {
            StringBuffer buffer = new StringBuffer();
            List<Query> queries = new ArrayList<Query>();
            addQueries(queries, app, null);
            String mediaType = StreamingOutputMarshaller.mediaType(hh.getAcceptableMediaTypes()).toString();
            rb.status(Status.OK);
            String result = null;
            try {
                result = marshallMetadata(queries, mediaType);
            } catch (JAXBException e){
                rb.status(Status.INTERNAL_SERVER_ERROR);
                return rb.build();
            }
            rb.status(Status.OK);
            rb.entity(new StreamingOutputMarshaller(null , result, hh.getAcceptableMediaTypes()));    
        }
        return rb.build();
    }
    
    @GET
    @Path("{context}/entity/{type}/{key}")
    public Response find(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @PathParam("key") String key, @Context HttpHeaders hh, @Context UriInfo ui) {
        ResponseBuilder rb = new ResponseBuilderImpl();
        PersistenceContext app = get(persistenceUnit, ui.getBaseUri());
        if (app == null || app.getClass(type) == null){
            rb.status(Status.NOT_FOUND);
            return rb.build();
        }
        Object id = IdHelper.buildId(app, type, key);

        Object entity = app.find(getTenantId(hh), type, id, Service.getHintMap(ui));

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
    public Response create(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, @Context UriInfo uriInfo, InputStream in) throws JAXBException {
        PersistenceContext app = get(persistenceUnit, uriInfo.getBaseUri());
        ResponseBuilder rb = new ResponseBuilderImpl();
        ClassDescriptor descriptor = app.getDescriptor(type);
        if (app == null || descriptor == null){
            rb.status(Status.NOT_FOUND);
            return rb.build();
        }
        Object entity = null;
        try{
            entity = app.unmarshalEntity(type, getTenantId(hh), mediaType(hh.getAcceptableMediaTypes()), in);
        } catch (JAXBException e){
            throw e;
        }

        // maintain itempotence on PUT by disallowing sequencing and cascade persist.
        AbstractDirectMapping sequenceMapping = descriptor.getObjectBuilder().getSequenceMapping();
        if (sequenceMapping != null){
            Object value = sequenceMapping.getAttributeAccessor().getAttributeValueFromObject(entity);

            if (descriptor.getObjectBuilder().isPrimaryKeyComponentInvalid(value, descriptor.getPrimaryKeyFields().indexOf(descriptor.getSequenceNumberField())) || descriptor.getSequence().shouldAlwaysOverrideExistingValue()){
                rb.status(Status.BAD_REQUEST);
                return rb.build();
            }
        }
        for (DatabaseMapping mapping: descriptor.getObjectBuilder().getRelationshipMappings()){
            if (mapping.isForeignReferenceMapping()){
                if (((ForeignReferenceMapping)mapping).isCascadePersist()){
                    Object value = mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
                    if (value != null){
                        rb.status(Status.BAD_REQUEST);
                        return rb.build();
                    }
                }
            }
        }

        app.create(getTenantId(hh), entity);
        rb.status(Status.CREATED);
        rb.entity(new StreamingOutputMarshaller(app, entity, hh.getAcceptableMediaTypes()));
        return rb.build();
    }

    @POST
    @Path("{context}/entity/{type}")
    public Response update(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, @Context UriInfo uriInfo, InputStream in) {
        PersistenceContext app = get(persistenceUnit, uriInfo.getBaseUri());
        ResponseBuilder rb = new ResponseBuilderImpl();
        if (app == null || app.getClass(type) == null){
            rb.status(Status.NOT_FOUND);
            return rb.build();
        }
        String tenantId = getTenantId(hh);
        MediaType contentType = mediaType(hh.getRequestHeader(HttpHeaders.CONTENT_TYPE)); 
        Object entity = null;
        try {
            entity = app.unmarshalEntity(type, tenantId, contentType, in);
        } catch (JAXBException e){
            throw new WebApplicationException(e);
        }
        entity = app.merge(tenantId, entity);
        rb.entity(new StreamingOutputMarshaller(app, entity, hh.getAcceptableMediaTypes()));
        return rb.build();
    }

    @DELETE
    @Path("{context}/entity/{type}/{key}")
    public Response delete(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @PathParam("key") String key, @Context HttpHeaders hh, @Context UriInfo ui) {
        ResponseBuilder rb = new ResponseBuilderImpl();
        PersistenceContext app = get(persistenceUnit, ui.getBaseUri());
        if (app == null || app.getClass(type) == null){
            rb.status(Status.NOT_FOUND);
            return rb.build();
        }
        String tenantId = getTenantId(hh);
        Object id = IdHelper.buildId(app, type, key);
        app.delete(tenantId, type, id);
        rb.status(Status.OK);
        return rb.build();
    }
    
    @GET
    @Path("{context}/query/{name}")
    public Response namedQuery(@PathParam("context") String persistenceUnit, @PathParam("name") String name, @Context HttpHeaders hh, @Context UriInfo ui) {
        PersistenceContext app = get(persistenceUnit, ui.getBaseUri());
        ResponseBuilder rb = new ResponseBuilderImpl();
        if (app == null){
            rb.status(Status.NOT_FOUND);
            return rb.build();
        }
        Object result = app.query(name, Service.getParameterMap(ui), Service.getHintMap(ui), false, false);

        rb.status(Status.OK);
        rb.entity(new StreamingOutputMarshaller(app, result, hh.getAcceptableMediaTypes()));
        return rb.build();
    }
    
    @POST
    @Path("{context}/query/{name}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM})
    public Response namedQueryUpdate(@PathParam("context") String persistenceUnit, @PathParam("name") String name, @Context HttpHeaders hh, @Context UriInfo ui) {
        PersistenceContext app = get(persistenceUnit, ui.getBaseUri());
        ResponseBuilder rb = new ResponseBuilderImpl();
        if (app == null){
            rb.status(Status.NOT_FOUND);
            return rb.build();
        }
        Object result = app.query(name, Service.getParameterMap(ui), Service.getHintMap(ui), false, true);
        rb.status(Status.OK);
        rb.entity(new StreamingOutputMarshaller(app, result.toString(), hh.getAcceptableMediaTypes()));
        return rb.build();
    }
    
    @GET
    @Path("{context}/singleResultQuery/{name}")
    @Produces(MediaType.WILDCARD)
    public Response namedQuerySingleResult(@PathParam("context") String persistenceUnit, @PathParam("name") String name, @Context HttpHeaders hh, @Context UriInfo ui) {
        PersistenceContext app = get(persistenceUnit, ui.getBaseUri());
        ResponseBuilder rb = new ResponseBuilderImpl();
        if (app == null){
            rb.status(Status.NOT_FOUND);
            return rb.build();
        }
        Object result = app.query(name, Service.getParameterMap(ui), Service.getHintMap(ui), true, false);
        rb.status(Status.OK);
        rb.entity(new StreamingOutputMarshaller(app, result, hh.getAcceptableMediaTypes()));
        return rb.build();
    }
    
    protected Descriptor buildDescriptor(PersistenceContext app, String persistenceUnit, ClassDescriptor descriptor, String baseUri){
        Descriptor returnDescriptor = new Descriptor();
        returnDescriptor.setName(descriptor.getAlias());
        returnDescriptor.setType(descriptor.getJavaClassName());
        returnDescriptor.getLinkTemplates().add(new LinkTemplate("find", "get", baseUri + persistenceUnit + "/entity/" + descriptor.getAlias() + "/{primaryKey}"));
        returnDescriptor.getLinkTemplates().add(new LinkTemplate("persist", "put", baseUri + persistenceUnit + "/entity/" + descriptor.getAlias()));
        returnDescriptor.getLinkTemplates().add(new LinkTemplate("update", "post", baseUri + persistenceUnit + "/entity/" + descriptor.getAlias()));
        returnDescriptor.getLinkTemplates().add(new LinkTemplate("delete", "delete", baseUri + persistenceUnit + "/entity/" + descriptor.getAlias() + "/{primaryKey}"));
        
        if (!descriptor.getMappings().isEmpty()){
            Iterator<DatabaseMapping> mappingIterator = descriptor.getMappings().iterator();
            while (mappingIterator.hasNext()){
                DatabaseMapping mapping = mappingIterator.next();
                addMapping(returnDescriptor, mapping);
            }
        }
        addQueries(returnDescriptor.getQueries(), app, descriptor.getJavaClassName());
        return returnDescriptor;
    }
    
    protected void addMapping(Descriptor descriptor, DatabaseMapping mapping){
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
        descriptor.getAttributes().add(new Attribute(mapping.getAttributeName(), target));
    }
    
    protected void addQueries(List<Query>  queryList, PersistenceContext app, String javaClassName){
        Map<String, List<DatabaseQuery>> queries = JpaHelper.getServerSession(app.getEmf()).getQueries();
        List<DatabaseQuery> returnQueries = new ArrayList<DatabaseQuery>();
        for (String key: queries.keySet()){
            List<DatabaseQuery> keyQueries = queries.get(key);
            Iterator<DatabaseQuery> queryIterator = keyQueries.iterator();
            while (queryIterator.hasNext()){
                DatabaseQuery query= queryIterator.next();
                if (javaClassName == null || query.getReferenceClassName().equals(javaClassName)){
                    returnQueries.add(query);
                }
            }
         }
        Iterator<DatabaseQuery> queryIterator = returnQueries.iterator();
        while(queryIterator.hasNext()){
            DatabaseQuery query= queryIterator.next();
            String method = query.isReadQuery() ? "get" : "post";
            String referenceClass = query.getReferenceClassName() == null ? "" : query.getReferenceClassName();
            String jpql = query.getJPQLString() == null? "" : query.getJPQLString();
            queryList.add(new Query(query.getName(), referenceClass, jpql, new LinkTemplate("execute", method, app.getBaseURI() + app.getName() + "/query/" + query.getName() + "/{parameters}")));
            
        }
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
    
    private String getSingleHeader(String parameterName, HttpHeaders hh){
        List<String> params = hh.getRequestHeader(parameterName);
        if (params == null || params.isEmpty()) {
            return null;
        }
        if (params.size() != 1) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        return params.get(0);
    }

    
    protected String marshallMetadata(Object metadata, String mediaType) throws JAXBException {
        Class[] jaxbClasses = new Class[]{Link.class, Attribute.class, Descriptor.class, LinkTemplate.class, PersistenceUnit.class, Query.class};
        JAXBContext context = (JAXBContext)JAXBContextFactory.createContext(jaxbClasses, null);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(JAXBContext.JSON_INCLUDE_ROOT, Boolean.FALSE);
        marshaller.setProperty(MEDIA_TYPE, mediaType);
        StringWriter writer = new StringWriter();
        marshaller.marshal(metadata, writer);
        return writer.toString();
    }
    
    protected SessionBeanCall unmarshallSessionBeanCall(InputStream data) throws JAXBException {
        Class[] jaxbClasses = new Class[]{SessionBeanCall.class};
        JAXBContext context = (JAXBContext)JAXBContextFactory.createContext(jaxbClasses, null);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setProperty(JAXBContext.JSON_INCLUDE_ROOT, Boolean.FALSE);
        unmarshaller.setProperty(MEDIA_TYPE, MediaType.APPLICATION_JSON);
        StreamSource ss = new StreamSource(data);
        return unmarshaller.unmarshal(ss, SessionBeanCall.class).getValue();
    }
}
