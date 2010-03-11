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
 *     dclarke - Bug 273057: FetchGroup Example
 ******************************************************************************/
package test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static test.FetchGroupAssert.assertFetched;
import static test.FetchGroupAssert.assertNotFetchedAttribute;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import model.Address;
import model.Employee;
import model.Gender;
import model.PhoneNumber;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.FetchGroupManager;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.queries.FetchGroup.FetchItem;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.Before;
import org.junit.Test;

public class NestedNamedFetchGroupTests extends BaseFetchGroupTests {

    @Test
    public void dynamicFetchGroup_EmployeeAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        query.setParameter("GENDER", Gender.Male);

        // Define the fields to be fetched on Employee
        FetchGroup fg = new FetchGroup();
        fg.addAttribute("firstName");
        fg.addAttribute("lastName");
        fg.addAttribute("address");
        fg.addAttribute("address.city");
        fg.addAttribute("address.postalCode");

        // Configure the dynamic FetchGroup
        query.setHint(QueryHints.FETCH_GROUP, fg);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        for (Employee emp : emps) {
            FetchGroupTracker tracker = (FetchGroupTracker) emp;

            assertNotNull(tracker._persistence_getFetchGroup());

            // Verify specified fields plus mandatory ones are loaded
            assertTrue(tracker._persistence_isAttributeFetched("id"));
            assertTrue(tracker._persistence_isAttributeFetched("firstName"));
            assertTrue(tracker._persistence_isAttributeFetched("lastName"));
            assertTrue(tracker._persistence_isAttributeFetched("version"));

            // Verify the other fields are not loaded
            assertFalse(tracker._persistence_isAttributeFetched("salary"));
            assertFalse(tracker._persistence_isAttributeFetched("startTime"));
            assertFalse(tracker._persistence_isAttributeFetched("endTime"));

            // Force the loading of lazy fields and verify
            emp.getSalary();

            assertTrue(tracker._persistence_isAttributeFetched("salary"));
            assertTrue(tracker._persistence_isAttributeFetched("startTime"));
            assertTrue(tracker._persistence_isAttributeFetched("endTime"));

            // Now we'll check the address uses the provided dynamic fetch-group
            FetchGroupTracker addrTracker = (FetchGroupTracker) emp.getAddress();
            assertNotNull("Address does not have a FetchGroup", addrTracker._persistence_getFetchGroup());
            assertTrue(addrTracker._persistence_isAttributeFetched("city"));
            assertTrue(addrTracker._persistence_isAttributeFetched("postalCode"));
            assertFalse(addrTracker._persistence_isAttributeFetched("street"));
            assertFalse(addrTracker._persistence_isAttributeFetched("country"));

            // Now we'll check the phoneNumbers use of the default fetch group
            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                FetchGroupTracker phoneTracker = (FetchGroupTracker) phone;
                assertNotNull("PhoneNumber does not have a FetchGroup", phoneTracker._persistence_getFetchGroup());
                assertTrue(phoneTracker._persistence_isAttributeFetched("number"));
                assertFalse(phoneTracker._persistence_isAttributeFetched("areaCode"));
            }
        }
    }

    @Test
    public void dynamicFetchGroup_Employee_NullAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        query.setParameter("GENDER", Gender.Male);

        // Define the fields to be fetched on Employee
        FetchGroup empGroup = new FetchGroup();
        empGroup.addAttribute("firstName");
        empGroup.addAttribute("lastName");
        empGroup.addAttribute("address");

        // Define the fields to be fetched on Address
        FetchGroup addressGroup = new FetchGroup();
        addressGroup.addAttribute("city");
        addressGroup.addAttribute("postalCode");

        empGroup.addAttribute("address", null);

        // Configure the dynamic FetchGroup
        query.setHint(QueryHints.FETCH_GROUP, empGroup);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        for (Employee emp : emps) {
            FetchGroupTracker tracker = (FetchGroupTracker) emp;

            assertNotNull(tracker._persistence_getFetchGroup());

            // Verify specified fields plus mandatory ones are loaded
            assertTrue(tracker._persistence_isAttributeFetched("id"));
            assertTrue(tracker._persistence_isAttributeFetched("firstName"));
            assertTrue(tracker._persistence_isAttributeFetched("lastName"));
            assertTrue(tracker._persistence_isAttributeFetched("version"));

            // Verify the other fields are not loaded
            assertFalse(tracker._persistence_isAttributeFetched("salary"));
            assertFalse(tracker._persistence_isAttributeFetched("startTime"));
            assertFalse(tracker._persistence_isAttributeFetched("endTime"));

            // Force the loading of lazy fields and verify
            emp.getSalary();

            assertTrue(tracker._persistence_isAttributeFetched("salary"));
            assertTrue(tracker._persistence_isAttributeFetched("startTime"));
            assertTrue(tracker._persistence_isAttributeFetched("endTime"));

            // Now we'll check the address uses the provided dynamic fetch-group
            FetchGroupTracker addrTracker = (FetchGroupTracker) emp.getAddress();
            assertNull("Address has an unexpected FetchGroup", addrTracker._persistence_getFetchGroup());

            // Now we'll check the phoneNumbers use of the default fetch group
            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                FetchGroupTracker phoneTracker = (FetchGroupTracker) phone;
                assertNotNull("PhoneNumber does not have a FetchGroup", phoneTracker._persistence_getFetchGroup());
                assertTrue(phoneTracker._persistence_isAttributeFetched("number"));
                assertFalse(phoneTracker._persistence_isAttributeFetched("areaCode"));
            }
        }
    }

    @Test
    public void dynamicFetchGroup_EmployeeAddressNullPhone() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        query.setParameter("GENDER", Gender.Male);

        // Define the fields to be fetched on Employee
        FetchGroup empGroup = new FetchGroup();
        empGroup.addAttribute("firstName");
        empGroup.addAttribute("lastName");
        empGroup.addAttribute("address");

        // Define the fields to be fetched on Address
        FetchGroup addressGroup = new FetchGroup();
        addressGroup.addAttribute("city");
        addressGroup.addAttribute("postalCode");

        empGroup.addAttribute("address", addressGroup);
        empGroup.addAttribute("phoneNumbers").setUseDefaultFetchGroup(false);

        // Configure the dynamic FetchGroup
        query.setHint(QueryHints.FETCH_GROUP, empGroup);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        for (Employee emp : emps) {
            FetchGroupTracker tracker = (FetchGroupTracker) emp;

            assertNotNull(tracker._persistence_getFetchGroup());

            // Verify specified fields plus mandatory ones are loaded
            assertTrue(tracker._persistence_isAttributeFetched("id"));
            assertTrue(tracker._persistence_isAttributeFetched("firstName"));
            assertTrue(tracker._persistence_isAttributeFetched("lastName"));
            assertTrue(tracker._persistence_isAttributeFetched("version"));

            // Verify the other fields are not loaded
            assertFalse(tracker._persistence_isAttributeFetched("salary"));
            assertFalse(tracker._persistence_isAttributeFetched("startTime"));
            assertFalse(tracker._persistence_isAttributeFetched("endTime"));

            // Force the loading of lazy fields and verify
            emp.getSalary();

            assertTrue(tracker._persistence_isAttributeFetched("salary"));
            assertTrue(tracker._persistence_isAttributeFetched("startTime"));
            assertTrue(tracker._persistence_isAttributeFetched("endTime"));

            // Now we'll check the address uses the provided dynamic fetch-group
            FetchGroupTracker addrTracker = (FetchGroupTracker) emp.getAddress();
            assertNotNull("Address does not have a FetchGroup", addrTracker._persistence_getFetchGroup());
            assertTrue(addrTracker._persistence_isAttributeFetched("city"));
            assertTrue(addrTracker._persistence_isAttributeFetched("postalCode"));
            assertFalse(addrTracker._persistence_isAttributeFetched("street"));
            assertFalse(addrTracker._persistence_isAttributeFetched("country"));

            // Now we'll check the phoneNumbers use of the default fetch group
            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                FetchGroupTracker phoneTracker = (FetchGroupTracker) phone;
                assertNull("PhoneNumber has a FetchGroup", phoneTracker._persistence_getFetchGroup());
            }
        }
    }

    @Test
    public void dynamicFetchGroup_EmployeeAddressEmptyPhone() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        query.setParameter("GENDER", Gender.Male);

        // Define the fields to be fetched on Employee
        FetchGroup empGroup = new FetchGroup();
        empGroup.addAttribute("firstName");
        empGroup.addAttribute("lastName");
        empGroup.addAttribute("address");

        // Define the fields to be fetched on Address
        FetchGroup addressGroup = new FetchGroup();
        addressGroup.addAttribute("city");
        addressGroup.addAttribute("postalCode");

        empGroup.addAttribute("address", addressGroup);
        empGroup.addAttribute("phoneNumbers", new FetchGroup());

        // Configure the dynamic FetchGroup
        query.setHint(QueryHints.FETCH_GROUP, empGroup);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        for (Employee emp : emps) {
            FetchGroupTracker tracker = (FetchGroupTracker) emp;

            assertNotNull(tracker._persistence_getFetchGroup());

            // Verify specified fields plus mandatory ones are loaded
            assertTrue(tracker._persistence_isAttributeFetched("id"));
            assertTrue(tracker._persistence_isAttributeFetched("firstName"));
            assertTrue(tracker._persistence_isAttributeFetched("lastName"));
            assertTrue(tracker._persistence_isAttributeFetched("version"));

            // Verify the other fields are not loaded
            assertFalse(tracker._persistence_isAttributeFetched("salary"));
            assertFalse(tracker._persistence_isAttributeFetched("startTime"));
            assertFalse(tracker._persistence_isAttributeFetched("endTime"));

            // Force the loading of lazy fields and verify
            emp.getSalary();

            assertTrue(tracker._persistence_isAttributeFetched("salary"));
            assertTrue(tracker._persistence_isAttributeFetched("startTime"));
            assertTrue(tracker._persistence_isAttributeFetched("endTime"));

            // Now we'll check the address uses the provided dynamic fetch-group
            FetchGroupTracker addrTracker = (FetchGroupTracker) emp.getAddress();
            assertNotNull("Address does not have a FetchGroup", addrTracker._persistence_getFetchGroup());
            assertTrue(addrTracker._persistence_isAttributeFetched("city"));
            assertTrue(addrTracker._persistence_isAttributeFetched("postalCode"));
            assertFalse(addrTracker._persistence_isAttributeFetched("street"));
            assertFalse(addrTracker._persistence_isAttributeFetched("country"));

            // Now we'll check the phoneNumbers use of the default fetch group
            for (PhoneNumber phone : emp.getPhoneNumbers()) {
                FetchGroupTracker phoneTracker = (FetchGroupTracker) phone;
                assertNotNull("PhoneNumber does not have a FetchGroup", phoneTracker._persistence_getFetchGroup());
                assertFalse(phoneTracker._persistence_isAttributeFetched("number"));
                assertFalse(phoneTracker._persistence_isAttributeFetched("areaCode"));

                phone.getNumber();

                assertTrue(phoneTracker._persistence_isAttributeFetched("number"));
                assertTrue(phoneTracker._persistence_isAttributeFetched("areaCode"));
            }
        }
    }

    @Test
    public void dynamicHierarchicalFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        query.setParameter("GENDER", Gender.Male);

        // Define the fields to be fetched on Employee
        FetchGroup fg = new FetchGroup();
        fg.addAttribute("firstName");
        fg.addAttribute("lastName");
        fg.addAttribute("manager.firstName");
        fg.addAttribute("manager.salary");
        fg.addAttribute("manager.manager.gender");

        FetchItem mgrItem = fg.getFetchItem("manager");
        assertNotNull(mgrItem);
        assertNotNull(mgrItem.getFetchGroup());
        FetchItem mgrMgrItem = fg.getFetchItem("manager.manager");
        assertNotNull(mgrMgrItem);
        assertNotNull(mgrMgrItem.getFetchGroup());

        query.setHint(QueryHints.FETCH_GROUP, fg);

        List<Employee> emps = query.getResultList();

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        List<Employee> loadedEmps = new ArrayList<Employee>();
        loadedEmps.addAll(emps);

        for (Employee emp : emps) {
            if (!loadedEmps.contains(emp)) {
                assertFetched(getEMF(), emp, fg);
            }

            assertNotFetchedAttribute(getEMF(), emp, "startDate");
            loadedEmps.add(emp);
        }
        // TODO assertEquals(1 + loadedEmps.size() - emps.size(), numSelect);
    }

    @Before
    public void initialize() {
        Server session = JpaHelper.getServerSession(getEMF());
        session.getIdentityMapAccessor().initializeAllIdentityMaps();

        assertTrue("Employee not FetchGroup enabled", FetchGroupTracker.class.isAssignableFrom(Employee.class));
        assertTrue("Address not FetchGroup enabled", FetchGroupTracker.class.isAssignableFrom(Address.class));
        assertTrue("PhoneNumber not FetchGroup enabled", FetchGroupTracker.class.isAssignableFrom(PhoneNumber.class));

        ClassDescriptor descriptor = session.getClassDescriptor(Employee.class);
        descriptor.getFetchGroupManager().setDefaultFetchGroup(null);
        descriptor.getFetchGroupManager().getFetchGroups().clear();

        assertNull(descriptor.getDefaultFetchGroup());
        assertTrue(descriptor.getFetchGroupManager().getFetchGroups().isEmpty());

        // We'll put a default FetchGroup on Phone
        FetchGroup phoneFG = new FetchGroup();
        phoneFG.addAttribute("number");
        ClassDescriptor phoneDescriptor = session.getClassDescriptor(PhoneNumber.class);
        phoneDescriptor.getFetchGroupManager().setDefaultFetchGroup(phoneFG);

        descriptor = session.getClassDescriptor(PhoneNumber.class);
        assertNotNull(descriptor.getDefaultFetchGroup());
        assertTrue(descriptor.getFetchGroupManager().getFetchGroups().isEmpty());

        descriptor = session.getClassDescriptor(Address.class);
        assertNull(descriptor.getDefaultFetchGroup());
        assertTrue(descriptor.getFetchGroupManager().getFetchGroups().isEmpty());
    }

}
