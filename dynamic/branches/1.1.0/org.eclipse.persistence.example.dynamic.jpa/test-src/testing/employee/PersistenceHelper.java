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
package testing.employee;

import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.eclipse.persistence.config.TargetServer;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.1.2
 */
public abstract class PersistenceHelper {

	public static final String PERSISTENCE_UNIT_NAME = "custom-types";

	public static EntityManagerFactory createEMF() {
		return createEMF(null);
	}

	/**
	 * 
	 * @param properties
	 * @return
	 */
	public static EntityManagerFactory createEMF(Map properties) {
		Map emfProps = getEMFProperties();

		if (properties != null) {
			emfProps.putAll(properties);
		}

		try {
			return Persistence.createEntityManagerFactory(
					PERSISTENCE_UNIT_NAME, emfProps);

		} catch (RuntimeException e) {
			System.out.println("Persistence.createEMF FAILED: "
					+ e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 
	 * @return
	 */
	public static Map getEMFProperties() {
		Map properties = new HashMap();

		// Ensure RESOURCE_LOCAL transactions is used.
		properties.put(TRANSACTION_TYPE,
				PersistenceUnitTransactionType.RESOURCE_LOCAL.name());

		// Configure the internal EclipseLink connection pool
		properties.put(JDBC_DRIVER, "oracle.jdbc.OracleDriver");
		properties.put(JDBC_URL, "jdbc:oracle:thin:@localhost:1521:ORCL");
		properties.put(JDBC_USER, "scott");
		properties.put(JDBC_PASSWORD, "tiger");
		properties.put(JDBC_READ_CONNECTIONS_MIN, "1");
		properties.put(JDBC_WRITE_CONNECTIONS_MIN, "1");

		// Configure logging. FINE ensures all SQL is shown
		properties.put(LOGGING_LEVEL, "FINE");
		properties.put(LOGGING_TIMESTAMP, "false");
		properties.put(LOGGING_THREAD, "false");
		properties.put(LOGGING_SESSION, "false");
		
		// Ensure that no server-platform is configured
		properties.put(TARGET_SERVER, TargetServer.None);

		return properties;
	}

}
