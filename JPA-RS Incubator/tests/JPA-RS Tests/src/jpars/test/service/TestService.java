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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import jpars.test.util.ExamplePropertiesLoader;
import jpars.test.util.TestHttpHeaders;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jaxb.JAXBMarshaller;
import org.eclipse.persistence.jpa.rs.PersistenceContext;
import org.eclipse.persistence.jpa.rs.PersistenceFactory;
import org.eclipse.persistence.jpa.rs.Service;
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
            factory.bootstrapPersistenceContext("auction", new URL("file:///C:/EclipseLinkView2/incubator/JPA-RS Incubator/tests/JPA-RS Tests/src/xmldocs/auction-persistence.xml"), properties);
        } catch (Exception e){
            e.printStackTrace();
            fail(e.toString());
        }
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
        
        DynamicEntity serializedData = context.newEntity("auctionSerializedData");
        List<DynamicEntity> entities = new ArrayList<DynamicEntity>();
        entities.add(entity);
        entities.add(entity2);
        serializedData.set("serializedData", entities);

        StringWriter writer = new StringWriter();
        
        JAXBMarshaller marshaller = null;

        try{
            marshaller = (JAXBMarshaller)context.getJAXBContext().createMarshaller();
            marshaller.setProperty("eclipselink.media-type", MediaType.APPLICATION_XML);
            marshaller.marshal(serializedData, writer);
        } catch (Exception e){
            e.printStackTrace();
            fail(e.toString());
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(writer.toString().getBytes());

        StreamingOutput output = service.update("auction", "auctionSerializedData", new TestHttpHeaders(), stream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            output.write(outputStream);
        } catch (IOException ex){
            fail(ex.toString());
        }
        stream = new ByteArrayInputStream(outputStream.toByteArray());
        serializedData = unmarshalEntity(context, "auctionSerializedData", null, MediaType.APPLICATION_XML, stream);
        
        assertNotNull("returned data was null", serializedData);
        entities = serializedData.get("serializedData");
        assertNotNull("returned data had null list", entities);
        assertTrue("returned data had wrong list size", entities.size() == 2);
        List<String> values = new ArrayList<String>();
        values.add("James");
        values.add("Gillian");
        for (DynamicEntity value: entities){
            assertTrue("Incorrect name returned", value.get("name").equals("James") || value.get("name").equals("Gillian"));
            values.remove(value.get("name"));
        }
        assertTrue("Incorrent set of names.", values.isEmpty());
    }
    
    private DynamicEntity unmarshalEntity(PersistenceContext app, String type, String tenantId, String acceptedMedia, InputStream in) {
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
    
}
