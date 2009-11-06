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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import junit.framework.Assert;
import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.junit.Before;
import org.junit.Test;

import testing.EclipseLinkJPAAssert;
import testing.EclipseLinkJPATest;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class BasicFetchPlanTests extends EclipseLinkJPATest {

    @Test
    public void employeeAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
        int expectedSelects = (emps.size() * 2) + 1;
        Assert.assertEquals(expectedSelects, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        for (Employee emp: emps) {
            emp.getAddress();
            for (PhoneNumber phone: emp.getPhoneNumbers()) {
                phone.getOwner();
            }
        }
        Assert.assertEquals(expectedSelects, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeAddressPhones_Batching() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");
        
        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
        int expectedSelects = 3;
        Assert.assertEquals(expectedSelects, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        for (Employee emp: emps) {
            emp.getAddress();
            for (PhoneNumber phone: emp.getPhoneNumbers()) {
                phone.getOwner();
            }
        }
        Assert.assertEquals(expectedSelects, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeAddressPhones_Joining() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");
        
        query.setHint(QueryHints.FETCH, "e.address");
        query.setHint(QueryHints.FETCH, "e.phoneNumbers");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
        int expectedSelects = 1;
        Assert.assertEquals(expectedSelects, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        for (Employee emp: emps) {
            emp.getAddress();
            for (PhoneNumber phone: emp.getPhoneNumbers()) {
                phone.getOwner();
            }
        }
        Assert.assertEquals(expectedSelects, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void managerAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void responsibilities() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.responsibilities");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void responsibilitiesBatch() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");
        query.setHint(QueryHints.FETCH, "e.responsibilities");
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.responsibilities");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void employeeAddress_ReturnBoth() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, e.address FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void readAllEmployee() throws Exception {
        EntityManager em = getEntityManager();

        ReadAllQuery raq = new ReadAllQuery(Employee.class);

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(raq);
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        Query query = JpaHelper.createQuery(raq, em);
        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }
    
    @Before
    public void verifyConfig() {
        EclipseLinkJPAAssert.assertIsWoven(getEMF(), "Employee");
        EclipseLinkJPAAssert.assertIsWoven(getEMF(), "PhoneNumber");
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
        getQuerySQLTracker(getEMF()).reset();
    }
}
