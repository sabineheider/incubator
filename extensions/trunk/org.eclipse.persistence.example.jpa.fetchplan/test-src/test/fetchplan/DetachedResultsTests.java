/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     dclarke - FetchPlan Extension incubator
 ******************************************************************************/
package test.fetchplan;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import model.Employee;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.FetchPlanHelper;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.junit.Before;
import org.junit.Test;

import testing.EclipseLinkJPAAssert;
import testing.EclipseLinkJPATest;

/**
 * Tests to verify that {@link FetchPlan} produced results can be dettached as
 * expected and later merged into other transactions in the same and different
 * {@link EntityManager} instances.
 * 
 * @author dclarke
 * @since EclipseLink 2.0
 */
@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class DetachedResultsTests extends EclipseLinkJPATest {

    @Test
    public void employeeAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> detachedEmps = detach(em, emps, fetchPlan);
        FetchPlanAssert.assertFetched(fetchPlan, detachedEmps);
    }

    @Test
    public void employeeAddressPhones_Batching() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> detachedEmps = detach(em, emps, fetchPlan);
        FetchPlanAssert.assertFetched(fetchPlan, detachedEmps);
    }

    @Test
    public void employeeAddressPhones_Joining() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        query.setHint(QueryHints.FETCH, "e.address");
        query.setHint(QueryHints.FETCH, "e.phoneNumbers");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> detachedEmps = detach(em, emps, fetchPlan);
        FetchPlanAssert.assertFetched(fetchPlan, detachedEmps);
    }

    @Test
    public void managerAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> detachedEmps = detach(em, emps, fetchPlan);
        FetchPlanAssert.assertFetched(fetchPlan, detachedEmps);
    }

    @Test
    public void responsibilities() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.responsibilities");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> detachedEmps = detach(em, emps, fetchPlan);
        FetchPlanAssert.assertFetched(fetchPlan, detachedEmps);
    }

    @Test
    public void responsibilitiesBatch() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");
        query.setHint(QueryHints.BATCH, "e.responsibilities");
        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.responsibilities");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> detachedEmps = detach(em, emps, fetchPlan);
        FetchPlanAssert.assertFetched(fetchPlan, detachedEmps);
    }

    @Test
    public void managedEmployeesAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.managedEmployees.address");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> detachedEmps = detach(em, emps, fetchPlan);
        FetchPlanAssert.assertFetched(fetchPlan, detachedEmps);
    }

    @Test
    public void readAllEmployee() throws Exception {
        EntityManager em = getEntityManager();

        ReadAllQuery raq = new ReadAllQuery(Employee.class);

        FetchPlan fetchPlan = FetchPlanHelper.create(raq);
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        Query query = JpaHelper.createQuery(raq, em);

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> detachedEmps = detach(em, emps, fetchPlan);
        FetchPlanAssert.assertFetched(fetchPlan, detachedEmps);
    }

    @Before
    public void verifyConfig() {
        EclipseLinkJPAAssert.assertWoven(getEMF(), "Employee");
        EclipseLinkJPAAssert.assertWoven(getEMF(), "PhoneNumber");
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
        getQuerySQLTracker(getEMF()).reset();
    }

    /**
     * Detach using a non-synchronized UnitOfWork to create copies. If a
     * {@link FetchPlan} is provided then ensure that resulting detached copies
     * have the necessary relationships populated.
     */
    private List detach(EntityManager em, List<?> results, FetchPlan fetchPlan) throws Exception {
        JpaEntityManager eclipseLinkEm = JpaHelper.getEntityManager(em);
        AbstractSession session = eclipseLinkEm.getServerSession();
        
        // Non-synchronized UOW is not hooked to JTA transaction
        UnitOfWork uow = session.acquireNonSynchronizedUnitOfWork(null);
        List<?> copies = uow.registerAllObjects(results);

        if (fetchPlan != null) {
            fetchPlan.instantiate(copies, uow);
        }

        uow.release();
        return copies;
    }
}
