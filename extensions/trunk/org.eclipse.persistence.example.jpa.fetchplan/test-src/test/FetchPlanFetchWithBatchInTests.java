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

import java.util.List;
import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import model.Employee;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.extension.query.BatchInConfig;
import org.junit.Test;

import testing.EclipseLinkJPATest;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.2
 */
@PersistenceContext(unitName="employee")
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
        
        for (Employee emp: emps) {
            System.out.println("> " + emp);
            for (Employee me: emp.getManagedEmployees()) {
                System.out.println("\t> " + me);
                for (Employee me2: me.getManagedEmployees()) {
                    System.out.println("\t\t> " + me2);
                }
            }
        }
        
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }
}
