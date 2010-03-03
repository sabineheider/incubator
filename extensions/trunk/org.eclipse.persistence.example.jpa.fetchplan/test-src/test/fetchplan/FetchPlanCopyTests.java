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

import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.After;
import org.junit.Test;

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

    @Test
    public void allEmployees_copyNames() throws Exception {
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

            assertTrue(emp.getId() > 0);
            assertEquals(0, copy.getId());
            assertTrue(emp.getVersion() > 0);
            assertEquals(new Long(0), copy.getVersion());

            assertEquals(emp.getFirstName(), copy.getFirstName());
            assertEquals(emp.getLastName(), copy.getLastName());

            assertNotNull(emp.getGender());
            assertNull(copy.getGender());

            assertNotNull(emp.getAddress());
            assertNull(copy.getAddress());

            assertNotNull(emp.getPhoneNumbers());
            assertNull(copy.getPhoneNumbers());
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

            assertTrue(emp.getId() > 0);
            assertEquals(emp.getId(), copy.getId());
            assertEquals(emp.getVersion(), copy.getVersion());

            assertEquals(emp.getFirstName(), copy.getFirstName());
            assertEquals(emp.getLastName(), copy.getLastName());

            assertNotNull(emp.getGender());
            assertNull(copy.getGender());

            assertNotNull(emp.getAddress());
            assertNull(copy.getAddress());

            assertNotNull(emp.getPhoneNumbers());
            assertNull(copy.getPhoneNumbers());
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

            assertTrue(emp.getId() > 0);
            assertEquals(0, copy.getId());
            assertTrue(emp.getVersion() > 0);
            assertEquals(new Long(0), copy.getVersion());

            assertEquals(emp.getFirstName(), copy.getFirstName());
            assertEquals(emp.getLastName(), copy.getLastName());

            assertNotNull(emp.getGender());
            assertNull(copy.getGender());

            assertNotNull(emp.getPhoneNumbers());
            assertNull(copy.getPhoneNumbers());

            assertNotNull(emp.getAddress());
            assertNotNull(copy.getAddress());

            assertEquals(emp.getAddress().getId(), copy.getAddress().getId());
            assertEquals(emp.getAddress().getCity(), copy.getAddress().getCity());
            assertEquals(emp.getAddress().getCountry(), copy.getAddress().getCountry());
            assertEquals(emp.getAddress().getPostalCode(), copy.getAddress().getPostalCode());
            assertEquals(emp.getAddress().getProvince(), copy.getAddress().getProvince());
            assertEquals(emp.getAddress().getStreet(), copy.getAddress().getStreet());
        }
    }

    @Test
    public void allEmployees_copyNamesAddressCity() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e").getResultList();

        FetchPlan fp = new FetchPlan("Just Names", Employee.class, false);
        fp.addAttribute("firstName");
        fp.addAttribute("lastName");
        fp.addAttribute("address.city");

        List<Employee> copies = JpaFetchPlanHelper.copy(em, fp, emps);

        assertEquals(emps.size(), copies.size());

        for (int index = 0; index < emps.size(); index++) {
            Employee emp = emps.get(index);
            Employee copy = copies.get(index);

            assertTrue(emp.getId() > 0);
            assertEquals(0, copy.getId());
            assertTrue(emp.getVersion() > 0);
            assertEquals(new Long(0), copy.getVersion());

            assertEquals(emp.getFirstName(), copy.getFirstName());
            assertEquals(emp.getLastName(), copy.getLastName());

            assertNotNull(emp.getGender());
            assertNull(copy.getGender());

            assertNotNull(emp.getPhoneNumbers());
            assertNull(copy.getPhoneNumbers());

            assertNotNull(emp.getAddress());
            assertNotNull(copy.getAddress());

            assertNotNull(copy.getAddress().getId());
            assertEquals(emp.getAddress().getCity(), copy.getAddress().getCity());
            assertNull(copy.getAddress().getCountry());
            assertNull(copy.getAddress().getPostalCode());
            assertNull(copy.getAddress().getProvince());
            assertNull(copy.getAddress().getStreet());
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

    @After
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

}
