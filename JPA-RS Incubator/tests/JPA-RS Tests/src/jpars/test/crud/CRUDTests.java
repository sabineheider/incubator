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
 *      tware - initial 
 ******************************************************************************/
package jpars.test.crud;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import jpars.test.util.ExamplePropertiesLoader;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jpa.rs.PersistenceContext;
import org.eclipse.persistence.jpa.rs.PersistenceFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test basic CRUD operations on a dynamically created PersistenceContext
 * @author tware
 *
 */
public class CRUDTests {

    private static PersistenceContext persistenceContext;
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
            fail(e.toString());
        }
        
        persistenceContext = factory.getPersistenceContext("auction");
    }
    
    @AfterClass
    public static void tearDown(){
        EntityManager em = persistenceContext.getEmf().createEntityManager();
        em.getTransaction().begin();
        em.createQuery("delete from Bid b").executeUpdate();
        em.createQuery("delete from Auction a").executeUpdate();
        em.createQuery("delete from User u").executeUpdate();
        em.getTransaction().commit();
        factory.closePersistenceContext("auction");
    }
    
    @Test
    public void testCreateAndDelete() {
        DynamicEntity entity = persistenceContext.newEntity("User");
        entity.set("name", "Jim");
        persistenceContext.create(null, entity);
        entity = persistenceContext.find("User", entity.get("id"));
        
        assertNotNull("Entity was note persisted", entity);
        assertTrue("Entity Name was incorrect", entity.get("name").equals("Jim"));
        
        persistenceContext.delete(null, "User", entity.get("id"));
        
        entity = persistenceContext.find("User", entity.get("id"));
        
        assertNull("Entity was note deleted", entity);
    }

    
    @Test
    public void testQuery(){
        DynamicEntity entity = persistenceContext.newEntity("User");
        entity.set("name", "Jill");
        persistenceContext.create(null, entity);
        
        entity = persistenceContext.newEntity("User");
        entity.set("name", "Arthur");
        persistenceContext.create(null, entity);
        
        entity = persistenceContext.newEntity("User");
        entity.set("name", "Judy");
        persistenceContext.create(null, entity);
        
        List<DynamicEntity> users = (List<DynamicEntity>)persistenceContext.query("User.all", null);
        assertTrue(users.size() == 3);
    }

}
