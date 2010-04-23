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
import javax.persistence.Query;

import model.Address;
import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
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
     * private-owned configuration will cause it to be deleted. For this to work
     * the PhoneNumber.owner relationship needs to be fetch=EAGER so that is is
     * loaded using the default mappings fetch. Without this the owner is null
     * and PK is incomplete in the partial entity.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void removeOnePhone() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e WHERE SIZE(e.phoneNumbers) > 1").setMaxResults(1).getResultList();
        Employee emp = emps.get(0);
        int numPhones = emp.getPhoneNumbers().size();

        assertNotNull(emp);
        assertTrue(numPhones > 1);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        FetchPlan plan = new FetchPlan(Employee.class);
        plan.addAttribute("phoneNumbers");

        Employee minimalEmp = JpaFetchPlanHelper.copy(em, plan, emp);

        assertNotNull(minimalEmp.getPhoneNumbers());
        assertEquals(emp.getPhoneNumbers().size(), minimalEmp.getPhoneNumbers().size());
        for (PhoneNumber phone : minimalEmp.getPhoneNumbers()) {
            assertSame(minimalEmp, phone.getOwner());
        }

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

    /**
     * Create a partial entity graph using
     * {@link JpaFetchPlanHelper#copy(EntityManager, FetchPlan, Object)}
     */
    @Test
    public void copyMerge_updateBasics() {
        EntityManager em = getEntityManager();

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        Employee emp = Queries.minEmployeeWithAddressAndPhones(em);

        assertNotNull(emp.getFirstName());
        assertNotNull(emp.getLastName());

        Employee partialEmp = JpaFetchPlanHelper.copy(em, fetchPlan, emp);

        // Verify state of partial entity
        assertEquals(emp.getId(), partialEmp.getId());
        assertEquals(emp.getVersion(), partialEmp.getVersion());
        assertNotNull(partialEmp.getFirstName());
        assertNotNull(partialEmp.getLastName());
        assertNull(partialEmp.getPeriod());
        assertNull(partialEmp.getProjects());
        assertNull(partialEmp.getGender());
        assertNull(partialEmp.getManager());
        assertNull(partialEmp.getManagedEmployees());
        assertNotNull(partialEmp.getAddress());
        assertNotNull(partialEmp.getPhoneNumbers());
        for (PhoneNumber phone : partialEmp.getPhoneNumbers()) {
            assertNotNull(phone.getNumber());
            assertNotNull(phone.getType());
            assertNotNull(phone.getAreaCode());
            assertNotNull(phone.getOwner());
        }

        double originalSalary = emp.getSalary();
        partialEmp.setSalary(emp.getSalary() + 1);
        partialEmp.setFirstName(null);
        partialEmp.setLastName(null);

        em.getTransaction().begin();
        Employee managedEmp = JpaFetchPlanHelper.merge(em, fetchPlan, partialEmp);

        assertSame(emp, managedEmp);
        assertNull(emp.getFirstName());
        assertNull(emp.getLastName());
        assertEquals(originalSalary, emp.getSalary());

        em.flush();

        // Verify that only 1 update was issued for the EMPLOYEE table. The
        // salary will not be updated
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLUPDATECalls());

        em.getTransaction().rollback();
    }

    /**
     * Create a partial entity graph using
     * {@link JpaFetchPlanHelper#copy(EntityManager, FetchPlan, Object)}
     */
    @Test
    public void copyMerge_removingPrivateOwnedOneToMany() {
        EntityManager em = getEntityManager();

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        Employee emp = Queries.minEmployeeWithAddressAndPhones(em);

        assertNotNull(emp.getFirstName());
        assertNotNull(emp.getLastName());

        Employee partialEmp = JpaFetchPlanHelper.copy(em, fetchPlan, emp);

        // Verify state of partial entity
        assertEquals(emp.getId(), partialEmp.getId());
        assertEquals(emp.getVersion(), partialEmp.getVersion());
        assertNotNull(partialEmp.getFirstName());
        assertNotNull(partialEmp.getLastName());
        assertNull(partialEmp.getPeriod());
        assertNull(partialEmp.getProjects());
        assertNull(partialEmp.getGender());
        assertNull(partialEmp.getManager());
        assertNull(partialEmp.getManagedEmployees());
        assertNotNull(partialEmp.getAddress());
        assertNotNull(partialEmp.getPhoneNumbers());
        int numPhones = partialEmp.getPhoneNumbers().size();
        for (PhoneNumber phone : partialEmp.getPhoneNumbers()) {
            assertNotNull(phone.getNumber());
            assertNotNull(phone.getType());
            assertNotNull(phone.getAreaCode());
            assertNotNull(phone.getOwner());
        }

        partialEmp.getPhoneNumbers().remove(0);

        em.getTransaction().begin();
        Employee managedEmp = JpaFetchPlanHelper.merge(em, fetchPlan, partialEmp);

        assertSame(emp, managedEmp);
        assertEquals(numPhones - 1, emp.getPhoneNumbers().size());

        em.flush();

        // Verify that only 1 delete
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLDELETECalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());

        em.getTransaction().rollback();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDetachMerge_clearEM() throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        Query q = em.createQuery("SELECT e FROM Employee e WHERE SIZE(e.managedEmployees) > 0");
        q.setMaxResults(1);
        List<Employee> ems = q.getResultList();

        // Verify that at this point only the Employee SELECT occurred
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        FetchPlan fp = new FetchPlan(Employee.class);
        fp.addAttribute("managedEmployees");

        // Ensure FetchPlan's requested attributes are loaded from database
        JpaFetchPlanHelper.fetch(em, fp, ems);

        // Primary SELECT plus 1 for each employee
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        Employee emp = ems.get(0);
        System.out.println("emp.id: " + emp.getId());
        System.out.println("emp.address: " + emp.getAddress().toString());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        System.out.println("emp.managedEmployees: " + emp.getManagedEmployees().toArray().toString());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // detach the emp
        emp = JpaFetchPlanHelper.copy(em, fp, emp);
        // after copy we see about 50 SQLs which we cant associate with this
        // detach action
        System.out.println("deserializedEmployee: " + emp.toString());
        em.getTransaction().commit();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLINSERTCalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLDELETECalls());

        // Clear EntityManager
        em.clear();

        Employee newEmp = new Employee();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        em.persist(newEmp);
        // Additional SELECT to get new sequence value
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // add the new to Collection
        emp.addManagedEmployee(newEmp);

        em.getTransaction().begin();
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Employee mergedEmployee = JpaFetchPlanHelper.merge(em, fp, emp);

        // No SELECT during merge since entities in shared cache
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        mergedEmployee.getAddress(); // null is okay

        // No SELECT for address as it was already read in previous transaction
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.flush();

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        // INSERT for each of EMPLOYEE AND SALARY table
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLINSERTCalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLDELETECalls());

        em.getTransaction().rollback();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDetachMerge_initializeCache() throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        Query q = em.createQuery("SELECT e FROM Employee e WHERE SIZE(e.managedEmployees) > 0");
        q.setMaxResults(1);
        List<Employee> ems = q.getResultList();

        // Verify that at this point only the Employee SELECT occurred
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        FetchPlan fp = new FetchPlan(Employee.class);
        fp.addAttribute("managedEmployees");

        // Ensure FetchPlan's requested attributes are loaded from database
        JpaFetchPlanHelper.fetch(em, fp, ems);

        // Primary SELECT plus 1 for each employee
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        Employee emp = ems.get(0);
        System.out.println("emp.id: " + emp.getId());
        System.out.println("emp.address: " + emp.getAddress().toString());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        System.out.println("emp.managedEmployees: " + emp.getManagedEmployees().toArray().toString());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // detach the emp
        emp = JpaFetchPlanHelper.copy(em, fp, emp);
        // after copy we see about 50 SQLs which we cant associate with this
        // detach action
        System.out.println("deserializedEmployee: " + emp.toString());
        em.getTransaction().commit();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLINSERTCalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLDELETECalls());

        // Initialize shared cache
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();

        Employee newEmp = new Employee();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        em.persist(newEmp);
        // Additional SELECT to get new sequence value
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // add the new to Collection
        emp.addManagedEmployee(newEmp);

        em.getTransaction().begin();
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Employee mergedEmployee = JpaFetchPlanHelper.merge(em, fp, emp);

        // No SELECT during merge since entities in shared cache
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        mergedEmployee.getAddress(); // null is okay

        // No SELECT for address as it was already read in previous transaction
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.flush();

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        // INSERT for each of EMPLOYEE AND SALARY table
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLINSERTCalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLDELETECalls());

        em.getTransaction().rollback();
    }

    /**
     * Read 1 Employee who has managedEmployees and fetch and copy the entity
     * graph with just its managedEmployees. Then the Employee is modified to
     * have a new managed Employee and merged back into a clear EntityManager
     * with an empty shared cache.
     * <p>
     * The expected result is that the minimal SQL is used to load the graph and
     * when merged additional SQL SELECTS are used to load the Employee and its
     * managedEmployees during the merge.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDetachMerge_clearEMAndInitializeCache() throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        Query q = em.createQuery("SELECT e FROM Employee e WHERE SIZE(e.managedEmployees) > 0");
        q.setMaxResults(1);
        List<Employee> ems = q.getResultList();

        // Verify that at this point only the Employee SELECT occurred
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        FetchPlan fp = new FetchPlan(Employee.class);
        fp.addAttribute("managedEmployees");

        // Ensure FetchPlan's requested attributes are loaded from database
        JpaFetchPlanHelper.fetch(em, fp, ems);

        // Primary SELECT plus 1 for each employee
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        Employee emp = ems.get(0);
        System.out.println("emp.id: " + emp.getId());
        System.out.println("emp.address: " + emp.getAddress().toString());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        System.out.println("emp.managedEmployees: " + emp.getManagedEmployees().toArray().toString());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // detach the emp
        emp = JpaFetchPlanHelper.copy(em, fp, emp);
        // after copy we see about 50 SQLs which we cant associate with this
        // detach action
        System.out.println("deserializedEmployee: " + emp.toString());
        em.getTransaction().commit();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLINSERTCalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLDELETECalls());

        // Clear EntityManager and initialize shared cache
        em.clear();
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();

        Employee newEmp = new Employee();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        em.persist(newEmp);
        // Additional SELECT to get new sequence value
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // add the new to Collection
        emp.addManagedEmployee(newEmp);

        em.getTransaction().begin();
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Employee mergedEmployee = JpaFetchPlanHelper.merge(em, fp, emp);

        // 2 SELECT for Employee and managed Employees to do merge
        assertEquals(6, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        mergedEmployee.getAddress(); // null is okay
        // Additional select for ADDRESS
        assertEquals(7, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.flush();

        assertEquals(7, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        // INSERT for each of EMPLOYEE AND SALARY table
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLINSERTCalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLDELETECalls());

        em.getTransaction().rollback();
    }

    /**
     * Clear the shared cache and any sequences between test cases
     */
    @After
    public void clearState() {
        Server session = JpaHelper.getServerSession(getEMF());

        session.getIdentityMapAccessor().initializeAllIdentityMaps();
        session.getSequencingControl().initializePreallocated();
    }

    @Override
    protected void verifyConfig(EntityManager em) {
        super.verifyConfig(em);
        FetchPlanAssert.verifyEmployeeConfig(getEMF());
    }
}
