package testing.simple.sequencing;

import static junit.framework.Assert.*;

import javax.persistence.*;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sequencing.NativeSequence;
import org.eclipse.persistence.sessions.IdentityMapAccessor;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.*;

public class NativeSequencing {

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

        NativeSequence sequence = new NativeSequence();
        sequence.setPreallocationSize(5);
        ((AbstractSession) session).getProject().getLogin().setDefaultSequence(sequence);
        sequence.onConnect(session.getPlatform());

        Class simpleTypeClass = DynamicConversionManager.lookup(session).createDynamicClass("model.sequencing." + ENTITY_TYPE);
        EntityTypeImpl entityType = new EntityTypeImpl(simpleTypeClass, TABLE_NAME);
        entityType.addDirectMapping("id", int.class, "SID", true);
        entityType.addDirectMapping("value1", String.class, "VAL_1", false);
        entityType.getDescriptor().setSequenceNumberName(ENTITY_TYPE + "_SEQ");
        entityType.getDescriptor().setSequenceNumberFieldName("SID");
        entityType.getDescriptor().setSequence(sequence);

        entityType.addToSession(session);

        DynamicSchemaManager dsm = new DynamicSchemaManager(session);
        dsm.createTables(entityType);
    }

    @Before
    public void clearSimpleTypeInstances() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM " + ENTITY_TYPE).executeUpdate();
        em.getTransaction().commit();
        em.close();

        Server session = JpaHelper.getServerSession(emf);
        new SchemaManager(session).replaceSequences();
        session.getSequencingControl().initializePreallocated();
    }

    @AfterClass
    public static void shutdown() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.createNativeQuery("DROP TABLE " + TABLE_NAME + " CASCADE CONSTRAINTS").executeUpdate();
        em.createNativeQuery("DROP SEQUENCE Simple_SEQ").executeUpdate();
        em.getTransaction().commit();

        em.close();
        emf.close();
    }
}
