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
 *     dclarke - Bug 273057: NestedFetchGroup Example
 ******************************************************************************/
package test;

import static junit.framework.Assert.*;

import java.util.*;

import javax.persistence.*;

import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.*;
import org.junit.*;

import example.Queries;

/**
 * Simple tests to verify the functionality of single level FetchGroup usage
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class SimpleNamedFetchGroupTests extends BaseFetchGroupTests {

    @Test
    public void findDefaultFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Employee emp = em.find(Employee.class, Queries.minimumEmployeeId(em));

        assertNotNull(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp);

        emp.getSalary();

        assertFetched(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertFetched(phone);
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertFetched(emp.getManager());
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void singleResultDefaultFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));

        Employee emp = (Employee) query.getSingleResult();

        assertNotNull(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp);

        emp.getSalary();

        assertFetched(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertFetched(phone);
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertFetched(emp.getManager());
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void resultListDefaultFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        assertEquals(1, emps.size());

        Employee emp = emps.get(0);

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(emp);

        emp.getSalary();

        assertFetched(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertFetched(phone);
        }
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertFetched(emp.getManager());
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }


    @Test
    public void singleResultNoFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        query.setHint(QueryHints.FETCH_GROUP, null);

        Employee emp = (Employee) query.getSingleResult();

        assertNotNull(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNotFetched(emp);
        assertFetched(emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertFetched(phone);
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void resultListNoFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        query.setHint(QueryHints.FETCH_GROUP, null);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        assertEquals(1, emps.size());

        Employee emp = emps.get(0);

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNotFetched(emp);
        assertFetched(emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertFetched(phone);
        }
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findDefaultEmptyFetchGroup() throws Exception {
        NestedFetchGroup emptyFG = new NestedFetchGroup();
        emptyFG.setAsDefault(getDescriptor("Employee"));

        EntityManager em = getEntityManager();

        Employee emp = em.find(Employee.class, Queries.minimumEmployeeId(em));

        assertNotNull(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNotFetched(emptyFG, emp);

        emp.getSalary();

        assertFetched(emptyFG, emp);
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp.getAddress());

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertFetched(phone);
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertNotFetched(emp.getManager());
        }

        assertEquals(6, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void singleResultDefaultEmptyFetchGroup() throws Exception {
        FetchGroup emptyFG = new FetchGroup();
        getDescriptor("Employee").getFetchGroupManager().setDefaultFetchGroup(emptyFG);

        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id IN (SELECT MIN(EE.id) FROM Employee ee)");

        Employee emp = (Employee) query.getSingleResult();

        assertNotNull(emp);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNotFetched(emptyFG, emp);

        emp.getSalary();

        assertFetched(emptyFG, emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertFetched(phone);
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertNotFetched(emptyFG, emp.getManager());
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void resultListDefaultEmptyFetchGroup() throws Exception {
        FetchGroup emptyFG = new FetchGroup();
        getDescriptor("Employee").getFetchGroupManager().setDefaultFetchGroup(emptyFG);

        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id IN (SELECT MIN(ee.id) FROM Employee ee)");

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        assertEquals(1, emps.size());

        Employee emp = emps.get(0);

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotFetched(emptyFG, emp);

        emp.getSalary();

        assertFetched(emptyFG, emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertFetched(phone);
        }
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertNotFetched(emptyFG, emp.getManager());
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void emptyFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        // Use q query since find will only use default fetch group
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        FetchGroup emptyFG = new FetchGroup();
        query.setHint(QueryHints.FETCH_GROUP, emptyFG);

        Employee emp = (Employee) query.getSingleResult();

        assertNotFetched(emptyFG, emp);

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNotFetched(phone);

            phone.getAreaCode();

            assertFetched(phone);
        }
    }

    @Test
    public void managerFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        // Use q query since find will only use default fetch group
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        FetchGroup managerFG = new FetchGroup();
        managerFG.addAttribute("manager");

        query.setHint(QueryHints.FETCH_GROUP, managerFG);

        assertNotNull(JpaHelper.getReadAllQuery(query).getFetchGroup());
        assertSame(managerFG, JpaHelper.getReadAllQuery(query).getFetchGroup());

        Employee emp = (Employee) query.getSingleResult();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotFetched(managerFG, emp);

        emp.getManager();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotFetched(managerFG, emp);

        emp.getLastName();

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(managerFG, emp);

        assertFetched(managerFG, emp);

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNotFetched(phone);

            phone.getAreaCode();

            assertFetched(phone);
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeNamesFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        int minId = Queries.minimumEmployeeId(em);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // Use q query since find will only use default fetch group
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", minId);

        FetchGroup namesFG = new FetchGroup();
        namesFG.addAttribute("firstName");
        namesFG.addAttribute("lastName");
        query.setHint(QueryHints.FETCH_GROUP, namesFG);

        assertNotNull(JpaHelper.getReadAllQuery(query).getFetchGroup());
        assertSame(namesFG, JpaHelper.getReadAllQuery(query).getFetchGroup());

        Employee emp = (Employee) query.getSingleResult();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotFetched(namesFG, emp);

        emp.getId();
        emp.getFirstName();
        emp.getLastName();
        emp.getVersion();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotFetched(namesFG, emp);

        emp.getGender();
        emp.getSalary();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(namesFG, emp);

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNotFetched(phone);

            phone.getAreaCode();

            assertFetched(phone);
        }
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getManager();

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(emp.getManager());
    }

    @Test
    public void namedEmptyFetchGroupUsingGetSingleResult() throws Exception {
        ClassDescriptor descriptor = JpaHelper.getServerSession(getEMF()).getClassDescriptor(Employee.class);

        FetchGroup fetchGroup = new FetchGroup("test");
        descriptor.getFetchGroupManager().addFetchGroup(fetchGroup);
        assertTrue(fetchGroup.getAttributes().isEmpty());

        assertEquals(2, descriptor.getFetchGroupManager().getFetchGroups().size());

        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        query.setHint(QueryHints.FETCH_GROUP_NAME, "test");

        Employee emp = (Employee) query.getSingleResult();
        assertNotNull(emp);

        FetchGroupTracker tracker = (FetchGroupTracker) emp;
        assertNotNull(tracker);

        FetchGroup usedFG = tracker._persistence_getFetchGroup();

        assertNotNull("No FetchGroup found on read Employee", usedFG);
        assertEquals(fetchGroup.getName(), usedFG.getName());
        assertSame(fetchGroup, usedFG);
        assertEquals(2, fetchGroup.getAttributes().size());
        assertTrue(tracker._persistence_isAttributeFetched("id"));
        assertTrue(tracker._persistence_isAttributeFetched("version"));
        assertFalse(tracker._persistence_isAttributeFetched("salary"));
        assertFalse(tracker._persistence_isAttributeFetched("firstName"));
        assertFalse(tracker._persistence_isAttributeFetched("lastName"));
    }

    @Test
    public void namedNamesFetchGroupUsingGetSingleResult() throws Exception {
        ClassDescriptor descriptor = getDescriptor("Employee");

        FetchGroup fetchGroup = new FetchGroup("names");
        fetchGroup.addAttribute("firstName");
        fetchGroup.addAttribute("lastName");

        descriptor.getFetchGroupManager().addFetchGroup(fetchGroup);
        assertEquals(2, fetchGroup.getAttributes().size());

        assertEquals(2, descriptor.getFetchGroupManager().getFetchGroups().size());

        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        query.setHint(QueryHints.FETCH_GROUP_NAME, "names");

        Employee emp = (Employee) query.getSingleResult();
        assertNotNull(emp);

        FetchGroupTracker tracker = (FetchGroupTracker) emp;
        assertNotNull(tracker);

        FetchGroup usedFG = tracker._persistence_getFetchGroup();

        assertNotNull("No FetcGroup found on read Employee", fetchGroup);
        assertEquals(fetchGroup.getName(), usedFG.getName());
        assertSame(fetchGroup, usedFG);
        assertEquals(4, fetchGroup.getAttributes().size());
        assertTrue(tracker._persistence_isAttributeFetched("id"));
        assertTrue(tracker._persistence_isAttributeFetched("version"));
        assertFalse(tracker._persistence_isAttributeFetched("salary"));
        assertTrue(tracker._persistence_isAttributeFetched("firstName"));
        assertTrue(tracker._persistence_isAttributeFetched("lastName"));
    }

    @Test
    public void joinFetchEmployeeAddressWithDynamicFetchGroup() {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address");

        FetchGroup fetchGroup = new FetchGroup("names");
        fetchGroup.addAttribute("firstName");
        fetchGroup.addAttribute("lastName");
        query.setHint(QueryHints.FETCH_GROUP, fetchGroup);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
    }

    @Test
    public void joinFetchEmployeeAddressPhoneWithDynamicFetchGroup() {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address WHERE e.id IN (SELECT p.id FROM PhoneNumber p)");

        FetchGroup fetchGroup = new FetchGroup("names");
        fetchGroup.addAttribute("firstName");
        fetchGroup.addAttribute("lastName");
        query.setHint(QueryHints.FETCH_GROUP, fetchGroup);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
    }

    private FetchGroup namedEmpFG;
    private FetchGroup namedPhoneFG;
    private FetchGroup namedAddressFG;

    @Before
    public void config() throws Exception {
        assertConfig(getEMF(), "Employee", null);
        assertConfig(getEMF(), "Address", null);
        assertConfig(getEMF(), "PhoneNumber", null);

        namedEmpFG = new FetchGroup("Employee.test");
        namedEmpFG.addAttribute("firstName");
        namedEmpFG.addAttribute("lastName");
        getDescriptor("Employee").getFetchGroupManager().addFetchGroup(namedEmpFG);

        namedPhoneFG = new FetchGroup("Phone.test");
        namedPhoneFG.addAttribute("number");
        getDescriptor("PhoneNumber").getFetchGroupManager().addFetchGroup(namedPhoneFG);

        namedAddressFG = new FetchGroup("Address.test");
        namedAddressFG.addAttribute("city");
        getDescriptor("Address").getFetchGroupManager().addFetchGroup(namedAddressFG);

        assertConfig(getEMF(), "Employee", null, 1);
        assertConfig(getEMF(), "Address", null, 1);
        assertConfig(getEMF(), "PhoneNumber", null, 1);

        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    @After
    public void resetFetchGroups() throws Exception {
        ClassDescriptor descriptor = getDescriptor("Employee");
        if (descriptor.getFetchGroupManager() != null) {
            descriptor.getFetchGroupManager().setDefaultFetchGroup(null);
            descriptor.getFetchGroupManager().getFetchGroups().clear();
        }

        descriptor = getDescriptor("Address");
        if (descriptor.getFetchGroupManager() != null) {
            descriptor.getFetchGroupManager().setDefaultFetchGroup(null);
            descriptor.getFetchGroupManager().getFetchGroups().clear();
        }
        descriptor = getDescriptor("PhoneNumber");
        if (descriptor.getFetchGroupManager() != null) {
            descriptor.getFetchGroupManager().setDefaultFetchGroup(null);
            descriptor.getFetchGroupManager().getFetchGroups().clear();
        }
    }

}
