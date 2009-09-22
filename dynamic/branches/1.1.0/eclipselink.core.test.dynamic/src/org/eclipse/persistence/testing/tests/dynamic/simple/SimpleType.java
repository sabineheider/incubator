package org.eclipse.persistence.testing.tests.dynamic.simple;

import static junit.framework.Assert.*;

import java.util.Calendar;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.junit.*;

public class SimpleType extends EclipseLinkORMTest {

    protected EntityType simpleType;

    protected EntityType getSimpleType() {
        if (simpleType == null) {
            this.simpleType = DynamicHelper.getType(getSharedSession(), "Simple");

            if (this.simpleType == null) {
                createSimpleType();
            }
        }
        return this.simpleType;
    }

    protected EntityType createSimpleType() {
        DatabaseSession session = getSharedSession();

        DynamicClassLoader dcl = DynamicClassLoader.lookup(session);
        Class<?> javaType = dcl.creatDynamicClass("model.Simple");

        EntityTypeBuilder typeBuilder = new EntityTypeBuilder(javaType, null, "SIMPLE_TYPE");
        typeBuilder.setPrimaryKeyFields("SID");
        typeBuilder.addDirectMapping("id", int.class, "SID");
        typeBuilder.addDirectMapping("value1", String.class, "VAL_1");
        typeBuilder.addDirectMapping("value2", boolean.class, "VAL_2");
        typeBuilder.addDirectMapping("value3", Calendar.class, "VAL_3");
        typeBuilder.addDirectMapping("value4", Character.class, "VAL_4");

        typeBuilder.addToSession(session, true, true);

        return typeBuilder.getType();
    }

    @Test
    public void verifyConfig() throws Exception {
        DatabaseSession session = getSharedSession();

        ClassDescriptor descriptor = session.getClassDescriptorForAlias("Simple");
        assertNotNull("No descriptor found for alias='Simple'", descriptor);

        EntityTypeImpl simpleType = (EntityTypeImpl) DynamicHelper.getType(session, "Simple");
        assertNotNull("'Simple' EntityType not found", simpleType);

        assertEquals(1 + descriptor.getPrimaryKeyFields().size(), simpleType.getMappingsRequiringInitialization().size());

        assertEquals(descriptor, simpleType.getDescriptor());
    }

    @Test
    public void find() {
        Session session = getSession();

        createSimpleInstance(session, 1);

        DynamicEntity simpleInstance = find(session, 1);
        assertNotNull("Could not find simple instance with id = 1", simpleInstance);

        simpleInstance = find(session, new Integer(1));
        assertNotNull("Could not find simple instance with id = Integer(1)", simpleInstance);
    }

    @Test
    public void simpleInstance_CRUD() {
        Session session = getSession();

        IdentityMapAccessor cache = session.getIdentityMapAccessor();

        DynamicEntity simpleInstance = createSimpleInstance(session, 1);
        assertNotNull(simpleInstance);

        assertTrue(cache.containsObjectInIdentityMap(simpleInstance));
        cache.initializeAllIdentityMaps();
        assertFalse(cache.containsObjectInIdentityMap(simpleInstance));

    }

    @Test
    public void verifyDefaultValuesFromEntityType() throws Exception {
        EntityType simpleType = DynamicHelper.getType(getSharedSession(), "Simple");

        assertNotNull(simpleType);

        DynamicEntity simpleInstance = simpleType.newInstance();
        assertDefaultValues(simpleInstance);
    }

    @Test
    public void verifyDefaultValuesFromDescriptor() throws Exception {
        EntityTypeImpl simpleType = (EntityTypeImpl) DynamicHelper.getType(getSharedSession(), "Simple");
        assertNotNull(simpleType);

        DynamicEntity simpleInstance = (DynamicEntity) simpleType.getDescriptor().getObjectBuilder().buildNewInstance();
        assertDefaultValues(simpleInstance);
    }

    protected void assertDefaultValues(DynamicEntity simpleInstance) {
        assertNotNull(simpleInstance);

        assertTrue("id not set on new instance", simpleInstance.isSet("id"));
        assertEquals("id not default value", 0, simpleInstance.get("id"));
        assertFalse("value1  set on new instance", simpleInstance.isSet("value1"));
        assertTrue("value2 not set on new instance", simpleInstance.isSet("value2"));
        assertEquals("value2 not default value", false, simpleInstance.get("value2"));
        assertFalse("value3 set on new instance", simpleInstance.isSet("value3"));
        assertFalse("value4  set on new instance", simpleInstance.isSet("value4"));
    }

    public DynamicEntity createSimpleInstance(Session session, int id) {
        EntityType simpleEntityType = DynamicHelper.getType(session, "Simple");
        Assert.assertNotNull(simpleEntityType);

        DynamicEntity simpleInstance = simpleEntityType.newInstance();
        simpleInstance.set("id", id);
        simpleInstance.set("value2", true);

        ReportQuery countQuery = DynamicHelper.newReportQuery(session, "Simple", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        assertEquals(0, ((Number) session.executeQuery(countQuery)).intValue());

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstance);
        uow.commit();

        assertEquals(1, ((Number) session.executeQuery(countQuery)).intValue());

        DynamicEntity foundEntity = find(session, 1);

        assertNotNull(foundEntity);
        assertEquals(simpleInstance.get("id"), foundEntity.get("id"));
        assertEquals(simpleInstance.get("value1"), foundEntity.get("value1"));
        assertEquals(simpleInstance.get("value2"), foundEntity.get("value2"));

        session.release();

        return simpleInstance;
    }

    protected DynamicEntity find(Session session, Object id) {
        ReadObjectQuery findQuery = DynamicHelper.newReadObjectQuery(session, "Simple");
        findQuery.setSelectionCriteria(findQuery.getExpressionBuilder().get("id").equal(id));
        return (DynamicEntity) session.executeQuery(findQuery);
    }

    @Before
    @After
    public void clearSimpleTypeInstances() {
        getSimpleType();

        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_TYPE");
        getSharedSession().getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    @AfterClass
    public static void shutdown() {
        sharedSession.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE CASCADE CONSTRAINTS");
    }
}
