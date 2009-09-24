package org.eclipse.persistence.testing.tests.dynamic.simple.mappings;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicTypeImpl;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.ManyToManyMapping;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

public class SimpleTypes_ManyToMany extends EclipseLinkORMTest {

    private DynamicHelper helper;
    private DynamicType aType;
    private DynamicType bType;

    public DynamicType getAType() {
        if (this.aType == null) {
            this.aType = getHelper().getType("SimpleA");
        }
        return aType;
    }

    public DynamicType getBType() {
        if (this.bType == null) {
            this.bType = getHelper().getType("SimpleB");
        }
        return bType;
    }

    public DynamicHelper getHelper() {
        if (this.helper == null) {
            this.helper = new DynamicHelper(getSharedSession());
        }
        return this.helper;
    }

    @Test
    public void verifyConfig() throws Exception {
        Session session = getSession();

        ClassDescriptor descriptorA = session.getClassDescriptorForAlias("SimpleA");
        assertNotNull("No descriptor found for alias='SimpleA'", descriptorA);

        DynamicTypeImpl simpleTypeA = (DynamicTypeImpl) getAType();
        assertNotNull("'SimpleA' EntityType not found", simpleTypeA);
        assertEquals(descriptorA, simpleTypeA.getDescriptor());
        DirectToFieldMapping a_id = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("id");
        assertEquals(int.class, a_id.getAttributeClassification());
        DirectToFieldMapping a_value1 = (DirectToFieldMapping) descriptorA.getMappingForAttributeName("value1");
        assertEquals(String.class, a_value1.getAttributeClassification());

        ClassDescriptor descriptorB = session.getClassDescriptorForAlias("SimpleB");
        assertNotNull("No descriptor found for alias='SimpleB'", descriptorB);

        DynamicTypeImpl simpleTypeB = (DynamicTypeImpl) getHelper().getType("SimpleB");
        assertNotNull("'SimpleB' EntityType not found", simpleTypeB);
        assertEquals(descriptorB, simpleTypeB.getDescriptor());
        DirectToFieldMapping b_id = (DirectToFieldMapping) descriptorB.getMappingForAttributeName("id");
        assertEquals(int.class, b_id.getAttributeClassification());
        DirectToFieldMapping b_value1 = (DirectToFieldMapping) descriptorB.getMappingForAttributeName("value1");
        assertEquals(String.class, b_value1.getAttributeClassification());

        ManyToManyMapping a_b = (ManyToManyMapping) descriptorA.getMappingForAttributeName("b");
        assertEquals(descriptorB, a_b.getReferenceDescriptor());
    }

    @Test
    public void createSimpleA() {
        Session session = getSession();

        DynamicTypeImpl simpleTypeA = (DynamicTypeImpl) getHelper().getType("SimpleA");
        Assert.assertNotNull(simpleTypeA);

        DynamicEntity simpleInstance = simpleTypeA.newDynamicEntity();
        simpleInstance.set("id", 1);
        simpleInstance.set("value1", "A1");

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstance);
        uow.commit();

        ReportQuery countQuery = getHelper().newReportQuery("SimpleA", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCount = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCount);
    }

    @Test
    public void createSimpleB() {
        Session session = getSession();

        DynamicTypeImpl simpleTypeB = (DynamicTypeImpl) getHelper().getType("SimpleB");
        Assert.assertNotNull(simpleTypeB);

        DynamicEntity simpleInstance = simpleTypeB.newDynamicEntity();
        simpleInstance.set("id", 1);
        simpleInstance.set("value1", "B1");

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstance);
        uow.commit();

        ReportQuery countQuery = getHelper().newReportQuery(simpleTypeB.getName(), new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int simpleCount = ((Number) session.executeQuery(countQuery)).intValue();
        Assert.assertEquals(1, simpleCount);
    }

    @Test
    public void createAwithB() {
        Session session = getSession();

        DynamicTypeImpl simpleTypeA = (DynamicTypeImpl) getHelper().getType("SimpleA");
        Assert.assertNotNull(simpleTypeA);
        DynamicTypeImpl simpleTypeB = (DynamicTypeImpl) getHelper().getType("SimpleB");
        Assert.assertNotNull(simpleTypeB);

        Assert.assertNotNull(session.getDescriptorForAlias("SimpleB"));

        DynamicEntity simpleInstanceB = simpleTypeB.newDynamicEntity();
        simpleInstanceB.set("id", 1);
        simpleInstanceB.set("value1", "B2");

        DynamicEntity simpleInstanceA = simpleTypeA.newDynamicEntity();
        simpleInstanceA.set("id", 1);
        simpleInstanceA.set("value1", "A2");
        simpleInstanceA.<Collection<DynamicEntity>> get("b").add(simpleInstanceA);

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(simpleInstanceB);
        uow.registerNewObject(simpleInstanceA);
        uow.commit();

        ReportQuery countQueryB = getHelper().newReportQuery("SimpleB", new ExpressionBuilder());
        countQueryB.addCount();
        countQueryB.setShouldReturnSingleValue(true);
        int simpleCountB = ((Number) session.executeQuery(countQueryB)).intValue();
        Assert.assertEquals(1, simpleCountB);

        ReportQuery countQueryA = getHelper().newReportQuery("SimpleA", new ExpressionBuilder());
        countQueryA.addCount();
        countQueryA.setShouldReturnSingleValue(true);
        int simpleCountA = ((Number) session.executeQuery(countQueryA)).intValue();
        Assert.assertEquals(1, simpleCountA);
    }

    @Test
    public void createAwithExistingB() {
        // TODO Assert.fail("Not Yet Implemented");
    }

    @Test
    public void removeRelationshop() {
        Session session = getSession();
        createAwithB();

        UnitOfWork uow = session.acquireUnitOfWork();

        ReadObjectQuery roq = getHelper().newReadObjectQuery("SimpleA");
        roq.setSelectionCriteria(roq.getExpressionBuilder().get("id").equal(1));
        DynamicEntity a = (DynamicEntity) session.executeQuery(roq);
        assertNotNull(a);

        List<DynamicEntity> bs = a.<List<DynamicEntity>> get("b");
        assertNotNull(bs);
        assertEquals(1, bs.size());
        bs.remove(0);

        uow.commit();
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

        Class<?> simpleTypeA = dcl.createDynamicClass("model.SimpleA");
        DynamicTypeBuilder aFactory = new DynamicTypeBuilder(simpleTypeA, null, "SIMPLE_TYPE_A");
        aFactory.setPrimaryKeyFields("SID");

        Class<?> simpleTypeB = dcl.createDynamicClass("model.SimpleB");
        DynamicTypeBuilder bFactory = new DynamicTypeBuilder(simpleTypeB, null, "SIMPLE_TYPE_B");
        bFactory.setPrimaryKeyFields("SID");

        bFactory.addDirectMapping("id", int.class, "SID");
        bFactory.addDirectMapping("value1", String.class, "VAL_1");

        aFactory.addDirectMapping("id", int.class, "SID");
        aFactory.addDirectMapping("value1", String.class, "VAL_1");
        aFactory.addManyToManyMapping("b", bFactory.getType(), "SIMPLE_A_B");

        helper.addTypes(true, true, aFactory.getType(), bFactory.getType());

        return shared;
    }

    @After
    public void clearDynamicTables() {
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_A_B");
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_TYPE_A");
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLE_TYPE_B");
    }

    @AfterClass
    public static void shutdown() {
        sharedSession.executeNonSelectingSQL("DROP TABLE SIMPLE_A_B CASCADE CONSTRAINTS");
        sharedSession.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE_A CASCADE CONSTRAINTS");
        sharedSession.executeNonSelectingSQL("DROP TABLE SIMPLE_TYPE_B CASCADE CONSTRAINTS");
    }

}
