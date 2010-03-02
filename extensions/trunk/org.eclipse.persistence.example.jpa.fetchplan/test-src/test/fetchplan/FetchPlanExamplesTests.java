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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import junit.framework.Assert;
import model.Address;
import model.Employee;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.junit.After;
import org.junit.Test;

import testing.EclipseLinkJPAAssert;
import testing.EclipseLinkJPATest;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class FetchPlanExamplesTests extends EclipseLinkJPATest {

    @Test
    public void employeeNames() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan("JustNames", Employee.class, false);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        Employee emp1 = emps.get(0);
        
        Assert.assertNotNull(emp1);
        Assert.assertNotNull(emp1.getFirstName());
        Assert.assertNotNull(emp1.getLastName());
        Assert.assertTrue(emp1.getId() > 0);
        Assert.assertTrue(emp1.getSalary() > 0);
        Assert.assertNotNull(emp1.getGender());

        Employee emp1Copy = JpaFetchPlanHelper.copy(em, fetchPlan, emp1);
        
        Assert.assertNotNull(emp1Copy);
        Assert.assertNotNull(emp1Copy.getFirstName());
        Assert.assertNotNull(emp1Copy.getLastName());
        Assert.assertEquals(0, emp1Copy.getId());
        Assert.assertEquals(0.0, emp1Copy.getSalary());
        Assert.assertNull(emp1Copy.getGender());
        Assert.assertNull(emp1Copy.getStartTime());
        Assert.assertNull(emp1Copy.getEndTime());
        Assert.assertNull(emp1Copy.getPeriod());
        Assert.assertNull(emp1Copy.getAddress());
        Assert.assertNull(emp1Copy.getPhoneNumbers());
        Assert.assertNull(emp1Copy.getProjects());
    }

    @Test
    public void employeeNamesWithFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");

        fetchPlan.setFetchGroup(query);

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        Assert.assertEquals(1 + (emps.size() * 2), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeAddressPhones_WOFetchPlan() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        List<Employee> emps = query.getResultList();

        fetchPlan.initialize(JpaHelper.getServerSession(getEMF()));
        FetchPlanAssert.assertNotFetched(fetchPlan, emps);
    }

    @Test
    public void employeeAddressPhones_Batching() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        List<Employee> emps = query.getResultList();
        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        Assert.assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        FetchPlanAssert.assertFetched(fetchPlan, emps);
        Assert.assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            EclipseLinkJPAAssert.assertLoaded(getEMF(), emp, "address");
            EclipseLinkJPAAssert.assertLoaded(getEMF(), emp, "phoneNumbers");
            EclipseLinkJPAAssert.assertNotLoaded(getEMF(), emp, "manager");
            EclipseLinkJPAAssert.assertNotLoaded(getEMF(), emp, "projects");
        }
    }

    @Test
    public void employeeAddress_Batching() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");

        query.setHint(QueryHints.BATCH, "e.address");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("address");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        FetchPlanAssert.assertFetched(fetchPlan, emps);
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            EclipseLinkJPAAssert.assertLoaded(getEMF(), emp, "address");
            EclipseLinkJPAAssert.assertNotLoaded(getEMF(), emp, "phoneNumbers");
            EclipseLinkJPAAssert.assertNotLoaded(getEMF(), emp, "manager");
            EclipseLinkJPAAssert.assertNotLoaded(getEMF(), emp, "projects");

            // Assumption that all employees have an address
            Assert.assertNotNull(emp.getAddress());
        }

        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeAddressPhones_Joining() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        query.setHint(QueryHints.FETCH, "e.address");
        query.setHint(QueryHints.FETCH, "e.phoneNumbers");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void managerAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("manager.address");
        fetchPlan.addAttribute("manager.phoneNumbers");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        int numWithManager = 0;
        for (Employee emp : emps) {
            if (emp.getManager() != null) {
                numWithManager++;
            }
        }
        Assert.assertEquals(11, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void responsibilities() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("responsibilities");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        Assert.assertEquals(1 + emps.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        FetchPlanAssert.assertFetched(fetchPlan, emps);

        Assert.assertEquals(1 + emps.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void responsibilities_JOIN() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("responsibilities");

        fetchPlan.setFetchGroup(query);

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        Assert.assertEquals(1 + emps.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void responsibilitiesBatch() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");
        query.setHint(QueryHints.BATCH, "e.responsibilities");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("responsibilities");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeAddress_ReturnBoth() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, e.address FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        List<Object[]> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps, 0);

        FetchPlanAssert.assertFetched(fetchPlan, emps, 0);

        Assert.assertEquals(1 + emps.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void managedEmployees() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan plan = new FetchPlan(Employee.class);
        plan.addAttribute("managedEmployees");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, plan, emps);

        FetchPlanAssert.assertFetched(plan, emps);

        Assert.assertEquals(1 + emps.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void managedEmployees_Batching() throws Exception {
        EntityManager em = getEntityManager();
        Assert.assertEquals("QuerySQLTracker not reset", 0, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");
        query.setHint(QueryHints.BATCH, "e.managedEmployees");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("managedEmployees");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void managedEmployeesAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("managedEmployees.address");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        int initialSqlSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();
        int sqlCount = 1;
        Set<Address> readAddresses = new HashSet<Address>();
        for (Employee emp : emps) {
            sqlCount++;
            for (Employee managedEmp : emp.getManagedEmployees()) {
                if (managedEmp.getAddress() != null) {
                    if (!readAddresses.contains(managedEmp.getAddress())) {
                        sqlCount++;
                        readAddresses.add(managedEmp.getAddress());
                    }
                }
            }
        }

        Assert.assertEquals("Counting expected SQL calls caused additional SELECT", initialSqlSelect, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Assert.assertEquals("SQL SELECTs executed does not match expected", sqlCount, initialSqlSelect);
    }

    @Test
    public void readAllEmployee() throws Exception {
        EntityManager em = getEntityManager();

        ReadAllQuery raq = new ReadAllQuery(Employee.class);

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        Query query = JpaHelper.createQuery(raq, em);

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        Assert.assertEquals(1 + (emps.size() * 2), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void emptyFetchPlan() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        for (Employee emp : emps) {
            EclipseLinkJPAAssert.assertNotLoaded(getEMF(), emp, "address");
            EclipseLinkJPAAssert.assertNotLoaded(getEMF(), emp, "phoneNumbers");
            EclipseLinkJPAAssert.assertNotLoaded(getEMF(), emp, "manager");
        }
    }
    
    @After
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }
}
