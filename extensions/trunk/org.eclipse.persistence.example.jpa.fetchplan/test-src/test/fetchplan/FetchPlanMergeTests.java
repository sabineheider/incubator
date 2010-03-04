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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.Address;
import model.Employee;

import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.After;
import org.junit.Test;

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
     * 
     * @throws Exception
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

    @After
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

}
