package org.eclipse.persistence.testing.tests.dynamic.simple.mappings;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
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

public class SimpleTypes_MultiTable extends EclipseLinkORMTest {

    @Test
    public void verifyConfig() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        ClassDescriptor descriptorA = helper.getSession().getClassDescriptorForAlias("SimpleA");
        assertNotNull("No descriptor found for alias='SimpleA'", descriptorA);

        DynamicType simpleTypeA = helper.getType("SimpleA");
        assertNotNull("'SimpleA' EntityType not found", simpleTypeA);
        assertEquals(descriptorA, simpleTypeA.getDescriptor());

        assertTrue(descriptorA.hasMultipleTables());
        assertEquals(3, descriptorA.getTables().size());

        DirectToFieldMapping a_id = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("id");
        assertEquals(int.class, a_id.getAttributeClassification());
        DirectToFieldMapping a_value1 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value1");
        assertEquals(String.class, a_value1.getAttributeClassification());

        DirectToFieldMapping a_value2 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value2");
        assertEquals(boolean.class, a_value2.getAttributeClassification());

        DirectToFieldMapping a_value3 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value3");
        assertEquals(String.class, a_value3.getAttributeClassification());

        DirectToFieldMapping a_value4 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value4");
        assertEquals(double.class, a_value4.getAttributeClassification());

        DirectToFieldMapping a_value5 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value5");
        assertEquals(String.class, a_value5.getAttributeClassification());
    }

    @Test
    public void verifyProperties() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        DynamicType simpleTypeA = helper.getType("SimpleA");
        Assert.assertNotNull(simpleTypeA);

        assertEquals(6, simpleTypeA.getNumberOfProperties());
        assertEquals("id", simpleTypeA.getPropertiesNames().get(0));
        assertEquals(int.class, simpleTypeA.getPropertyType(0));
        assertEquals("value1", simpleTypeA.getPropertiesNames().get(1));
        assertEquals(String.class, simpleTypeA.getPropertyType(1));
        assertEquals("value2", simpleTypeA.getPropertiesNames().get(2));
        assertEquals(boolean.class, simpleTypeA.getPropertyType(2));
        assertEquals("value3", simpleTypeA.getPropertiesNames().get(3));
        assertEquals(String.class, simpleTypeA.getPropertyType(3));
        assertEquals("value4", simpleTypeA.getPropertiesNames().get(4));
        assertEquals(double.class, simpleTypeA.getPropertyType(4));
        assertEquals("value5", simpleTypeA.getPropertiesNames().get(5));
        assertEquals(String.class, simpleTypeA.getPropertyType(5));

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
        assertTrue(a.isSet("value2"));
        assertFalse(a.isSet("value3"));
        assertTrue(a.isSet("value4"));
        assertFalse(a.isSet("value5"));
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

        session.release();
    }

    @Test
    public void verifyChangeTracking() {
        persistSimpleA();

        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        DynamicType simpleTypeA = helper.getType("SimpleA");
        Assert.assertNotNull(simpleTypeA);

        UnitOfWork uow = session.acquireUnitOfWork();

        ReadObjectQuery findQuery = helper.newReadObjectQuery("SimpleA");
        findQuery.setSelectionCriteria(findQuery.getExpressionBuilder().get("id").equal(1));
        DynamicEntityImpl a = (DynamicEntityImpl) uow.executeQuery(findQuery);

        assertNotNull(a);
        assertNotNull(a._persistence_getPropertyChangeListener());

        uow.release();
        session.release();
    }

    @Override
    protected DatabaseSession createSharedSession() {
        DatabaseSession shared = super.createSharedSession();
        DynamicHelper helper = new DynamicHelper(shared);
        DynamicClassLoader dcl = helper.getDynamicClassLoader();

        Class<?> simpleTypeA = dcl.createDynamicClass("model.SimpleA");

        DynamicTypeBuilder typeBuilder = new DynamicTypeBuilder(simpleTypeA, null, "SIMPLE_TYPE_A", "SIMPLE_TYPE_B", "SIMPLE_TYPE_C");
        typeBuilder.setPrimaryKeyFields("SIMPLE_TYPE_A.SID");
        typeBuilder.addDirectMapping("id", int.class, "SIMPLE_TYPE_A.SID");
        typeBuilder.addDirectMapping("value1", String.class, "SIMPLE_TYPE_A.VAL_1");
        typeBuilder.addDirectMapping("value2", boolean.class, "SIMPLE_TYPE_B.VAL_2");
        typeBuilder.addDirectMapping("value3", String.class, "SIMPLE_TYPE_B.VAL_3");
        typeBuilder.addDirectMapping("value4", double.class, "SIMPLE_TYPE_C.VAL_4");
        typeBuilder.addDirectMapping("value5", String.class, "SIMPLE_TYPE_C.VAL_5");

        shared.login();
        helper.addTypes(true, true, typeBuilder.getType());

        return shared;
    }

    @After
    public void clearDynamicTables() {
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_TYPE_C");
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_TYPE_B");
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_TYPE_A");
    }

    @AfterClass
    public static void shutdown() {
        sharedSession.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE_A CASCADE CONSTRAINTS");
        sharedSession.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE_B CASCADE CONSTRAINTS");
        sharedSession.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE_C CASCADE CONSTRAINTS");
    }

}
