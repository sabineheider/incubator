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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import javax.persistence.*;

import model.Employee;
import model.Gender;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.junit.Test;

import testing.SamplePopulation;

/**
 * Simple query examples for the XML mapped Employee domain model.
 * 
 * @author dclarke
 * @since EclipseLink 1.0
 */
@PersistenceContext(unitName = "employee")
public class QueryTests extends EclipseLinkJPATest {

	protected static int minimumEmployeeId(EntityManager em) {
		return ((Number) em.createQuery("SELECT MIN(e.id) FROM Employee e").getSingleResult()).intValue();
	}

	/**
	 * Simple example using dynamic JP QL to retrieve all Employee instances
	 * sorted by lastName and firstName.
	 */
	@Test
	public void readAllEmployees_JPQL() {
		EntityManager em = getEntityManager();

		List<Employee> emps = em.createQuery("SELECT e FROM Employee e ORDER BY e.lastName ASC, e.firstName ASC").getResultList();

		SamplePopulation.population.assertSame(emps);
	}

	@Test
	public void joinFetchJPQL() {
		EntityManager em = getEntityManager();

		List<Employee> emps = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address ORDER BY e.lastName ASC, e.firstName ASC").getResultList();

		assertNotNull(emps);
	}

	@Test
	public void joinFetchHint() {
		EntityManager em = getEntityManager();

		Query query = em.createQuery("SELECT e FROM Employee e WHERE e.manager.address.city = 'Ottawa' ORDER BY e.lastName ASC, e.firstName ASC");
		query.setHint(QueryHints.FETCH, "e.address");
		query.setHint(QueryHints.FETCH, "e.manager");
		query.setHint(QueryHints.FETCH, "e.manager.address");
		query.setHint(QueryHints.BATCH, "e.manager.phoneNumbers");
		List<Employee> emps = query.getResultList();

		for (Employee emp : emps) {
			emp.getManager().getPhoneNumbers().size();
		}

		assertNotNull(emps);
	}

	@Test
	public void minEmployeeId() {
		int minId = minimumEmployeeId(getEntityManager());

		assertTrue(minId > 0);
	}

	@Test
	public void testLazyLoading() {
		EntityManager em = getEntityManager();
		int minEmpId = minimumEmployeeId(em);

		Employee emp = em.find(Employee.class, minEmpId);
		assertNotNull(emp);
	}

	@Test
	public void testGenderIn() throws Exception {
		EntityManager em = getEntityManager();

		List<Employee> emps = em.createQuery("SELECT e FROM Employee e WHERE e.gender IN (:GENDER1, :GENDER2)").setParameter("GENDER1", Gender.Male).setParameter("GENDER2", Gender.Female)
				.getResultList();

		assertNotNull(emps);
	}

	@Test
	public void testReadAllExressions() throws Exception {
		EntityManager em = getEntityManager();

		ReadAllQuery raq = new ReadAllQuery(Employee.class);
		ExpressionBuilder eb = raq.getExpressionBuilder();
		raq.setSelectionCriteria(eb.get("gender").equal(Gender.Male));

		Query query = JpaHelper.createQuery(raq, em);

		List<Employee> emps = query.getResultList();

		assertNotNull(emps);
	}

}
