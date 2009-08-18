package testing.simple.mappings;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPAEntityTypeBuilder;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleTypes_MultiTable {

    private static EntityManagerFactory emf;

    @Test
    public void verifyConfig() throws Exception {
        Server session = JpaHelper.getServerSession(emf);

        ClassDescriptor descriptorA = session.getClassDescriptorForAlias("SimpleA");
        assertNotNull("No descriptor found for alias='SimpleA'", descriptorA);

        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        assertNotNull("'SimpleA' EntityType not found", simpleTypeA);
        assertEquals(descriptorA, simpleTypeA.getDescriptor());

        assertTrue(descriptorA.hasMultipleTables());
        assertEquals(3, descriptorA.getTables().size());

        DirectToFieldMapping a_id = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("id");
        assertEquals(int.class, a_id.getAttributeClassification());
        DirectToFieldMapping a_value1 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value1");
        assertEquals(String.class, a_value1.getAttributeClassification());

        DirectToFieldMapping a_value2 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value2");
        assertEquals(boolean.class, a_value2.getAttributeClassification());

        DirectToFieldMapping a_value3 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value3");
        assertEquals(String.class, a_value3.getAttributeClassification());

        DirectToFieldMapping a_value4 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value4");
        assertEquals(double.class, a_value4.getAttributeClassification());

        DirectToFieldMapping a_value5 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value5");
        assertEquals(String.class, a_value5.getAttributeClassification());
    }

    @Test
    public void verifyProperties() {
        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);

        assertEquals(6, simpleTypeA.getNumberOfProperties());
        assertEquals("id", simpleTypeA.getPropertiesNames().get(0));
        assertEquals(int.class, simpleTypeA.getPropertyType(0));
        assertEquals("value1", simpleTypeA.getPropertiesNames().get(1));
        assertEquals(String.class, simpleTypeA.getPropertyType(1));
        assertEquals("value2", simpleTypeA.getPropertiesNames().get(2));
        assertEquals(boolean.class, simpleTypeA.getPropertyType(2));
        assertEquals("value3", simpleTypeA.getPropertiesNames().get(3));
        assertEquals(String.class, simpleTypeA.getPropertyType(3));
        assertEquals("value4", simpleTypeA.getPropertiesNames().get(4));
        assertEquals(double.class, simpleTypeA.getPropertyType(4));
        assertEquals("value5", simpleTypeA.getPropertiesNames().get(5));
        assertEquals(String.class, simpleTypeA.getPropertyType(5));

    }

    @Test
    public void createSimpleA() {
        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);

        DynamicEntity a = simpleTypeA.newInstance();

        assertNotNull(a);
        assertTrue(a.isSet("id"));
        assertFalse(a.isSet("value1"));
        assertTrue(a.isSet("value2"));
        assertFalse(a.isSet("value3"));
        assertTrue(a.isSet("value4"));
        assertFalse(a.isSet("value5"));
    }

    @Test
    public void persistSimpleA() {
        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);

        EntityManager em = emf.createEntityManager();

        DynamicEntity simpleInstance = simpleTypeA.newInstance();
        simpleInstance.set("id", 1);
        simpleInstance.set("value1", "A1");

        em.getTransaction().begin();
        em.persist(simpleInstance);
        em.getTransaction().commit();

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(s) FROM SimpleA s").getSingleResult()).intValue();
        Assert.assertEquals(1, simpleCount);

        em.close();
    }

    @Test
    public void verifyChangeTracking() {
        persistSimpleA();

        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        DynamicEntityImpl a = (DynamicEntityImpl) em.find(simpleTypeA.getJavaClass(), 1);
        assertNotNull(a);
        assertNotNull(a._persistence_getPropertyChangeListener());

        em.getTransaction().rollback();
        em.close();
    }

    @BeforeClass
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("empty");
        Server session = JpaHelper.getServerSession(emf);

        EntityTypeBuilder typeBuilder = new JPAEntityTypeBuilder(session, "model.SimpleA", null, "SIMPLE_TYPE_A", "SIMPLE_TYPE_B", "SIMPLE_TYPE_C");
        typeBuilder.setPrimaryKeyFields("SIMPLE_TYPE_A.SID");
        typeBuilder.addDirectMapping("id", int.class, "SIMPLE_TYPE_A.SID");
        typeBuilder.addDirectMapping("value1", String.class, "SIMPLE_TYPE_A.VAL_1");
        typeBuilder.addDirectMapping("value2", boolean.class, "SIMPLE_TYPE_B.VAL_2");
        typeBuilder.addDirectMapping("value3", String.class, "SIMPLE_TYPE_B.VAL_3");
        typeBuilder.addDirectMapping("value4", double.class, "SIMPLE_TYPE_C.VAL_4");
        typeBuilder.addDirectMapping("value5", String.class, "SIMPLE_TYPE_C.VAL_5");
        
        EntityTypeBuilder.addToSession(session, true, true, typeBuilder.getType());
    }

    @After
    public void clearDynamicTables() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.createQuery("DELETE FROM SimpleA").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @AfterClass
    public static void shutdown() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.createNativeQuery("DROP TABLE SIMPLE_TYPE_A CASCADE CONSTRAINTS").executeUpdate();
        em.createNativeQuery("DROP TABLE SIMPLE_TYPE_B CASCADE CONSTRAINTS").executeUpdate();
        em.createNativeQuery("DROP TABLE SIMPLE_TYPE_C CASCADE CONSTRAINTS").executeUpdate();
        em.getTransaction().commit();

        em.close();
        emf.close();
    }

}
