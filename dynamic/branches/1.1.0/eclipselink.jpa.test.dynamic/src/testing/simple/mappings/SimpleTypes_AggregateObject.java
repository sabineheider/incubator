package testing.simple.mappings;

import static junit.framework.Assert.*;

import javax.persistence.*;

import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.internal.descriptors.changetracking.AggregateAttributeChangeListener;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.AggregateObjectMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.junit.*;

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
        DirectToFieldMapping b_id = (DirectToFieldMapping) descriptorB.getMappingForAttributeName("value2");
        assertEquals(boolean.class, b_id.getAttributeClassification());
        DirectToFieldMapping b_value1 = (DirectToFieldMapping) descriptorB.getMappingForAttributeName("value3");
        assertEquals(String.class, b_value1.getAttributeClassification());
        assertTrue(descriptorB.isAggregateDescriptor());

        AggregateObjectMapping a_b = (AggregateObjectMapping) descriptorA.getMappingForAttributeName("b");
        assertSame(descriptorB.getJavaClass(), a_b.getReferenceDescriptor().getJavaClass());
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

        Class simpleTypeBClass = DynamicConversionManager.lookup(session).createDynamicClass("model.SimpleB");
        EntityTypeImpl entityTypeB = new EntityTypeImpl(simpleTypeBClass, null);
        entityTypeB.addDirectMapping("value2", boolean.class, "VAL_2", true);
        entityTypeB.addDirectMapping("value3", String.class, "VAL_3", false);
        entityTypeB.getDescriptor().descriptorIsAggregate();
        entityTypeB.addToSession(session);

        Class simpleTypeCClass = DynamicConversionManager.lookup(session).createDynamicClass("model.SimpleC");
        EntityTypeImpl entityTypeC = new EntityTypeImpl(simpleTypeCClass, null);
        entityTypeC.addDirectMapping("value4", double.class, "VAL_4", true);
        entityTypeC.addDirectMapping("value5", String.class, "VAL_5", false);
        entityTypeC.getDescriptor().descriptorIsAggregate();
        entityTypeC.addToSession(session);

        Class simpleTypeAClass = DynamicConversionManager.lookup(session).createDynamicClass("model.SimpleA");
        EntityTypeImpl entityTypeA = new EntityTypeImpl(simpleTypeAClass, "SIMPLE_TYPE_A");
        entityTypeA.addDirectMapping("id", int.class, "SID", true);
        entityTypeA.addDirectMapping("value1", String.class, "VAL_1", false);
        entityTypeA.addAggregateObjectMapping("b", simpleTypeBClass);
        entityTypeA.addAggregateObjectMapping("c", simpleTypeCClass).setIsNullAllowed(false);
        entityTypeA.addToSession(session);

        new DynamicSchemaManager(session).createTables(entityTypeA, entityTypeB);
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
