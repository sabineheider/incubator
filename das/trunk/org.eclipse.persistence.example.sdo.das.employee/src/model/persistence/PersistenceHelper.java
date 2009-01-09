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
 *     dclarke - JPA DAS INCUBATOR - Enhancement 258057
 *     			 http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package model.persistence;

import java.io.*;
import java.util.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.0
 */
public abstract class PersistenceHelper {

	public static final String PERSISTENCE_UNIT_NAME = "employee";

	public static EntityManagerFactory createEMF() {
		return createEMF(new HashMap());
	}

	/**
	 * 
	 * @param properties
	 * @return
	 */
	public static EntityManagerFactory createEMF(Map properties) {
		try {
			applyUserHomeProperties(properties);
			return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);

		} catch (RuntimeException e) {
			System.out.println("Persistence.createEMF FAILED: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 
	 * @param properties
	 */
	public static void applyUserHomeProperties(Map puProperties) {
		String userHome = System.getProperty("user.home");

		if (userHome != null && !userHome.isEmpty()) {
			if (!userHome.endsWith(System.getProperty("file.separator"))) {
				userHome = userHome + System.getProperty("file.separator");
			}
			
			File testPropFile = new File(userHome + "test.properties");

			if (testPropFile.exists()) {
				Properties testProperties = new Properties();
				FileInputStream in = null;

				try {
					in = new FileInputStream(testPropFile);
					testProperties.load(in);
				} catch (FileNotFoundException fnfe) {
					return;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Now process the test properties into the PU properties
				copyProperty(testProperties, "db.driver", puProperties, PersistenceUnitProperties.JDBC_DRIVER);
				copyProperty(testProperties, "db.url", puProperties, PersistenceUnitProperties.JDBC_URL);
				copyProperty(testProperties, "db.user", puProperties, PersistenceUnitProperties.JDBC_USER);
				copyProperty(testProperties, "db.pwd", puProperties, PersistenceUnitProperties.JDBC_PASSWORD);
				copyProperty(testProperties, "db.platform", puProperties, PersistenceUnitProperties.TARGET_DATABASE);
			}
		}
	}
	
	private static void copyProperty(Properties testProperties, String testProp, Map puProperties, String puProperty) {
		if (testProperties.containsKey(testProp)) {
			puProperties.put(puProperty, testProperties.get(testProp));
		}
	}
}
