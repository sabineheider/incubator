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
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroup;
import org.junit.Before;
import org.junit.Test;

import example.Queries;

/**
 * Simple tests to verify the functionality of single level FetchGroup usage
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class SimpleFetchGroupTests extends BaseFetchGroupTests {

    @Test
    public void findNoFetchGroup() throws Exception {
        super.findNoFetchGroup(getEntityManager());
    }

    @Test
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

    @Test
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


    @Test
    public void singleResultDefaultEmptyFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        FetchGroup emptyFG = new FetchGroup();
        query.setHint(QueryHints.FETCH_GROUP, emptyFG);

        Employee emp = (Employee) query.getSingleResult();

        assertNotNull(emp);
        assertNotFetched(emptyFG, emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

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

    @Test
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

    @Before
    public void config() throws Exception {
        assertConfig(getEMF(), "Employee", null);
        assertConfig(getEMF(), "Address", null);
        assertConfig(getEMF(), "PhoneNumber", null);

        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

}
