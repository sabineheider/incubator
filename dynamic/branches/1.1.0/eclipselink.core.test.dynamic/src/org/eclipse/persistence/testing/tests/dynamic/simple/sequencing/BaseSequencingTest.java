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

import static junit.framework.Assert.*;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.testing.tests.dynamic.DynamicTestHelper;
import org.junit.*;

public abstract class BaseSequencingTest {

    protected static DatabaseSession sharedSession;

    public static final String TABLE_NAME = "SIMPLE_TABLE_SEQ";

    public static final String ENTITY_TYPE = "Simple";

    @Test
    public void verifyConfig() throws Exception {
        Session session = acquireSession();

        ClassDescriptor descriptor = session.getClassDescriptorForAlias(ENTITY_TYPE);
        assertNotNull("No descriptor found for alias: " + ENTITY_TYPE, descriptor);

        EntityTypeImpl simpleType = (EntityTypeImpl) DynamicHelper.getType(session, ENTITY_TYPE);
        assertNotNull("EntityType not found for alias: " + ENTITY_TYPE, simpleType);

        assertEquals(descriptor, simpleType.getDescriptor());
        
        assertTrue("Descriptor does not use sequencing", descriptor.usesSequenceNumbers());
        verifySequencingConfig(session, descriptor);

        session.release();
    }
    
    protected abstract void verifySequencingConfig(Session session, ClassDescriptor descriptor);

    @Test
    public void createSingleInstances() throws Exception {
        Session session = acquireSession();

        DynamicEntity simpleInstance = createSimpleInstance(session, 1);

        ReportQuery countQuery = DynamicHelper.newReportQuery(session, ENTITY_TYPE, new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);

        assertEquals(1, count(session));

        IdentityMapAccessor cache = session.getIdentityMapAccessor();
        assertTrue(cache.containsObjectInIdentityMap(simpleInstance));
        cache.initializeAllIdentityMaps();

        DynamicEntity findResult = find(session, 1);

        assertNotNull(findResult);
        assertEquals(simpleInstance.get("id"), findResult.get("id"));
        assertEquals(simpleInstance.get("value1"), findResult.get("value1"));

        session.release();
    }

    @Test
    public void createTwoInstances() throws DatabaseException, Exception {
        Session session = acquireSession();

        DynamicEntity simpleInstance1 = createSimpleInstance(session, 1);
        DynamicEntity simpleInstance2 = createSimpleInstance(session, 2);

        ReportQuery countQuery = DynamicHelper.newReportQuery(session, ENTITY_TYPE, new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);

        assertEquals(2, count(session));

        IdentityMapAccessor cache = session.getIdentityMapAccessor();
        assertTrue(cache.containsObjectInIdentityMap(simpleInstance1));
        assertTrue(cache.containsObjectInIdentityMap(simpleInstance2));

        cache.initializeAllIdentityMaps();

        DynamicEntity findResult1 = find(session, 1);
        DynamicEntity findResult2 = find(session, 2);

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

    protected DynamicEntity find(Session session, int id) {
        ReadObjectQuery findQuery = DynamicHelper.newReadObjectQuery(session, ENTITY_TYPE);
        findQuery.setSelectionCriteria(findQuery.getExpressionBuilder().get("id").equal(id));
        return (DynamicEntity) session.executeQuery(findQuery);
    }

    protected int count(Session session) {
        ReportQuery countQuery = DynamicHelper.newReportQuery(session, ENTITY_TYPE, new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);

        return ((Number) session.executeQuery(countQuery)).intValue();
    }

    protected DynamicEntity createSimpleInstance(Session session, int expectedId) {
        EntityType simpleEntityType = DynamicHelper.getType(session, ENTITY_TYPE);
        Assert.assertNotNull(simpleEntityType);

        DynamicEntity simpleInstance = simpleEntityType.newInstance();
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

    protected DatabaseSession getSharedSession() throws Exception {
        if (sharedSession == null) {
            sharedSession = DynamicTestHelper.createEmptySession();

            DynamicClassLoader dcl = DynamicClassLoader.lookup(getSharedSession());

            Class<?> dynamicType = dcl.creatDynamicClass("model.sequencing." + ENTITY_TYPE);
            EntityTypeBuilder typeBuilder = new EntityTypeBuilder(dynamicType, null, TABLE_NAME);
            typeBuilder.setPrimaryKeyFields("SID");
            typeBuilder.addDirectMapping("id", int.class, "SID");
            typeBuilder.addDirectMapping("value1", String.class, "VAL_1");

            configureSequencing(getSharedSession(), typeBuilder);

            typeBuilder.addToSession(getSharedSession(), true, true);
        }

        return sharedSession;
    }

    protected abstract void configureSequencing(DatabaseSession session, EntityTypeBuilder typeBuilder);

    @Before
    public void clearSimpleTypeInstances() throws Exception {
        getSharedSession().executeNonSelectingSQL("DELETE FROM " + TABLE_NAME);
        resetSequence(getSharedSession());
        getSharedSession().getSequencingControl().initializePreallocated();
    }
    
    protected abstract void resetSequence(DatabaseSession session);

    public static void shutdown() {
        sharedSession.executeNonSelectingSQL("DROP TABLE " + TABLE_NAME + " CASCADE CONSTRAINTS");

        sharedSession.logout();
        sharedSession = null;
    }
}
