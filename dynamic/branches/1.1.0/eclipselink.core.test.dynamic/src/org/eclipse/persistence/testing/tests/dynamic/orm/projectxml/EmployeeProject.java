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

import java.math.BigInteger;
import java.sql.Date;
import java.util.Vector;

import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Test cases verifying the use of the simple-map-project.xml 
 */
public class EmployeeProject {

    // JUnit static fixtures
    static Project p;
    static DatabaseSession ds;

    @SuppressWarnings( { "unchecked", "deprecation" })
    @BeforeClass
    public static void setUp() throws Exception {
        DatabaseLogin login = AllTests.getTestLogin();
        p = EntityTypeBuilder.loadDynamicProject("org/eclipse/persistence/testing/tests/dynamic/orm/projectxml/Employee_utf8.xml", login);

        ds = p.createDatabaseSession();
        ds.setLogLevel(SessionLog.FINE);
        ds.login();

        new DynamicSchemaManager(ds).createTables(new EntityType[0]);
        ds.executeNonSelectingSQL("DELETE FROM SIMPLETABLE");

        EntityType type = DynamicHelper.getType(ds, "simpletableType");

        DynamicEntity entity = type.newInstance();
        entity.set("id", new BigInteger("1"));
        entity.set("name", "Doug");
        entity.set("since", new Date(100, 06, 06));

        ds.writeObject(entity);
    }

    @Test
    public void readAll() {
        EntityType type = DynamicHelper.getType(ds, "simpletableType");

        Vector<Object> allObjects = ds.readAllObjects(type.getJavaClass());
        for (Object o : allObjects) {
            System.out.println(o);
        }

        System.identityHashCode(allObjects);
    }

}