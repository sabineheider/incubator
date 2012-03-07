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
package org.eclipse.persistence.jpars.test.service;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBException;


import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jpa.rs.PersistenceContext;
import org.eclipse.persistence.jpa.rs.PersistenceFactory;
import org.eclipse.persistence.jpa.rs.Service;
import org.eclipse.persistence.jpa.rs.metadata.DatabaseMetadataStore;
import org.eclipse.persistence.jpars.test.util.ExamplePropertiesLoader;
import org.eclipse.persistence.jpars.test.util.TestHttpHeaders;
import org.eclipse.persistence.jpars.test.util.TestURIInfo;
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
            FileInputStream xmlStream = new FileInputStream("classes/xmldocs/auction-persistence.xml");

            PersistenceContext context = factory.bootstrapPersistenceContext("auction", xmlStream, properties, true);
            context.setBaseURI(new URI("http://localhost:8080/JPA-RS/"));
            
            xmlStream = new FileInputStream("classes/xmldocs/phonebook-persistence.xml");
            context = factory.bootstrapPersistenceContext("phonebook", xmlStream, properties, true);
            context.setBaseURI(new URI("http://localhost:8080/JPA-RS/"));
            
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
        em = factory.getPersistenceContext("phonebook").getEmf().createEntityManager();
        em.getTransaction().begin();
        em.createQuery("delete from Person p").executeUpdate();
        em.getTransaction().commit();
    }
    
    @Test
    public void testUpdateUserList(){
        Service service = new Service();
        service.setPersistenceFactory(factory);
        PersistenceContext context = factory.getPersistenceContext("auction");
        
        DynamicEntity entity = (DynamicEntity)context.newEntity("User");
        entity.set("name", "Jim");
        context.create(null, entity);
        
        entity.set("name", "James");
        
        DynamicEntity entity2 = (DynamicEntity)context.newEntity("User");
        entity2.set("name", "Jill");
        context.create(null, entity2);
        
        entity2.set("name", "Gillian");    
        
        List<DynamicEntity> entities = new ArrayList<DynamicEntity>();
        entities.add(entity);
        entities.add(entity2);

        StreamingOutput output = service.update("auction", "User", generateHTTPHeader(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON), new TestURIInfo(), TestService.serializeListToStream(entities, context, MediaType.APPLICATION_JSON_TYPE));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            output.write(outputStream);
        } catch (IOException ex){
            fail(ex.toString());
        }
        InputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
        try{
            entities = (List<DynamicEntity>)context.unmarshalEntity("User", null, MediaType.APPLICATION_JSON_TYPE, stream);
        } catch (JAXBException e){
            fail("Exception unmarsalling: " + e);
        }
        assertNotNull("returned data was null", entities);
        assertTrue("returned data had wrong list size", entities.size() == 2);
        List<String> values = new ArrayList<String>();
        values.add("James");
        values.add("Gillian");
        for (DynamicEntity value: entities){
            assertTrue("Incorrect name returned", value.get("name").equals("James") || value.get("name").equals("Gillian"));
            values.remove(value.get("name"));
        }
        assertTrue("Incorrent set of names.", values.isEmpty());
        
        clearData();
    }
    
    @Test
    public void testUpdatePhoneNumberList(){
        Service service = new Service();
        service.setPersistenceFactory(factory);
        PersistenceContext context = factory.getPersistenceContext("phonebook");
        
        DynamicEntity entity = (DynamicEntity)context.newEntity("Person");
        entity.set("firstName", "Jim");
        entity.set("lastName", "Jones");
        entity.set("phoneNumber", "1234567");
        context.create(null, entity);
        
        entity.set("firstName", "James");
        
        DynamicEntity entity2 = (DynamicEntity)context.newEntity("Person");
        entity2.set("firstName", "Jill");
        entity2.set("lastName", "Jones");
        context.create(null, entity2);
        
        entity2.set("firstName", "Gillian");    
        
        List<DynamicEntity> entities = new ArrayList<DynamicEntity>();
        entities.add(entity);
        entities.add(entity2);

        StreamingOutput output = service.update("phonebook", "Person", generateHTTPHeader(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON), new TestURIInfo(), serializeListToStream(entities, context, MediaType.APPLICATION_JSON_TYPE));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            output.write(outputStream);
        } catch (IOException ex){
            fail(ex.toString());
        }
        InputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
        try{
            entities = (List<DynamicEntity>)context.unmarshalEntity("Person", null, MediaType.APPLICATION_JSON_TYPE, stream);
        } catch (JAXBException e){
            fail("Exception unmarsalling: " + e);
        }        
        assertNotNull("returned data was null", entities);
        assertNotNull("returned data had null list", entities);
        assertTrue("returned data had wrong list size", entities.size() == 2);
        List<String> values = new ArrayList<String>();
        values.add("James");
        values.add("Gillian");
        for (DynamicEntity value: entities){
            assertTrue("Incorrect firstName returned", value.get("firstName").equals("James") || value.get("firstName").equals("Gillian"));
            values.remove(value.get("firstName"));
        }
        assertTrue("Incorrent set of names.", values.isEmpty());
        
        clearData();
    }
    
  /*  @Test
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
            factory.getPersistenceContext("auction").setBaseURI(new URI("http://localhost:8080/JPA-RS/"));
            factory.getPersistenceContext("phonebook").setBaseURI(new URI("http://localhost:8080/JPA-RS/"));
        } catch (Exception e){
            fail(e.toString());
        }
        assertTrue("factory was not recreated at boot time.", factory.getPersistenceContext("auction") != null);
    }*/
    
    @Test 
    public void testMarshallBid(){
        Service service = new Service();
        service.setPersistenceFactory(factory);
        PersistenceContext context = factory.getPersistenceContext("auction");
        
        DynamicEntity entity1 = (DynamicEntity)context.newEntity("Auction");
        entity1.set("name", "Computer");
        context.create(null, entity1);
        
        DynamicEntity entity2 = (DynamicEntity)context.newEntity("User");
        entity2.set("name", "Bob");
        context.create(null, entity2);
        
        DynamicEntity entity3 = (DynamicEntity)context.newEntity("Bid");
        entity3.set("bid", 200d);
        entity3.set("user", entity2);
        entity3.set("auction", entity1);
        context.create(null, entity3);
        
        InputStream stream = serializeToStream(entity3, context, MediaType.APPLICATION_JSON_TYPE);

        try{
            entity3 = (DynamicEntity)context.unmarshalEntity("Bid", null, MediaType.APPLICATION_JSON_TYPE, stream);
        } catch (JAXBException e){
            fail("Exception unmarsalling: " + e);
        }
        System.out.println(entity3);
        entity2 = entity3.get("auction");

        assertNotNull("Name of auction is null.", entity2.get("name"));
        assertTrue("Name of auction is incorrect.", entity2.get("name").equals("Computer"));
        
        entity1 = entity3.get("user");
        
        assertNotNull("Name of user is null.", entity1.get("name"));
        assertTrue("Name of user is incorrect.", entity1.get("name").equals("Bob"));
    }
    
    @Test
    public void testNamedQuery(){
        Service service = new Service();
        service.setPersistenceFactory(factory);
        PersistenceContext context = factory.getPersistenceContext("auction");
        
        DynamicEntity entity1 = (DynamicEntity)context.newEntity("Auction");
        entity1.set("name", "Computer");
        context.create(null, entity1);
        
        DynamicEntity entity2 = (DynamicEntity)context.newEntity("Auction");
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
        
        DynamicEntity entity1 = (DynamicEntity)context.newEntity("Auction");
        entity1.set("name", "Computer");
        context.create(null, entity1);
        
        DynamicEntity entity2 = (DynamicEntity)context.newEntity("Auction");
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
        
        DynamicEntity entity1 = (DynamicEntity)context.newEntity("Auction");
        entity1.set("name", "Computer");
        context.create(null, entity1);
        entity1.set("name", "Laptop");
        entity1.set("description", "Speedy");

        TestURIInfo ui = new TestURIInfo();
        ui.addMatrixParameter("name", "Computer");
        StreamingOutput output = service.update("auction", "Auction", generateHTTPHeader(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON), new TestURIInfo(), serializeToStream(entity1, context, MediaType.APPLICATION_JSON_TYPE));

        String resultString = stringifyResults(output);
        
        assertTrue("Laptop was not in results.", resultString.contains("\"name\" : \"Laptop\""));
        assertTrue("Laptop was not in results.", resultString.contains("\"description\" : \"Speedy\""));
    }
    
    @Test 
    public void testMetadataQuery(){
        Service service = new Service();
        service.setPersistenceFactory(factory);
        StreamingOutput output = (StreamingOutput)service.getContexts(generateHTTPHeader(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON)).getEntity();
        String result = stringifyResults(output);
        assertTrue("auction was not in the results", result.contains("auction"));
        assertTrue("phonebook was not in the results", result.contains("phonebook"));
        
        output = (StreamingOutput)service.getTypes("auction", generateHTTPHeader(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON), new TestURIInfo()).getEntity();
        result = stringifyResults(output);
        
        assertTrue("Bid was not in the results", result.contains("Bid"));
        assertTrue("Auction was not in the results", result.contains("Auction"));
        assertTrue("User was not in the results", result.contains("User"));

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
    
    public static InputStream serializeToStream(Object object, PersistenceContext context, MediaType mediaType){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            context.marshallEntity(object, mediaType, os);
        } catch (Exception e){
            e.printStackTrace();
            fail(e.toString());
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(os.toByteArray());
        return stream;
    }
    
    public static InputStream serializeListToStream(List<DynamicEntity> object, PersistenceContext context, MediaType mediaType){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            context.marshallEntity(object, mediaType, os);
        } catch (Exception e){
            e.printStackTrace();
            fail(e.toString());
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(os.toByteArray());
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