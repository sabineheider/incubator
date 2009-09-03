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
package test.nestedfetchgroup;

import static junit.framework.Assert.*;

import java.util.*;

import javax.persistence.*;

import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.queries.*;

import testing.EclipseLinkJPATest;
import example.Queries;

/**
 * Simple tests to verify the functionality of single level FetchGroup usage
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public abstract class BaseFetchGroupTests extends EclipseLinkJPATest {

    @Override
    protected EntityManagerFactory createEMF(String unitName, Map properties) {
        EntityManagerFactory emf = super.createEMF(unitName, properties);

        new NestedFetchGroup.Customizer().customize(JpaHelper.getServerSession(emf));
        return emf;
    }

    protected void findNoFetchGroup(EntityManager em) throws Exception {
        Employee emp = findMinimumEmployee(em);

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp);
        assertFetched(emp.getAddress());

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp.getManager());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertFetched(phone);
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    public void singleResultNoFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));

        Employee emp = (Employee) query.getSingleResult();

        assertNotNull(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp);
        assertFetched(emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertFetched(phone);
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    public void resultListNoFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        assertEquals(1, emps.size());

        Employee emp = emps.get(0);

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetched(emp);
        assertFetched(emp.getAddress());

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertFetched(phone);
        }
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }


    public void singleResultDefaultEmptyFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        FetchGroup emptyFG = new FetchGroup();
        query.setHint(QueryHints.FETCH_GROUP, emptyFG);

        Employee emp = (Employee) query.getSingleResult();

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
    }

    public void resultListDefaultEmptyFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        FetchGroup emptyFG = new FetchGroup();
        query.setHint(QueryHints.FETCH_GROUP, emptyFG);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        assertEquals(1, emps.size());

        Employee emp = emps.get(0);

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
    }

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
        if (emp.getManager() != null) {
            assertFetched(emp.getManager());
        }

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotFetched(managerFG, emp);

        emp.getLastName();

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(managerFG, emp);

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNotFetched(phone);

            phone.getAreaCode();

            assertFetched(phone);
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

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

    protected Employee findMinimumEmployee(EntityManager em) {
        List<Employee> emps = em.createQuery("SELECT e FROM Employee e WHERE e.id in (SELECT MIN(ee.id) FROM Employee ee)").getResultList();

        assertNotNull("Null returned for min employee query", emps);
        assertEquals("No results returned for Mmin employee query", 1, emps.size());

        return emps.get(0);
    }

    public void assertFetched(FetchGroup fetchGroup, Object entity) {
        ClassDescriptor descriptor = getDescriptor(entity);
        FetchGroupTracker tracker = (FetchGroupTracker) entity;

        if (tracker._persistence_getFetchGroup() != null && fetchGroup != null) {
            Iterator<DatabaseMapping> descI = descriptor.getMappings().iterator();
            while (descI.hasNext()) {
                DatabaseMapping mapping = descI.next();
                assertTrue("Attribute not fetched: " + mapping, tracker._persistence_isAttributeFetched(mapping.getAttributeName()));
            }
        } 
    }

    public void assertHasFetchGroup(Object entity) {
        assertNotNull("Entity is null", entity);
        assertTrue("Entity does not implement FetchGroupTracker", entity instanceof FetchGroupTracker);
        assertNotNull("Entity does not have FetchGroup", ((FetchGroupTracker) entity)._persistence_getFetchGroup());
    }

    public void assertFetched(Object entity) {
        ClassDescriptor descriptor = getDescriptor(entity);

        if (descriptor != null) {
            FetchGroup fetchGroup = descriptor.getFetchGroupManager().getDefaultFetchGroup();

            assertFetched(fetchGroup, entity);
        }
    }

    public void assertNotFetched(FetchGroup fetchGroup, Object entity) {
        ClassDescriptor descriptor = getDescriptor(entity);
        FetchGroupTracker tracker = (FetchGroupTracker) entity;

        if (tracker._persistence_getFetchGroup() == null) {
            toString();
        }
        assertNotNull("No FetchGroup found on " + entity, tracker._persistence_getFetchGroup());

        Iterator<DatabaseMapping> descI = descriptor.getMappings().iterator();
        while (descI.hasNext()) {
            DatabaseMapping mapping = descI.next();

            if (tracker._persistence_getFetchGroup().getAttributes().contains(mapping.getAttributeName())) {
                assertTrue(tracker._persistence_isAttributeFetched(mapping.getAttributeName()));
            } else {
                assertFalse(tracker._persistence_isAttributeFetched(mapping.getAttributeName()));
            }
        }
    }

    public void assertNotFetched(Object entity) {
        ClassDescriptor descriptor = getDescriptor(entity);

        if (descriptor != null) {
            FetchGroup fetchGroup = descriptor.getFetchGroupManager().getDefaultFetchGroup();
            assertNotNull("No default FetchGroup for: " + descriptor, fetchGroup);
            assertNotFetched(fetchGroup, entity);
        }
    }

    public ClassDescriptor getDescriptor(Object entity) {
        assertNotNull("Entity is null", entity);
        
        if (entity instanceof String) {
        	return JpaHelper.getServerSession(getEMF()).getClassDescriptorForAlias((String) entity);
        }
        
        assertTrue("Entity "+ entity +" does not implement FetchGroupTracker", entity instanceof FetchGroupTracker);

        FetchGroupTracker tracker = (FetchGroupTracker) entity;

        if (tracker._persistence_getSession() != null) {
            ClassDescriptor descriptor = tracker._persistence_getSession().getClassDescriptor(entity);
            assertNotNull("No descriptor found for: " + entity.getClass(), descriptor);
            return descriptor;
        }

        return null;
    }

    public void assertConfig(EntityManagerFactory emf, String entityName, FetchGroup defaultFetchGroup) throws Exception {
        assertConfig(emf, entityName, defaultFetchGroup, 0);
    }

    public void assertConfig(EntityManagerFactory emf, String entityName, FetchGroup defaultFetchGroup, int numNamedFetchGroups) throws Exception {
        ClassDescriptor descriptor = JpaHelper.getServerSession(emf).getClassDescriptorForAlias(entityName);
        assertNotNull("Not descriptor found for: " + entityName, descriptor);

        assertTrue("FetchGroupTracker not implemented by: " + entityName, FetchGroupTracker.class.isAssignableFrom(descriptor.getJavaClass()));

        if (defaultFetchGroup == null) {
            assertNull("Default FetchGroup not null: " + entityName, descriptor.getFetchGroupManager().getDefaultFetchGroup());
        } else {
            assertEquals("Default FetchGroup does not match", defaultFetchGroup, descriptor.getFetchGroupManager().getDefaultFetchGroup());
        }

        assertEquals("Incorrect number of Named FetchGroups: " + entityName, numNamedFetchGroups, descriptor.getFetchGroupManager().getFetchGroups().size());
    }

}
