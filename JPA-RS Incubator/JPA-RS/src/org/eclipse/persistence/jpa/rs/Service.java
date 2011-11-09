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
 * 		dclarke - 
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs;

import static org.eclipse.persistence.jaxb.JAXBContext.MEDIA_TYPE;
import static org.eclipse.persistence.jpa.rs.util.StreamingOutputMarshaller.mediaType;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jpa.rs.util.IdHelper;
import org.eclipse.persistence.jpa.rs.util.StreamingOutputMarshaller;

import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;

/**
 * JAX-RS application interface for avatar POC clients. All /app requests are
 * handled here.
 * 
 * @author douglas.clarke
 * @since Avatar POC - September 2011
 */
@Singleton
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Path("/")
public class Service {
	static final Logger logger = Logger.getLogger("AppService");	

    private PersistenceUnitHelper repository;

    public PersistenceUnitHelper getRepository() {
        return repository;
    }

    @Inject
    public void setRepository(PersistenceUnitHelper repository) {
        this.repository = repository;
    }
    
    @GET
    @Path("{context}/entity/{type}")
    public Response find(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, @Context UriInfo ui) {
        PersistenceUnitWrapper app = get(persistenceUnit);
        Object id = IdHelper.buildId(app, type, ui.getQueryParameters());

        Object entity = app.find(getTenantId(hh), type, id);
        ResponseBuilder rb = new ResponseBuilderImpl();
        if (entity == null) {
            rb.status(Status.NOT_FOUND);
        } else {
            rb.status(Status.OK);
            rb.entity(new StreamingOutputMarshaller(app.getJAXBContext(), entity, hh.getAcceptableMediaTypes()));
        }
        return rb.build();
    }

    @PUT
    @Path("{context}/entity/{type}")
    public Response create(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, InputStream in) {
        PersistenceUnitWrapper app = get(persistenceUnit);
        DynamicEntity entity = unmarshalEntity(app, type, getTenantId(hh), mediaType(hh.getAcceptableMediaTypes()), in);

        app.create(getTenantId(hh), entity);

        ResponseBuilder rb = new ResponseBuilderImpl();
        rb.status(Status.OK);
        rb.entity(new StreamingOutputMarshaller(app.getJAXBContext(), entity, hh.getAcceptableMediaTypes()));
        return rb.build();
    }

    @POST
    @Path("{context}/entity/{type}")
    public StreamingOutput update(@PathParam("context") String persistenceUnit, @PathParam("type") String type, @Context HttpHeaders hh, InputStream in) {
        PersistenceUnitWrapper app = get(persistenceUnit);
        String tenantId = getTenantId(hh);
        MediaType contentType = mediaType(hh.getRequestHeader(HttpHeaders.CONTENT_TYPE)); 

        DynamicEntity entity = unmarshalEntity(app, type, tenantId, contentType, in);
        entity = app.merge(tenantId, entity);

        JAXBContext context = app.getJAXBContext();
        return new StreamingOutputMarshaller(context, entity, hh.getAcceptableMediaTypes());
    }

    @GET
    @Path("{context}/query/{name}")
    public StreamingOutput namedQuery(@PathParam("context") String persistenceUnit, @PathParam("name") String name, @Context HttpHeaders hh, @Context UriInfo ui) {
        PersistenceUnitWrapper app = get(persistenceUnit);
        Object result = app.query(name, Service.getParameterMap(ui), false);
        JAXBContext context = app.getJAXBContext();
        return new StreamingOutputMarshaller(context, result, hh.getAcceptableMediaTypes());
    }
    
    @GET
    @Path("{context}/singleResultQuery/{name}")
    @Produces(MediaType.WILDCARD)
    public StreamingOutput namedQuerySingleResult(@PathParam("context") String persistenceUnit, @PathParam("name") String name, @Context HttpHeaders hh, @Context UriInfo ui) {
        PersistenceUnitWrapper app = get(persistenceUnit);
        Object result = app.query(name, Service.getParameterMap(ui), true);
        JAXBContext context = app.getJAXBContext();
        return new StreamingOutputMarshaller(context, result, hh.getAcceptableMediaTypes());
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
    
    @GET
    @Path("{context}/query")
    public StreamingOutput adhocQuery(@PathParam("context") String persistenceUnit, @Context HttpHeaders hh, @Context UriInfo ui) {
        throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
    }

    private PersistenceUnitWrapper get(String persistenceUnit) {
        PersistenceUnitWrapper app = getRepository().getPersistenceUnit(persistenceUnit);

        if (app == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        return app;
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

    private DynamicEntity unmarshalEntity(PersistenceUnitWrapper app, String type, String tenantId, MediaType acceptedMedia, InputStream in) {
        Unmarshaller unmarshaller;
        try {
            unmarshaller = app.getJAXBContext().createUnmarshaller();
            unmarshaller.setProperty(MEDIA_TYPE, acceptedMedia.toString());
            JAXBElement<?> element = unmarshaller.unmarshal(new StreamSource(in), app.getClass(type));
            return (DynamicEntity) element.getValue();
        } catch (JAXBException e) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
    }

}