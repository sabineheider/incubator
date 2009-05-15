package testing;

import java.util.*;

import javax.persistence.*;

import junit.framework.Assert;
import model.meta.CustomType;

import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.*;

public class SimpleType {

    private static EntityManagerFactory emf;

    @Test
    public void simpleType() {
        EntityManager em = emf.createEntityManager();

        Assert.assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("Simple"));

        CustomType simpleType = em.find(CustomType.class, "Simple");

        Assert.assertNotNull(simpleType);

        EntityType simpleEntityType = DynamicHelper.getType(JpaHelper.getServerSession(emf), "Simple");
        Assert.assertNotNull(simpleEntityType);

        DynamicEntity simpleInstance = simpleEntityType.newInstance();
        simpleInstance.set("id", 1);
        simpleInstance.set("value2", true);

        em.getTransaction().begin();
        em.persist(simpleInstance);
        em.getTransaction().commit();

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(s) FROM Simple s").getSingleResult()).intValue();
        Assert.assertEquals(1, simpleCount);

        em.close();
    }

    @Test
    public void buildCustomTyeFromDB() {
        emf.close();
        emf = Persistence.createEntityManagerFactory("custom-types");

        Assert.assertNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("Simple"));

        EntityManager em = emf.createEntityManager();

        CustomType simpleType = em.find(CustomType.class, "Simple");
        Assert.assertNotNull(simpleType);

        simpleType.createType(emf, false, false);
        Assert.assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("Simple"));

        EntityType simpleEntityType = DynamicHelper.getType(JpaHelper.getServerSession(emf), "Simple");
        Assert.assertNotNull(simpleEntityType);

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(s) FROM Simple s").getSingleResult()).intValue();
        Assert.assertEquals(1, simpleCount);

        DynamicEntity simpleInstance = (DynamicEntity) em.find(simpleEntityType.getJavaClass(), 1);
        Assert.assertNotNull(simpleInstance);
        Assert.assertTrue(simpleInstance.get("id", Integer.class).equals(1));
        Assert.assertNull(simpleInstance.get("value1", Integer.class));
        Assert.assertTrue(simpleInstance.get("value2", Boolean.class).equals(true));
        Assert.assertTrue(simpleInstance.get("value2").equals(true));
        Assert.assertNull(simpleInstance.get("value3", Integer.class));
        Assert.assertNull(simpleInstance.get("value4", Integer.class));

        em.close();
    }

    @BeforeClass
    public static void setUp() {
        Map properties = new HashMap();

        properties.put("eclipselink.ddl-generation.output-mode", "database");
        properties.put("eclipselink.ddl-generation", "drop-and-create-tables");

        emf = Persistence.createEntityManagerFactory("custom-types", properties);

        CustomType simpleType = new CustomType();
        simpleType.setName("Simple");
        simpleType.setClassName("model.Simple");
        simpleType.setTableName("CUSTOM_SIMPLE");
        simpleType.addField("id", int.class, "SID").setId(true);
        simpleType.addField("value1", String.class, "VAL_1");
        simpleType.addField("value2", boolean.class, "VAL_2");
        simpleType.addField("value3", Calendar.class, "VAL_3");
        simpleType.addField("value4", Character.class, "VAL_4");

        simpleType.createType(emf, true, true);

    }

    @AfterClass
    public static void shutdown() {
        emf.close();
    }
}
