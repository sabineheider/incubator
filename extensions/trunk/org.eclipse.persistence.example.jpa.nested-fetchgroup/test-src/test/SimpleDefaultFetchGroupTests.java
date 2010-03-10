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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static test.FetchGroupAssert.assertDefaultFetched;
import static test.FetchGroupAssert.assertFetched;
import static test.FetchGroupAssert.assertFetchedAttribute;
import static test.FetchGroupAssert.assertNoFetchGroup;
import static test.FetchGroupAssert.assertNotFetchedAttribute;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.FetchGroupManager;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.junit.Before;
import org.junit.Test;

import example.Queries;

/**
 * Simple tests to verify the functionality of single level FetchGroup usage
 * 
 * @author dclarke
 * @since EclipseLink 2.1
 */
public class SimpleDefaultFetchGroupTests extends BaseFetchGroupTests {

    @Test
    public void findDefaultFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Employee emp = Queries.minimumEmployee(em);

        assertNotNull(emp);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertDefaultFetched(getEMF(), emp);

        assertNotFetchedAttribute(getEMF(), emp, "salary");
        emp.getSalary();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetchedAttribute(getEMF(), emp, "salary");

        assertNoFetchGroup(getEMF(), emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertDefaultFetched(getEMF(), phone);
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertDefaultFetched(getEMF(), emp.getManager());
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

        assertDefaultFetched(getEMF(), emp);

        emp.getSalary();

        assertFetchedAttribute(getEMF(), emp, "salary");
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNoFetchGroup(getEMF(), emp.getAddress());

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertDefaultFetched(getEMF(), phone);
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertDefaultFetched(getEMF(), emp.getManager());
        }

        assertEquals(6, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
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
        assertDefaultFetched(getEMF(), emp);

        emp.getSalary();

        assertNoFetchGroup(getEMF(), emp);
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNoFetchGroup(getEMF(), emp.getAddress());

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertDefaultFetched(getEMF(), phone);
        }
        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertDefaultFetched(getEMF(), emp.getManager());
        }

        assertEquals(6, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void resultListWithJoinFetchAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        assertEquals(1, emps.size());

        Employee emp = emps.get(0);

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertDefaultFetched(getEMF(), emp);

        emp.getSalary();

        assertNoFetchGroup(getEMF(), emp);
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNoFetchGroup(getEMF(), emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertDefaultFetched(getEMF(), phone);
        }
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertDefaultFetched(getEMF(), emp.getManager());
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void singleResultNoFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));

        assertNull(JpaHelper.getReadAllQuery(query).getFetchGroup());
        assertNotNull(getDescriptor("Employee").getFetchGroupManager().getFetchGroup(null, true));

        query.setHint(QueryHints.FETCH_GROUP, null);
        assertNull(JpaHelper.getReadAllQuery(query).getFetchGroup());

        Employee emp = (Employee) query.getSingleResult();

        assertNotNull(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);
        assertNoFetchGroup(getEMF(), emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertDefaultFetched(getEMF(), phone);
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

        assertNoFetchGroup(getEMF(), emp);
        assertNoFetchGroup(getEMF(), emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertDefaultFetched(getEMF(), phone);
        }
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void emptyFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        // Use q query since find will only use default fetch group
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        FetchGroup emptyFG = new FetchGroup("empty@" + System.currentTimeMillis());
        query.setHint(QueryHints.FETCH_GROUP, emptyFG);

        assertEquals(emptyFG, JpaHelper.getReadAllQuery(query).getFetchGroup());

        Employee emp = (Employee) query.getSingleResult();

        assertFetched(getEMF(), emp, emptyFG);

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertDefaultFetched(getEMF(), phone);
            phone.getAreaCode();
            assertNoFetchGroup(getEMF(), phone);
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
        query.setHint(QueryHints.LEFT_FETCH, "e.manager");

        assertEquals(managerFG, JpaHelper.getReadAllQuery(query).getFetchGroup());

        Employee emp = (Employee) query.getSingleResult();

        assertFetched(getEMF(), emp, managerFG);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getManager();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(getEMF(), emp, managerFG);

        emp.getLastName();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertDefaultFetched(getEMF(), phone);

            phone.getAreaCode();

            assertNoFetchGroup(getEMF(), phone);
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
        assertFetched(getEMF(), emp, namesFG);

        emp.getId();
        emp.getFirstName();
        emp.getLastName();
        emp.getVersion();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(getEMF(), emp, namesFG);

        emp.getGender();
        emp.getSalary();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertDefaultFetched(getEMF(), phone);

            phone.getAreaCode();

            assertNoFetchGroup(getEMF(), phone);
        }
        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getManager();

        assertEquals(6, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertDefaultFetched(getEMF(), emp.getManager());
    }

    @Test
    public void namedEmptyFetchGroupUsingGetSingleResult() throws Exception {
        ClassDescriptor descriptor = JpaHelper.getServerSession(getEMF()).getClassDescriptor(Employee.class);

        FetchGroup fetchGroup = new FetchGroup("test");
        descriptor.getFetchGroupManager().addFetchGroup(fetchGroup);
        assertTrue(fetchGroup.getFetchItems().isEmpty());
        assertEquals(1, descriptor.getFetchGroupManager().getFetchGroups().size());

        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        query.setHint(QueryHints.FETCH_GROUP_NAME, "test");

        Employee emp = (Employee) query.getSingleResult();
        assertNotNull(emp);
        assertFetched(getEMF(), emp, "test");
    }

    @Test
    public void namedNamesFetchGroupUsingGetSingleResult() throws Exception {
        ClassDescriptor descriptor = getDescriptor("Employee");

        FetchGroup fetchGroup = new FetchGroup("names");
        fetchGroup.addAttribute("firstName");
        fetchGroup.addAttribute("lastName");

        descriptor.getFetchGroupManager().addFetchGroup(fetchGroup);
        assertEquals(2, fetchGroup.getFetchItems().size());

        assertEquals(1, descriptor.getFetchGroupManager().getFetchGroups().size());

        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        query.setHint(QueryHints.FETCH_GROUP_NAME, "names");

        Employee emp = (Employee) query.getSingleResult();
        assertNotNull(emp);

        FetchGroupTracker tracker = (FetchGroupTracker) emp;
        assertNotNull(tracker);

        FetchGroup usedFG = tracker._persistence_getFetchGroup();

        assertNotNull("No FetchGroup found on read Employee", fetchGroup);
        assertEquals(fetchGroup.getName(), usedFG.getName());
        assertSame(fetchGroup, usedFG);
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

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotNull(emps);
        for (Employee emp : emps) {
            assertFetched(getEMF(), emp, fetchGroup);
        }
    }

    private FetchGroup<Employee> defaultEmpFG = null;
    FetchGroup<PhoneNumber> defaultPhoneFG = null;

    /**
     * Verify the configuration made in the customizers and clear the cache. Any
     * FetchGroups setup in test cases will be removed.
     * 
     * @see EmployeeCustomizer
     * @see PhoneCustomizer
     */
    @Before
    public void config() throws Exception {
        getDescriptor("Employee").getFetchGroupManager().getFetchGroups().clear();
        getDescriptor("PhoneNumber").getFetchGroupManager().getFetchGroups().clear();

        defaultEmpFG = getDescriptor("Employee").getDefaultFetchGroup();
        defaultPhoneFG = getDescriptor("PhoneNumber").getDefaultFetchGroup();

        assertConfig(getEMF(), "Employee", defaultEmpFG, 0);
        assertConfig(getEMF(), "Address", null, 0);
        assertConfig(getEMF(), "PhoneNumber", defaultPhoneFG, 0);

        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    @Override
    protected Map getEMFProperties() {
        Map properties = super.getEMFProperties();
        properties.put(PersistenceUnitProperties.DESCRIPTOR_CUSTOMIZER_ + "Employee", EmployeeCustomizer.class.getName());
        properties.put(PersistenceUnitProperties.DESCRIPTOR_CUSTOMIZER_ + "PhoneNumber", PhoneCustomizer.class.getName());
        return properties;
    }

    public static class EmployeeCustomizer implements DescriptorCustomizer {

        public void customize(ClassDescriptor descriptor) throws Exception {
            FetchGroup fg = new FetchGroup("Employee-default");
            fg.addAttribute("firstName");
            fg.addAttribute("lastName");
            if (!descriptor.hasFetchGroupManager()) {
                descriptor.setFetchGroupManager(new FetchGroupManager());
            }
            descriptor.getFetchGroupManager().setDefaultFetchGroup(fg);
        }

    }

    public static class PhoneCustomizer implements DescriptorCustomizer {

        public void customize(ClassDescriptor descriptor) throws Exception {
            FetchGroup fg = new FetchGroup("Phone-default");
            fg.addAttribute("number");
            if (!descriptor.hasFetchGroupManager()) {
                descriptor.setFetchGroupManager(new FetchGroupManager());
            }
            descriptor.getFetchGroupManager().setDefaultFetchGroup(fg);
        }

    }
}
