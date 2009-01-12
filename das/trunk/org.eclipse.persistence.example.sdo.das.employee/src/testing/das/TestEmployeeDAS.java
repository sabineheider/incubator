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
 *    dclarke - JPA DAS INCUBATOR - Enhancement 258057
 *              http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing.das;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import model.Employee;
import model.persistence.PersistenceHelper;

import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;
import org.junit.*;

import service.EmployeeDAS;

/**
 * 
 * @author dclarke EclipseLink 1.1
 */
public abstract class TestEmployeeDAS {

	private EmployeeDAS das;

	private static EntityManagerFactory emf;

	private static JAXBHelperContext context;

	public EmployeeDAS getDAS() {
		return this.das;
	}
	
	protected EntityManagerFactory getEMF() {
		return emf;
	}
	
	protected JAXBHelperContext getSDOContext() {
		return context;
	}

	public int findMinimumEmployeeId() {
		EntityManager em = getEMF().createEntityManager();

		try {
			return (Integer) em.createQuery("SELECT MIN(E.id) FROM Employee e").getSingleResult();
		} finally {
			em.close();
		}
	}

	public int findMaximumEmployeeId() {
		EntityManager em = getEMF().createEntityManager();

		try {
			return (Integer) em.createQuery("SELECT MAX(E.id) FROM Employee e").getSingleResult();
		} finally {
			em.close();
		}
	}

	@Before
	public void initializeDAS() {
		this.das = new EmployeeDAS();

		this.das.setEMF(emf.createEntityManager());
		this.das.setHelperContext(context);
	}

	@After
	public void shutdown() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		List<Employee> deleteEmps = em.createQuery("SELECT e FROM Employee e WHERE e.firstName = 'Delete' and e.lastName = 'Me'").getResultList();
		for (Employee emp : deleteEmps) {
			em.remove(emp);
		}
		em.getTransaction().commit();
		em.close();

		this.das.close();
		this.das = null;
	}

	@BeforeClass
	public static void intialize() {
		emf = PersistenceHelper.createEMF(null);
		context = PersistenceHelper.createJAXBHelperContext();
	}

	@AfterClass
	public static void afterClass() {
		if (emf != null && emf.isOpen()) {
			emf.close();
			emf = null;
		}
	}

}
