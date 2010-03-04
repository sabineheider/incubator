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
import model.Gender;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;

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

        // Configure a dynamic FetchGroup based on the FetchPlan
        query.setHint(QueryHints.FETCH_GROUP, fetchPlan.createFetchGroup());

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

        // Configure a dynamic FetchGroup based on the FetchPlan
        query.setHint(QueryHints.FETCH_GROUP, fetchPlan.createFetchGroup());

        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        List<Employee> emps = query.getResultList();
        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        return emps;
    }

    public List<Employee> employeesFetchAddressAndPhonesBatchAndUsingRedirector(EntityManager em) {
        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");
        fetchPlan.addAttribute("gender");
        fetchPlan.addAttribute("salary");
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.salary > 0");

        // Configure a dynamic FetchGroup based on the FetchPlan
        query.setHint(QueryHints.FETCH_GROUP, fetchPlan.createFetchGroup());

        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");
        
        // Configure redirector so the FetchPlan.fetch is executed automatically
        fetchPlan.fetchOnExecute(JpaHelper.getReadAllQuery(query));

        return query.getResultList();
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

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        query.setParameter("GENDER", Gender.Male);

        // Configure a dynamic FetchGroup based on the FetchPlan
        query.setHint(QueryHints.FETCH_GROUP, fetchPlan.createFetchGroup());

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
     * Load all employees's first and last names (plus required id and version)
     * attributes based on a FetchGroup and copy the results based on this plan.
     */
    public List<Employee> employeeCopyNamesWithFetchGroup(EntityManager em) {
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");

        // Configure a dynamic FetchGroup based on the FetchPlan
        query.setHint(QueryHints.FETCH_GROUP, fetchPlan.createFetchGroup());

        List<Employee> emps = query.getResultList();

        return JpaFetchPlanHelper.copy(em, fetchPlan, emps);
    }

    /**
     * Create copies of managed objects requiring relationships that were not
     * loaded in the initial query. The copy operation will force the fetching
     * of required relationships.
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
     * Create copies of managed objects requiring relationships that were not
     * loaded in the initial query. The copy operation will force the fetching
     * of required relationships.
     */
    public List<Employee> employeeCopyWithNamesAddressAndPhonesWithBatching(EntityManager em) {
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.salary > 0");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");

        // Optimize graph loading using batching
        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        return JpaFetchPlanHelper.copy(em, fetchPlan, emps);
    }

    /**
     * Illustrate a multi-level fetch
     */
    public List<Employee> managerManagerManagerFetchWithNames(EntityManager em) {
        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");
        fetchPlan.addAttribute("manager.firstName");
        fetchPlan.addAttribute("manager.lastName");
        fetchPlan.addAttribute("manager.manager.firstName");
        fetchPlan.addAttribute("manager.manager.lastName");

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.manager.manager IS NOT NULL");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchPlan, emps);

        return emps;
    }

    /**
     * Example of a composite select returning both an Employee and its count of
     * PhoneNumbers. To use the {@link FetchPlan} in this case you must also
     * provide the index into the resulting Object[].
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
    
    /**
     * TODO
     * 
     * Note: caller manages transactions
     */
    public void fetchCopyMergeExample(EntityManager em) {
        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("firstName");
        fetchPlan.addAttribute("lastName");
        //fetchPlan.addAttribute("address");
        //fetchPlan.addAttribute("phoneNumbers");
        
        int minId = ((Number) em.createQuery("SELECT MIN(e.id) FROM Employee e").getSingleResult()).intValue();
        
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = " + minId);
        query.setHint(QueryHints.FETCH_GROUP, fetchPlan.createFetchGroup());
        
        Employee emp = (Employee) query.getSingleResult();
        
        JpaFetchPlanHelper.fetch(em, fetchPlan, emp);
        
        Employee copy = JpaFetchPlanHelper.copy(em, fetchPlan, emp);
        
        copy.setSalary(Integer.MAX_VALUE);
        copy.setFirstName(emp.getLastName());
        copy.setLastName(emp.getFirstName());
        
        JpaFetchPlanHelper.merge(em, fetchPlan, copy);
    }

}
