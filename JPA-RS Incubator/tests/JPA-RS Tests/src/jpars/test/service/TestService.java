/****************************************************************************
 * Copyright (c) 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *      tware - 
 ******************************************************************************/
package jpars.test.service;

import static org.eclipse.persistence.jaxb.JAXBContext.MEDIA_TYPE;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import jpars.test.util.ExamplePropertiesLoader;
import jpars.test.util.TestHttpHeaders;
import jpars.test.util.TestURIInfo;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.jaxb.JAXBMarshaller;
import org.eclipse.persistence.jpa.rs.PersistenceContext;
import org.eclipse.persistence.jpa.rs.PersistenceFactory;
import org.eclipse.persistence.jpa.rs.Service;
import org.eclipse.persistence.jpa.rs.metadata.DatabaseMetadataStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the JPA RS service class
 * @author tware
 *
 */
public class TestService {

    private static PersistenceFactory factory;
    
    @BeforeClass
    public static void setup(){
        Map<String, Object> properties = new HashMap<String, Object>();
        ExamplePropertiesLoader.loadProperties(properties); 
        factory = null;
        try{
            factory = new PersistenceFactory();
            factory.setMetadataStore(new DatabaseMetadataStore());
            factory.getMetadataStore().setProperties(properties);
            factory.getMetadataStore().clearMetadata();
            factory.bootstrapPersistenceContext("auction", new URL("file:///C:/EclipseLinkView2/incubator/JPA-RS Incubator/tests/JPA-RS Tests/src/xmldocs/auction-persistence.xml"), properties, true);
            clearData();
        } catch (Exception e){
            fail(e.toString());
        }
    }
    
    @AfterClass
    public static void teardown(){
        clearData();
        factory.getMetadataStore().clearMetadata();
    }
    
    protected static void clearData(){
        EntityManager em = factory.getPersistenceContext("auction").getEmf().createEntityManager();
        em.getTransaction().begin();
        em.createQuery("delete from Bid b").executeUpdate();
        em.createQuery("delete from Auction a").executeUpdate();
        em.createQuery("delete from User u").executeUpdate();
        em.getTransaction().commit();
    }
    
    @Test
    public void testUpdateList(){
        Service service = new Service();
        service.setPersistenceFactory(factory);
        PersistenceContext context = factory.getPersistenceContext("auction");
        
        DynamicEntity entity = context.newEntity("User");
        entity.set("name", "Jim");
        context.create(null, entity);
        
        entity.set("name", "James");
        
        DynamicEntity entity2 = context.newEntity("User");
        entity2.set("name", "Jill");
        context.create(null, entity2);
        
        entity2.set("name", "Gillian");    
        
        DynamicEntity entity3 = context.newEntity("Auction");
        entity3.set("name", "Computer");
        context.create(null, entity3);
        
        entity3.set("name", "Tablet");
        
        DynamicEntity serializedData = context.newEntity("auctionSerializedData");
        List<DynamicEntity> entities = new ArrayList<DynamicEntity>();
        entities.add(entity);
        entities.add(entity2);
        entities.add(entity3);
        serializedData.set("serializedData", entities);

        StreamingOutput output = service.update("auction", "auctionSerializedData", generateHTTPHeader(MediaType.APPLICATION_XML_TYPE, MediaType.APPLICATION_XML), serializeToSteam(serializedData, context, MediaType.APPLICATION_XML));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            output.write(outputStream);
        } catch (IOException ex){
            fail(ex.toString());
        }
        InputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
        serializedData = unmarshalEntity(context, "auctionSerializedData", null, MediaType.APPLICATION_XML, stream);
        
        assertNotNull("returned data was null", serializedData);
        entities = serializedData.get("serializedData");
        assertNotNull("returned data had null list", entities);
        assertTrue("returned data had wrong list size", entities.size() == 3);
        List<String> values = new ArrayList<String>();
        values.add("James");
        values.add("Gillian");
        values.add("Tablet");
        for (DynamicEntity value: entities){
            assertTrue("Incorrect name returned", value.get("name").equals("Tablet") || value.get("name").equals("James") || value.get("name").equals("Gillian"));
            values.remove(value.get("name"));
        }
        assertTrue("Incorrent set of names.", values.isEmpty());
        
        clearData();
    }
    
    @Test
    public void testRestart(){
        factory.close();
        Map<String, Object> properties = new HashMap<String, Object>();
        ExamplePropertiesLoader.loadProperties(properties); 
        factory = null;
        try{
            factory = new PersistenceFactory();
            factory.setMetadataStore(new DatabaseMetadataStore());
            factory.getMetadataStore().setProperties(properties);
            factory.initialize(properties);
        } catch (Exception e){
            fail(e.toString());
        }
        assertTrue("factory was not recreated at boot time.", factory.getPersistenceContext("auction") != null);
    }
    
    @Test
    public void testNamedQuery(){
        Service service = new Service();
        service.setPersistenceFactory(factory);
        PersistenceContext context = factory.getPersistenceContext("auction");
        
        DynamicEntity entity1 = context.newEntity("Auction");
        entity1.set("name", "Computer");
        context.create(null, entity1);
        
        DynamicEntity entity2 = context.newEntity("Auction");
        entity2.set("name", "Word Processor");
        context.create(null, entity2);
        
        TestHttpHeaders headers = new TestHttpHeaders();
        headers.getAcceptableMediaTypes().add(MediaType.APPLICATION_JSON_TYPE);
        List<String> mediaTypes = new ArrayList<String>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        TestURIInfo ui = new TestURIInfo();
        StreamingOutput output = service.namedQuery("auction", "Auction.all", headers, ui);
     
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            output.write(outputStream);
        } catch (IOException ex){
            fail(ex.toString());
        }
        String resultString = outputStream.toString();
        
        assertTrue("Computer was not in results.", resultString.contains("\"name\" : \"Computer\""));
        assertTrue("Word Processor was not in restuls.", resultString.contains("\"name\" : \"Word Processor\""));
        clearData();
    }
    
    @Test
    public void testNamedQuerySingleResult(){
        Service service = new Service();
        service.setPersistenceFactory(factory);
        PersistenceContext context = factory.getPersistenceContext("auction");
        
        DynamicEntity entity1 = context.newEntity("Auction");
        entity1.set("name", "Computer");
        context.create(null, entity1);
        
        DynamicEntity entity2 = context.newEntity("Auction");
        entity2.set("name", "Word Processor");
        context.create(null, entity2);
        
        TestHttpHeaders headers = new TestHttpHeaders();
        headers.getAcceptableMediaTypes().add(MediaType.APPLICATION_JSON_TYPE);
        List<String> mediaTypes = new ArrayList<String>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        TestURIInfo ui = new TestURIInfo();
        ui.addMatrixParameter("name", "Computer");
        StreamingOutput output = service.namedQuerySingleResult("auction", "Auction.forName", headers, ui);
        
        String resultString = stringifyResults(output);
        
        assertTrue("Computer was not in results.", resultString.contains("\"name\" : \"Computer\""));
        assertFalse("Word Processor was in results.", resultString.contains("\"name\" : \"Word Processor\""));
        
        clearData();
    }
   
    @Test
    public void testUpdate(){
        Service service = new Service();
        service.setPersistenceFactory(factory);
        PersistenceContext context = factory.getPersistenceContext("auction");
        
        DynamicEntity entity1 = context.newEntity("Auction");
        entity1.set("name", "Computer");
        context.create(null, entity1);
        entity1.set("name", "Laptop");
        entity1.set("description", "Speedy");

        TestURIInfo ui = new TestURIInfo();
        ui.addMatrixParameter("name", "Computer");
        StreamingOutput output = service.update("auction", "Auction", generateHTTPHeader(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON), serializeToSteam(entity1, context, MediaType.APPLICATION_JSON));

        String resultString = stringifyResults(output);
        
        assertTrue("Laptop was not in results.", resultString.contains("\"name\" : \"Laptop\""));
        assertTrue("Laptop was not in results.", resultString.contains("\"description\" : \"Speedy\""));
    }
    
    private static DynamicEntity unmarshalEntity(PersistenceContext app, String type, String tenantId, String acceptedMedia, InputStream in) {
        Unmarshaller unmarshaller;
        try {
            unmarshaller = app.getJAXBContext().createUnmarshaller();
            unmarshaller.setProperty(MEDIA_TYPE, acceptedMedia);
            JAXBElement<?> element = unmarshaller.unmarshal(new StreamSource(in), app.getClass(type));
            return (DynamicEntity) element.getValue();
        } catch (JAXBException e) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
    }
    
    public static String stringifyResults(StreamingOutput output){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            output.write(outputStream);
        } catch (IOException ex){
            fail(ex.toString());
        }
        return outputStream.toString();
    }
    
    public static InputStream serializeToSteam(DynamicEntity object, PersistenceContext context, String mediaType){
        StringWriter writer = new StringWriter();
        JAXBMarshaller marshaller = null;
        try{
            marshaller = (JAXBMarshaller)context.getJAXBContext().createMarshaller();
            marshaller.setProperty("eclipselink.media-type", mediaType);
            marshaller.setProperty(JAXBContext.INCLUDE_ROOT, Boolean.FALSE);
            marshaller.marshal(object, writer);
        } catch (Exception e){
            fail(e.toString());
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(writer.toString().getBytes());
        return stream;
    }
    
    public static HttpHeaders generateHTTPHeader(MediaType acceptableMedia, String mediaTypeString){
        TestHttpHeaders headers = new TestHttpHeaders();
        headers.getAcceptableMediaTypes().add(acceptableMedia);
        List<String> mediaTypes = new ArrayList<String>();
        mediaTypes.add(mediaTypeString);

        headers.getRequestHeaders().put(HttpHeaders.CONTENT_TYPE, mediaTypes);
        return headers;
    }
    
}
