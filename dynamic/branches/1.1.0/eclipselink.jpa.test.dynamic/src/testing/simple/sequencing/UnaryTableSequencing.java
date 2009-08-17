package testing.simple.sequencing;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPAEntityTypeBuilder;
import org.eclipse.persistence.sequencing.UnaryTableSequence;
import org.eclipse.persistence.sessions.IdentityMapAccessor;
import org.eclipse.persistence.sessions.server.Server;
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
        Server session = JpaHelper.getServerSession(emf);

        ClassDescriptor descriptor = session.getClassDescriptorForAlias(ENTITY_TYPE);
        assertNotNull("No descriptor found for alias: " + ENTITY_TYPE, descriptor);

        EntityTypeImpl simpleType = (EntityTypeImpl) DynamicHelper.getType(session, ENTITY_TYPE);
        assertNotNull("EntityType not found for alias: " + ENTITY_TYPE, simpleType);

        assertEquals(descriptor, simpleType.getDescriptor());
    }

    @Test
    public void createSingleInstances() {
        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleType = (EntityTypeImpl) DynamicHelper.getType(session, ENTITY_TYPE);

        EntityManager em = emf.createEntityManager();

        DynamicEntity simpleInstance = createSimpleInstance(emf, 1);

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(o) FROM " + ENTITY_TYPE + " o").getSingleResult()).intValue();
        assertEquals(1, simpleCount);

        IdentityMapAccessor cache = session.getIdentityMapAccessor();
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
        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleType = (EntityTypeImpl) DynamicHelper.getType(session, ENTITY_TYPE);

        EntityManager em = emf.createEntityManager();

        DynamicEntity simpleInstance1 = createSimpleInstance(emf, 1);
        DynamicEntity simpleInstance2 = createSimpleInstance(emf, 2);

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(o) FROM " + ENTITY_TYPE + " o").getSingleResult()).intValue();
        assertEquals(2, simpleCount);

        IdentityMapAccessor cache = session.getIdentityMapAccessor();
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
        EntityType simpleEntityType = DynamicHelper.getType(JpaHelper.getServerSession(emf), ENTITY_TYPE);
        Assert.assertNotNull(simpleEntityType);

        DynamicEntity simpleInstance = simpleEntityType.newInstance();
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
        Server session = JpaHelper.getServerSession(emf);

        UnaryTableSequence sequence = new UnaryTableSequence("TEST_SEQ");
        sequence.setCounterFieldName("SEQ_VALUE");
        sequence.setPreallocationSize(5);
        ((AbstractSession) session).getProject().getLogin().setDefaultSequence(sequence);
        sequence.onConnect(session.getPlatform());

        EntityTypeBuilder factory = new JPAEntityTypeBuilder(session, "model.sequencing." + ENTITY_TYPE, null, TABLE_NAME);
        factory.setPrimaryKeyFields("SID");
        factory.addDirectMapping("id", int.class, "SID");
        factory.addDirectMapping("value1", String.class, "VAL_1");
        factory.configureSequencing(sequence, "TEST_SEQ", "SID");

        factory.addToSession(session, true);
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
