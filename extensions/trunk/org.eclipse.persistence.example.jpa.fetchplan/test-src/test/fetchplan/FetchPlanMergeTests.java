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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.Address;
import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.junit.After;
import org.junit.Test;

import testing.EclipseLinkJPAAssert;
import testing.EclipseLinkJPATest;
import example.Queries;

/**
 * Set of tests that verify the
 * {@link FetchPlan#copy(Object, org.eclipse.persistence.internal.sessions.AbstractSession)}
 * operation.
 * 
 * @author dclarke
 * @since EclispeLink 1.2
 */
@PersistenceContext(unitName = "employee")
public class FetchPlanMergeTests extends EclipseLinkJPATest {

    @Test
    public void incrementSalary() throws Exception {
        EntityManager em = getEntityManager();

        Employee emp = Queries.minEmployeeWithAddressAndPhones(em);

        assertNotNull(emp);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFalse(emp.getFirstName().equals("-"));

        FetchPlan plan = new FetchPlan(Employee.class);
        plan.addAttribute("salary");

        Employee minimalEmp = new Employee();
        minimalEmp.setId(emp.getId());
        minimalEmp.setVersion(emp.getVersion());
        minimalEmp.setSalary(Integer.MAX_VALUE);
        minimalEmp.setFirstName("-");
        minimalEmp.setAddress(null);

        em.getTransaction().begin();
        Employee empWC = JpaFetchPlanHelper.merge(em, plan, minimalEmp);
        em.flush();

        assertSame(emp, empWC);
        assertFalse(empWC.getFirstName().equals("-"));
        assertNotNull(empWC.getAddress());
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLUPDATECalls());

        em.getTransaction().rollback();
    }

    /**
     * Test doing a merge on an entity not read in.
     */
    @Test
    public void incrementSalary_NotInContext() throws Exception {
        EntityManager em = getEntityManager();

        int minId = Queries.minEmployeeIdWithAddressAndPhones(em);
        long version = ((Number) em.createQuery("SELECT e.version FROM Employee e WHERE e.id = " + minId).getSingleResult()).longValue();

        FetchPlan plan = new FetchPlan(Employee.class);
        plan.addAttribute("id");
        plan.addAttribute("salary");

        Employee minimalEmp = new Employee();
        minimalEmp.setId(minId);
        minimalEmp.setVersion(version);
        minimalEmp.setSalary(Integer.MAX_VALUE);
        minimalEmp.setFirstName("-");
        minimalEmp.setAddress(null);

        em.getTransaction().begin();
        Employee empWC = JpaFetchPlanHelper.merge(em, plan, minimalEmp);
        em.flush();

        assertFalse(empWC.getFirstName().equals("-"));
        assertNotNull(empWC.getAddress());
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLUPDATECalls());

        em.getTransaction().rollback();
    }

    @Test
    public void updateCity() throws Exception {
        EntityManager em = getEntityManager();

        Employee emp = Queries.minEmployeeWithAddressAndPhones(em);
        String country = emp.getAddress().getCountry();

        assertNotNull(emp);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        FetchPlan plan = new FetchPlan(Employee.class);
        plan.addAttribute("address.city");

        Employee minimalEmp = new Employee();
        minimalEmp.setId(emp.getId());
        minimalEmp.setVersion(emp.getVersion());
        Address minimalAddress = new Address();
        minimalEmp.setAddress(minimalAddress);
        minimalAddress.setId(emp.getAddress().getId());
        minimalAddress.setCity("-");
        minimalAddress.setCountry("-");
        minimalAddress.setStreet("-");

        em.getTransaction().begin();
        Employee empWC = JpaFetchPlanHelper.merge(em, plan, minimalEmp);
        em.flush();

        assertSame(emp, empWC);
        assertNotNull(empWC.getAddress());
        assertEquals("-", empWC.getAddress().getCity());
        assertEquals(country, empWC.getAddress().getCountry());
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLDELETECalls());

        em.getTransaction().rollback();
    }

    /**
     * Delete the employee's address by setting it to null in the copy being
     * merged and the FetchPlan's merge combined with the mapping's
     * private-owned configuration will cause it to be deleted.
     */
    @Test
    public void deleteAddress() throws Exception {
        EntityManager em = getEntityManager();

        Employee emp = Queries.minEmployeeWithAddressAndPhones(em);

        assertNotNull(emp);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        FetchPlan plan = new FetchPlan(Employee.class);
        plan.addAttribute("address");

        Employee minimalEmp = new Employee();
        minimalEmp.setId(emp.getId());
        minimalEmp.setVersion(emp.getVersion());
        minimalEmp.setAddress(null);

        em.getTransaction().begin();
        Employee empWC = JpaFetchPlanHelper.merge(em, plan, minimalEmp);
        em.flush();

        assertSame(emp, empWC);
        assertNull(empWC.getAddress());
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLDELETECalls());

        em.getTransaction().rollback();
    }

    /**
     * Delete the employee's address by setting it to null in the copy being
     * merged and the FetchPlan's merge combined with the mapping's
     * private-owned configuration will cause it to be deleted.
     */
    @Test
    public void removeAllPhones() throws Exception {
        EntityManager em = getEntityManager();

        Employee emp = Queries.minEmployeeWithAddressAndPhones(em);

        assertNotNull(emp);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        FetchPlan plan = new FetchPlan(Employee.class);
        plan.addAttribute("phoneNumbers");

        Employee minimalEmp = new Employee();
        minimalEmp.setId(emp.getId());
        minimalEmp.setVersion(emp.getVersion());
        minimalEmp.setPhoneNumbers(new ArrayList<PhoneNumber>());

        em.getTransaction().begin();
        Employee empWC = JpaFetchPlanHelper.merge(em, plan, minimalEmp);
        em.flush();

        assertSame(emp, empWC);
        assertTrue(empWC.getPhoneNumbers().isEmpty());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLDELETECalls());

        em.getTransaction().rollback();
    }

    /**
     * Delete the employee's address by setting it to null in the copy being
     * merged and the FetchPlan's merge combined with the mapping's
     * private-owned configuration will cause it to be deleted.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void removeOnePhone() throws Exception {
        EntityManager em = getEntityManager();

        List<Object[]> phoneIds = em.createQuery("SELECT p.id, COUNT(p.id) FROM PhoneNumber p GROUP BY p.id").getResultList();
        int empId = -1;
        for (Object[] values : phoneIds) {
            if (empId == -1 && ((Number) values[1]).intValue() > 1) {
                empId = ((Number) values[0]).intValue();
            }
        }

        Employee emp = (Employee) em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address WHERE e.id = " + empId).getSingleResult();
        int numPhones = emp.getPhoneNumbers().size();

        assertNotNull(emp);
        assertTrue(numPhones > 1);
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        FetchPlan plan = new FetchPlan(Employee.class);
        plan.addAttribute("phoneNumbers");

        Employee minimalEmp = JpaFetchPlanHelper.copy(em, plan, emp);

        minimalEmp.getPhoneNumbers().remove(0);

        em.getTransaction().begin();
        Employee empWC = JpaFetchPlanHelper.merge(em, plan, minimalEmp);
        em.flush();

        assertSame(emp, empWC);
        assertEquals(numPhones - 1, empWC.getPhoneNumbers().size());
        assertEquals(minimalEmp.getPhoneNumbers().size(), empWC.getPhoneNumbers().size());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLDELETECalls());

        em.getTransaction().rollback();
    }

    @After
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    @Override
    protected void verifyConfig(EntityManager em) {
        super.verifyConfig(em);
        FetchPlanAssert.verifyEmployeeConfig(getEMF());
    }
}
