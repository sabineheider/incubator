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

import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.internal.helper.SerializationHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.Before;
import org.junit.Test;

import testing.EclipseLinkJPAAssert;
import testing.EclipseLinkJPATest;
import example.FetchPlanExamples;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class SerializedResultsTests extends EclipseLinkJPATest {

    private FetchPlanExamples examples = new FetchPlanExamples();

    @Test
    public void employeeAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddressPhones(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void employeeAddressPhones_Batching() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddressPhones_Batching(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void employeeAddressPhones_Joining() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddressPhones_Joining(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void managerAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.managerAddressPhones(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void responsibilities() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.responsibilities(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void responsibilitiesBatch() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.responsibilitiesBatch(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void employeeAddress_ReturnBoth() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddress_ReturnBoth(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }

    @Test
    public void managedEmployeesAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.managedEmployeesAddress(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);
        List<Employee> serializedEmps = serialize(emps);
        FetchPlanAssert.assertFetched(fetchPlan, serializedEmps);
    }
    
    @Test
    public void readAllEmployee() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.readAllEmployee(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
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
