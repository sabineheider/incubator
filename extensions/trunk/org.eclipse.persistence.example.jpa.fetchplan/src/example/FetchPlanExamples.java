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
package example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import model.Employee;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.queries.ReadAllQuery;

import example.util.ExamplePropertiesLoader;

/**
 * 
 * @author dclarke
 * 
 */
public class FetchPlanExamples {

    public static void main(String[] args) {
        Map<String, Object> properties = new HashMap<String, Object>();
        ExamplePropertiesLoader.loadProperties(properties);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("employee", properties);

        EntityManager em = emf.createEntityManager();

        // Run Examples
        FetchPlanExamples examples = new FetchPlanExamples();
        List<?> results;

        try {
            results = examples.employeeAddressPhones(em).getResultList();
            printResults(em, results);

            results = examples.employeeAddressPhones_Batching(em).getResultList();
            printResults(em, results);

            results = examples.employeeAddressPhones_Joining(em).getResultList();
            printResults(em, results);

            results = examples.managerAddressPhones(em).getResultList();
            printResults(em, results);

            results = examples.responsibilities(em).getResultList();
            printResults(em, results);

            results = examples.responsibilities(em).getResultList();
            printResults(em, results);

            results = examples.employeeAddress_ReturnBoth(em).getResultList();
            printResults(em, results);

            results = examples.readAllEmployee(em).getResultList();
            printResults(em, results);

        } finally {
            em.close();
            emf.close();
        }
    }

    private static void printResults(EntityManager em, List<?> results) {
        SessionLog log = JpaHelper.getEntityManager(em).getServerSession().getSessionLog();
        for (Object result : results) {
            log.fine("> " + result);
        }
    }

    public Query employeeAddressPhones(EntityManager em) {
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        return query;
    }

    public Query employeeAddressPhones_Batching(EntityManager em) {
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        return query;
    }

    public Query employeeAddressPhones_Joining(EntityManager em) {
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        query.setHint(QueryHints.FETCH, "e.address");
        query.setHint(QueryHints.FETCH, "e.phoneNumbers");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        return query;
    }

    public Query managerAddressPhones(EntityManager em) {
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        return query;
    }

    public Query responsibilities(EntityManager em) {
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.responsibilities");

        return query;
    }

    public Query responsibilitiesBatch(EntityManager em) {
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");
        query.setHint(QueryHints.FETCH, "e.responsibilities");
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.responsibilities");

        return query;
    }

    public Query employeeAddress_ReturnBoth(EntityManager em) {
        Query query = em.createQuery("SELECT e, e.address FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        return query;
    }

    public Query readAllEmployee(EntityManager em) {
        ReadAllQuery raq = new ReadAllQuery(Employee.class);

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(raq);
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        Query query = JpaHelper.createQuery(raq, em);
        return query;
    }

}
