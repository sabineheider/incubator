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
import org.junit.Test;

import testing.EclipseLinkJPATest;

@PersistenceContext(unitName = "employee")
public class FailureTests extends EclipseLinkJPATest {

    @Test
    public void nullItemsName() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        try {
            fetchPlan.addFetchItem((String[]) null);
        } catch (IllegalArgumentException iae) {
            return;
        }
        Assert.fail("Should have thrown IllegalArgumentException");
    }

    @Test
    public void emptyArrayItemsName() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        try {
            fetchPlan.addFetchItem(new String[0]);
        } catch (IllegalArgumentException iae) {
            return;
        }
        Assert.fail("Should have thrown IllegalArgumentException");
    }

    @Test
    public void emptyStringItemsName() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        try {
            fetchPlan.addFetchItem("");
        } catch (IllegalArgumentException iae) {
            return;
        }
        Assert.fail("Should have thrown IllegalArgumentException");
    }

    @Test
    public void nullInStringArrayItemsName() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        try {
            fetchPlan.addFetchItem(new String[] { null });
        } catch (IllegalArgumentException iae) {
            return;
        }
        Assert.fail("Should have thrown IllegalArgumentException");
    }

    // TODO - should this test fail. currently its equivalent to an empty FetchPlan
    @Test
    public void validAliasOnly() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        fetchPlan.addFetchItem("e");

        query.getResultList();
    }

    @Test
    public void validAliasNoAttribute() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        try {
            fetchPlan.addFetchItem("e.");
        } catch (IllegalArgumentException iae) {
            return;
        }
        Assert.fail("Should have thrown IllegalArgumentException");
    }

    @Test
    public void validAliasWithValidAttributeMissingSecond() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(query);
        try {
            fetchPlan.addFetchItem("e.manager.");
        } catch (IllegalArgumentException iae) {
            return;
        }
        Assert.fail("Should have thrown IllegalArgumentException");
    }
}
