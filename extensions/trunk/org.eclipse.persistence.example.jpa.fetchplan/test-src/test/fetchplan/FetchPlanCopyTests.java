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
import static junit.framework.Assert.*;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.After;
import org.junit.Test;

import testing.EclipseLinkJPAAssert;
import testing.EclipseLinkJPATest;

/**
 * Set of tests that verify the
 * {@link FetchPlan#copy(Object, org.eclipse.persistence.internal.sessions.AbstractSession)}
 * operation.
 * 
 * @author dclarke
 * @since EclispeLink 1.2
 */
@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class FetchPlanCopyTests extends EclipseLinkJPATest {

    /**
     * Verify the behavior of a new entity
     */
    @Test
    public void verifyDetachedEntity_DefaultConstructor() {
        Employee newEmp = new Employee();

        assertNotNull(newEmp.getPhoneNumbers());
        assertTrue(newEmp.getPhoneNumbers() instanceof List);
        assertNotNull(newEmp.getProjects());
        assertTrue(newEmp.getProjects() instanceof List);
        assertNotNull(newEmp.getManagedEmployees());
        assertTrue(newEmp.getManagedEmployees() instanceof List);
    }

    /**
     * Verify the behavior of a new entity
     */
    @Test
    public void verifyDetachedEntity_InstantiationPolicy() {
        ClassDescriptor descriptor = getDescriptor("Employee");

        Employee newEmp = (Employee) descriptor.getInstantiationPolicy().buildNewInstance();

        assertNull(newEmp.getPhoneNumbers());
        assertNull(newEmp.getProjects());
        assertNull(newEmp.getManagedEmployees());
    }

    @Test
    public void allEmployees_copyNames() {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e").getResultList();

        FetchPlan fp = new FetchPlan("Just Names", Employee.class);
        fp.addAttribute("firstName");
        fp.addAttribute("lastName");

        List<Employee> copies = JpaFetchPlanHelper.copy(em, fp, emps);

        assertEquals(emps.size(), copies.size());

        for (int index = 0; index < emps.size(); index++) {
            Employee emp = emps.get(index);
            Employee copy = copies.get(index);

            assertEquals(emp.getId(), copy.getId());
            assertEquals(emp.getVersion(), copy.getVersion());

            assertEquals(emp.getFirstName(), copy.getFirstName());
            assertEquals(emp.getLastName(), copy.getLastName());

            assertNull(copy.getGender());
            assertNull(copy.getPeriod());
            assertNull(copy.getAddress());
            assertNull(copy.getPhoneNumbers());
            assertNull(copy.getProjects());
            assertNull(copy.getManagedEmployees());
        }
    }

    @Test
    public void allEmployees_copyPeriod() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e").getResultList();

        FetchPlan fp = new FetchPlan(Employee.class);
        fp.addAttribute("period");

        List<Employee> copies = JpaFetchPlanHelper.copy(em, fp, emps);

        assertEquals(emps.size(), copies.size());

        for (int index = 0; index < emps.size(); index++) {
            Employee emp = emps.get(index);
            Employee copy = copies.get(index);

            assertEquals(emp.getId(), copy.getId());
            assertEquals(emp.getVersion(), copy.getVersion());

            assertNull(copy.getFirstName());
            assertNull(copy.getLastName());
            assertNull(copy.getGender());
            assertNull(copy.getAddress());
            assertNull(copy.getPhoneNumbers());
            assertNull(copy.getProjects());
            assertNull(copy.getManagedEmployees());
            assertNotNull(copy.getPeriod());
            assertEquals(emp.getPeriod().getStartDate(), copy.getPeriod().getStartDate());
            assertEquals(emp.getPeriod().getEndDate(), copy.getPeriod().getEndDate());
        }
    }

    @Test
    public void allEmployees_copyNamesAdress() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e").getResultList();

        FetchPlan fp = new FetchPlan("Just Names", Employee.class);
        fp.addAttribute("firstName");
        fp.addAttribute("lastName");
        fp.addAttribute("address");

        List<Employee> copies = JpaFetchPlanHelper.copy(em, fp, emps);

        assertEquals(emps.size(), copies.size());

        for (int index = 0; index < emps.size(); index++) {
            Employee emp = emps.get(index);
            Employee copy = copies.get(index);

            assertEquals(emp.getId(), copy.getId());
            assertEquals(emp.getVersion(), copy.getVersion());

            assertEquals(emp.getFirstName(), copy.getFirstName());
            assertEquals(emp.getLastName(), copy.getLastName());

            assertNull(copy.getGender());
            assertNull(copy.getPeriod());
            assertNotNull(copy.getAddress());
            assertNull(copy.getPhoneNumbers());
            assertNull(copy.getProjects());
            assertNull(copy.getManagedEmployees());
        }
    }

    @Test
    public void allEmployees_copyNamesAddressCity() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e").getResultList();

        FetchPlan fp = new FetchPlan("Just Names", Employee.class);
        fp.addAttribute("firstName");
        fp.addAttribute("lastName");
        fp.addAttribute("address.city");

        List<Employee> copies = JpaFetchPlanHelper.copy(em, fp, emps);

        assertEquals(emps.size(), copies.size());

        for (int index = 0; index < emps.size(); index++) {
            Employee emp = emps.get(index);
            Employee copy = copies.get(index);

            assertEquals(emp.getId(), copy.getId());
            assertEquals(emp.getVersion(), copy.getVersion());

            assertEquals(emp.getFirstName(), copy.getFirstName());
            assertEquals(emp.getLastName(), copy.getLastName());

            assertNull(copy.getGender());
            assertNull(copy.getPeriod());
            assertNotNull(copy.getAddress());
            assertNull(copy.getPhoneNumbers());
            assertNull(copy.getProjects());
            assertNull(copy.getManagedEmployees());
        }
    }

    @Test
    public void allEmployees_copyNamesPhoneNumbers() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e").getResultList();

        FetchPlan fp = new FetchPlan("Just Names", Employee.class);
        fp.addAttribute("firstName");
        fp.addAttribute("lastName");
        fp.addAttribute("phoneNumbers");

        List<Employee> copies = JpaFetchPlanHelper.copy(em, fp, emps);

        assertEquals(emps.size(), copies.size());

        for (int index = 0; index < emps.size(); index++) {
            Employee emp = emps.get(index);
            Employee copy = copies.get(index);

            assertNotSame(emp, copy);
            assertEquals(emp.getId(), copy.getId());
            assertEquals(emp.getVersion(), copy.getVersion());

            assertEquals(emp.getFirstName(), copy.getFirstName());
            assertEquals(emp.getLastName(), copy.getLastName());

            assertNotNull(emp.getGender());
            assertNull(copy.getGender());

            assertNotNull(emp.getAddress());
            assertNull(copy.getAddress());

            assertEquals(emp.getPhoneNumbers().size(), copy.getPhoneNumbers().size());

            for (int pI = 0; pI < emp.getPhoneNumbers().size(); pI++) {
                PhoneNumber phone = emp.getPhoneNumbers().get(pI);
                PhoneNumber phoneCopy = copy.getPhoneNumbers().get(pI);

                assertEquals(phone.getId(), phoneCopy.getId());
                assertEquals(phone.getNumber(), phoneCopy.getNumber());
                assertEquals(phone.getAreaCode(), phoneCopy.getAreaCode());
                assertEquals(phone.getType(), phoneCopy.getType());

                assertNotNull(phone.getOwner());
                assertSame(emp, phone.getOwner());

                assertNotNull(phoneCopy.getOwner());
                assertSame(copy, phoneCopy.getOwner());
            }
        }
    }

    @Test
    public void allEmployees_copyNamesPhoneNumbersAreaCodeAndNumber() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e").getResultList();

        FetchPlan fp = new FetchPlan("Just Names", Employee.class);
        fp.addAttribute("id");
        fp.addAttribute("version");
        fp.addAttribute("firstName");
        fp.addAttribute("lastName");
        fp.addAttribute("phoneNumbers.type");
        fp.addAttribute("phoneNumbers.number");
        fp.addAttribute("phoneNumbers.areaCode");

        List<Employee> copies = JpaFetchPlanHelper.copy(em, fp, emps);

        assertEquals(emps.size(), copies.size());

        for (int index = 0; index < emps.size(); index++) {
            Employee emp = emps.get(index);
            Employee copy = copies.get(index);

            assertTrue(emp.getId() > 0);
            assertEquals(emp.getId(), copy.getId());
            assertEquals(emp.getVersion(), copy.getVersion());

            assertEquals(emp.getFirstName(), copy.getFirstName());
            assertEquals(emp.getLastName(), copy.getLastName());

            assertNotNull(emp.getGender());
            assertNull(copy.getGender());

            assertNotNull(emp.getAddress());
            assertNull(copy.getAddress());

            assertEquals(emp.getPhoneNumbers().size(), copy.getPhoneNumbers().size());

            for (int pI = 0; pI < emp.getPhoneNumbers().size(); pI++) {
                PhoneNumber phone = emp.getPhoneNumbers().get(pI);
                PhoneNumber phoneCopy = copy.getPhoneNumbers().get(pI);

                assertEquals(0, phoneCopy.getId());
                assertEquals(phone.getNumber(), phoneCopy.getNumber());
                assertEquals(phone.getAreaCode(), phoneCopy.getAreaCode());
                assertEquals(phone.getType(), phoneCopy.getType());

                assertNotNull(phone.getOwner());
                assertSame(emp, phone.getOwner());

                assertNotNull(phoneCopy.getOwner());
                assertSame(copy, phoneCopy.getOwner());
            }
        }
    }

    /**
     * Test that copying handles entities in the root collection also existing
     * within the graph. In this case the initial query retrieves a collection
     * of entities populating their managedEmployees list. The employees in this
     * list only have their id copied. The challenge is that when an entity
     * exists in the root query as well as in one of the entity's
     * managedEmployees list then the copy operation must ensure that the root
     * entities are properly populated.
     */
    @Test
    public void selfReferencedRelationshipCopying() {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e").getResultList();

        FetchPlan fetchplan = new FetchPlan(Employee.class);
        fetchplan.addAttribute("managedEmployees");

        List<Employee> copiedEmps = JpaFetchPlanHelper.copy(em, fetchplan, emps);

        assertEquals(emps.size(), copiedEmps.size());

        for (int index = 0; index < emps.size(); index++) {
            Employee emp = emps.get(index);
            Employee empCopy = copiedEmps.get(index);

            assertNotSame(emp, empCopy);

            // Verify required attributes
            assertEquals(emp.getId(), empCopy.getId());
            assertEquals(emp.getVersion(), empCopy.getVersion());
            assertEquals(emp.getManagedEmployees().size(), empCopy.getManagedEmployees().size());

            // These should not be loaded in the original and should be null in
            // all copies
            EclipseLinkJPAAssert.assertNotLoaded(getEMF(), emp, "address");
            assertNull(empCopy.getAddress());
            EclipseLinkJPAAssert.assertNotLoaded(getEMF(), emp, "projects");
            assertNull(empCopy.getProjects());
            EclipseLinkJPAAssert.assertNotLoaded(getEMF(), emp, "phoneNumbers");
            assertNull(empCopy.getPhoneNumbers());

            // if the employee has a manager then it should have all non-lazy
            // attributes
            if (emp.getManager() != null) {
                assertEquals(emp.getFirstName(), empCopy.getFirstName());
                assertEquals(emp.getLastName(), empCopy.getLastName());
                assertEquals(emp.getGender(), empCopy.getGender());
                assertEquals(emp.getSalary(), empCopy.getSalary());
                assertEquals(emp.getStartTime(), empCopy.getStartTime());
                assertEquals(emp.getEndTime(), empCopy.getEndTime());

                if (emp.getPeriod() != null) {
                    assertEquals(emp.getPeriod().getStartDate(), empCopy.getPeriod().getStartDate());
                    assertEquals(emp.getPeriod().getEndDate(), empCopy.getPeriod().getEndDate());
                }
            }
            // otherwise only the managedEmployees should be populated
            else {
                assertNull(empCopy.getFirstName());
                assertNull(empCopy.getLastName());
                assertNull(empCopy.getGender());
                assertEquals(0d, empCopy.getSalary());
                assertNull(empCopy.getStartTime());
                assertNull(empCopy.getEndTime());
                assertNull(empCopy.getPeriod());
                assertNull(empCopy.getManager());
            }

        }
    }

    @After
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    @Override
    protected void verifyConfig(EntityManager em) {
        super.verifyConfig(em);

        EclipseLinkJPAAssert.assertWoven(getDescriptor("Employee"));
        FetchPlanAssert.verifyEmployeeConfig(getEMF());
    }

}
