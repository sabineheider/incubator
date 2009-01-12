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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.List;

import javax.persistence.EntityManager;

import junit.framework.Assert;
import model.*;

import org.eclipse.persistence.config.PessimisticLock;
import org.eclipse.persistence.config.QueryHints;
import org.junit.Test;

import testing.SamplePopulation;

public class TransactionExamples extends EclipseLinkJPATest {

	@Test
	public void pessimisticLocking() throws Exception {
		EntityManager em = getEntityManager();

		// Find the Employee with the minimum ID
		int minId = QueryTests.minimumEmployeeId(em);

		em.getTransaction().begin();

		// Lock Employee using query with hint
		Employee emp = (Employee) em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID").setParameter("ID", minId).setHint(QueryHints.PESSIMISTIC_LOCK, PessimisticLock.Lock).getSingleResult();

		emp.setSalary(emp.getSalary() - 1);

		em.flush();
	}

	@Test
	public void updateEmployeeWithCity() throws Exception {
		EntityManager em = getEntityManager();

		em.getTransaction().begin();

		List<Object[]> emps = em.createQuery("SELECT e, e.address.city FROM Employee e").getResultList();
		Employee emp = (Employee) emps.get(0)[0];
		emp.setSalary(emp.getSalary() + 1);

		em.flush();

		em.getTransaction().rollback();

		SamplePopulation.population.resetDatabase(em);
	}

	@Test
	public void createUsingPersist() throws Exception {
		EntityManager em = getEntityManager();

		Employee emp = new Employee();
		emp.setFirstName("Sample");
		emp.setLastName("Employee");
		emp.setGender(Gender.Male);
		emp.setSalary(123456);

		Address address = new Address();
		emp.setAddress(address);

		emp.addPhoneNumber("Mobile", "6135551212");

		em.getTransaction().begin();
		em.persist(emp);
		em.getTransaction().commit();

		Assert.assertNotNull(emp);
		Assert.assertTrue(emp.getId() > 0);

		em.getTransaction().begin();
		em.createQuery("DELETE from PhoneNumber p WHERE p.owner.id = " + emp.getId()).executeUpdate();
		em.createQuery("DELETE from Employee e WHERE e.id = " + emp.getId()).executeUpdate();
		em.createQuery("DELETE from Address a WHERE a.id = " + emp.getAddress().getId()).executeUpdate();
		em.getTransaction().commit();

		SamplePopulation.population.verifyCounts(em);
	}

	@Test
	public void createUsingMerge() throws Exception {
		EntityManager em = getEntityManager();

		Employee emp = new Employee();
		emp.setFirstName("Sample");
		emp.setLastName("Employee");
		emp.setGender(Gender.Male);
		emp.setSalary(123456);

		Address address = new Address();
		emp.setAddress(address);

		emp.addPhoneNumber("Mobile", "6135551212");

		em.getTransaction().begin();
		// When merging the managed instance is returned from the call.
		// Further usage within the transaction must be done with this managed
		// entity.
		emp = em.merge(emp);
		em.getTransaction().commit();

		Assert.assertNotNull(emp);
		Assert.assertTrue(emp.getId() > 0);

		em.getTransaction().begin();
		em.createQuery("DELETE from PhoneNumber p WHERE p.owner.id = " + emp.getId()).executeUpdate();
		em.createQuery("DELETE from Employee e WHERE e.id = " + emp.getId()).executeUpdate();
		em.createQuery("DELETE from Address a WHERE a.id = " + emp.getAddress().getId()).executeUpdate();
		em.getTransaction().commit();

		SamplePopulation.population.verifyCounts(em);
	}

	@Test
	public void incrementSalary() {
		EntityManager em = getEntityManager();

		Employee emp = (Employee) em.createQuery("SELECT e FROM Employee e WHERE e.id IN (SELECT MIN(ee.id) FROM Employee ee)").getSingleResult();
		assertNotNull("Null POJO in DataObject wrapper", emp);

		long initialVersion = emp.getVersion();
		double initialSalary = emp.getSalary();

		emp.setSalary(initialSalary + 1);

		em.getTransaction().begin();
		em.merge(emp);
		em.getTransaction().commit();

		assertEquals(initialVersion + 1, emp.getVersion());
		assertEquals(initialSalary + 1, emp.getSalary());
	}

}
