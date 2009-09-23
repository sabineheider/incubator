package org.eclipse.persistence.testing.tests.dynamic.simple.mappings;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.testing.tests.dynamic.DynamicTestHelper;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.*;

public class SimpleTypes_OneToOne extends EclipseLinkORMTest {

    @Test
    public void verifyConfig() throws Exception {
        Session session = getSession();

        ClassDescriptor descriptorA = session.getClassDescriptorForAlias("SimpleA");
        assertNotNull("No descriptor found for alias='SimpleA'", descriptorA);

        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
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

        OneToOneMapping a_b = (OneToOneMapping) descriptorA.getMappingForAttributeName("b");
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
    public void createSimpleAwithSimpleB() {
        Session session = getSession();

        EntityTypeImpl simpleTypeA = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleA");
        Assert.assertNotNull(simpleTypeA);
        EntityTypeImpl simpleTypeB = (EntityTypeImpl) DynamicHelper.getType(session, "SimpleB");
        Assert.assertNotNull(simpleTypeB);
        Assert.assertNotNull(session.getDescriptorForAlias("SimpleB"));

        DynamicEntity simpleInstanceB = simpleTypeB.newInstance();
        simpleInstanceB.set("id", 2);
        simpleInstanceB.set("value1", "B2");

        DynamicEntity simpleInstanceA = simpleTypeA.newInstance();
        simpleInstanceA.set("id", 2);
        simpleInstanceA.set("value1", "A2");
        simpleInstanceA.set("b", simpleInstanceB);

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstanceA);
        uow.registerNewObject(simpleInstanceB);
        uow.commit();
        
        assertEquals(2, getQuerySQLTracker(session).getTotalSQLINSERTCalls());
        // There is no reason for a shallow insert and an update in this mapping
        assertEquals("No update expected for new objects with 1:1", 0, getQuerySQLTracker(session).getTotalSQLUPDATECalls());

        ReportQuery countQuery = DynamicHelper.newReportQuery(session, "SimpleB", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        Assert.assertEquals(1, ((Number) session.executeQuery(countQuery)).intValue());
        
        countQuery = DynamicHelper.newReportQuery(session, "SimpleA", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        Assert.assertEquals(1, ((Number) session.executeQuery(countQuery)).intValue());

    }

    @Override
    protected DatabaseSession createSharedSession() {
        Project project = new Project(DynamicTestHelper.getTestLogin());
        DatabaseSession shared = project.createDatabaseSession();
        DynamicClassLoader dcl = DynamicClassLoader.lookup(shared);

        Class<?> simpleTypeB = dcl.createDynamicClass("model.SimpleB");
        EntityTypeBuilder bFactory = new EntityTypeBuilder(simpleTypeB, null, "SIMPLE_TYPE_B");
        bFactory.setPrimaryKeyFields("SID");
        bFactory.addDirectMapping("id", int.class, "SID");
        bFactory.addDirectMapping("value1", String.class, "VAL_1");

        Class<?> simpleTypeA = dcl.createDynamicClass("model.SimpleA");
        EntityTypeBuilder aFactory = new EntityTypeBuilder(simpleTypeA, null, "SIMPLE_TYPE_A");
        aFactory.setPrimaryKeyFields("SID");
        aFactory.addDirectMapping("id", int.class, "SID");
        aFactory.addDirectMapping("value1", String.class, "VAL_1");
        aFactory.addOneToOneMapping("b", bFactory.getType(), "B_FK");

        EntityTypeBuilder.addToSession(shared, false, true, aFactory.getType(), bFactory.getType());

        shared.getSessionLog().setLevel(SessionLog.FINE);
        shared.login();
        
        new SchemaManager(shared).replaceDefaultTables();
        
        return shared;
    }

    @After
    public void clearDynamicTables() {
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_TYPE_A");
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_TYPE_B");
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
