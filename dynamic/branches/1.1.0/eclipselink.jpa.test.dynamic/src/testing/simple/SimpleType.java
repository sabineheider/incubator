package testing.simple;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPAEntityTypeBuilder;
import org.eclipse.persistence.sessions.IdentityMapAccessor;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleType {

    protected static EntityManagerFactory emf;

    protected EntityType simpleType;

    protected EntityType getSimpleType() {
        if (simpleType == null) {
            this.simpleType = DynamicHelper.getType(JpaHelper.getServerSession(emf), "Simple");

            if (this.simpleType == null) {
                this.simpleType = createSimpleType();
            }
        }
        return this.simpleType;
    }

    protected EntityType createSimpleType() {
        Server session = JpaHelper.getServerSession(emf);
        DynamicClassLoader dcl = DynamicClassLoader.lookup(session);
        Class<?> javaType = dcl.createDynamicClass("model.Simple");

        EntityTypeBuilder typeBuilder = new JPAEntityTypeBuilder(javaType, null, "SIMPLE_TYPE");
        typeBuilder.setPrimaryKeyFields("SID");
        typeBuilder.addDirectMapping("id", int.class, "SID");
        typeBuilder.addDirectMapping("value1", String.class, "VAL_1");
        typeBuilder.addDirectMapping("value2", boolean.class, "VAL_2");
        typeBuilder.addDirectMapping("value3", Calendar.class, "VAL_3");
        typeBuilder.addDirectMapping("value4", Character.class, "VAL_4");

        typeBuilder.addToSession(session, true, true);

        return typeBuilder.getType();
    }

    @Test
    public void verifyConfig() throws Exception {
        Server session = JpaHelper.getServerSession(emf);

        ClassDescriptor descriptor = session.getClassDescriptorForAlias("Simple");
        assertNotNull("No descriptor found for alias='Simple'", descriptor);

        EntityTypeImpl simpleType = (EntityTypeImpl) DynamicHelper.getType(session, "Simple");
        assertNotNull("'Simple' EntityType not found", simpleType);

        assertEquals(1 + descriptor.getPrimaryKeyFields().size(), simpleType.getMappingsRequiringInitialization().size());

        assertEquals(descriptor, simpleType.getDescriptor());
    }

    @Test
    public void find() {
        createSimpleInstance(emf, 1);

        EntityManager em = emf.createEntityManager();

        DynamicEntity simpleInstance = find(em, 1);
        assertNotNull("Could not find simple instance with id = 1", simpleInstance);

        simpleInstance = find(em, new Integer(1));
        assertNotNull("Could not find simple instance with id = Integer(1)", simpleInstance);

        try {
            em.find(getSimpleType().getJavaClass(), 1l);
        } catch (IllegalArgumentException iae) {
            return;
        }
        fail("em.find should have failed with incorrect PK type");
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
        simpleInstance = (DynamicEntity) em.merge(simpleInstance);
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

    protected void assertDefaultValues(DynamicEntity simpleInstance) {
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
        simpleInstance.set("id", id);
        simpleInstance.set("value2", true);

        assertEquals(0, ((Number) em.createQuery("SELECT COUNT(s) FROM Simple s").getSingleResult()).intValue());

        em.getTransaction().begin();
        em.persist(simpleInstance);
        em.getTransaction().commit();

        assertEquals(1, ((Number) em.createQuery("SELECT COUNT(s) FROM Simple s").getSingleResult()).intValue());

        DynamicEntity foundEntity = find(em, 1);

        assertNotNull(foundEntity);
        assertEquals(simpleInstance.get("id"), foundEntity.get("id"));
        assertEquals(simpleInstance.get("value1"), foundEntity.get("value1"));
        assertEquals(simpleInstance.get("value2"), foundEntity.get("value2"));

        em.close();

        return simpleInstance;
    }

    protected DynamicEntity find(EntityManager em, int id) {
        return (DynamicEntity) em.find(getSimpleType().getJavaClass(), 1);
    }

    @BeforeClass
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("empty");
    }

    @Before
    @After
    public void clearSimpleTypeInstances() {
        getSimpleType();

        if (emf != null && emf.isOpen()) {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Simple").executeUpdate();
            em.getTransaction().commit();
            em.close();
        }

        JpaHelper.getServerSession(emf).getIdentityMapAccessor().initializeAllIdentityMaps();
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
