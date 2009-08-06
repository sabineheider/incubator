package testing;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

import junit.framework.Assert;
import model.meta.CustomType;

import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.*;

public class AB_OneToOne {

    private static EntityManagerFactory emf;

    @Test
    public void simpleTypeA() {
        EntityManager em = emf.createEntityManager();

        Assert.assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("SimpleA"));

        CustomType simpleType = em.find(CustomType.class, "SimpleA");

        Assert.assertNotNull(simpleType);

        EntityType simpleEntityType = DynamicHelper.getType(JpaHelper.getServerSession(emf), "SimpleA");
        Assert.assertNotNull(simpleEntityType);

        DynamicEntity simpleInstance = simpleEntityType.newInstance();
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
    public void simpleTypeB() {
        EntityManager em = emf.createEntityManager();

        Assert.assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("SimpleB"));

        CustomType simpleType = em.find(CustomType.class, "SimpleB");

        Assert.assertNotNull(simpleType);

        EntityType simpleEntityType = DynamicHelper.getType(JpaHelper.getServerSession(emf), "SimpleB");
        Assert.assertNotNull(simpleEntityType);

        DynamicEntity simpleInstance = simpleEntityType.newInstance();
        simpleInstance.set("id", 1);
        simpleInstance.set("value1", "B1");

        em.getTransaction().begin();
        em.persist(simpleInstance);
        em.getTransaction().commit();

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(s) FROM SimpleB s").getSingleResult()).intValue();
        Assert.assertEquals(1, simpleCount);

        em.close();
    }

    @Test
    public void simpleTypeAwithB() {
        EntityManager em = emf.createEntityManager();

        Assert.assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("SimpleB"));

        CustomType simpleType = em.find(CustomType.class, "SimpleB");

        Assert.assertNotNull(simpleType);

        EntityType simpleEntityTypeB = DynamicHelper.getType(JpaHelper.getServerSession(emf), "SimpleB");
        Assert.assertNotNull(simpleEntityTypeB);

        DynamicEntity simpleInstanceB = simpleEntityTypeB.newInstance();
        simpleInstanceB.set("id", 1);
        simpleInstanceB.set("value1", "B1");

        EntityType simpleEntityTypeA = DynamicHelper.getType(JpaHelper.getServerSession(emf), "SimpleA");
        Assert.assertNotNull(simpleEntityTypeA);

        DynamicEntity simpleInstanceA = simpleEntityTypeA.newInstance();
        simpleInstanceA.set("id", 1);
        simpleInstanceA.set("value1", "A1");
        simpleInstanceA.set("b", simpleInstanceA);

        em.getTransaction().begin();
        em.persist(simpleInstanceB);
        em.persist(simpleInstanceA);
        em.getTransaction().commit();

        int simpleCount = ((Number) em.createQuery("SELECT COUNT(s) FROM SimpleB s").getSingleResult()).intValue();
        Assert.assertEquals(1, simpleCount);

        em.close();
    }

    @Test
    public void buildCustomTypesFromDB() {
        emf.close();
        emf = Persistence.createEntityManagerFactory("custom-types");

        Assert.assertNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("SimpleA"));
        Assert.assertNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("SimpleB"));

        EntityManager em = emf.createEntityManager();

        CustomType simpleTypeB = em.find(CustomType.class, "SimpleB");
        Assert.assertNotNull(simpleTypeB);
        CustomType simpleTypeA = em.find(CustomType.class, "SimpleA");
        Assert.assertNotNull(simpleTypeA);

        simpleTypeB.createType(emf, false, false);
        Assert.assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("SimpleB"));
        simpleTypeA.createType(emf, false, false);
        Assert.assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("SimpleA"));

        EntityType simpleEntityTypeA = DynamicHelper.getType(JpaHelper.getServerSession(emf), "SimpleA");
        Assert.assertNotNull(simpleEntityTypeA);
        EntityType simpleEntityTypeB = DynamicHelper.getType(JpaHelper.getServerSession(emf), "SimpleB");
        Assert.assertNotNull(simpleEntityTypeB);

        int simpleCountA = ((Number) em.createQuery("SELECT COUNT(s) FROM SimpleA s").getSingleResult()).intValue();
        Assert.assertEquals(1, simpleCountA);
        int simpleCountB = ((Number) em.createQuery("SELECT COUNT(s) FROM SimpleB s").getSingleResult()).intValue();
        Assert.assertEquals(1, simpleCountB);

        DynamicEntity simpleInstanceB = (DynamicEntity) em.find(simpleEntityTypeB.getJavaClass(), 1);
        Assert.assertNotNull(simpleInstanceB);
        Assert.assertTrue(simpleInstanceB.get("id", Integer.class).equals(1));
        Assert.assertNotNull(simpleInstanceB.get("value1", String.class));
        Assert.assertTrue(simpleInstanceB.get("value1", String.class).equals("B1"));

        DynamicEntity simpleInstanceA = (DynamicEntity) em.find(simpleEntityTypeA.getJavaClass(), 1);
        Assert.assertNotNull(simpleInstanceA);
        Assert.assertTrue(simpleInstanceA.get("id", Integer.class).equals(1));
        Assert.assertNotNull(simpleInstanceA.get("value1", String.class));
        Assert.assertTrue(simpleInstanceA.get("value1", String.class).equals("A1"));

        em.close();
    }

    @BeforeClass
    public static void setUp() {
        Map properties = new HashMap();

        // properties.put("eclipselink.ddl-generation.output-mode", "database");
        // properties.put("eclipselink.ddl-generation",
        // "drop-and-create-tables");

        emf = Persistence.createEntityManagerFactory("custom-types", properties);

        CustomType simpleTypeB = new CustomType();
        simpleTypeB.setName("SimpleB");
        simpleTypeB.setClassName("model.SimpleB");
        simpleTypeB.setTableName("CUSTOM_SIMPLE_B");
        simpleTypeB.addField("id", int.class.getName(), "SID").setId(true);
        simpleTypeB.addField("value1", String.class.getName(), "VAL_1");

        simpleTypeB.createType(emf, true, true);

        CustomType simpleTypeA = new CustomType();
        simpleTypeA.setName("SimpleA");
        simpleTypeA.setClassName("model.SimpleA");
        simpleTypeA.setTableName("CUSTOM_SIMPLE_A");
        simpleTypeA.addField("id", int.class.getName(), "SID").setId(true);
        simpleTypeA.addField("value1", String.class.getName(), "VAL_1");
        simpleTypeA.addOneToOne("b", simpleTypeB, "B_FK");

        simpleTypeA.createType(emf, true, true);

    }

    @AfterClass
    public static void shutdown() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.createQuery("DELETE FROM CustomField").executeUpdate();
        em.createQuery("DELETE FROM CustomType").executeUpdate();
        try {
            em.createNativeQuery("DROP TABLE CUSTOM_SIMPLE_A CASCADE CONSTRAINTS").executeUpdate();
        } catch (DatabaseException dbe) {
            // ignore
        }
        try {
            em.createNativeQuery("DROP TABLE CUSTOM_SIMPLE_B CASCADE CONSTRAINTS").executeUpdate();
        } catch (DatabaseException dbe) {
            // ignore
        }
        em.getTransaction().commit();

        emf.close();
    }
}
