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
 *     dclarke - TODO
 ******************************************************************************/
package test.fetchplan;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import junit.framework.Assert;

import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.FetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.junit.Test;

import testing.EclipseLinkJPATest;

/**
 * These tests verify the basic operation of the FetchPlan extension ensuring
 * the query redirector and property usage is as expected.
 * 
 * @author dclarke
 * @since EclipseLink 2.0
 */
@PersistenceContext(unitName = "employee")
public class FetchPlanConfigTests extends EclipseLinkJPATest {

    /*
     * Verify that after adding a FetchPlan to a query that it no longer exists
     * in the JPQL query cache.
     */
    @Test
    public void testFetchPlanQueryNotInCache() {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");
        DatabaseQuery dbQuery = JpaHelper.getDatabaseQuery(query);

        Assert.assertTrue(JpaHelper.getServerSession(getEMF()).getProject().getJPQLParseCache().getCache().containsValue(dbQuery));
        Assert.assertTrue(JpaHelper.getDatabaseQuery(query).getProperties().isEmpty());
        Assert.assertNull(dbQuery.getRedirector());

        FetchPlanHelper.create(query);

        Assert.assertNotSame(dbQuery, JpaHelper.getDatabaseQuery(query));

        dbQuery = JpaHelper.getDatabaseQuery(query);
        Assert.assertTrue(dbQuery.getProperties().containsKey(FetchPlan.class.getName()));
        Assert.assertNotNull(dbQuery.getRedirector());
    }

    /*
     * Verify that two queries created from the same JPQL string do not share
     * the same underlying query and thus the same redirector and properties.
     */
    @Test
    public void testDuplicateFetchPlanQueriesQueries() {
        EntityManager em = getEntityManager();

        Query q1 = em.createQuery("SELECT e FROM Employee e");
        FetchPlanHelper.create(q1);

        Query q2 = em.createQuery("SELECT e FROM Employee e");
        FetchPlanHelper.create(q2);

        Assert.assertNotSame("Different JPA queries share same native query", JpaHelper.getDatabaseQuery(q1), JpaHelper.getDatabaseQuery(q2));
    }
}
