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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.Employee;

import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.junit.After;
import org.junit.Test;

import testing.EclipseLinkJPATest;
import example.Queries;

@PersistenceContext(unitName = "employee")
public class ReadObjectFetchPlanTests extends EclipseLinkJPATest {

    @Test
    public void employeeId_Address() throws Exception {
        EntityManager em = getEntityManager();

        int empId = Queries.minEmployeeIdWithAddressAndPhones(em);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        ReadObjectQuery roq = new ReadObjectQuery(Employee.class);
        ExpressionBuilder eb = roq.getExpressionBuilder();
        roq.setSelectionCriteria(eb.get("id").equal(empId));

        Employee emp = (Employee) JpaHelper.createQuery(roq, em).getSingleResult();

        assertNotNull(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("address");
        fetchPlan.addAttribute("phoneNumbers");
        JpaFetchPlanHelper.fetch(em, fetchPlan, emp);

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getAddress().getCity();
        emp.getPhoneNumbers().size();

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLCalls());
    }

    @After
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

}
