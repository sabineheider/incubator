package testing.simple;

import static junit.framework.Assert.*;

import java.util.*;

import javax.persistence.*;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.IdentityMapAccessor;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.junit.*;

public class SimpleType {

    private static EntityManagerFactory emf;

    @Test
    public void verifyConfig() throws Exception {
        Server session = JpaHelper.getServerSession(emf);

        ClassDescriptor descriptor = session.getClassDescriptorForAlias("Simple");
        assertNotNull("No descriptor found for alias='Simple'", descriptor);

        EntityTypeImpl simpleType = (EntityTypeImpl) DynamicHelper.getType(session, "Simple");
        assertNotNull("'Simple' EntityType not found", simpleType);

        assertEquals(descriptor, simpleType.getDescriptor());
    }

    @Test
    public void simpleInstance_CRUD() {
        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleType = (EntityTypeImpl) DynamicHelper.getType(session, "Simple");

        EntityManager em = emf.createEntityManager();

        DynamicEntity simpleInstance = createSimpleInstance(emf, 1);

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(s) FROM Simple s").getSingleResult()).intValue();
        assertEquals(1, simpleCount);

        IdentityMapAccessor cache = session.getIdentityMapAccessor();
        assertTrue(cache.containsObjectInIdentityMap(simpleInstance));

        em.clear();
        cache.initializeAllIdentityMaps();

        DynamicEntity findResult = (DynamicEntity) em.find(simpleType.getJavaClass(), 1);

        assertNotNull(findResult);
        assertEquals(simpleInstance.get("id"), findResult.get("id"));
        assertEquals(simpleInstance.get("value2"), findResult.get("value2"));

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

        em.getTransaction().begin();
        em.persist(simpleInstance);
        em.getTransaction().commit();

        em.close();
        return simpleInstance;
    }

    @BeforeClass
    public static void setUp() {
        Map properties = new HashMap();

        properties.put("eclipselink.ddl-generation.output-mode", "database");
        properties.put("eclipselink.ddl-generation", "drop-and-create-tables");

        emf = Persistence.createEntityManagerFactory("empty", properties);

        Server session = JpaHelper.getServerSession(emf);
        try {
            session.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE CASCADE CONSTRAINTS");
        } catch (DatabaseException dbe) {
            // ignore
        }

        Class simpleTypeClass = DynamicConversionManager.lookup(session).createDynamicClass("model.Simple");
        EntityTypeImpl entityType = new EntityTypeImpl(simpleTypeClass, "SIMPLE_TYPE");
        entityType.addDirectMapping("id", int.class, "SID", true);
        entityType.addDirectMapping("value1", String.class, "VAL_1", false);
        entityType.addDirectMapping("value2", boolean.class, "VAL_2", false);
        entityType.addDirectMapping("value3", Calendar.class, "VAL_3", false);
        entityType.addDirectMapping("value4", Character.class, "VAL_4", false);

        entityType.addToSession(session);
        new DynamicSchemaManager(session).createTables(entityType);
    }

    @Before
    @After
    public void clearSimpleTypeInstances() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Simple").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @AfterClass
    public static void shutdown() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM CUSTOM_REL_MTM").executeUpdate();
        em.createNativeQuery("DELETE FROM CUSTOM_REL_MTO").executeUpdate();
        em.createNativeQuery("DELETE FROM CUSTOM_REL_OTO").executeUpdate();
        em.createNativeQuery("DELETE FROM CUSTOM_FIELD").executeUpdate();
        em.createNativeQuery("DELETE FROM CUSTOM_TYPE").executeUpdate();
        em.createNativeQuery("DROP TABLE SIMPLE_TYPE CASCADE CONSTRAINTS").executeUpdate();
        em.getTransaction().commit();

        em.close();
        emf.close();
    }
}
