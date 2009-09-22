package org.eclipse.persistence.testing.tests.dynamic.simple.mappings;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Collection;

import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.junit.*;

public class SimpleTypes_OneToMany extends EclipseLinkORMTest {

    private EntityType aType;
    private EntityType bType;

    public EntityType getAType() {
        if (this.aType == null) {
            this.aType = DynamicHelper.getType(getSharedSession(), "SimpleA");
        }
        return aType;
    }

    public EntityType getBType() {
        if (this.bType == null) {
            this.bType = DynamicHelper.getType(getSharedSession(), "SimpleB");
        }
        return bType;
    }

    @Test
    public void verifyConfig() throws Exception {
        Session session = getSession();

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
        Session session = getSession();

        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);

        DynamicEntity simpleInstance = simpleTypeA.newInstance();
        simpleInstance.set("id", 1);
        simpleInstance.set("value1", "A1");

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstance);
        uow.commit();

        ReportQuery countQuery = DynamicHelper.newReportQuery(session, "SimpleA", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCount = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCount);

        session.release();
    }

    @Test
    public void createSimpleB() {
        Session session = getSession();

        EntityTypeImpl simpleTypeB = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleB");
        Assert.assertNotNull(simpleTypeB);

        DynamicEntity simpleInstance = simpleTypeB.newInstance();
        simpleInstance.set("id", 1);
        simpleInstance.set("value1", "B1");

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstance);
        uow.commit();

        ReportQuery countQuery = DynamicHelper.newReportQuery(session, "SimpleB", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCount = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCount);

    }

    @Test
    public void createAwithB() {
        Session session = getSession();

        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);
        EntityTypeImpl simpleTypeB = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleB");
        Assert.assertNotNull(simpleTypeB);

        Assert.assertNotNull(session.getDescriptorForAlias("SimpleB"));

        DynamicEntity simpleInstanceB = simpleTypeB.newInstance();
        simpleInstanceB.set("id", 1);
        simpleInstanceB.set("value1", "B2");

        DynamicEntity simpleInstanceA = simpleTypeA.newInstance();
        simpleInstanceA.set("id", 1);
        simpleInstanceA.set("value1", "A2");
        simpleInstanceA.<Collection<DynamicEntity>> get("b").add(simpleInstanceA);

        simpleInstanceB.set("a", simpleInstanceB);

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstanceB);
        uow.registerNewObject(simpleInstanceA);
        uow.commit();

        ReportQuery countQuery = DynamicHelper.newReportQuery(session, "SimpleB", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCountB = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCountB);
        countQuery = DynamicHelper.newReportQuery(session, "SimpleA", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCountA = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCountA);

        session.release();
    }

    @Test
    public void removeAwithB_PrivateOwned() {
        createAwithB();

        ((OneToManyMapping) getAType().getDescriptor().getMappingForAttributeName("b")).setIsPrivateOwned(true);

        Session session = getSession();

        UnitOfWork uow = session.acquireUnitOfWork();

        ReadObjectQuery findQuery = DynamicHelper.newReadObjectQuery(session, "SimpleA");
        findQuery.setSelectionCriteria(findQuery.getExpressionBuilder().get("id").equal(1));
        DynamicEntity a = (DynamicEntity) uow.executeQuery(findQuery);

        assertNotNull(a);
        ReportQuery countQuery = DynamicHelper.newReportQuery(session, "SimpleB", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCountB = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCountB);
        countQuery = DynamicHelper.newReportQuery(session, "SimpleA", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCountA = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCountA);

        uow.deleteObject(a);
        // em.remove(a.get("b", List.class).get(0));

        uow.commit();

        countQuery = DynamicHelper.newReportQuery(session, "SimpleB", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        simpleCountB = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(0, simpleCountB);
        countQuery = DynamicHelper.newReportQuery(session, "SimpleA", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        simpleCountA = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(0, simpleCountA);

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

    @Override
    protected DatabaseSession createSharedSession() {
        DatabaseSession shared = super.createSharedSession();
        DynamicClassLoader dcl = DynamicClassLoader.lookup(shared);

        // Create Dynamic Classes
        Class<?> simpleTypeA = dcl.creatDynamicClass("model.SimpleA");
        Class<?> simpleTypeB = dcl.creatDynamicClass("model.SimpleB");

        // Build dynamic types with mappings
        EntityTypeBuilder aTypeBuilder = new EntityTypeBuilder(simpleTypeA, null, "SIMPLE_TYPE_A");
        aTypeBuilder.setPrimaryKeyFields("SID");

        EntityTypeBuilder bTypeBuilder = new EntityTypeBuilder(simpleTypeB, null, "SIMPLE_TYPE_B");
        bTypeBuilder.setPrimaryKeyFields("SID");

        bTypeBuilder.addDirectMapping("id", int.class, "SID");
        bTypeBuilder.addDirectMapping("value1", String.class, "VAL_1");
        bTypeBuilder.addOneToOneMapping("a", aTypeBuilder.getType(), "A_FK");

        aTypeBuilder.addDirectMapping("id", int.class, "SID");
        aTypeBuilder.addDirectMapping("value1", String.class, "VAL_1");
        aTypeBuilder.addOneToManyMapping("b", bTypeBuilder.getType(), "A_FK");

        EntityTypeBuilder.addToSession(shared, true, true, aTypeBuilder.getType(), bTypeBuilder.getType());

        return shared;
    }

    @After
    public void clearDynamicTables() {
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_TYPE_B");
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_TYPE_A");
    }

    @AfterClass
    public static void shutdown() {
        try {
            sharedSession.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE_A CASCADE CONSTRAINTS");
            sharedSession.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE_B CASCADE CONSTRAINTS");
        } catch (DatabaseException dbe) {
            // ignore
        }
    }

}
