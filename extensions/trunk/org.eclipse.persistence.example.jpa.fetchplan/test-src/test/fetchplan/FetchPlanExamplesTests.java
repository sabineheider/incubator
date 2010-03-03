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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.Employee;

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
    public void employeeAddress_ReturnBoth() throws Exception {
        EntityManager em = getEntityManager();

        List<Object[]> results = this.examples.employeeAddress_ReturnBoth(em);

        assertEquals(1 + results.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @After
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }
}
