package testing.simple.mappings;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.EntityTypeFactory;
import org.eclipse.persistence.internal.descriptors.changetracking.AggregateAttributeChangeListener;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPAEntityTypeFactory;
import org.eclipse.persistence.mappings.AggregateObjectMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleTypes_AggregateObject {

    private static EntityManagerFactory emf;

    @Test
    public void verifyConfig() throws Exception {
        Server session = JpaHelper.getServerSession(emf);

        ClassDescriptor descriptorA = session.getClassDescriptorForAlias("SimpleA");
        assertNotNull("No descriptor found for alias='SimpleA'", descriptorA);

        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        assertNotNull("'SimpleA' EntityType not found", simpleTypeA);
        assertEquals(descriptorA, simpleTypeA.getDescriptor());
        DirectToFieldMapping a_id = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("id");
        assertEquals(int.class, a_id.getAttributeClassification());
        DirectToFieldMapping a_value1 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value1");
        assertEquals(String.class, a_value1.getAttributeClassification());

        ClassDescriptor descriptorB = session.getClassDescriptorForAlias("SimpleB");
        assertNotNull("No descriptor found for alias='SimpleB'", descriptorB);

        EntityTypeImpl simpleTypeB = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleB");
        assertNotNull("'SimpleB' EntityType not found", simpleTypeB);
        assertEquals(descriptorB, simpleTypeB.getDescriptor());
        DirectToFieldMapping b_value2 = (DirectToFieldMapping) descriptorB.getMappingForAttributeName("value2");
        assertEquals(boolean.class, b_value2.getAttributeClassification());
        DirectToFieldMapping b_value3 = (DirectToFieldMapping) descriptorB.getMappingForAttributeName("value3");
        assertEquals(String.class, b_value3.getAttributeClassification());
        assertTrue(descriptorB.isAggregateDescriptor());

        AggregateObjectMapping a_b = (AggregateObjectMapping) descriptorA.getMappingForAttributeName("b");
        assertSame(descriptorB.getJavaClass(), a_b.getReferenceDescriptor().getJavaClass());
        assertTrue(a_b.isNullAllowed());

        ClassDescriptor descriptorC = session.getClassDescriptorForAlias("SimpleC");
        assertNotNull("No descriptor found for alias='SimpleB'", descriptorB);

        EntityTypeImpl simpleTypeC = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleC");
        assertNotNull("'SimpleC' EntityType not found", simpleTypeC);
        assertEquals(descriptorB, simpleTypeB.getDescriptor());
        DirectToFieldMapping c_value4 = (DirectToFieldMapping) descriptorC.getMappingForAttributeName("value4");
        assertEquals(double.class, c_value4.getAttributeClassification());
        DirectToFieldMapping c_value5 = (DirectToFieldMapping) descriptorC.getMappingForAttributeName("value5");
        assertEquals(String.class, c_value5.getAttributeClassification());
        assertTrue(descriptorB.isAggregateDescriptor());

        AggregateObjectMapping a_c = (AggregateObjectMapping) descriptorA.getMappingForAttributeName("c");
        assertSame(descriptorC.getJavaClass(), a_c.getReferenceDescriptor().getJavaClass());
        assertFalse(a_c.isNullAllowed());
    }

    @Test
    public void verifyProperties() {
        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);

        assertEquals(4, simpleTypeA.getNumberOfProperties());
        assertEquals("id", simpleTypeA.getPropertiesNames().get(0));
        assertEquals("value1", simpleTypeA.getPropertiesNames().get(1));
        assertEquals("b", simpleTypeA.getPropertiesNames().get(2));
        assertEquals("c", simpleTypeA.getPropertiesNames().get(3));
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
        assertFalse(a.isSet("b"));
        assertTrue(a.isSet("c"));

        DynamicEntity c = a.get("c", DynamicEntity.class);
        assertNotNull(c);
        assertTrue(c.isSet("value4"));
        assertFalse(c.isSet("value5"));
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
    public void verifyChangTracking() {
        persistSimpleA();

        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        DynamicEntityImpl a = (DynamicEntityImpl) em.find(simpleTypeA.getJavaClass(), 1);
        assertNotNull(a);
        assertNotNull(a._persistence_getPropertyChangeListener());

        DynamicEntityImpl c = a.get("c", DynamicEntityImpl.class);
        assertNotNull(c);
        assertNotNull(c._persistence_getPropertyChangeListener());
        assertTrue(c._persistence_getPropertyChangeListener() instanceof AggregateAttributeChangeListener);

        em.getTransaction().rollback();
        em.close();
    }

    @Test
    public void createSimpleAwithSimpleB() {
        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);
        EntityTypeImpl simpleTypeB = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleB");
        Assert.assertNotNull(simpleTypeB);

        EntityManager em = emf.createEntityManager();

        Assert.assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("SimpleB"));

        DynamicEntity simpleInstanceB = simpleTypeB.newInstance();
        simpleInstanceB.set("value2", true);
        simpleInstanceB.set("value3", "B2");

        DynamicEntity simpleInstanceA = simpleTypeA.newInstance();
        simpleInstanceA.set("id", 2);
        simpleInstanceA.set("value1", "A2");
        simpleInstanceA.set("b", simpleInstanceB);

        em.getTransaction().begin();
        em.persist(simpleInstanceA);
        em.getTransaction().commit();

        int simpleCountA = ((Number) em.createQuery("SELECT COUNT(s) FROM SimpleA s").getSingleResult()).intValue();
        Assert.assertEquals(1, simpleCountA);

        em.close();
    }

    @BeforeClass
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("empty");
        Server session = JpaHelper.getServerSession(emf);

        EntityTypeFactory bFactory = new JPAEntityTypeFactory(session, "model.SimpleB");
        bFactory.addDirectMapping("value2", boolean.class, "VAL_2");
        bFactory.addDirectMapping("value3", String.class, "VAL_3");
        bFactory.addToSession(session, false);

        EntityTypeFactory cFactory = new JPAEntityTypeFactory(session, "model.SimpleC");
        cFactory.addDirectMapping("value4", double.class, "VAL_4");
        cFactory.addDirectMapping("value5", String.class, "VAL_5");
        cFactory.addToSession(session, false);

        EntityTypeFactory aFactory = new JPAEntityTypeFactory(session, "model.SimpleA", "SIMPLE_TYPE_A");
        aFactory.addPrimaryKeyFields("SID");
        aFactory.addDirectMapping("id", int.class, "SID");
        aFactory.addDirectMapping("value1", String.class, "VAL_1");
        aFactory.addAggregateObjectMapping("b", bFactory.getType(), true);
        aFactory.addAggregateObjectMapping("c", cFactory.getType(), false);
        aFactory.addToSession(session, true);

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
        em.getTransaction().commit();

        em.close();
        emf.close();
    }

}
