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
package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import model.Address;
import model.Employee;
import model.SmallProject;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.extension.query.BatchInConfig;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.After;
import org.junit.Test;

import test.fetchplan.FetchPlanAssert;
import testing.EclipseLinkJPATest;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.2
 */
@PersistenceContext(unitName = "employee")
public class FetchPlanFetchWithBatchInTests extends EclipseLinkJPATest {

    @SuppressWarnings("unchecked")
    @Test
    public void fetchAddressAndPhones() {
        EntityManager em = getEntityManager();

        FetchPlan plan = new FetchPlan(Employee.class);
        plan.addAttribute("address");
        plan.addAttribute("phoneNumbers");

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.salary > 0.0");

        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        BatchInConfig.config(em, emps, "address");
        BatchInConfig.config(em, emps, "phoneNumbers");

        JpaFetchPlanHelper.fetch(em, plan, emps);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchManagerManager() {
        EntityManager em = getEntityManager();

        FetchPlan plan = new FetchPlan(Employee.class);
        plan.addAttribute("managedEmployees.managedEmployees");

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.managedEmployees IS NOT EMPTY");

        query.setHint(QueryHints.BATCH, "e.managedEmployees");
        query.setHint(QueryHints.BATCH, "e.managedEmployees.managedEmployees");

        List<Employee> emps = query.getResultList();

        BatchInConfig.config(em, emps, "managedEmployees");
        BatchInConfig.config(em, emps.get(0).getManagedEmployees(), "managedEmployees");

        JpaFetchPlanHelper.fetch(em, plan, emps);

        for (Employee emp : emps) {
            System.out.println("> " + emp);
            for (Employee me : emp.getManagedEmployees()) {
                System.out.println("\t> " + me);
                for (Employee me2 : me.getManagedEmployees()) {
                    System.out.println("\t\t> " + me2);
                }
            }
        }

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    /**
     * In this test we only try to select all small projects and fetch their
     * teamleaders. Somehow a strange sql gets created and a database exception
     * occurs.
     */
    @Test
    public void findSmallProjectsWithBatchInConfigFetch() {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        Query q = em.createQuery("SELECT sm FROM SmallProject sm");
        q.setHint(QueryHints.BATCH, "sm.teamLeader");

        List<SmallProject> sms = q.getResultList();

        BatchInConfig.config(em, sms, "teamLeader");
        FetchPlan fp = new FetchPlan(SmallProject.class);
        fp.addAttribute("teamLeader");
        JpaFetchPlanHelper.fetch(em, fp, sms);
        // Here comes Exception
        em.getTransaction().rollback();
    }

    /**
     * In this test, we select all employees with their managed employees and
     * their addresses. Because of unknown reason, we get a
     * IllegalArgumentException on the second call to BatchInConfig.config. The
     * BatchValueHolder should not be set. But we have set the batch hint. So
     * whats wrong?
     */
    @SuppressWarnings("unchecked")
    @Test
    public void findEmployeesWithBatchInConfigFetch() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT em FROM Employee em ORDER BY em.id");
        q.setHint(QueryHints.BATCH, "em.managedEmployees");
        q.setHint(QueryHints.BATCH, "em.managedEmployees.address");

        List<Employee> ems = q.getResultList();

        // Display initial Employees read:
        System.out.println("Employees Read - " + ems.size());
        for (Employee emp : ems) {
            System.out.println("\t> " + emp);
        }

        BatchInConfig.config(em, ems, "managedEmployees");
        FetchPlan fp = new FetchPlan(Employee.class);
        fp.addAttribute("managedEmployees");
        JpaFetchPlanHelper.fetch(em, fp, ems);

        // iterate through all employees and collect their managers
        Set<Employee> managedEmployees = new HashSet<Employee>();
        for (Employee employee : ems) {
            managedEmployees.addAll(employee.getManagedEmployees());
        }

        // Fetch for all managers the addresses
        BatchInConfig.config(em, new ArrayList<Employee>(managedEmployees), "address");
        FetchPlan fp2 = new FetchPlan(Employee.class);
        fp2.addAttribute("address");
        JpaFetchPlanHelper.fetch(em, fp2, managedEmployees);

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        System.out.println("Employees Read - " + ems.size());
        for (Employee emp : ems) {
            System.out.println("\t> " + emp);
            for (Employee managedEmp : emp.getManagedEmployees()) {
                System.out.println("\t\t> " + managedEmp);
                System.out.println("\t\t\t> " + managedEmp.getAddress());
            }
            if (emp.getManagedEmployees() == null) {

            }
        }

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.clear();
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();

        q = em.createNativeQuery("SELECT EMP_ID, ADDR_ID, MANAGER_ID FROM EMPLOYEE ORDER BY EMP_ID");
        List<Object[]> ids = q.getResultList();

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertEquals(ems.size(), ids.size());

        for (int index = 0; index < ems.size(); index++) {
            Employee emp = ems.get(index);
            Object[] idRow = ids.get(index);

            assertEquals(emp.getId(), ((Number) idRow[0]).intValue());
            if (idRow[2] != null) { // We have a manager
                assertNotNull(emp.getAddress());
                assertEquals(emp.getAddress().getId(), ((Number) idRow[1]).intValue());
            } else {

            }
        }
    }

    /**
     * This test does not used BatchIn but is here to provide a verification for
     * the assertions of {@link #findEmployeesWithBatchInConfigFetch()}
     */
    @SuppressWarnings("unchecked")
    @Test
    public void findEmployeesWithJoinBatching() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT em FROM Employee em ORDER BY em.id");
        q.setHint(QueryHints.BATCH, "em.managedEmployees");
        q.setHint(QueryHints.BATCH, "em.managedEmployees.address");

        List<Employee> ems = q.getResultList();

        // Display initial Employees read:
        System.out.println("Employees Read - " + ems.size());
        for (Employee emp : ems) {
            System.out.println("\t> " + emp);
        }

        //BatchInConfig.config(em, ems, "managedEmployees");
        FetchPlan fp = new FetchPlan(Employee.class);
        fp.addAttribute("managedEmployees");
        JpaFetchPlanHelper.fetch(em, fp, ems);

        // iterate through all employees and collect their managers
        Set<Employee> managedEmployees = new HashSet<Employee>();
        for (Employee employee : ems) {
            managedEmployees.addAll(employee.getManagedEmployees());
        }

        // Fetch for all managers the addresses
        //BatchInConfig.config(em, new ArrayList<Employee>(managedEmployees), "address");
        FetchPlan fp2 = new FetchPlan(Employee.class);
        fp2.addAttribute("address");
        JpaFetchPlanHelper.fetch(em, fp2, managedEmployees);

        assertEquals(13, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        System.out.println("Employees Read - " + ems.size());
        for (Employee emp : ems) {
            System.out.println("\t> " + emp);
            for (Employee managedEmp : emp.getManagedEmployees()) {
                System.out.println("\t\t> " + managedEmp);
                System.out.println("\t\t\t> " + managedEmp.getAddress());
            }
            if (emp.getManagedEmployees() == null) {

            }
        }

        assertEquals(13, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.clear();
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();

        q = em.createNativeQuery("SELECT e.EMP_ID, e.ADDR_ID, e.MANAGER_ID FROM EMPLOYEE e, SALARY s WHERE s.EMP_ID = e.EMP_ID ORDER BY e.EMP_ID");
        List<Object[]> ids = q.getResultList();

        assertEquals(14, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertEquals(ems.size(), ids.size());

        for (int index = 0; index < ems.size(); index++) {
            Employee emp = ems.get(index);
            Object[] idRow = ids.get(index);

            assertEquals(emp.getId(), ((Number) idRow[0]).intValue());
            if (idRow[2] != null) { // We have a manager
                assertNotNull(emp.getAddress());
                assertEquals("Incorrect address on: " + emp, emp.getAddress().getId(), ((Number) idRow[1]).intValue());
            } else {

            }
        }
    }

    /**
     * Here we just want to show that in the merge SQLs are fired which we
     * consider unnecessary.
     * 
     * @throws Exception
     */
    @Test
    public void findEmployeesWithBatchInConfigFetchAndDetachThem() throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        Query q = em.createQuery("SELECT em FROM Employee em");
        q.setHint(QueryHints.BATCH, "em.managedEmployees");

        List<Employee> ems = q.getResultList();

        BatchInConfig.config(em, ems, "managedEmployees");
        FetchPlan fp = new FetchPlan(Employee.class);
        fp.addAttribute("managedEmployees");
        JpaFetchPlanHelper.fetch(em, fp, ems);

        // Employee deserializedEmployee =
        // (Employee)SerializationHelper.clone(ems.get(0));
        // Hier clone statt copy führt im merge zu
        // anderem Ergebnis.
        Employee deserializedEmployee = JpaFetchPlanHelper.copy(em, new FetchPlan(Employee.class), ems.get(0));
        em.getTransaction().commit();
        closeEMF();
        super.cleanupClosedEMF();
        em = getEntityManager();
        System.out.println(deserializedEmployee.getAddress());// Would throw
        // exception,
        // because
        // relation not instantiated. But only on clone object. On copied
        // object, null is returned.
        em.getTransaction().begin();
        deserializedEmployee.setFirstName(deserializedEmployee.getFirstName() + "x");
        Employee mergedEmployee = getEntityManager().merge(deserializedEmployee);
        Address address = mergedEmployee.getAddress();
        System.out.println(mergedEmployee.getAddress());

        em.getTransaction().commit();
    }

    @After
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    @Override
    protected void verifyConfig(EntityManager em) {
        super.verifyConfig(em);
        FetchPlanAssert.verifyEmployeeConfig(getEMF());
    }

}
