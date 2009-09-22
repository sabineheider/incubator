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
import static junit.framework.Assert.fail;

import java.util.List;

import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.queries.*;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.junit.Test;

/**
 * Set of tests verifying that the DynamicHelper functions as expected.
 */
public class DynamicHelperTests {

    @Test
    public void createQuery_ValidReadAllQuery() throws Exception {
        DatabaseSession session = createEmployeeSession();

        ReadAllQuery query = DynamicHelper.newReadAllQuery(session, "Employee");

        assertNotNull(query);

        List<DynamicEntity> emps = (List<DynamicEntity>) session.executeQuery(query);
        assertNotNull(emps);
        session.logout();
    }

    @Test
    public void createQuery_ValidReadObjectQuery() throws Exception {
        DatabaseSession session = createEmployeeSession();

        ReadObjectQuery query = DynamicHelper.newReadObjectQuery(session, "Employee");

        assertNotNull(query);

        DynamicEntity emp = (DynamicEntity) session.executeQuery(query);
        assertNotNull(emp);
        
        session.logout();
    }

    @Test
    public void createQuery_ValidReportQuery() throws Exception {
        DatabaseSession session = createEmployeeSession();

        ReportQuery query = DynamicHelper.newReportQuery(session, "Employee", new ExpressionBuilder());
        query.addCount();
        query.setShouldReturnSingleValue(true);

        assertNotNull(query);

        Number count = (Number) session.executeQuery(query);
        assertNotNull(count);
    }

    @Test
    public void nullArgs() {
        try {
            DynamicHelper.newReadAllQuery(null, null);
        } catch (NullPointerException e) {
            return;
        }

        fail("NullPointerException expected");
    }

    protected DatabaseSession createEmployeeSession() throws Exception {
        DatabaseSession session = DynamicTestHelper.createEmptySession();

        DynamicClassLoader dcl = DynamicClassLoader.lookup(session);
        Class<?> empClass = dcl.creatDynamicClass(getClass().getName() + ".Employee");

        EntityTypeBuilder typeBuilder = new EntityTypeBuilder(empClass, null, "D_EMPLOYEE");
        typeBuilder.setPrimaryKeyFields("EMP_ID");
        typeBuilder.addDirectMapping("id", int.class, "EMP_ID");
        typeBuilder.addDirectMapping("firstName", String.class, "F_NAME");
        typeBuilder.addDirectMapping("lastName", String.class, "L_NAME");

        typeBuilder.addToSession(session, true, true);

        EntityType empType = DynamicHelper.getType(session, "Employee");
        assertNotNull("No type found for Employee", empType);

        return session;
    }

}
