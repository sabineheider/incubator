package testing.simple.mappings;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.*;

import javax.persistence.*;

import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.junit.*;

public class SimpleTypes_OneToMany {

    private static EntityManagerFactory emf;

    @Test
    public void verifyConfig() throws Exception {
        Server session = JpaHelper.getServerSession(emf);

        ClassDescriptor descriptorA = session.getClassDescriptorForAlias("SimpleA");
        assertNotNull("No descriptor found for alias='SimpleA'", descriptorA);

        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        assertNotNull("'SimpleA' EntityType not found", simpleTypeA);

        assertEquals(descriptorA, simpleTypeA.getDescriptor());

        ClassDescriptor descriptorB = session.getClassDescriptorForAlias("SimpleB");
        assertNotNull("No descriptor found for alias='SimpleB'", descriptorB);

        EntityTypeImpl simpleTypeB = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleB");
        assertNotNull("'SimpleB' EntityType not found", simpleTypeB);

        assertEquals(descriptorB, simpleTypeB.getDescriptor());
}

    @Test
    public void createSimpleA() {
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
    public void createSimpleB() {
        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleTypeB = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleB");
        Assert.assertNotNull(simpleTypeB);

        EntityManager em = emf.createEntityManager();

        DynamicEntity simpleInstance = simpleTypeB.newInstance();
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
    public void createSimpleAwithSimpleB() {
        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);
        EntityTypeImpl simpleTypeB = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleB");
        Assert.assertNotNull(simpleTypeB);
        
        EntityManager em = emf.createEntityManager();

        Assert.assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("SimpleB"));


        DynamicEntity simpleInstanceB = simpleTypeB.newInstance();
        simpleInstanceB.set("id", 2);
        simpleInstanceB.set("value1", "B2");

        DynamicEntity simpleInstanceA = simpleTypeA.newInstance();
        simpleInstanceA.set("id", 2);
        simpleInstanceA.set("value1", "A2");
        simpleInstanceA.set("b", simpleInstanceA);

        em.getTransaction().begin();
        em.persist(simpleInstanceB);
        em.persist(simpleInstanceA);
        em.getTransaction().commit();

        int simpleCountB = ((Number) em.createQuery("SELECT COUNT(s) FROM SimpleB s").getSingleResult()).intValue();
        Assert.assertEquals(1, simpleCountB);
        int simpleCountA = ((Number) em.createQuery("SELECT COUNT(s) FROM SimpleA s").getSingleResult()).intValue();
        Assert.assertEquals(1, simpleCountA);

        em.close();
    }

    @BeforeClass
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("empty");

        Server session = JpaHelper.getServerSession(emf);
        try {
            session.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE_A CASCADE CONSTRAINTS");
        } catch (DatabaseException dbe) {
            // ignore
        }
        try {
            session.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE_B CASCADE CONSTRAINTS");
        } catch (DatabaseException dbe) {
            // ignore
        }

        Class simpleTypeBClass = DynamicConversionManager.lookup(session).createDynamicClass("model.SimpleB");
        EntityTypeImpl entityTypeB = new EntityTypeImpl(simpleTypeBClass, "SIMPLE_TYPE_B");
        entityTypeB.addDirectMapping("id", int.class, "SID", true);
        entityTypeB.addDirectMapping("value1", String.class, "VAL_1", false);
        entityTypeB.addToSession(session);

        Class simpleTypeAClass = DynamicConversionManager.lookup(session).createDynamicClass("model.SimpleA");
        EntityTypeImpl entityTypeA = new EntityTypeImpl(simpleTypeAClass, "SIMPLE_TYPE_A");
        entityTypeA.addDirectMapping("id", int.class, "SID", true);
        entityTypeA.addDirectMapping("value1", String.class, "VAL_1", false);
        entityTypeA.addOneToOneMapping("b", simpleTypeBClass, "B_FK", "SID");
        entityTypeA.addToSession(session);


        new DynamicSchemaManager(session).createTables(entityTypeA, entityTypeB);
    }

    @After
    @Before
    public void clearDynamicTables() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.createQuery("DELETE FROM SimpleA").executeUpdate();
        em.createQuery("DELETE FROM SimpleB").executeUpdate();
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
        em.createNativeQuery("DROP TABLE SIMPLE_TYPE_A CASCADE CONSTRAINTS").executeUpdate();
        em.createNativeQuery("DROP TABLE SIMPLE_TYPE_B CASCADE CONSTRAINTS").executeUpdate();
        em.getTransaction().commit();

        em.close();
        emf.close();
    }

}
