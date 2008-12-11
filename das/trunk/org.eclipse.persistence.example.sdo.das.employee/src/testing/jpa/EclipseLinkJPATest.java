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
package testing.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

import org.junit.After;
import org.junit.AfterClass;

/**
 * Base test case for testing a JPA persistence unit in JavaSE using JUnit4.
 * 
 * Through the usage
 * 
 * @PersistenceContext on subclasses a developer can indicate the persistence
 *                     unit name that the
 * @BeforeClass method should use to access the entityManager.
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public abstract class EclipseLinkJPATest {

	public static final String PERSISTENCE_UNIT_NAME = "employee";

	/**
	 * This is he current EMF in use
	 */
	private static EntityManagerFactory emf;

	private EntityManager entityManager;

	protected EntityManagerFactory getEMF() {
		if (emf == null) {
			emf = createEMF();
		}

		return emf;
	}

	protected EntityManager getEntityManager() {
		if (this.entityManager == null) {
			this.entityManager = getEMF().createEntityManager();
		}

		return this.entityManager;
	}

	private EntityManagerFactory createEMF() {
		try {
			return createEMF(null);
		} catch (RuntimeException e) {
			System.out.println("Persistence.createEMF FAILED: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 
	 * @param properties
	 * @return
	 */
	protected EntityManagerFactory createEMF(Map properties) {
		Map emfProps = getEMFProperties();

		if (properties != null) {
			emfProps.putAll(properties);
		}

		try {
			return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, emfProps);

		} catch (RuntimeException e) {
			System.out.println("Persistence.createEMF FAILED: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 
	 * @return
	 */
	protected Map getEMFProperties() {
		Map properties = new HashMap();

		// Use the system properties to override the ones specified in the
		// persistence.xml
		// This allows the caller< Example build/test system, to override the
		// default values
		properties.putAll(System.getProperties());

		return properties;
	}

	@After
	public void cleanupClosedEMF() {
		if (this.entityManager != null && this.entityManager.isOpen()) {
			if (this.entityManager.getTransaction().isActive()) {
				this.entityManager.getTransaction().rollback();
			}
			this.entityManager.close();
		}
		this.entityManager = null;

		if (emf != null && !emf.isOpen()) {
			emf = null;
		}
	}

	@AfterClass
	public static void closeEMF() throws Exception {
		if (emf != null && emf.isOpen()) {
			emf.close();
			emf = null;
		}
	}

}
