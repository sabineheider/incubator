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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import junit.framework.Assert;
import model.Employee;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.junit.After;
import org.junit.Test;

import testing.EclipseLinkJPATest;
import example.FetchPlanExamples;

@PersistenceContext(unitName = "employee")
public class FetchPlanExamplesTests extends EclipseLinkJPATest {

    FetchPlanExamples examples = new FetchPlanExamples();

    @Test
    public void employeesFetchAddressAndPhones() {
        EntityManager em = getEntityManager();

        List<Employee> emps = this.examples.employeesFetchAddressAndPhones(em);

        assertNotNull(emps);
        assertTrue(emps.size() > 0);
        assertEquals(1 + (emps.size() * 2), getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("id"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("version"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("firstName"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("lastName"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("gender"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("salary"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("address"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("phoneNumbers"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("startDate"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("endDate"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("projects"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("period"));
            assertNotNull(emp.getAddress());
            emp.getPhoneNumbers().size();
        }
        assertEquals(1 + (emps.size() * 2), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeesFetchAddressAndPhones_optimized() {
        EntityManager em = getEntityManager();

        List<Employee> emps = this.examples.employeesFetchAddressAndPhones_optimized(em);

        assertNotNull(emps);
        assertTrue(emps.size() > 0);
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("id"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("version"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("firstName"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("lastName"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("gender"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("salary"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("address"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("phoneNumbers"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("startDate"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("endDate"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("projects"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("period"));
            assertNotNull(emp.getAddress());
            emp.getPhoneNumbers().size();
        }
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeesFetchAddressAndPhonesBatchAndUsingRedirector() {
        EntityManager em = getEntityManager();

        List<Employee> emps = this.examples.employeesFetchAddressAndPhonesBatchAndUsingRedirector(em);

        assertNotNull(emps);
        assertTrue(emps.size() > 0);
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("id"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("version"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("firstName"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("lastName"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("gender"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("salary"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("address"));
            assertTrue(((FetchGroupTracker) emp)._persistence_isAttributeFetched("phoneNumbers"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("startDate"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("endDate"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("projects"));
            assertFalse(((FetchGroupTracker) emp)._persistence_isAttributeFetched("period"));
            assertNotNull(emp.getAddress());
            emp.getPhoneNumbers().size();
        }
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void maleEmployeeCopyNames() {
        EntityManager em = getEntityManager();

        List<Employee> emps = examples.maleEmployeeCopyNames(em);

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertNotNull(emp);
            assertNotNull(emp.getFirstName());
            assertNotNull(emp.getLastName());
            assertTrue(emp.getId() > 0);
            assertTrue(emp.getVersion() > 0);
            assertEquals(0.0, emp.getSalary(), 0.0);
            assertNull(emp.getGender());
            assertNull(emp.getStartTime());
            assertNull(emp.getEndTime());
            assertNull(emp.getPeriod());
            assertNull(emp.getAddress());
            assertNull(emp.getPhoneNumbers());
            assertNull(emp.getProjects());
        }
    }

    @Test
    public void employeeCopyNamesWithFetchGroup() {
        EntityManager em = getEntityManager();

        List<Employee> emps = examples.employeeCopyNamesWithFetchGroup(em);

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertNotNull(emp);
            assertNotNull(emp.getFirstName());
            assertNotNull(emp.getLastName());
            assertTrue(emp.getId() > 0);
            assertTrue(emp.getVersion() > 0);
            assertEquals(0.0, emp.getSalary(), 0.0);
            assertNull(emp.getGender());
            assertNull(emp.getStartTime());
            assertNull(emp.getEndTime());
            assertNull(emp.getPeriod());
            assertNull(emp.getAddress());
            assertNull(emp.getPhoneNumbers());
            assertNull(emp.getProjects());

            // Check in EM to ensure FetchGroup was used
            // use getReference as find will load the entire entity
            Employee managedEmp = em.getReference(Employee.class, emp.getId());
            assertNotNull(managedEmp);
            assertEquals(emp.getId(), managedEmp.getId());
            assertNotSame(emp, managedEmp);

            assertTrue(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("id"));
            assertTrue(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("version"));
            assertTrue(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("firstName"));
            assertTrue(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("lastName"));
            assertFalse(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("gender"));
            assertFalse(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("salary"));
            assertFalse(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("address"));
            assertFalse(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("phoneNumbers"));
            assertFalse(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("startDate"));
            assertFalse(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("endDate"));
            assertFalse(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("projects"));
            assertFalse(((FetchGroupTracker) managedEmp)._persistence_isAttributeFetched("period"));
        }
    }

    @Test
    public void employeeCopyWithNamesAddressAndPhones() {
        EntityManager em = getEntityManager();

        List<Employee> emps = this.examples.employeeCopyWithNamesAddressAndPhones(em);

        assertTrue(emps.size() > 0);
        assertEquals(1 + (2 * emps.size()), getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertNotNull(emp);
            assertNotNull(emp.getFirstName());
            assertNotNull(emp.getLastName());
            assertTrue(emp.getId() > 0);
            assertTrue(emp.getVersion() > 0);
            assertEquals(0.0, emp.getSalary(), 0.0);
            assertNull(emp.getGender());
            assertNull(emp.getStartTime());
            assertNull(emp.getEndTime());
            assertNull(emp.getPeriod());
            assertNotNull(emp.getAddress());
            assertNotNull(emp.getPhoneNumbers());
            assertNull(emp.getProjects());
        }
        assertEquals(1 + (2 * emps.size()), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeCopyWithNamesAddressAndPhonesWithBatching() {
        EntityManager em = getEntityManager();

        List<Employee> emps = this.examples.employeeCopyWithNamesAddressAndPhonesWithBatching(em);

        assertTrue(emps.size() > 0);
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertNotNull(emp);
            assertNotNull(emp.getFirstName());
            assertNotNull(emp.getLastName());
            assertTrue(emp.getId() > 0);
            assertTrue(emp.getVersion() > 0);
            assertEquals(0.0, emp.getSalary(), 0.0);
            assertNull(emp.getGender());
            assertNull(emp.getStartTime());
            assertNull(emp.getEndTime());
            assertNull(emp.getPeriod());
            assertNotNull(emp.getAddress());
            assertNotNull(emp.getPhoneNumbers());
            assertNull(emp.getProjects());
        }
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void managerManagerManagerFetchWithNames() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = this.examples.managerManagerManagerFetchWithNames(em);

        assertTrue(emps.size() > 0);
        int selectsRun = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        // Calculate unique managers to figure out expected SQL
        HashSet<Employee> managers = new HashSet<Employee>();
        for (Employee emp : emps) {
            managers.add(emp);
            managers.add(emp.getManager());
            managers.add(emp.getManager().getManager());
        }
        managers.removeAll(emps);

        assertEquals(1 + managers.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertEquals(selectsRun, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeAddress_ReturnBoth() throws Exception {
        EntityManager em = getEntityManager();

        List<Object[]> results = this.examples.employeeAddress_ReturnBoth(em);

        assertEquals(1 + results.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void fetchCopyMergeExample() throws Exception {
        EntityManager em = getEntityManager();

        em.getTransaction().begin();

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");
        fetchPlan.addAttribute("address");
        // fetchPlan.addAttribute("phoneNumbers");

        int minId = ((Number) em.createQuery("SELECT MIN(e.id) FROM Employee e").getSingleResult()).intValue();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = " + minId);
        query.setHint(QueryHints.FETCH_GROUP, fetchPlan.createFetchGroup());

        Employee emp = (Employee) query.getSingleResult();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emp);

        assertFalse(emp.getFirstName().equals(emp.getLastName()));

        Employee copy = JpaFetchPlanHelper.copy(em, fetchPlan, emp);

        Assert.assertNotSame(emp, copy);

        copy.setSalary(Integer.MAX_VALUE);
        copy.setFirstName(emp.getLastName());
        copy.setLastName(emp.getFirstName());

        assertFalse(copy.getFirstName().equals(copy.getLastName()));

        JpaFetchPlanHelper.merge(em, fetchPlan, copy);

        // assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());

        em.flush();

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLUPDATECalls());

        em.getTransaction().rollback();
    }

    @After
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }
}
