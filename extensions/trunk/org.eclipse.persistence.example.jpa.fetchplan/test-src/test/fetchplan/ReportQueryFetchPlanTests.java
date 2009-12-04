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

import javax.persistence.*;

import junit.framework.Assert;
import model.Employee;

import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.Test;

import testing.EclipseLinkJPATest;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class ReportQueryFetchPlanTests extends EclipseLinkJPATest {

    @Test
    public void employeeAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, e.address FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void employeeManager() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, e.manager FROM Employee e");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void employeeCountPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, COUNT(e.phoneNumbers) FROM Employee e GROUP BY e");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Object[]> results = query.getResultList();

        Assert.assertNotNull(results);

        for (Object[] result : results) {
            System.out.println(result[0] + "- phones: " + result[1]);
        }
    }
}
