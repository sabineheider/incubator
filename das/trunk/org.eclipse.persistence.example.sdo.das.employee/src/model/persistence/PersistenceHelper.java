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

import javax.persistence.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.0
 */
public abstract class PersistenceHelper {

	public static final String PERSISTENCE_UNIT_NAME = "employee";

	public static EntityManagerFactory createEMF() {
		return createEMF(null);
	}

	/**
	 * 
	 * @param properties
	 * @return
	 */
	public static EntityManagerFactory createEMF(Map properties) {
		Map puProperties = properties == null ? new HashMap() : properties;
		
		try {
			applyUserHomeProperties(puProperties);
			return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, puProperties);

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
	
	public static final String MODEL_PACKAGE = "model";

	public static final String SCHEMA_REOSURCE = "xsd/jpadas-employee.xsd";
	
	public static final String URI = "http://www.example.org/jpadas-employee";
	
	public static final String EMPLOYEE_TYPE = "employee-type";
	public static final String ADDRESS_TYPE = "address-type";
	public static final String PHONE_TYPE = "phone-type";

	/**
	 * Return the JAXBHelperContext and lazily create one if null.
	 */
	public static JAXBHelperContext createJAXBHelperContext() {
			JAXBHelperContext context = null;
			InputStream xsdIn = null;

			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(MODEL_PACKAGE);
				context = new JAXBHelperContext(jaxbContext);

				xsdIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_REOSURCE);
				context.getXSDHelper().define(xsdIn, null);

				// Make this the default context
				context.makeDefaultContext();
			} catch (JAXBException e) {
				throw new RuntimeException("EmployeeDAS.getContext()::Could not create JAXBContext for: " + MODEL_PACKAGE, e);
			} finally {
				if (xsdIn != null) {
					try {
						xsdIn.close();
					} catch (IOException e) {
					}
				}
			}
		return context;
	}

}
