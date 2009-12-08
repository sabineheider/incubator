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
 *     dclarke - FetchPlan Extension Incubator
 ******************************************************************************/
package org.eclipse.persistence.extension.fetchplan;

import javax.persistence.Query;

import org.eclipse.persistence.internal.helper.ConcurrentFixedCache;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.queries.QueryRedirector;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;

/**
 * Helper class to facilitate creating, accessing and verifying a FetchPlan.
 * <p>
 * This class encapsulates how the FetchPlan is used as an extension. It is
 * responsible for the {@link FetchPlanRedirector} redirecting the query and the
 * {@link #QUERY_PROPERTY} property on the native query caching the
 * {@link FetchPlan} to simply looking it up.
 * 
 * @author dclarke
 * @since EclipseLink 2.0
 */
public class FetchPlanHelper {

    /**
     * This property name is used to cache the {@link FetchPlan} within the
     * native query so that it could be retrieved if necessary.
     */
    private static final String QUERY_PROPERTY = FetchPlan.class.getName();

    /**
     * Create a FetchPlan and associate it to the provided native query. This
     * involves putting the FetchPlan on the query using the
     * {@link #QUERY_PROPERTY} property and setting up a redirector on the
     * query.
     * <p>
     * JPA usage should use {@link #create(Query)} method instead of calling
     * this one directly with the unwrapped native query as that method handles
     * additional requirements.
     * 
     * @param query
     * @throws IllegalStateException
     *             if the query is already configured with a {@link FetchPlan}
     */
    public static FetchPlan create(ObjectLevelReadQuery query) {
        if (query.getProperties().containsKey(QUERY_PROPERTY)) {
            throw new IllegalStateException("Query already has a FetchPlan configured.");
        }
        if (query.getRedirector() != null) {
            throw new IllegalStateException("Query already has a QueryRedirector configured");
        }

        FetchPlan fetchPlan = new FetchPlan(query);
        query.setProperty(QUERY_PROPERTY, fetchPlan);
        query.setRedirector(new FetchPlanRedirector(fetchPlan));

        return fetchPlan;
    }

    /**
     * Create a FetchPlan and associate it to the native query wrapped within
     * this Jpa {@link Query}. This involves putting the FetchPlan on the query
     * using the {@link #QUERY_PROPERTY} property and setting up a redirector on
     * the query.
     * <p>
     * JPA usage should use this method to ensure that the JPQL cached query is
     * not used directly as that would cause state to be shared between
     * executions.
     * 
     * @see FetchPlanHelper#create(ObjectLevelReadQuery)
     * @param query
     * @throws IllegalStateException
     *             if the query is already configured with a {@link FetchPlan}
     */
    public static FetchPlan create(Query query) {
        DatabaseQuery dbQuery = JpaHelper.getDatabaseQuery(query);
        if (!dbQuery.isObjectLevelReadQuery()) {
            throw new IllegalArgumentException("FetchPlan can only be created for ObjectLevelReadQueries: " + dbQuery);
        }

        // If this query is stored in the JPA query cache we need to remove it
        // This is a work around for Bug XXX
        EJBQueryImpl<?> queryImpl = (EJBQueryImpl<?>) query;
        ConcurrentFixedCache queryCache = queryImpl.getEntityManager().getServerSession().getProject().getJPQLParseCache();
        if (queryCache.getCache().containsValue(dbQuery)) {
            dbQuery = (DatabaseQuery) dbQuery.clone();
            queryImpl.setDatabaseQuery(dbQuery);

            // TODO - not sure how cached queries have a FetchPlan but resetting
            // for now
            dbQuery.removeProperty(QUERY_PROPERTY);
            if (dbQuery.getRedirector() != null && dbQuery.getRedirector() instanceof FetchPlanRedirector) {
                dbQuery.setRedirector(null);
            }
        }

        return create((ObjectLevelReadQuery) dbQuery);
    }

    /**
     * Retrieve the {@link FetchPlan} if it is already cached on the query.
     * Otherwise return null.
     * 
     * @param query
     * @return
     */
    public static FetchPlan get(ObjectLevelReadQuery query) {
        return (FetchPlan) query.getProperty(QUERY_PROPERTY);
    }

    /**
     * Retrieve the {@link FetchPlan} if it is already cached on the query.
     * Otherwise return null.
     * <p>
     * The JPA query must be a read query returning at least one entity type.
     * 
     * @param query
     * @return
     */
    public static FetchPlan get(Query query) {
        DatabaseQuery dbQuery = JpaHelper.getDatabaseQuery(query);
        if (!dbQuery.isObjectLevelReadQuery()) {
            throw new IllegalArgumentException("FetchPlan can only be created for ObjectLevelReadQueries: " + dbQuery);
        }
        return get((ObjectLevelReadQuery) dbQuery);
    }

    /**
     * Test to see if a {@link FetchPlan} has been configured on the provided
     * query.
     */
    public static boolean has(ObjectLevelReadQuery query) {
        return query.getProperties().containsKey(QUERY_PROPERTY);
    }

    /**
     * Test to see if a {@link FetchPlan} has been configured on the provided
     * query.
     */
    public static boolean has(Query query) {
        DatabaseQuery dbQuery = JpaHelper.getDatabaseQuery(query);
        return dbQuery != null && dbQuery.isObjectLevelReadQuery() && has((ObjectLevelReadQuery) query);
    }

    /**
     * 
     */
    static class FetchPlanRedirector implements QueryRedirector {

        private FetchPlan fetchPlan;

        public FetchPlanRedirector(FetchPlan fetchPlan) {
            this.fetchPlan = fetchPlan;
        }

        /**
         * {@link QueryRedirector#invokeQuery(DatabaseQuery, Record, Session)}
         * implementation that executes the query and then post processes the
         * result to force all requested relationships in the {@link FetchPlan}
         * to be loaded.
         */
        public Object invokeQuery(DatabaseQuery query, Record arguments, Session session) {
            Object result = ((AbstractSession) session).internalExecuteQuery(query, (AbstractRecord) arguments);
            this.fetchPlan.instantiate((ObjectLevelReadQuery) query, result, session);
            return result;
        }
    }

}
