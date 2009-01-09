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
package service;

import java.io.IOException;
import java.io.InputStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import model.Employee;
import model.persistence.PersistenceHelper;

import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;

import commonj.sdo.DataObject;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class EmployeeDAS {

	private static final String MODEL_PACKAGE = "model";

	private static final String SCHEMA = "xsd/jpadas-employee.xsd";

	private JAXBHelperContext context;

	private EntityManagerFactory emf;

	/**
	 * Return the JAXBHelperContext and lazily create one if null.
	 */
	public JAXBHelperContext getContext() {
		if (this.context == null) {
			InputStream xsdIn = null;

			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(MODEL_PACKAGE);
				this.context = new JAXBHelperContext(jaxbContext);

				xsdIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA);
				this.context.getXSDHelper().define(xsdIn, null);

				// Make this the default context
				this.context.makeDefaultContext();
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
		}
		return this.context;
	}

	public EntityManagerFactory getEMF() {
		if (this.emf == null || !this.emf.isOpen()) {
			this.emf = PersistenceHelper.createEMF();
		}

		return this.emf;
	}

	public void close() {
		if (this.emf != null && this.emf.isOpen()) {
			this.emf.close();
		}

		this.emf = null;
		this.context = null;
	}

	public DataObject findEmployee(int id) {
		EntityManager em = getEMF().createEntityManager();

		try {
			Employee emp = em.find(Employee.class, id);

			if (emp == null) {
				return null;
			}

			return getContext().wrap(emp);
		} finally {
			em.close();
		}
	}

	public DataObject merge(DataObject empDO) {
		EntityManager em = getEMF().createEntityManager();

		try {
			em.getTransaction().begin();
			Employee emp = (Employee) getContext().unwrap(empDO);

			if (emp == null) {
				return null;
			}
			emp = em.merge(emp);

			em.getTransaction().commit();

			return getContext().wrap(emp);
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	public int findMinimumEmployeeId() {
		EntityManager em = getEMF().createEntityManager();

		try {
			return (Integer) em.createQuery("SELECT MIN(E.id) FROM Employee e").getSingleResult();
		} finally {
			em.close();
		}
	}
}
