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
 *     dclarke - FetchPlan Extension incubator
 ******************************************************************************/
package test.fetchplan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import model.Employee;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.FetchPlanHelper;
import org.eclipse.persistence.internal.helper.SerializationHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.junit.Before;
import org.junit.Test;

import testing.EclipseLinkJPAAssert;
import testing.EclipseLinkJPATest;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class SerializedResultsTests extends EclipseLinkJPATest {

    @Test
    public void employeeAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void employeeAddressPhones_Batching() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void employeeAddressPhones_Joining() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        query.setHint(QueryHints.FETCH, "e.address");
        query.setHint(QueryHints.FETCH, "e.phoneNumbers");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void managerAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void responsibilities() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.responsibilities");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void responsibilitiesBatch() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");
        query.setHint(QueryHints.BATCH, "e.responsibilities");
        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.responsibilities");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void employeeAddress_ReturnBoth() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, e.address FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void managedEmployeesAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlanHelper.create(query);
        fetchPlan.addFetchItem("e.managedEmployees.address");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void readAllEmployee() throws Exception {
        EntityManager em = getEntityManager();

        ReadAllQuery raq = new ReadAllQuery(Employee.class);

        FetchPlan fetchPlan = FetchPlanHelper.create(raq);
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        Query query = JpaHelper.createQuery(raq, em);

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Before
    public void verifyConfig() {
        EclipseLinkJPAAssert.assertIsWoven(getEMF(), "Employee");
        EclipseLinkJPAAssert.assertIsWoven(getEMF(), "PhoneNumber");
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
        getQuerySQLTracker(getEMF()).reset();
    }

    /*
     * clone using serialization
     */
    private List serialize(final List<?> results) throws Exception {
        List cloneList = new ArrayList(results.size());

        for (int i = 0; i < results.size(); i++) {
            cloneList.add(SerializationHelper.clone((Serializable) results.get(i)));
        }

        return cloneList;
    }
}
