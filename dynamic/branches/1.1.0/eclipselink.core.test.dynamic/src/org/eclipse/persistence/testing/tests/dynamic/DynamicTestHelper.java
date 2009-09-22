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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.persistence.internal.databaseaccess.DatabasePlatform;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.sessions.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { DynamicClassLoaderTests.class, DynamicHelperTests.class, EntityTypeFromDescriptor.class, EntityTypeFromScratch.class, org.eclipse.persistence.testing.tests.dynamic.orm.comics.AllTests.class, org.eclipse.persistence.testing.tests.dynamic.orm.projectxml.AllTests.class })
public class DynamicTestHelper {

    /**
     * Configure and return a {@link DatabaseLogin} based on test.properties
     * 
     * db.driver=oracle.jdbc.OracleDriver
     * db.url=jdbc:oracle:thin:@tlsvrdb5.ca.oracle.com:1521:toplink db.user=user
     * db.pwd=password
     * db.platform=org.eclipse.persistence.platform.database.oracle
     * .Oracle11Platform
     * 
     * @return
     */
    public static DatabaseLogin getTestLogin() {
        DatabaseLogin login = new DatabaseLogin();
        Properties props = new Properties();

        try {
            FileInputStream in = new FileInputStream("./test.properties");
            props.load(in);
            in.close();

            if (props.containsKey("db.platform")) {
                Class<DatabasePlatform> platformClass = (Class<DatabasePlatform>) Class.forName(props.getProperty("db.platform"));
                login.setPlatform(platformClass.newInstance());
            } else {
                login.setPlatform(new DatabasePlatform());
            }
        } catch (IOException e) {
            throw new RuntimeException("DynamicTestHelper.getTestLogin()::", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("DynamicTestHelper.getTestLogin()::", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("DynamicTestHelper.getTestLogin()::", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("DynamicTestHelper.getTestLogin()::", e);
        }

        login.setConnectionString(props.getProperty("db.url"));
        login.setUserName(props.getProperty("db.user"));
        login.setPassword(props.getProperty("db.pwd"));

        return login;
    }

    public static DatabaseSession createEmptySession() {
        Project project = new Project(getTestLogin());
        DatabaseSession session = project.createDatabaseSession();
        session.getSessionLog().setLevel(SessionLog.FINE);
        session.login();
        return session;
    }
}
