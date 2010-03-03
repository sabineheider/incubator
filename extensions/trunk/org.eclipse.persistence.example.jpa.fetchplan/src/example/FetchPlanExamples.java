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
package example;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import model.Employee;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;

@SuppressWarnings("unchecked")
public class FetchPlanExamples {

    /**
     * Query for all employees with a salary greater then zero using a
     * FetchGroup to only load id, firstName, lastName, gender, salary, and
     * version and then fetch their address and phone numbers using the same
     * FetchPlan.
     */
    public List<Employee> employeesFetchAddressAndPhones(EntityManager em) {
        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");
        fetchPlan.addAttribute("gender");
        fetchPlan.addAttribute("salary");
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.salary > 0");
        // Use helper to configure dynamic FetchGroup on query
        fetchPlan.setFetchGroup(query);

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        return emps;
    }

    /**
     * Similar to {@link #employeesFetchAddressAndPhones(EntityManager)} but the
     * loading of the related address and phoneNumbers is optimized using batch
     * reading.
     */
    // Note: FetchGroup with FETCH (JOIN) does not work - see bug XXX
    public List<Employee> employeesFetchAddressAndPhones_optimized(EntityManager em) {
        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");
        fetchPlan.addAttribute("gender");
        fetchPlan.addAttribute("salary");
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.salary > 0");
        // Use helper to configure dynamic FetchGroup on query
        fetchPlan.setFetchGroup(query);
        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        List<Employee> emps = query.getResultList();
        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        return emps;
    }

    /**
     * Simple example retrieving just the firstName and lastName of the
     * Employee. This will also include the required identifier and optimistic
     * locking values (id and version).
     */
    public List<Employee> maleEmployeeCopyNames(EntityManager em) {
        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender = 'Male'");
        fetchPlan.setFetchGroup(query);
        List<Employee> emps = query.getResultList();

        // This ensures all required relationships are loaded
        // In this case it does nothing
        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        // Get a set of copies with only the names and required attributes
        // populated
        List<Employee> copies = JpaFetchPlanHelper.copy(em, fetchPlan, emps);

        return copies;
    }

    /**
     * TODO
     */
    public List<Employee> employeeCopyNamesWithFetchGroup(EntityManager em) {
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");

        fetchPlan.setFetchGroup(query);

        List<Employee> emps = query.getResultList();

        return JpaFetchPlanHelper.copy(em, fetchPlan, emps);
    }

    /**
     * Create copies of managed objects requiring relationships that were not
     * loaded in the initial query.
     */
    public List<Employee> employeeCopyWithNamesAddressAndPhones(EntityManager em) {
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.salary > 0");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        List<Employee> emps = query.getResultList();

        return JpaFetchPlanHelper.copy(em, fetchPlan, emps);
    }

    /**
     * Example of a composite select returning both an Employee and its count of
     * PhoneNumbers.
     */
    public List<Object[]> employeeAddress_ReturnBoth(EntityManager em) {
        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        Query query = em.createQuery("SELECT e, e.address FROM Employee e WHERE e.firstName <> e.lastName");
        List<Object[]> results = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, results, 0);

        return results;
    }

}
