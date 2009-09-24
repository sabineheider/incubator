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

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.testing.tests.dynamic.DynamicTestHelper;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

/*
 * Test cases verifying the use of the simple-map-project.xml 
 */
public class EmployeeProject extends EclipseLinkORMTest {

    @Override
    protected DatabaseSession createSharedSession() {
        DatabaseLogin login = DynamicTestHelper.getTestLogin();
        Project project = null;
        try {
            project = DynamicTypeBuilder.loadDynamicProject("org/eclipse/persistence/testing/tests/dynamic/orm/projectxml/employee-project.xml", login, new DynamicClassLoader(EmployeeProject.class.getClassLoader()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        DatabaseSession ds = project.createDatabaseSession();
        ds.login();
        new SchemaManager(ds).replaceDefaultTables();

        return ds;
    }

    @Test
    public void createNewInstance() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        DynamicType employeeType = helper.getType("Employee");
        DynamicType periodType = helper.getType("EmploymentPeriod");

        DynamicEntity entity = employeeType.newDynamicEntity();
        // entity.set("id", 1);
        entity.set("firstName", "First");
        entity.set("lastName", "Last");
        entity.set("salary", 12345);

        DynamicEntity period = periodType.newDynamicEntity();
        period.set("startDate", Calendar.getInstance());

        entity.set("period", period);

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(entity);
        uow.commit();

        ReportQuery countQuery = helper.newReportQuery("Employee", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        assertEquals(1, ((Number) session.executeQuery(countQuery)).intValue());

        session.release();
    }

    @Test
    public void readAll() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        createNewInstance();
        DynamicType type = helper.getType("Employee");

        List<DynamicEntity> allObjects = session.readAllObjects(type.getJavaClass());
        assertEquals(1, allObjects.size());
    }

    @After
    public void clearDatabase() {
        getSharedSession().executeNonSelectingSQL("DELETE FROM DX_ADDRESS");
        getSharedSession().executeNonSelectingSQL("DELETE FROM DX_SALARY");
        getSharedSession().executeNonSelectingSQL("DELETE FROM DX_EMPLOYEE");
    }

    @AfterClass
    public static void dropTables() {
        sharedSession.executeNonSelectingSQL("DROP TABLE DX_EMPLOYEE CASCADE CONSTRAINTS");
        sharedSession.executeNonSelectingSQL("DROP TABLE DX_SALARY CASCADE CONSTRAINTS");
        sharedSession.executeNonSelectingSQL("DROP TABLE DX_ADDRESS CASCADE CONSTRAINTS");
    }

}