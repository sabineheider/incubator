package testing.simple.sequencing;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPADynamicHelper;
import org.eclipse.persistence.jpa.dynamic.JPADynamicTypeBuilder;
import org.eclipse.persistence.sequencing.UnaryTableSequence;
import org.eclipse.persistence.sessions.IdentityMapAccessor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UnaryTableSequencing {

    private static EntityManagerFactory emf;

    public static final String TABLE_NAME = "SIMPLE_TABLE_SEQ";

    public static final String ENTITY_TYPE = "Simple";

    @Test
    public void verifyConfig() throws Exception {
        DynamicHelper helper = new JPADynamicHelper(emf);

        ClassDescriptor descriptor = helper.getSession().getClassDescriptorForAlias(ENTITY_TYPE);
        assertNotNull("No descriptor found for alias: " + ENTITY_TYPE, descriptor);

        DynamicType simpleType = helper.getType(ENTITY_TYPE);
        assertNotNull("EntityType not found for alias: " + ENTITY_TYPE, simpleType);

        assertEquals(descriptor, simpleType.getDescriptor());
    }

    @Test
    public void createSingleInstances() {
        DynamicHelper helper = new JPADynamicHelper(emf);

        DynamicType simpleType = helper.getType(ENTITY_TYPE);

        EntityManager em = emf.createEntityManager();

        DynamicEntity simpleInstance = createSimpleInstance(emf, 1);

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(o) FROM " + ENTITY_TYPE + " o").getSingleResult()).intValue();
        assertEquals(1, simpleCount);

        IdentityMapAccessor cache = helper.getSession().getIdentityMapAccessor();
        assertTrue(cache.containsObjectInIdentityMap(simpleInstance));

        em.clear();
        cache.initializeAllIdentityMaps();

        DynamicEntity findResult = (DynamicEntity) em.find(simpleType.getJavaClass(), 1);

        assertNotNull(findResult);
        assertEquals(simpleInstance.get("id"), findResult.get("id"));
        assertEquals(simpleInstance.get("value1"), findResult.get("value1"));

        em.close();
    }

    @Test
    public void createTwoInstances() {
        DynamicHelper helper = new JPADynamicHelper(emf);

        DynamicType simpleType = helper.getType(ENTITY_TYPE);

        EntityManager em = emf.createEntityManager();

        DynamicEntity simpleInstance1 = createSimpleInstance(emf, 1);
        DynamicEntity simpleInstance2 = createSimpleInstance(emf, 2);

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(o) FROM " + ENTITY_TYPE + " o").getSingleResult()).intValue();
        assertEquals(2, simpleCount);

        IdentityMapAccessor cache = helper.getSession().getIdentityMapAccessor();
        assertTrue(cache.containsObjectInIdentityMap(simpleInstance1));
        assertTrue(cache.containsObjectInIdentityMap(simpleInstance2));

        em.clear();
        cache.initializeAllIdentityMaps();

        DynamicEntity findResult1 = (DynamicEntity) em.find(simpleType.getJavaClass(), 1);
        DynamicEntity findResult2 = (DynamicEntity) em.find(simpleType.getJavaClass(), 2);

        assertNotNull(findResult1);
        assertNotNull(findResult2);
        assertEquals(simpleInstance1.get("id"), findResult1.get("id"));
        assertEquals(simpleInstance2.get("value1"), findResult2.get("value1"));

        em.close();
    }

    public DynamicEntity createSimpleInstance(EntityManagerFactory emf, int expectedId) {
        EntityManager em = emf.createEntityManager();
        DynamicHelper helper = new JPADynamicHelper(emf);

        DynamicType simpleEntityType = helper.getType(ENTITY_TYPE);
        Assert.assertNotNull(simpleEntityType);

        DynamicEntity simpleInstance = simpleEntityType.newDynamicEntity();
        simpleInstance.set("value1", TABLE_NAME);

        em.getTransaction().begin();
        assertEquals(0, simpleInstance.get("id"));
        em.persist(simpleInstance);
        assertEquals(expectedId, simpleInstance.get("id"));
        em.getTransaction().commit();

        em.close();
        return simpleInstance;
    }

    @BeforeClass
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("empty");
        DynamicHelper helper = new JPADynamicHelper(emf);
        DynamicClassLoader dcl = helper.getDynamicClassLoader();

        UnaryTableSequence sequence = new UnaryTableSequence("TEST_SEQ");
        sequence.setCounterFieldName("SEQ_VALUE");
        sequence.setPreallocationSize(5);
        helper.getSession().getProject().getLogin().setDefaultSequence(sequence);
        sequence.onConnect(helper.getSession().getPlatform());

        Class<?> dynamicType = dcl.createDynamicClass("model.sequencing." + ENTITY_TYPE);
        DynamicTypeBuilder typeBuilder = new JPADynamicTypeBuilder(dynamicType, null, TABLE_NAME);
        typeBuilder.setPrimaryKeyFields("SID");
        typeBuilder.addDirectMapping("id", int.class, "SID");
        typeBuilder.addDirectMapping("value1", String.class, "VAL_1");
        typeBuilder.configureSequencing(sequence, "TEST_SEQ", "SID");

        helper.addTypes(true, true, typeBuilder.getType());
    }

    @Before
    public void clearSimpleTypeInstances() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM " + ENTITY_TYPE).executeUpdate();
        em.createNativeQuery("UPDATE TEST_SEQ SET SEQ_VALUE = 0").executeUpdate();
        em.getTransaction().commit();
        em.close();

        JpaHelper.getServerSession(emf).getSequencingControl().initializePreallocated();
    }

    @AfterClass
    public static void shutdown() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.createNativeQuery("DROP TABLE " + TABLE_NAME + " CASCADE CONSTRAINTS").executeUpdate();
        em.createNativeQuery("DROP TABLE TEST_SEQ CASCADE CONSTRAINTS").executeUpdate();
        em.getTransaction().commit();

        em.close();
        emf.close();
    }
}
