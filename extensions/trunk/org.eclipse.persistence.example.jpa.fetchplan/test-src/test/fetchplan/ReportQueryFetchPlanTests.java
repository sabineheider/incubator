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
 *     dclarke - Bug 288307: Extensions Incubator - FetchPlan 
 ******************************************************************************/
package test.fetchplan;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import model.Employee;

import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.After;
import org.junit.Test;

import testing.EclipseLinkJPATest;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class ReportQueryFetchPlanTests extends EclipseLinkJPATest {

    @Test
    public void employeeManagerPhonesAndAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, e.address FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("manager.address");
        fetchPlan.addAttribute("manager.phoneNumbers");

        List<Object[]> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps, 0);

        FetchPlanAssert.assertFetched(fetchPlan, emps, 0);
    }

    @Test
    public void employeeManager_FetchEmployeeManagerAddressAndPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, e.manager FROM Employee e");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("manager.address");
        fetchPlan.addAttribute("phoneNumbers");

        List<Object[]> results = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, results, 0);

        FetchPlanAssert.assertFetched(fetchPlan, results, 0);
    }

    @Test
    public void employeeCountPhones_FetchManageraddressAndPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, COUNT(e.phoneNumbers) FROM Employee e GROUP BY e");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("manager.address");
        fetchPlan.addAttribute("phoneNumbers");

        List<Object[]> results = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, results, 0);

        FetchPlanAssert.assertFetched(fetchPlan, results, 0);
    }

    @Test
    public void employeeWithId_FetchingManagerPhonesAndAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e.id, e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("manager.address");
        fetchPlan.addAttribute("manager.phoneNumbers");

        List<Object[]> results = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, results, 1);

        FetchPlanAssert.assertFetched(fetchPlan, results, 1);
    }

    @Test
    public void employeeManager_FetchingManagerPhonesAndAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e.manager FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("manager.address");
        fetchPlan.addAttribute("manager.phoneNumbers");

        List<Object[]> results = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, results);

        FetchPlanAssert.assertFetched(fetchPlan, results);
    }

    @After
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

}
