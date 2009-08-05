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
 * 		dclarke - initial JPA Employee example using XML (bug 217884)
 ******************************************************************************/
package testing;

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.factories.SessionManager;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.*;
import org.junit.Test;

/**
 * Utility class to create the database schema and populate it for the Employee
 * JPA example using XML configuration. This
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
@PersistenceContext(unitName = "employee")
public class CreateDatabaseSession extends EclipseLinkJPATest {

    @Test
    public void createDatabaseSession() {
        // Equivalent to EntityManager injections
        EntityManager em = getEntityManager();
        Assert.assertNotNull(em);
        Assert.assertTrue(em.isOpen());
        
        Server serverSession = (Server) SessionManager.getManager().getSession("employee");
        Assert.assertNotNull(serverSession);
        Assert.assertTrue(serverSession.isConnected());
        
        // Create a Single connection DatabaseSession for direct manipulation of the database
        DatabaseSession dbSession = serverSession.getProject().createDatabaseSession();
        
        // Replace the database login assuming the one used by the shared session uses a JTA data source
        // The new one will connect directly to the database and manage its own transactions
        DatabaseLogin login = (DatabaseLogin) dbSession.getLogin().clone();
        login.useOracleThinJDBCDriver();
        login.setConnectionString("jdbc:oracle:thin:@localhost:1521:ORCL");
        login.setUserName("scott");
        login.setPassword("tiger");
        dbSession.setExternalTransactionController(null);
        dbSession.setDatasourceLogin(login);
        dbSession.getSessionLog().setLevel(SessionLog.FINE);
        
        // Now connect to the database
        dbSession.login();
        
        // Collect all of the table definitions in a map so specific ones can then be accessed for creation
        DefaultTableGenerator tableGen = new DefaultTableGenerator(dbSession.getProject(), true);
        Map<String, TableDefinition> tableDefs = new HashMap<String, TableDefinition>();
        for (TableDefinition tableDef: (List<TableDefinition>) tableGen.generateDefaultTableCreator().getTableDefinitions()) {
            tableDefs.put(tableDef.getName(), tableDef);
        }
        
        // Now we'll create the RESPON_DESC table as an example
        TableDefinition responsTable = tableDefs.get("RESPONS");
        SchemaManager sm = new SchemaManager(dbSession);
        sm.replaceObject(responsTable);
        
        dbSession.logout();
    }
}
