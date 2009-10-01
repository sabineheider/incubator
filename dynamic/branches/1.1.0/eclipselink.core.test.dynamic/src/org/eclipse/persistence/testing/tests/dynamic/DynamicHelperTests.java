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
 *     			 http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.testing.tests.dynamic;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.List;

import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.junit.Test;

/**
 * Set of tests verifying that the DynamicHelper functions as expected.
 */
public class DynamicHelperTests {

    @Test
    public void createQuery_ValidReadAllQuery() throws Exception {
        DatabaseSession session = createEmployeeSession();

        ReadAllQuery query = new DynamicHelper(session).newReadAllQuery("Employee");

        assertNotNull(query);

        List<DynamicEntity> emps = (List<DynamicEntity>) session.executeQuery(query);
        assertNotNull(emps);
        session.logout();
    }

    @Test
    public void createQuery_ValidReadObjectQuery() throws Exception {
        DatabaseSession session = createEmployeeSession();

        ReadObjectQuery query = new DynamicHelper(session).newReadObjectQuery("Employee");

        assertNotNull(query);

        DynamicEntity emp = (DynamicEntity) session.executeQuery(query);
        assertNotNull(emp);

        session.logout();
    }

    @Test
    public void createQuery_ValidReportQuery() throws Exception {
        DatabaseSession session = createEmployeeSession();

        ReportQuery query = new DynamicHelper(session).newReportQuery("Employee", new ExpressionBuilder());
        query.addCount();
        query.setShouldReturnSingleValue(true);

        assertNotNull(query);

        Number count = (Number) session.executeQuery(query);
        assertNotNull(count);
        assertTrue(count.intValue() > 0);
    }

    @Test
    public void nullArgs() {
        try {
            new DynamicHelper(null).newReadAllQuery(null);
        } catch (NullPointerException e) {
            return;
        }

        fail("NullPointerException expected");
    }

    protected DatabaseSession createEmployeeSession() throws Exception {
        DatabaseSession session = DynamicTestHelper.createEmptySession();
        DynamicHelper helper = new DynamicHelper(session);
        DynamicClassLoader dcl = helper.getDynamicClassLoader();

        Class<?> empClass = dcl.createDynamicClass(getClass().getName() + ".Employee");

        DynamicTypeBuilder typeBuilder = new DynamicTypeBuilder(empClass, null, "D_EMPLOYEE");
        typeBuilder.setPrimaryKeyFields("EMP_ID");
        typeBuilder.addDirectMapping("id", int.class, "EMP_ID");
        typeBuilder.addDirectMapping("firstName", String.class, "F_NAME");
        typeBuilder.addDirectMapping("lastName", String.class, "L_NAME");

        helper.addTypes(true, true, typeBuilder.getType());

        DynamicType empType = helper.getType("Employee");
        assertNotNull("No type found for Employee", empType);

        ReportQuery countQuery = helper.newReportQuery("Employee", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        int empCount = ((Number) session.executeQuery(countQuery)).intValue();

        if (empCount == 0) {
            UnitOfWork uow = session.acquireUnitOfWork();
            DynamicEntity emp = empType.newDynamicEntity();
            emp.set("id", 1);
            emp.set("firstName", "John");
            emp.set("lastName", "Doe");
            uow.registerNewObject(emp);
            uow.commit();
        }

        return session;
    }

}
