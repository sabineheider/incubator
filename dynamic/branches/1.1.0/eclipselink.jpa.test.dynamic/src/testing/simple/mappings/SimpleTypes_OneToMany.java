package testing.simple.mappings;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPAEntityTypeBuilder;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleTypes_OneToMany {

    private static EntityManagerFactory emf;

    private EntityType aType;
    private EntityType bType;

    public EntityType getAType() {
        if (this.aType == null) {
            this.aType = DynamicHelper.getType(JpaHelper.getServerSession(emf), "SimpleA");
        }
        return aType;
    }

    public EntityType getBType() {
        if (this.bType == null) {
            this.bType = DynamicHelper.getType(JpaHelper.getServerSession(emf), "SimpleB");
        }
        return bType;
    }

    @Test
    public void verifyConfig() throws Exception {
        Server session = JpaHelper.getServerSession(emf);

        ClassDescriptor descriptorA = session.getClassDescriptorForAlias("SimpleA");
        assertNotNull("No descriptor found for alias='SimpleA'", descriptorA);

        EntityTypeImpl simpleTypeA = (EntityTypeImpl) getAType();
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
        DirectToFieldMapping b_id = (DirectToFieldMapping) descriptorB.getMappingForAttributeName("id");
        assertEquals(int.class, b_id.getAttributeClassification());
        DirectToFieldMapping b_value1 = (DirectToFieldMapping) descriptorB.getMappingForAttributeName("value1");
        assertEquals(String.class, b_value1.getAttributeClassification());

        OneToManyMapping a_b = (OneToManyMapping) descriptorA.getMappingForAttributeName("b");
        assertEquals(descriptorB, a_b.getReferenceDescriptor());
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
    public void createAwithB() {
        Server session = JpaHelper.getServerSession(emf);
        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);
        EntityTypeImpl simpleTypeB = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleB");
        Assert.assertNotNull(simpleTypeB);

        EntityManager em = emf.createEntityManager();

        Assert.assertNotNull(JpaHelper.getServerSession(emf).getDescriptorForAlias("SimpleB"));

        DynamicEntity simpleInstanceB = simpleTypeB.newInstance();
        simpleInstanceB.set("id", 1);
        simpleInstanceB.set("value1", "B2");

        DynamicEntity simpleInstanceA = simpleTypeA.newInstance();
        simpleInstanceA.set("id", 1);
        simpleInstanceA.set("value1", "A2");
        simpleInstanceA.get("b", Collection.class).add( simpleInstanceA);

        simpleInstanceB.set("a", simpleInstanceB);

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

    @Test
    public void removeAwithB_PrivateOwned() {
        createAwithB();

        ((OneToManyMapping) getAType().getDescriptor().getMappingForAttributeName("b")).setIsPrivateOwned(true);

        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        DynamicEntity a = (DynamicEntity) em.find(getAType().getJavaClass(), 1);
        assertNotNull(a);
        assertEquals(1, a.get("b", Collection.class).size());

        em.remove(a);
        //em.remove(a.get("b", List.class).get(0));

        em.getTransaction().commit();
    }

    @Test
    public void createAwithExistingB() {
        // TODO Assert.fail("Not Yet Implemented");
    }

    @Test
    public void removeBfromA() {
        // TODO Assert.fail("Not Yet Implemented");
    }

    @Test
    public void addAtoB() {
        // TODO Assert.fail("Not Yet Implemented");
    }

    @BeforeClass
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("empty");
        Server session = JpaHelper.getServerSession(emf);

        EntityTypeBuilder aFactory = new JPAEntityTypeBuilder(session, "model.SimpleA", null, "SIMPLE_TYPE_A");
        aFactory.setPrimaryKeyFields("SID");
        EntityTypeBuilder bFactory = new JPAEntityTypeBuilder(session, "model.SimpleB", null, "SIMPLE_TYPE_B");
        bFactory.setPrimaryKeyFields("SID");

        bFactory.addDirectMapping("id", int.class, "SID");
        bFactory.addDirectMapping("value1", String.class, "VAL_1");
        bFactory.addOneToOneMapping("a", aFactory.getType(), "A_FK");

        aFactory.addDirectMapping("id", int.class, "SID");
        aFactory.addDirectMapping("value1", String.class, "VAL_1");
        aFactory.addOneToManyMapping("b", bFactory.getType(), "A_FK");

        EntityTypeBuilder.addToSession(session, true, true, aFactory.getType(), bFactory.getType());
    }

    @After
    public void clearDynamicTables() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.createQuery("DELETE FROM SimpleB").executeUpdate();
        em.createQuery("DELETE FROM SimpleA").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @AfterClass
    public static void shutdown() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        try {
            em.createNativeQuery("DROP TABLE SIMPLE_TYPE_A CASCADE CONSTRAINTS").executeUpdate();
            em.createNativeQuery("DROP TABLE SIMPLE_TYPE_B CASCADE CONSTRAINTS").executeUpdate();
        } catch (DatabaseException dbe) {
            // ignore
        }
        em.getTransaction().commit();

        em.close();
        emf.close();
    }

}
