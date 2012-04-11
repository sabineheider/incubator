package org.eclipse.persistence.jpars.test.server;

import static org.eclipse.persistence.jaxb.JAXBContext.MEDIA_TYPE;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jpa.rs.PersistenceContext;
import org.eclipse.persistence.jpa.rs.PersistenceFactory;
import org.eclipse.persistence.jpa.rs.metadata.DatabaseMetadataStore;
import org.eclipse.persistence.jpa.rs.metadata.model.Attribute;
import org.eclipse.persistence.jpa.rs.metadata.model.Descriptor;
import org.eclipse.persistence.jpa.rs.metadata.model.Link;
import org.eclipse.persistence.jpa.rs.metadata.model.PersistenceUnit;
import org.eclipse.persistence.jpa.rs.metadata.model.Query;
import org.eclipse.persistence.jpa.rs.util.LinkAdapter;
import org.eclipse.persistence.jpars.test.model.StaticAuction;
import org.eclipse.persistence.jpars.test.model.StaticBid;
import org.eclipse.persistence.jpars.test.model.StaticUser;
import org.eclipse.persistence.jpars.test.util.ExamplePropertiesLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ServerCrudTest {
    
    public static final String SERVER_URI = "http://localhost:8080/org.eclipse.persistence.jpars.test.server/jpa-rs/";
    protected static Client client = null;
    protected static Unmarshaller unmarshaller = null;
    protected static PersistenceContext context = null;
    
    protected static int user1Id;
    protected static int user2Id;
    protected static int user3Id;
    
    protected static int auction1Id;
    protected static int auction2Id;
    protected static int auction3Id;
    
    protected static int bid1Id;;
    protected static int bid2Id;
    protected static int bid3Id;
    
    @BeforeClass
    public static void setup(){
        Map<String, Object> properties = new HashMap<String, Object>();
        ExamplePropertiesLoader.loadProperties(properties); 
        properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, null);
        properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.DROP_AND_CREATE);

        PersistenceFactory factory = new PersistenceFactory();
        factory.setMetadataStore(new DatabaseMetadataStore());
        factory.getMetadataStore().setProperties(properties);
        factory.getMetadataStore().clearMetadata();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("auction-static", properties);
        try{
            context = factory.bootstrapPersistenceContext("auction-static", emf, new URI("http://localhost:8080/JPA-RS/"), false);
        } catch (URISyntaxException e){
            throw new RuntimeException("Setup Exception ", e);
        }
        
        populateDB(emf);
        
        client = Client.create();
    }
    
    
    @Test
    public void testRead(){
        WebResource webResource = client.resource(SERVER_URI + "auction-static/entity/StaticBid/" + bid1Id);
        String result = webResource.accept(MediaType.APPLICATION_JSON).get(String.class);
        StaticBid bid = null;
        try {
            bid = (StaticBid)context.unmarshalEntity("StaticBid", null, MediaType.APPLICATION_JSON_TYPE, new ByteArrayInputStream(result.getBytes()));
        } catch (JAXBException e){
            fail("Exception thrown unmarshalling: " + e);
        }
        assertTrue("Wrong big retrieved.", bid.getBid() == 110);
        assertTrue("No user for Bid", bid.getUser() != null);
        assertTrue("No auction for Bid", bid.getAuction() != null);
    }
    
    @Test
    public void testUpdate(){
        WebResource webResource = client.resource(SERVER_URI + "auction-static/entity/StaticBid/" + bid1Id);
        String result = webResource.accept(MediaType.APPLICATION_JSON).get(String.class);
        StaticBid bid = null;
        try {
            bid = (StaticBid)context.unmarshalEntity("StaticBid", null, MediaType.APPLICATION_JSON_TYPE, new ByteArrayInputStream(result.getBytes()));
        } catch (JAXBException e){
            fail("Exception thrown unmarshalling: " + e);
        }
        bid.setBid(120);
        webResource = client.resource(SERVER_URI + "auction-static/entity/StaticBid");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            context.marshallEntity(bid, MediaType.APPLICATION_JSON_TYPE, os);
        
        } catch (JAXBException e){
            fail("Exception thrown unmarshalling: " + e);
        }
        result = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(String.class, os.toString());
        try {
            bid = (StaticBid)context.unmarshalEntity("StaticBid", null, MediaType.APPLICATION_JSON_TYPE, new ByteArrayInputStream(result.getBytes()));
        } catch (JAXBException e){
            fail("Exception thrown unmarshalling: " + e);
        }
        assertTrue("Wrong big retrieved.", bid.getBid() == 120);
        assertTrue("No user for Bid", bid.getUser() != null);
        assertTrue("No auction for Bid", bid.getAuction() != null);
    }
    
    public static Object unmarshall(String result, String mediaType, Class expectedResultClass){
        if (unmarshaller == null){
            Class[] jaxbClasses = new Class[]{Link.class, LinkAdapter.class, PersistenceUnit.class, Descriptor.class, Attribute.class, Query.class};
            JAXBContext context = null;
            try{
                context = (JAXBContext)JAXBContextFactory.createContext(jaxbClasses, null);
                unmarshaller = context.createUnmarshaller();
                unmarshaller.setProperty(JAXBContext.JSON_INCLUDE_ROOT, Boolean.FALSE);
            } catch (JAXBException e){
                e.printStackTrace();
            }
        }
        try{
            unmarshaller.setProperty(MEDIA_TYPE, mediaType);
            CharArrayReader reader = new CharArrayReader(result.toCharArray());
            StreamSource ss = new StreamSource(reader);
            Object unmarshalled = unmarshaller.unmarshal(ss, expectedResultClass);
            return unmarshalled;
        } catch (PropertyException exc){
            throw new RuntimeException(exc);
        } catch (JAXBException e){
            throw new RuntimeException(e);
        }

    }
    
    public static void populateDB(EntityManagerFactory emf){
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        StaticUser user1 = user1();
        em.persist(user1);
        StaticUser user2 = user2();
        em.persist(user2);
        StaticUser user3 = user3();
        em.persist(user3);
        StaticAuction auction1 = auction1();
        em.persist(auction1);
        StaticAuction auction2 = auction1();
        em.persist(auction2);
        StaticAuction auction3 = auction1();
        em.persist(auction3);
        
        StaticBid bid1 = new StaticBid();
        bid1.setBid(110);
        bid1.setTime(System.currentTimeMillis());
        bid1.setAuction(auction1);
        bid1.setUser(user1);
        em.persist(bid1);
        
        StaticBid bid2 = new StaticBid();
        bid2.setBid(111);
        bid2.setTime(System.currentTimeMillis());
        bid2.setAuction(auction1);
        bid2.setUser(user2);
        em.persist(bid2);
        
        StaticBid bid3 = new StaticBid();
        bid3.setBid(1100);
        bid3.setTime(System.currentTimeMillis());
        bid3.setAuction(auction2);
        bid3.setUser(user2);
        em.persist(bid3);
        
        em.getTransaction().commit();
        user1Id = user1.getId();
        user2Id = user2.getId();
        user3Id = user3.getId();
        
        auction1Id = auction1.getId();
        auction2Id = auction2.getId();
        auction3Id = auction3.getId();
        
        bid1Id = bid1.getId();
        bid2Id = bid2.getId();
        bid3Id = bid3.getId();
        
    }
    
    public static StaticUser user1(){
        StaticUser user = new StaticUser();
        user.setName("user1");
        return user;
    }
    
    public static StaticUser user2(){
        StaticUser user = new StaticUser();
        user.setName("user2");
        return user;
    }
    
    public static StaticUser user3(){
        StaticUser user = new StaticUser();
        user.setName("user3");
        return user;
    }
    
    public static StaticAuction auction1(){
        StaticAuction auction = new StaticAuction();
        auction.setDescription("Auction 1");
        auction.setImage("auction1.jpg");
        auction.setName("A1");
        auction.setStartPrice(100);
        return auction;
    }
    
    public static StaticAuction auction2(){
        StaticAuction auction = new StaticAuction();
        auction.setDescription("Auction 2");
        auction.setImage("auction2.jpg");
        auction.setName("A2");
        auction.setStartPrice(1000);
        return auction;
    }
    
    public static StaticAuction auction3(){
        StaticAuction auction = new StaticAuction();
        auction.setDescription("Auction 3");
        auction.setImage("auction3.jpg");
        auction.setName("A3");
        auction.setStartPrice(1010);
        return auction;
    }

}
