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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import junit.framework.Assert;
import model.Address;
import model.Employee;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.Test;

import testing.EclipseLinkJPATest;
import testing.QuerySQLTracker;
import example.FetchPlanExamples;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class FetchPlanExamplesTests extends EclipseLinkJPATest {

    private FetchPlanExamples examples = new FetchPlanExamples();

    @Test
    public void employeeAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddressPhones(em);

        // Verify state of created query
        Assert.assertTrue("Query does not contain FetchPlan", JpaHelper.getDatabaseQuery(query).getProperties().containsKey("org.eclipse.persistence.extension.fetchplan.FetchPlan"));
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        Assert.assertEquals("Incorrect # of items in FetchPlan", 2, fetchPlan.getItems().size());

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        getQuerySQLTracker(em).printResults("employeeAddressPhones> ");
        Assert.assertEquals(1 + (emps.size() * 2), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeAddressPhones_Batching() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddressPhones_Batching(em);

        // Verify state of created query
        Assert.assertTrue("Query does not contain FetchPlan", JpaHelper.getDatabaseQuery(query).getProperties().containsKey("org.eclipse.persistence.extension.fetchplan.FetchPlan"));
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        Assert.assertEquals("Incorrect # of items in FetchPlan", 2, fetchPlan.getItems().size());

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        getQuerySQLTracker(em).printResults("employeeAddressPhones_Batching> ");
        Assert.assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeAddressPhones_Joining() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddressPhones_Joining(em);

        // Verify state of created query
        Assert.assertTrue("Query does not contain FetchPlan", JpaHelper.getDatabaseQuery(query).getProperties().containsKey("org.eclipse.persistence.extension.fetchplan.FetchPlan"));
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        Assert.assertEquals("Incorrect # of items in FetchPlan", 2, fetchPlan.getItems().size());

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        getQuerySQLTracker(em).printResults("employeeAddressPhones_Joining> ");
        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void managerAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.managerAddressPhones(em);

        // Verify state of created query
        Assert.assertTrue("Query does not contain FetchPlan", JpaHelper.getDatabaseQuery(query).getProperties().containsKey("org.eclipse.persistence.extension.fetchplan.FetchPlan"));
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        Assert.assertEquals("Incorrect # of items in FetchPlan", 2, fetchPlan.getItems().size());

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        int numWithManager = 0;
        for (Employee emp : emps) {
            if (emp.getManager() != null) {
                numWithManager++;
            }
        }
        getQuerySQLTracker(em).printResults("managerAddressPhones> ");
        Assert.assertEquals(11, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void responsibilities() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.responsibilities(em);

        // Verify state of created query
        Assert.assertTrue("Query does not contain FetchPlan", JpaHelper.getDatabaseQuery(query).getProperties().containsKey("org.eclipse.persistence.extension.fetchplan.FetchPlan"));
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        Assert.assertEquals("Incorrect # of items in FetchPlan", 1, fetchPlan.getItems().size());

        List<Employee> emps = query.getResultList();

        Assert.assertEquals(1 + emps.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        FetchPlanAssert.assertFetched(fetchPlan, emps);

        getQuerySQLTracker(em).printResults("responsibilities> ");
        Assert.assertEquals(1 + emps.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void responsibilities_JOIN() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.responsibilities(em);

        // Verify state of created query
        Assert.assertTrue("Query does not contain FetchPlan", JpaHelper.getDatabaseQuery(query).getProperties().containsKey("org.eclipse.persistence.extension.fetchplan.FetchPlan"));
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        Assert.assertEquals("Incorrect # of items in FetchPlan", 1, fetchPlan.getItems().size());

        query.setHint(QueryHints.FETCH, "e.responsibilities");

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        getQuerySQLTracker(em).printResults("responsibilities_JOIN> ");
        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void responsibilitiesBatch() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.responsibilitiesBatch(em);

        // Verify state of created query
        Assert.assertTrue("Query does not contain FetchPlan", JpaHelper.getDatabaseQuery(query).getProperties().containsKey("org.eclipse.persistence.extension.fetchplan.FetchPlan"));
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        Assert.assertEquals("Incorrect # of items in FetchPlan", 1, fetchPlan.getItems().size());

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        getQuerySQLTracker(em).printResults("responsibilitiesBatch> ");
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void employeeAddress_ReturnBoth() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.employeeAddress_ReturnBoth(em);

        // Verify state of created query
        Assert.assertTrue("Query does not contain FetchPlan", JpaHelper.getDatabaseQuery(query).getProperties().containsKey("org.eclipse.persistence.extension.fetchplan.FetchPlan"));
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        Assert.assertEquals("Incorrect # of items in FetchPlan", 2, fetchPlan.getItems().size());

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);

        getQuerySQLTracker(em).printResults("employeeAddress_ReturnBoth> ");
        Assert.assertEquals(1 + emps.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void managedEmployees() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.managedEmployees(em);
        
        // Verify state of created query
        Assert.assertTrue("Query does not contain FetchPlan", JpaHelper.getDatabaseQuery(query).getProperties().containsKey("org.eclipse.persistence.extension.fetchplan.FetchPlan"));
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        Assert.assertEquals("Incorrect # of items in FetchPlan", 1, fetchPlan.getItems().size());
        
        List<Employee> emps = query.getResultList();
        FetchPlanAssert.assertFetched(fetchPlan, emps);

        getQuerySQLTracker(em).printResults("managedEmployees> ");
        Assert.assertEquals(1 + emps.size(), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void managedEmployees_Batching() throws Exception {
        EntityManager em = getEntityManager();
        Assert.assertEquals("QuerySQLTracker not reset", 0, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        Query query = this.examples.managedEmployees_Batching(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);

        getQuerySQLTracker(em).printResults("managedEmployees_Batching> ");
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void managedEmployeesAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.managedEmployeesAddress(em);
        List<Employee> emps = query.getResultList();

        // FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        // FetchPlanAssert.assertFetched(fetchPlan, emps);

        getQuerySQLTracker(em).printResults("managedEmployeesAddress> ");

        int initialSqlSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();
        int sqlCount = 1;
        Set<Address> readAddresses = new HashSet<Address>();
        for (Employee emp : emps) {
            sqlCount++;
            for (Employee managedEmp : emp.getManagedEmployees()) {
                if (managedEmp.getAddress() != null) {
                    if (!readAddresses.contains(managedEmp.getAddress())) {
                        sqlCount++;
                        readAddresses.add(managedEmp.getAddress());
                    }
                }
            }
        }

        Assert.assertEquals("Counting expected SQL calls caused additional SELECT", initialSqlSelect, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Assert.assertEquals("SQL SELECTs executed does not match expected", sqlCount, initialSqlSelect);
    }

    @Test
    public void readAllEmployee() throws Exception {
        EntityManager em = getEntityManager();

        Query query = this.examples.readAllEmployee(em);
        List<Employee> emps = query.getResultList();

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        FetchPlanAssert.assertFetched(fetchPlan, emps);

        getQuerySQLTracker(em).printResults("readAllEmployee> ");
        Assert.assertEquals(1 + (emps.size() * 2), getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void emptyFetchPlan() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");

        FetchPlan fetchPlan = new FetchPlan(query);

        List<Employee> emps = query.getResultList();

        FetchPlanAssert.assertFetched(fetchPlan, emps);
    }

    @Override
    protected EntityManager getEntityManager() {
        getEMF().getCache().evictAll();
        EntityManager em = super.getEntityManager();
        getQuerySQLTracker(em).reset();
        Assert.assertEquals("QuerySQLTracker not reset", 0, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        return em;
    }
}
