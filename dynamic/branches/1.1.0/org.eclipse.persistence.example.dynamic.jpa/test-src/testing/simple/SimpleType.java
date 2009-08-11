package testing.simple;

import static junit.framework.Assert.*;

import java.util.*;

import javax.persistence.*;

import model.meta.CustomType;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
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
    public void simpleType() {
        EntityManager em = emf.createEntityManager();

        assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("Simple"));

        CustomType simpleType = em.find(CustomType.class, "Simple");

        assertNotNull(simpleType);

        createSimpleInstance(emf, 1);

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(s) FROM Simple s").getSingleResult()).intValue();
        assertEquals(1, simpleCount);

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

    @Test
    public void buildCustomTypeFromDB() {
        createSimpleInstance(emf, 1);

        emf.close();
        emf = Persistence.createEntityManagerFactory("custom-types");

        Assert.assertNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("Simple"));

        EntityManager em = emf.createEntityManager();

        CustomType simpleType = em.find(CustomType.class, "Simple");
        Assert.assertNotNull(simpleType);

        simpleType.createType(emf, false, false);
        Assert.assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("Simple"));

        EntityType simpleEntityType = DynamicHelper.getType(JpaHelper.getServerSession(emf), "Simple");
        assertNotNull(simpleEntityType);
        assertSame(simpleEntityType, simpleType.getEntityType());

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(s) FROM Simple s").getSingleResult()).intValue();
        Assert.assertEquals(1, simpleCount);

        DynamicEntity simpleInstance = (DynamicEntity) em.find(simpleEntityType.getJavaClass(), 1);

        assertNotNull(simpleInstance);
        assertTrue(simpleInstance.get("id", Integer.class).equals(1));
        assertNull(simpleInstance.get("value1", Integer.class));
        assertTrue(simpleInstance.get("value2", Boolean.class).equals(true));
        assertTrue(simpleInstance.get("value2").equals(true));
        assertNull(simpleInstance.get("value3", Integer.class));
        assertNull(simpleInstance.get("value4", Integer.class));

        em.close();
    }

    public void createSimpleInstance(EntityManagerFactory emf, int id) {
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
    }

    @BeforeClass
    public static void setUp() {
        Map properties = new HashMap();

        properties.put("eclipselink.ddl-generation.output-mode", "database");
        properties.put("eclipselink.ddl-generation", "drop-and-create-tables");

        emf = Persistence.createEntityManagerFactory("custom-types", properties);

        Server session = JpaHelper.getServerSession(emf);
        try {
            session.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE CASCADE CONSTRAINTS");
        } catch (DatabaseException dbe) {
            // ignore
        }

        CustomType simpleType = new CustomType();
        simpleType.setName("Simple");
        simpleType.setClassName("model.Simple");
        simpleType.setTableName("SIMPLE_TYPE");
        simpleType.addField("id", int.class.getName(), "SID").setId(true);
        simpleType.addField("value1", String.class.getName(), "VAL_1");
        simpleType.addField("value2", boolean.class.getName(), "VAL_2");
        simpleType.addField("value3", Calendar.class.getName(), "VAL_3");
        simpleType.addField("value4", Character.class.getName(), "VAL_4");

        simpleType.createType(emf, true, true);

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
