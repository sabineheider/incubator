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
import static junit.framework.Assert.assertNotNull;
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

        FetchPlan fp = new FetchPlan("Just Names", Employee.class, false);
        fp.addAttribute("firstName");
        fp.addAttribute("lastName");

        List<Employee> copies = JpaFetchPlanHelper.copy(em, fp, emps);

        assertEquals(emps.size(), copies.size());

        for (int index = 0; index < emps.size(); index++) {
            Employee emp = emps.get(index);
            Employee copy = copies.get(index);

            assertEquals(0, copy.getId());
            assertEquals(new Long(0), copy.getVersion());

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
    public void allEmployees_copyNames_WithRequired() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e").getResultList();

        FetchPlan fp = new FetchPlan("Just Names", Employee.class, true);
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
    public void allEmployees_copyNamesAdress() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e").getResultList();

        FetchPlan fp = new FetchPlan("Just Names", Employee.class, false);
        fp.addAttribute("firstName");
        fp.addAttribute("lastName");
        fp.addAttribute("address");

        List<Employee> copies = JpaFetchPlanHelper.copy(em, fp, emps);

        assertEquals(emps.size(), copies.size());

        for (int index = 0; index < emps.size(); index++) {
            Employee emp = emps.get(index);
            Employee copy = copies.get(index);

            assertEquals(0, copy.getId());
            assertEquals(new Long(0), copy.getVersion());

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

        FetchPlan fp = new FetchPlan("Just Names", Employee.class, true);
        fp.addAttribute("firstName");
        fp.addAttribute("lastName");
        fp.addAttribute("phoneNumbers");

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
    public void allEmployees_copyNamesPhoneNumbersAll() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e").getResultList();

        FetchPlan fp = new FetchPlan("Just Names", Employee.class, true);
        fp.addAttribute("firstName");
        fp.addAttribute("lastName");
        fp.addAttribute("phoneNumbers.id");
        fp.addAttribute("phoneNumbers.number");
        fp.addAttribute("phoneNumbers.areaCode");
        fp.addAttribute("phoneNumbers.type");
        fp.addAttribute("phoneNumbers.owner");

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

        FetchPlan fp = new FetchPlan("Just Names", Employee.class, false);
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

                assertNull(phoneCopy.getOwner());
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

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e where e.managedEmployees is not empty").getResultList();

        FetchPlan fetchplan = new FetchPlan("f", Employee.class, true);
        fetchplan.addAttribute("managedEmployees");
        fetchplan.addAttribute("managedEmployees.id");

        List<Employee> detachedEmps = JpaFetchPlanHelper.copy(em, fetchplan, emps);

        for (Employee emp : detachedEmps) {
            assertNull(emp.getAddress());
            assertNull(emp.getPhoneNumbers());
            assertNull(emp.getManager());
            assertNull(emp.getPeriod());
            assertNull(emp.getProjects());

            assertNotNull(emp.getManagedEmployees());

            for (Employee managedEmp : emp.getManagedEmployees()) {
                assertNull(managedEmp.getAddress());
                assertNull(managedEmp.getPhoneNumbers());
                assertNull(managedEmp.getManager());
                assertNull(managedEmp.getPeriod());
                assertNull(managedEmp.getProjects());

                // If the managedEmp is part of the original result then it will
                // have a list of managed employees. Otherwise it will be null.
                if (detachedEmps.contains(managedEmp)) {
                    assertNotNull(managedEmp.getManagedEmployees());
                } else {
                    assertNull(managedEmp.getManagedEmployees());
                }
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
