package org.eclipse.persistence.testing.tests.dynamic.simple;

import static junit.framework.Assert.*;

import java.util.Calendar;

import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.*;
import org.junit.Assert;

public class SimpleTypeCompositeKey extends SimpleType {

    @Override
    protected DynamicType createSimpleType() {
        DatabaseSession session = getSharedSession();
        DynamicHelper helper = new DynamicHelper(session);
        DynamicClassLoader dcl = helper.getDynamicClassLoader();

        Class<?> javaType = dcl.createDynamicClass("model.Simple");

        DynamicTypeBuilder typeBuilder = new DynamicTypeBuilder(javaType, null, "SIMPLE_TYPE");
        typeBuilder.setPrimaryKeyFields("SID1", "SID2");
        typeBuilder.addDirectMapping("id1", int.class, "SID1");
        typeBuilder.addDirectMapping("id2", int.class, "SID2");
        typeBuilder.addDirectMapping("value1", String.class, "VAL_1");
        typeBuilder.addDirectMapping("value2", boolean.class, "VAL_2");
        typeBuilder.addDirectMapping("value3", Calendar.class, "VAL_3");
        typeBuilder.addDirectMapping("value4", Character.class, "VAL_4");

        helper.addTypes(true, false, typeBuilder.getType());

        return typeBuilder.getType();
    }

    @Override
    protected void assertDefaultValues(DynamicEntity simpleInstance) {
        assertNotNull(simpleInstance);

        assertTrue("id1 not set on new instance", simpleInstance.isSet("id1"));
        assertEquals("id1 not default value", 0, simpleInstance.get("id1"));
        assertTrue("id2 not set on new instance", simpleInstance.isSet("id2"));
        assertEquals("id2 not default value", 0, simpleInstance.get("id2"));
        assertFalse("value1  set on new instance", simpleInstance.isSet("value1"));
        assertTrue("value2 not set on new instance", simpleInstance.isSet("value2"));
        assertEquals("value2 not default value", false, simpleInstance.get("value2"));
        assertFalse("value3 set on new instance", simpleInstance.isSet("value3"));
        assertFalse("value4  set on new instance", simpleInstance.isSet("value4"));
    }

    public DynamicEntity createSimpleInstance(Session session, int id) {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        DynamicType simpleEntityType = helper.getType("Simple");
        Assert.assertNotNull(simpleEntityType);

        DynamicEntity simpleInstance = simpleEntityType.newDynamicEntity();
        simpleInstance.set("id1", id);
        simpleInstance.set("id2", id);
        simpleInstance.set("value2", true);

        ReportQuery countQuery = helper.newReportQuery("Simple", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        assertEquals(0, ((Number) session.executeQuery(countQuery)).intValue());

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstance);
        uow.commit();

        assertEquals(1, ((Number) session.executeQuery(countQuery)).intValue());

        DynamicEntity foundEntity = find(helper, session, 1);

        assertNotNull(foundEntity);
        assertEquals(simpleInstance.get("id1"), foundEntity.get("id1"));
        assertEquals(simpleInstance.get("id2"), foundEntity.get("id2"));
        assertEquals(simpleInstance.get("value1"), foundEntity.get("value1"));
        assertEquals(simpleInstance.get("value2"), foundEntity.get("value2"));

        return simpleInstance;
    }

    @Override
    protected DynamicEntity find(DynamicHelper helper, Session session, Object id) {
        ReadObjectQuery findQuery = helper.newReadObjectQuery( "Simple");
        ExpressionBuilder eb = findQuery.getExpressionBuilder();
        findQuery.setSelectionCriteria(eb.get("id1").equal(id).and(eb.get("id2").equal(id)));
        return (DynamicEntity) session.executeQuery(findQuery);
    }

}
