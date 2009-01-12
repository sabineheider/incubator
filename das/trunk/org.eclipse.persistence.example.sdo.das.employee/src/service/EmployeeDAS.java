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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.Employee;

import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;

import commonj.sdo.DataObject;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class EmployeeDAS {

	private JAXBHelperContext context;

	private EntityManager entityManager;

	public void setHelperContext(JAXBHelperContext context) {
		this.context = context;
	}

	public JAXBHelperContext getContext() {
		return this.context;
	}

	@PersistenceContext(unitName = "employee")
	public void setEMF(EntityManager em) {
		this.entityManager = em;
	}

	public EntityManager getEntityManager() {
		return this.entityManager;
	}

	public void close() {
		if (this.entityManager != null && this.entityManager.isOpen()) {
			if (this.entityManager.getTransaction().isActive()) {
				this.entityManager.getTransaction().rollback();
			}
			this.entityManager.close();
		}

		this.entityManager = null;
		this.context = null;
	}

	public DataObject findEmployee(int id) {
		EntityManager em = getEntityManager();

		Employee emp = em.find(Employee.class, id);

		if (emp == null) {
			return null;
		}

		return getContext().wrap(emp);
	}

	public DataObject merge(DataObject empDO) {
		EntityManager em = getEntityManager();

		em.getTransaction().begin();
		Employee emp = (Employee) getContext().unwrap(empDO);

		if (emp == null) {
			return null;
		}
		emp = em.merge(emp);

		em.getTransaction().commit();

		return getContext().wrap(emp);
	}

	public void remove(DataObject empDO) {
		EntityManager em = getEntityManager();

		em.getTransaction().begin();
		Employee emp = (Employee) getContext().unwrap(empDO);

		if (emp == null) {
			return;
		}
		emp = em.find(Employee.class, emp.getId());

		em.remove(emp);

		em.getTransaction().commit();
	}
}
