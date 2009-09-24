/*******************************************************************************
 * Copyright (c) 1998, 2008 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     dclarke - Dynamic Persistence INCUBATION - Enhancement 200045
 *               http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.testing.tests.dynamic.simple.sequencing;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.IdentityMapAccessor;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.testing.tests.dynamic.DynamicTestHelper;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public abstract class BaseSequencingTest extends EclipseLinkORMTest {

    public static final String TABLE_NAME = "SIMPLE_TABLE_SEQ";

    public static final String ENTITY_TYPE = "Simple";

    @Test
    public void verifyConfig() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());

        ClassDescriptor descriptor = helper.getSession().getClassDescriptorForAlias(ENTITY_TYPE);
        assertNotNull("No descriptor found for alias: " + ENTITY_TYPE, descriptor);

        DynamicType simpleType = helper.getType(ENTITY_TYPE);
        assertNotNull("EntityType not found for alias: " + ENTITY_TYPE, simpleType);

        assertEquals(descriptor, simpleType.getDescriptor());

        assertTrue("Descriptor does not use sequencing", descriptor.usesSequenceNumbers());
        verifySequencingConfig(helper.getSession(), descriptor);
    }

    protected abstract void verifySequencingConfig(Session session, ClassDescriptor descriptor);

    @Test
    public void createSingleInstances() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = acquireSession();

        DynamicEntity simpleInstance = createSimpleInstance(helper, session, 1);

        ReportQuery countQuery = helper.newReportQuery(ENTITY_TYPE, new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);

        assertEquals(1, count(helper, session));

        IdentityMapAccessor cache = session.getIdentityMapAccessor();
        assertTrue(cache.containsObjectInIdentityMap(simpleInstance));
        cache.initializeAllIdentityMaps();

        DynamicEntity findResult = find(helper, session, 1);

        assertNotNull(findResult);
        assertEquals(simpleInstance.get("id"), findResult.get("id"));
        assertEquals(simpleInstance.get("value1"), findResult.get("value1"));

        session.release();
    }

    @Test
    public void createTwoInstances() throws DatabaseException, Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = acquireSession();

        DynamicEntity simpleInstance1 = createSimpleInstance(helper, session, 1);
        DynamicEntity simpleInstance2 = createSimpleInstance(helper, session, 2);

        ReportQuery countQuery = helper.newReportQuery(ENTITY_TYPE, new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);

        assertEquals(2, count(helper, session));

        IdentityMapAccessor cache = session.getIdentityMapAccessor();
        assertTrue(cache.containsObjectInIdentityMap(simpleInstance1));
        assertTrue(cache.containsObjectInIdentityMap(simpleInstance2));

        cache.initializeAllIdentityMaps();

        DynamicEntity findResult1 = find(helper, session, 1);
        DynamicEntity findResult2 = find(helper, session, 2);

        assertNotNull(findResult1);
        assertNotNull(findResult2);
        assertEquals(simpleInstance1.get("id"), findResult1.get("id"));
        assertEquals(simpleInstance2.get("value1"), findResult2.get("value1"));

        session.release();
    }

    protected Session acquireSession() throws DatabaseException, Exception {
        if (getSharedSession().isServerSession()) {
            return ((Server) getSharedSession()).acquireClientSession();
        }
        return getSharedSession();
    }

    protected DynamicEntity find(DynamicHelper helper, Session session, int id) {
        ReadObjectQuery findQuery = helper.newReadObjectQuery(ENTITY_TYPE);
        findQuery.setSelectionCriteria(findQuery.getExpressionBuilder().get("id").equal(id));
        return (DynamicEntity) session.executeQuery(findQuery);
    }

    protected int count(DynamicHelper helper, Session session) {
        ReportQuery countQuery = helper.newReportQuery(ENTITY_TYPE, new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);

        return ((Number) session.executeQuery(countQuery)).intValue();
    }

    protected DynamicEntity createSimpleInstance(DynamicHelper helper, Session session, int expectedId) {
        DynamicType simpleEntityType = helper.getType(ENTITY_TYPE);
        Assert.assertNotNull(simpleEntityType);

        DynamicEntity simpleInstance = simpleEntityType.newDynamicEntity();
        simpleInstance.set("value1", TABLE_NAME);

        UnitOfWork uow = session.acquireUnitOfWork();

        assertEquals(0, simpleInstance.get("id"));
        uow.registerNewObject(simpleInstance);
        assertEquals(0, simpleInstance.get("id"));

        // uow.assignSequenceNumber(simpleInstance);

        uow.commit();
        assertEquals(expectedId, simpleInstance.get("id"));

        return simpleInstance;
    }

    @Override
    protected DatabaseSession createSharedSession() {
        if (sharedSession == null) {
            sharedSession = DynamicTestHelper.createEmptySession();
            sharedSession.login();
            
            DynamicHelper helper = new DynamicHelper(sharedSession);
            DynamicClassLoader dcl = DynamicClassLoader.lookup(getSharedSession());

            Class<?> dynamicType = dcl.createDynamicClass("model.sequencing." + ENTITY_TYPE);
            DynamicTypeBuilder typeBuilder = new DynamicTypeBuilder(dynamicType, null, TABLE_NAME);
            typeBuilder.setPrimaryKeyFields("SID");
            typeBuilder.addDirectMapping("id", int.class, "SID");
            typeBuilder.addDirectMapping("value1", String.class, "VAL_1");

            configureSequencing(sharedSession, typeBuilder);

            helper.addTypes(true, true, typeBuilder.getType());
        }

        return sharedSession;
    }

    protected abstract void configureSequencing(DatabaseSession session, DynamicTypeBuilder typeBuilder);

    @After
    public void clearSimpleTypeInstances() throws Exception {
        getSharedSession().executeNonSelectingSQL("DELETE FROM " + TABLE_NAME);
        resetSequence(getSharedSession());
        getSharedSession().getSequencingControl().initializePreallocated();
    }

    protected abstract void resetSequence(DatabaseSession session);

    @AfterClass
    public static void shutdown() {
        sharedSession.executeNonSelectingSQL("DROP TABLE " + TABLE_NAME + " CASCADE CONSTRAINTS");
    }
}
