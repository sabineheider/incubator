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

import model.Employee;

import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.Before;
import org.junit.Test;

import testing.EclipseLinkJPAAssert;
import testing.EclipseLinkJPATest;
import example.FetchPlanExamples;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class BasicFetchPlanTests extends EclipseLinkJPATest {

    private FetchPlanExamples examples = new FetchPlanExamples();

    @Test
    public void employeeAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddressPhones(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
    }

    @Test
    public void employeeAddressPhones_Batching() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddressPhones_Batching(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
    }

    @Test
    public void employeeAddressPhones_Joining() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddressPhones_Joining(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
    }

    @Test
    public void managerAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.managerAddressPhones(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
    }

    @Test
    public void responsibilities() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.responsibilities(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
    }

    @Test
    public void responsibilitiesBatch() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.responsibilitiesBatch(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
    }

    @Test
    public void employeeAddress_ReturnBoth() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddress_ReturnBoth(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
    }

    @Test
    public void managedEmployeesAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.managedEmployeesAddress(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
    }

    @Test
    public void readAllEmployee() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.readAllEmployee(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
    }

    @Test
    public void emptyFetchPlan() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");
        
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        
        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
    }

    @Before
    public void verifyConfig() {
        EclipseLinkJPAAssert.assertIsWoven(getEMF(), "Employee");
        EclipseLinkJPAAssert.assertIsWoven(getEMF(), "PhoneNumber");
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
        getQuerySQLTracker(getEMF()).reset();
    }
}
