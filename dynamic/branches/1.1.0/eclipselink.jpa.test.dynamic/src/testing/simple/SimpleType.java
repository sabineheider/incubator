package testing.simple;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.dynamic.EntityTypeFactory;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPAEntityTypeFactory;
import org.eclipse.persistence.sessions.IdentityMapAccessor;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleType {

    private static EntityManagerFactory emf;

    @Test
    public void verifyConfig() throws Exception {
        Server session = JpaHelper.getServerSession(emf);

        ClassDescriptor descriptor = session.getClassDescriptorForAlias("Simple");
        assertNotNull("No descriptor found for alias='Simple'", descriptor);

        EntityTypeImpl simpleType = (EntityTypeImpl) DynamicHelper.getType(session, "Simple");
        assertNotNull("'Simple' EntityType not found", simpleType);

        assertEquals(2, simpleType.getMappingsRequiringInitialization().size());

        assertEquals(descriptor, simpleType.getDescriptor());
    }

    @Test
    public void simpleInstance_CRUD() {
        Server session = JpaHelper.getServerSession(emf);
        IdentityMapAccessor cache = session.getIdentityMapAccessor();

        DynamicEntity simpleInstance = createSimpleInstance(emf, 1);
        assertNotNull(simpleInstance);

        assertTrue(cache.containsObjectInIdentityMap(simpleInstance));
        cache.initializeAllIdentityMaps();
        assertFalse(cache.containsObjectInIdentityMap(simpleInstance));

        EntityManager em = emf.createEntityManager();

        assertFalse(em.contains(simpleInstance));
        simpleInstance = em.merge(simpleInstance);
        assertTrue(em.contains(simpleInstance));

        em.close();
    }

    @Test
    public void verifyDefaultValuesFromEntityType() throws Exception {
        EntityType simpleType = DynamicHelper.getType(JpaHelper.getServerSession(emf), "Simple");

        assertNotNull(simpleType);

        DynamicEntity simpleInstance = simpleType.newInstance();
        assertDefaultValues(simpleInstance);
    }

    @Test
    public void verifyDefaultValuesFromDescriptor() throws Exception {
        EntityTypeImpl simpleType = (EntityTypeImpl) DynamicHelper.getType(JpaHelper.getServerSession(emf), "Simple");
        assertNotNull(simpleType);

        DynamicEntity simpleInstance = (DynamicEntity) simpleType.getDescriptor().getObjectBuilder().buildNewInstance();
        assertDefaultValues(simpleInstance);
    }

    private void assertDefaultValues(DynamicEntity simpleInstance) {
        assertNotNull(simpleInstance);

        assertTrue("id not set on new instance", simpleInstance.isSet("id"));
        assertEquals("id not default value", 0, simpleInstance.get("id"));
        assertFalse("value1  set on new instance", simpleInstance.isSet("value1"));
        assertTrue("value2 not set on new instance", simpleInstance.isSet("value2"));
        assertEquals("value2 not default value", false, simpleInstance.get("value2"));
        assertFalse("value3 set on new instance", simpleInstance.isSet("value3"));
        assertFalse("value4  set on new instance", simpleInstance.isSet("value4"));
    }

    public DynamicEntity createSimpleInstance(EntityManagerFactory emf, int id) {
        EntityManager em = emf.createEntityManager();
        EntityType simpleEntityType = DynamicHelper.getType(JpaHelper.getServerSession(emf), "Simple");
        Assert.assertNotNull(simpleEntityType);

        DynamicEntity simpleInstance = simpleEntityType.newInstance();
        simpleInstance.set("id", 1);
        simpleInstance.set("value2", true);

        assertEquals(0, ((Number) em.createQuery("SELECT COUNT(s) FROM Simple s").getSingleResult()).intValue());

        em.getTransaction().begin();
        em.persist(simpleInstance);
        em.getTransaction().commit();

        assertEquals(1, ((Number) em.createQuery("SELECT COUNT(s) FROM Simple s").getSingleResult()).intValue());

        DynamicEntity foundEntity = (DynamicEntity) em.find(simpleEntityType.getJavaClass(), 1);

        assertNotNull(foundEntity);
        assertEquals(simpleInstance.get("id"), foundEntity.get("id"));
        assertEquals(simpleInstance.get("value1"), foundEntity.get("value1"));
        assertEquals(simpleInstance.get("value2"), foundEntity.get("value2"));

        em.close();

        return simpleInstance;
    }

    @BeforeClass
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("empty");
        Server session = JpaHelper.getServerSession(emf);

        EntityTypeFactory factory = new JPAEntityTypeFactory(session, "model.Simple", "SIMPLE_TYPE");
        factory.addPrimaryKeyFields("SID");
        factory.addDirectMapping("id", int.class, "SID");
        factory.addDirectMapping("value1", String.class, "VAL_1");
        factory.addDirectMapping("value2", boolean.class, "VAL_2");
        factory.addDirectMapping("value3", Calendar.class, "VAL_3");
        factory.addDirectMapping("value4", Character.class, "VAL_4");

        factory.addToSession(session, true);
    }

    @Before
    @After
    public void clearSimpleTypeInstances() {
        if (emf != null && emf.isOpen()) {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Simple").executeUpdate();
            em.getTransaction().commit();
            em.close();
        }
    }

    @AfterClass
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            EntityManager em = emf.createEntityManager();

            em.getTransaction().begin();
            em.createNativeQuery("DROP TABLE SIMPLE_TYPE CASCADE CONSTRAINTS").executeUpdate();
            em.getTransaction().commit();

            em.close();
            emf.close();
        }
    }
}
