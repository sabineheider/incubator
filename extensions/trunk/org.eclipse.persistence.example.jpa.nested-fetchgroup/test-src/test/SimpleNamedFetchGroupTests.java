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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static test.FetchGroupAssert.assertFetched;
import static test.FetchGroupAssert.assertNoFetchGroup;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import model.Address;
import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.EntityFetchGroup;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

        Employee emp = Queries.minimumEmployee(em);

        assertNotNull(emp);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        emp.getSalary();

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNoFetchGroup(getEMF(), emp.getAddress());
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
        }

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertNoFetchGroup(getEMF(), emp.getManager());
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void singleResultDefaultFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));

        Employee emp = (Employee) query.getSingleResult();

        assertNotNull(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        emp.getSalary();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNoFetchGroup(getEMF(), emp.getAddress());
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertNoFetchGroup(getEMF(), emp.getManager());
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

        assertNotNull(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        emp.getSalary();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNoFetchGroup(getEMF(), emp.getAddress());
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertNoFetchGroup(getEMF(), emp.getManager());
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
        assertNoFetchGroup(getEMF(), emp);

        emp.getSalary();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNoFetchGroup(getEMF(), emp.getAddress());
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertNoFetchGroup(getEMF(), emp.getManager());
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
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

        assertNotNull(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        emp.getSalary();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNoFetchGroup(getEMF(), emp.getAddress());
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertNoFetchGroup(getEMF(), emp.getManager());
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
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

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(getEMF(), emp, managerFG);

        emp.getManager();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(getEMF(), emp, managerFG);

        emp.getLastName();

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void namedEmptyFetchGroupUsingGetSingleResult() throws Exception {
        ClassDescriptor descriptor = JpaHelper.getServerSession(getEMF()).getClassDescriptor(Employee.class);

        FetchGroup fetchGroup = new FetchGroup("test");
        descriptor.getFetchGroupManager().addFetchGroup(fetchGroup);
        assertTrue(fetchGroup.getFetchItems().isEmpty());

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
        assertSame(fetchGroup, ((EntityFetchGroup<?>) usedFG).getParent());
        assertEquals(2, fetchGroup.getFetchItems().size());
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
        assertEquals(2, fetchGroup.getFetchItems().size());

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
        assertSame(fetchGroup, ((EntityFetchGroup<?>) usedFG).getParent());
        assertEquals(4, fetchGroup.getFetchItems().size());
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

    /**
     * 
     */
    @Before
    public void config() throws Exception {
        assertConfig(getEMF(), "Employee", null, 0);
        assertConfig(getEMF(), "Address", null, 0);
        assertConfig(getEMF(), "PhoneNumber", null, 0);

        FetchGroup<Employee> namedEmpFG = new FetchGroup<Employee>("Employee.test");
        namedEmpFG.addAttribute("firstName");
        namedEmpFG.addAttribute("lastName");
        getDescriptor("Employee").getFetchGroupManager().addFetchGroup(namedEmpFG);

        FetchGroup<PhoneNumber> namedPhoneFG = new FetchGroup<PhoneNumber>("Phone.test");
        namedPhoneFG.addAttribute("number");
        getDescriptor("PhoneNumber").getFetchGroupManager().addFetchGroup(namedPhoneFG);

        FetchGroup<Address> namedAddressFG = new FetchGroup<Address>("Address.test");
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
