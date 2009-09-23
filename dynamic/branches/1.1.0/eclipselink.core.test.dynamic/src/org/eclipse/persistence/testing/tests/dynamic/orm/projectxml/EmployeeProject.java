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
import java.math.BigInteger;
import java.sql.Date;
import java.util.List;

import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.testing.tests.dynamic.DynamicTestHelper;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
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
            project = EntityTypeBuilder.loadDynamicProject("org/eclipse/persistence/testing/tests/dynamic/orm/projectxml/Employee_utf8.xml", login, new DynamicClassLoader(EmployeeProject.class.getClassLoader()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        DatabaseSession ds = project.createDatabaseSession();
        ds.setLogLevel(SessionLog.FINE);

        ds.login();

        return ds;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void createNewInstance() throws Exception {
        Session session = getSession();

        EntityType type = DynamicHelper.getType(session, "Employee");

        DynamicEntity entity = type.newInstance();
        entity.set("id", new BigInteger("1"));
        entity.set("name", "Doug");
        entity.set("since", new Date(100, 06, 06));

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(entity);
        uow.commit();

        ReportQuery countQuery = DynamicHelper.newReportQuery(session, "Employee", new ExpressionBuilder());
        countQuery.addCount();
        countQuery.setShouldReturnSingleValue(true);
        assertEquals(1, ((Number) session.executeQuery(countQuery)).intValue());

        session.release();
    }

    @Test
    public void readAll() throws Exception {
        Session session = getSession();

        createNewInstance();
        EntityType type = DynamicHelper.getType(session, "Employee");

        List<DynamicEntity> allObjects = session.readAllObjects(type.getJavaClass());
        assertEquals(1, allObjects.size());
    }

}