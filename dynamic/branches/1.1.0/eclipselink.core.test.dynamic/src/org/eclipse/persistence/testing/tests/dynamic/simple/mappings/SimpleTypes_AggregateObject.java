package org.eclipse.persistence.testing.tests.dynamic.simple.mappings;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.descriptors.changetracking.AggregateAttributeChangeListener;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
import org.eclipse.persistence.mappings.AggregateObjectMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

public class SimpleTypes_AggregateObject extends EclipseLinkORMTest {

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
        DirectToFieldMapping b_value2 = (DirectToFieldMapping) descriptorB.getMappingForAttributeName("value2");
        assertEquals(boolean.class, b_value2.getAttributeClassification());
        DirectToFieldMapping b_value3 = (DirectToFieldMapping) descriptorB.getMappingForAttributeName("value3");
        assertEquals(String.class, b_value3.getAttributeClassification());
        assertTrue(descriptorB.isAggregateDescriptor());

        AggregateObjectMapping a_b = (AggregateObjectMapping) descriptorA.getMappingForAttributeName("b");
        assertSame(descriptorB.getJavaClass(), a_b.getReferenceDescriptor().getJavaClass());
        assertTrue(a_b.isNullAllowed());

        ClassDescriptor descriptorC = helper.getSession().getClassDescriptorForAlias("SimpleC");
        assertNotNull("No descriptor found for alias='SimpleB'", descriptorB);

        DynamicType simpleTypeC = helper.getType("SimpleC");
        assertNotNull("'SimpleC' EntityType not found", simpleTypeC);
        assertEquals(descriptorB, simpleTypeB.getDescriptor());
        DirectToFieldMapping c_value4 = (DirectToFieldMapping) descriptorC.getMappingForAttributeName("value4");
        assertEquals(double.class, c_value4.getAttributeClassification());
        DirectToFieldMapping c_value5 = (DirectToFieldMapping) descriptorC.getMappingForAttributeName("value5");
        assertEquals(String.class, c_value5.getAttributeClassification());
        assertTrue(descriptorB.isAggregateDescriptor());

        AggregateObjectMapping a_c = (AggregateObjectMapping) descriptorA.getMappingForAttributeName("c");
        assertSame(descriptorC.getJavaClass(), a_c.getReferenceDescriptor().getJavaClass());
        assertFalse(a_c.isNullAllowed());
    }

    @Test
    public void verifyProperties() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        DynamicType simpleTypeA = helper.getType("SimpleA");
        Assert.assertNotNull(simpleTypeA);

        assertEquals(4, simpleTypeA.getNumberOfProperties());
        assertEquals("id", simpleTypeA.getPropertiesNames().get(0));
        assertEquals("value1", simpleTypeA.getPropertiesNames().get(1));
        assertEquals("b", simpleTypeA.getPropertiesNames().get(2));
        assertEquals("c", simpleTypeA.getPropertiesNames().get(3));
    }

    @Test
    public void createSimpleA() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        DynamicType simpleTypeA = helper.getType("SimpleA");
        Assert.assertNotNull(simpleTypeA);

        DynamicEntity a = simpleTypeA.newDynamicEntity();

        assertNotNull(a);
        assertTrue(a.isSet("id"));
        assertFalse(a.isSet("value1"));
        assertFalse(a.isSet("b"));
        assertTrue(a.isSet("c"));

        DynamicEntity c = a.<DynamicEntity> get("c");
        assertNotNull(c);
        assertTrue(c.isSet("value4"));
        assertFalse(c.isSet("value5"));
    }

    @Test
    public void persistSimpleA() {
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
    }

    @Test
    public void verifyChangTracking() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        persistSimpleA();

        DynamicType simpleTypeA = helper.getType("SimpleA");
        Assert.assertNotNull(simpleTypeA);

        UnitOfWork uow = session.acquireUnitOfWork();

        ReadObjectQuery roq = helper.newReadObjectQuery("SimpleA");
        roq.setSelectionCriteria(roq.getExpressionBuilder().get("id").equal(1));

        DynamicEntityImpl sharedA = (DynamicEntityImpl) session.executeQuery(roq);
        assertNotNull(sharedA);
        assertNull(sharedA._persistence_getPropertyChangeListener());

        DynamicEntityImpl a = (DynamicEntityImpl) uow.executeQuery(roq);
        assertNotNull(a);
        assertNotNull(a._persistence_getPropertyChangeListener());

        DynamicEntityImpl c = a.<DynamicEntityImpl> get("c");
        assertNotNull(c);
        assertNotNull(c._persistence_getPropertyChangeListener());
        assertTrue(c._persistence_getPropertyChangeListener() instanceof AggregateAttributeChangeListener);

        uow.release();
    }

    @Test
    public void createSimpleAwithSimpleB() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        DynamicType simpleTypeA = helper.getType("SimpleA");
        Assert.assertNotNull(simpleTypeA);
        DynamicType simpleTypeB = helper.getType("SimpleB");
        Assert.assertNotNull(simpleTypeB);

        Assert.assertNotNull(session.getDescriptorForAlias("SimpleB"));

        DynamicEntity simpleInstanceB = simpleTypeB.newDynamicEntity();
        simpleInstanceB.set("value2", true);
        simpleInstanceB.set("value3", "B2");

        DynamicEntity simpleInstanceA = simpleTypeA.newDynamicEntity();
        simpleInstanceA.set("id", 2);
        simpleInstanceA.set("value1", "A2");
        simpleInstanceA.set("b", simpleInstanceB);

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstanceA);
        uow.commit();

        ReportQuery countQuery = helper.newReportQuery("SimpleA", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);

        int simpleCountA = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCountA);

    }

    @Override
    protected DatabaseSession getSharedSession() {
        DatabaseSession shared = super.getSharedSession();

        DynamicHelper helper = new DynamicHelper(shared);
        DynamicClassLoader dcl = helper.getDynamicClassLoader();

        Class<?> simpleTypeB = dcl.createDynamicClass("model.SimpleB");
        DynamicTypeBuilder bTypeBuilder = new DynamicTypeBuilder(simpleTypeB, null);
        bTypeBuilder.addDirectMapping("value2", boolean.class, "VAL_2");
        bTypeBuilder.addDirectMapping("value3", String.class, "VAL_3");

        Class<?> simpleTypeC = dcl.createDynamicClass("model.SimpleC");
        DynamicTypeBuilder cTypeBuilder = new DynamicTypeBuilder(simpleTypeC, null);
        cTypeBuilder.addDirectMapping("value4", double.class, "VAL_4");
        cTypeBuilder.addDirectMapping("value5", String.class, "VAL_5");

        Class<?> simpleTypeA = dcl.createDynamicClass("model.SimpleA");
        DynamicTypeBuilder aTypeBuilder = new DynamicTypeBuilder(simpleTypeA, null, "SIMPLE_TYPE_A");
        aTypeBuilder.setPrimaryKeyFields("SID");
        aTypeBuilder.addDirectMapping("id", int.class, "SID");
        aTypeBuilder.addDirectMapping("value1", String.class, "VAL_1");
        aTypeBuilder.addAggregateObjectMapping("b", bTypeBuilder.getType(), true);
        aTypeBuilder.addAggregateObjectMapping("c", cTypeBuilder.getType(), false);

        helper.addTypes(true, true, aTypeBuilder.getType(), bTypeBuilder.getType(), cTypeBuilder.getType());
        return shared;
    }

    @After
    public void clearDynamicTables() {
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_TYPE_A");
    }

    @AfterClass
    public static void shutdown() {
        sharedSession.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE_A CASCADE CONSTRAINTS");
    }

}
