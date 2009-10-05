package org.eclipse.persistence.testing.tests.dynamic.simple.mappings;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.DynamicException;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

public class SimpleTypes_OneToMany extends EclipseLinkORMTest {

    @Test
    public void verifyConfig() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        ClassDescriptor descriptorA = helper.getSession().getClassDescriptorForAlias("SimpleA");
        assertNotNull("No descriptor found for alias='SimpleA'", descriptorA);

        DynamicType simpleTypeA = helper.getType("SimpleA");
        assertNotNull("'SimpleA' EntityType not found", simpleTypeA);
        assertEquals(descriptorA, simpleTypeA.getDescriptor());
        DirectToFieldMapping a_id = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("id");
        assertEquals(int.class, a_id.getAttributeClassification());
        DirectToFieldMapping a_value1 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value1");
        assertEquals(String.class, a_value1.getAttributeClassification());

        ClassDescriptor descriptorB = helper.getSession().getClassDescriptorForAlias("SimpleB");
        assertNotNull("No descriptor found for alias='SimpleB'", descriptorB);

        DynamicType simpleTypeB = helper.getType("SimpleB");
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
    public void verifyNewSimpleA() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        DynamicEntity newA = helper.getType("SimpleA").newDynamicEntity();

        assertNotNull(newA);
        assertTrue(newA.isSet("id"));
        assertFalse(newA.isSet("value1"));
        assertTrue(newA.isSet("b"));

        Object b = newA.get("b");
        assertNotNull(b);
        assertTrue(b instanceof Collection);
    }

    @Test
    public void verifyNewSimpleA_InvalidB_Map() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        DynamicEntity newA = helper.getType("SimpleA").newDynamicEntity();

        try {
            newA.set("b", new HashMap());
        } catch (DynamicException e) {
            return;
        }
        fail("DynamicException expected putting Map in 'b'");
    }

    @Test
    public void verifyNewSimpleA_InvalidB_Object() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        DynamicEntity newA = helper.getType("SimpleA").newDynamicEntity();

        try {
            newA.set("b", new Object());
        } catch (DynamicException e) {
            return;
        }
        fail("DynamicException expected putting Object in 'b'");
    }

    @Test
    public void verifyNewSimpleA_InvalidB_A() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        DynamicEntity newA = helper.getType("SimpleA").newDynamicEntity();

        try {
            newA.set("b", helper.getType("SimpleA").newDynamicEntity());
        } catch (DynamicException e) {
            return;
        }
        fail("DynamicException expected putting A in 'b'");
    }

    @Test
    public void verifyNewSimpleA_InvalidB_B() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        DynamicEntity newA = helper.getType("SimpleA").newDynamicEntity();

        try {
            newA.set("b", helper.getType("SimpleB").newDynamicEntity());
        } catch (DynamicException e) {
            return;
        }
        fail("DynamicException expected putting B in 'b'");
    }

    @Test
    public void verifyNewSimpleA_InvalidB_NULL() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        DynamicEntity newA = helper.getType("SimpleA").newDynamicEntity();

        try {
            newA.set("b", null);
        } catch (DynamicException e) {
            return;
        }
        fail("DynamicException expected putting NULL in 'b'");
    }

    @Test
    public void createSimpleA() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        DynamicType simpleTypeA = helper.getType("SimpleA");
        Assert.assertNotNull(simpleTypeA);

        DynamicEntity simpleInstance = simpleTypeA.newDynamicEntity();
        simpleInstance.set("id", 1);
        simpleInstance.set("value1", "A1");

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstance);
        uow.commit();

        ReportQuery countQuery = helper.newReportQuery("SimpleA", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCount = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCount);

        session.release();
    }

    @Test
    public void createSimpleB() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        DynamicType simpleTypeB = helper.getType("SimpleB");
        Assert.assertNotNull(simpleTypeB);

        DynamicEntity simpleInstance = simpleTypeB.newDynamicEntity();
        simpleInstance.set("id", 1);
        simpleInstance.set("value1", "B1");

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstance);
        uow.commit();

        ReportQuery countQuery = helper.newReportQuery("SimpleB", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCount = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCount);

    }

    @Test
    public void createAwithB() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        DynamicType simpleTypeA = helper.getType("SimpleA");
        Assert.assertNotNull(simpleTypeA);
        DynamicType simpleTypeB = helper.getType("SimpleB");
        Assert.assertNotNull(simpleTypeB);

        Assert.assertNotNull(session.getDescriptorForAlias("SimpleB"));

        DynamicEntity simpleInstanceB = simpleTypeB.newDynamicEntity();
        simpleInstanceB.set("id", 1);
        simpleInstanceB.set("value1", "B2");

        DynamicEntity simpleInstanceA = simpleTypeA.newDynamicEntity();
        simpleInstanceA.set("id", 1);
        simpleInstanceA.set("value1", "A2");
        simpleInstanceA.<Collection<DynamicEntity>> get("b").add(simpleInstanceB);

        simpleInstanceB.set("a", simpleInstanceA);

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstanceB);
        uow.registerNewObject(simpleInstanceA);
        uow.commit();

        ReportQuery countQuery = helper.newReportQuery("SimpleB", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCountB = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCountB);

        countQuery = helper.newReportQuery("SimpleA", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCountA = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCountA);

        session.release();
    }

    @Test
    public void createAwithBCollection() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        DynamicType simpleTypeA = helper.getType("SimpleA");
        Assert.assertNotNull(simpleTypeA);
        DynamicType simpleTypeB = helper.getType("SimpleB");
        Assert.assertNotNull(simpleTypeB);

        Assert.assertNotNull(session.getDescriptorForAlias("SimpleB"));

        DynamicEntity simpleInstanceB = simpleTypeB.newDynamicEntity();
        simpleInstanceB.set("id", 1);
        simpleInstanceB.set("value1", "B2");

        DynamicEntity simpleInstanceA = simpleTypeA.newDynamicEntity();
        simpleInstanceA.set("id", 1);
        simpleInstanceA.set("value1", "A2");

        Collection<DynamicEntity> bs = new ArrayList<DynamicEntity>();
        bs.add(simpleInstanceB);
        simpleInstanceA.set("b", bs);

        simpleInstanceB.set("a", simpleInstanceA);

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstanceB);
        uow.registerNewObject(simpleInstanceA);
        uow.commit();

        ReportQuery countQuery = helper.newReportQuery("SimpleB", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCountB = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCountB);

        countQuery = helper.newReportQuery("SimpleA", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCountA = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCountA);

        session.release();
    }

    @Test
    public void removeAwithB_PrivateOwned() {
        createAwithB();

        DynamicHelper helper = new DynamicHelper(getSharedSession());

        DynamicType simpleAType = helper.getType("SimpleA");
        ((OneToManyMapping) simpleAType.getDescriptor().getMappingForAttributeName("b")).setIsPrivateOwned(true);

        Session session = getSession();

        UnitOfWork uow = session.acquireUnitOfWork();

        ReadObjectQuery findQuery = helper.newReadObjectQuery("SimpleA");
        findQuery.setSelectionCriteria(findQuery.getExpressionBuilder().get("id").equal(1));
        DynamicEntity a = (DynamicEntity) uow.executeQuery(findQuery);

        assertNotNull(a);
        ReportQuery countQuery = helper.newReportQuery("SimpleB", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCountB = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCountB);
        countQuery = helper.newReportQuery("SimpleA", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCountA = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCountA);

        uow.deleteObject(a);
        // em.remove(a.get("b", List.class).get(0));

        uow.commit();

        countQuery = helper.newReportQuery("SimpleB", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        simpleCountB = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(0, simpleCountB);
        countQuery = helper.newReportQuery("SimpleA", new ExpressionBuilder());
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
        DynamicHelper helper = new DynamicHelper(shared);
        DynamicClassLoader dcl = helper.getDynamicClassLoader();

        // Create Dynamic Classes
        Class<?> simpleTypeA = dcl.createDynamicClass("model.SimpleA");
        Class<?> simpleTypeB = dcl.createDynamicClass("model.SimpleB");

        // Build dynamic types with mappings
        DynamicTypeBuilder aTypeBuilder = new DynamicTypeBuilder(simpleTypeA, null, "SIMPLE_TYPE_A");
        aTypeBuilder.setPrimaryKeyFields("SID");

        DynamicTypeBuilder bTypeBuilder = new DynamicTypeBuilder(simpleTypeB, null, "SIMPLE_TYPE_B");
        bTypeBuilder.setPrimaryKeyFields("SID");

        bTypeBuilder.addDirectMapping("id", int.class, "SID");
        bTypeBuilder.addDirectMapping("value1", String.class, "VAL_1");
        bTypeBuilder.addOneToOneMapping("a", aTypeBuilder.getType(), "A_FK");

        aTypeBuilder.addDirectMapping("id", int.class, "SID");
        aTypeBuilder.addDirectMapping("value1", String.class, "VAL_1");
        aTypeBuilder.addOneToManyMapping("b", bTypeBuilder.getType(), "A_FK");

        helper.addTypes(true, true, aTypeBuilder.getType(), bTypeBuilder.getType());

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
