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
 *     mnorman - Dynamic Persistence INCUBATION - Enhancement 200045
 *               http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.testing.tests.dynamic.orm.projectxml;

import static junit.framework.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Date;
import java.util.List;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.testing.tests.dynamic.DynamicTestHelper;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.junit.*;

/*
 * Test cases verifying the use of the simple-map-project.xml 
 */
public class SimpleMapProject extends EclipseLinkORMTest {

    protected String getProjectLocation() {
        return "org/eclipse/persistence/testing/tests/dynamic/orm/projectxml/simple-map-project.xml";
    }

    @Override
    protected DatabaseSession createSharedSession() {
        DatabaseLogin login = DynamicTestHelper.getTestLogin();
        DynamicClassLoader dcl = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
        Project project = null;
        try {
            project = EntityTypeBuilder.loadDynamicProject(getProjectLocation(), login, dcl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DatabaseSession ds = project.createDatabaseSession();
        ds.setLogLevel(SessionLog.FINE);
        ds.login();

        new DynamicSchemaManager(ds).createTables(new EntityType[0]);

        return ds;
    }

    @Test
    public void verifyDescriptor() throws Exception {
        ClassDescriptor descriptor = getSharedSession().getClassDescriptorForAlias("simpletableType");

        assertNotNull(descriptor);
        assertEquals("simpletable.Simpletable", descriptor.getJavaClassName());

        assertEquals(3, descriptor.getMappings().size());

        DatabaseMapping idMapping = descriptor.getMappingForAttributeName("id");
        assertNotNull(idMapping);
        assertTrue(idMapping.isDirectToFieldMapping());
        assertEquals(BigInteger.class, idMapping.getAttributeClassification());

        DatabaseMapping nameMapping = descriptor.getMappingForAttributeName("name");
        assertNotNull(nameMapping);
        assertTrue(nameMapping.isDirectToFieldMapping());
        assertEquals(String.class, nameMapping.getAttributeClassification());

        DatabaseMapping sinceMapping = descriptor.getMappingForAttributeName("since");
        assertNotNull(sinceMapping);
        assertTrue(sinceMapping.isDirectToFieldMapping());
        assertEquals(Date.class, sinceMapping.getAttributeClassification());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void createInstance() {
        Session session = getSession();

        EntityType type = DynamicHelper.getType(session, "simpletableType");

        ReportQuery countQuery = DynamicHelper.newReportQuery(session, "simpletableType", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        assertEquals(0, ((Number) session.executeQuery(countQuery)).intValue());

        DynamicEntity entity = type.newInstance();
        entity.set("id", new BigInteger("1"));
        entity.set("name", "Example");
        entity.set("since", new Date(100, 06, 06));

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(entity);
        uow.commit();

        assertEquals(1, ((Number) session.executeQuery(countQuery)).intValue());
        session.release();
    }

    @Test
    public void readAll() {
        Session session = getSession();

        createInstance();
        EntityType type = DynamicHelper.getType(session, "simpletableType");

        List<DynamicEntity> allObjects = session.readAllObjects(type.getJavaClass());
        assertEquals(1, allObjects.size());
    }

    @Test
    public void readById() {
        Session session = getSession();

        createInstance();

        ReadObjectQuery query = DynamicHelper.newReadObjectQuery(session, "simpletableType");
        query.setSelectionCriteria(query.getExpressionBuilder().get("id").equal(1));

        DynamicEntity entity = (DynamicEntity) session.executeQuery(query);

        assertNotNull(entity);
    }

    @Test
    public void delete() {
        Session session = getSession();

        createInstance();

        ReadObjectQuery query = DynamicHelper.newReadObjectQuery(session, "simpletableType");
        query.setSelectionCriteria(query.getExpressionBuilder().get("id").equal(1));

        UnitOfWork uow = session.acquireUnitOfWork();

        DynamicEntity entity = (DynamicEntity) uow.executeQuery(query);
        assertNotNull(entity);

        uow.deleteObject(entity);
        uow.commit();

        ReportQuery countQuery = DynamicHelper.newReportQuery(session, "simpletableType", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        assertEquals(0, ((Number) session.executeQuery(countQuery)).intValue());
    }

    @After
    public void clearTable() {
        getSharedSession().executeNonSelectingSQL("DELETE FROM SIMPLETABLE");
    }

    @AfterClass
    public static void removeTables() {
        sharedSession.executeNonSelectingSQL("DROP TABLE SIMPLETABLE CASCADE CONSTRAINTS");
    }

}