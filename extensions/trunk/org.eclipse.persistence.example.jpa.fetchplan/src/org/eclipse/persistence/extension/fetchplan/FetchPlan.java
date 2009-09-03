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
 *     dclarke - Bug 288307: FetchPlan Example
 *     ssmith  - various minor edits
 ******************************************************************************/
package org.eclipse.persistence.extension.fetchplan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.*;
import org.eclipse.persistence.sessions.*;

/**
 * FetchPlan handles specifying a set of relationships in a query result that
 * need to be instantiated on a given query result.
 * 
 * @author dclarke
 * @since EclipseLink 1.1.2
 */
public class FetchPlan implements Serializable {

    private List<FetchItem> items;

    private ObjectLevelReadQuery query;

    private static final String QUERY_PROPERTY = FetchPlan.class.getName();

    private static final long serialVersionUID = 1L;

    public static FetchPlan getFetchPlan(ObjectLevelReadQuery query) {
        FetchPlan fetchPlan = (FetchPlan) query.getProperty(QUERY_PROPERTY);

        if (fetchPlan == null) {
            fetchPlan = new FetchPlan(query);
            query.setProperty(QUERY_PROPERTY, fetchPlan);
        }

        return fetchPlan;
    }

    private FetchPlan(ObjectLevelReadQuery query) {
        this.query = query;
        this.items = new ArrayList<FetchItem>();
        query.setRedirector(new FetchPlanRedirector());
    }

    public ObjectLevelReadQuery getQuery() {
        return this.query;
    }

    public List<FetchItem> getItems() {
        return this.items;
    }

    /**
     * Add an item to be fetched
     * 
     * @param attributePath
     * @return
     */
    public FetchPlan addFetchItem(String attributePath) {
        if (attributePath == null || attributePath.length() < 1) {
            throw new IllegalArgumentException("FetchPlan.addItem: " + attributePath);
        }

        FetchItem fetchItem = null;

        if (getQuery().isReportQuery()) {
            fetchItem = new ReportItemFetchItem((ReportQuery) query, attributePath);
        } else if (getQuery().isReadAllQuery()) {
            fetchItem = new ReadAllFetchItem((ReadAllQuery) query, attributePath);
        } else {
            fetchItem = new FetchItem(attributePath);
        }

        getItems().add(fetchItem);
        return this;
    }

    /**
     * Instantiate all items ({@link FetchItem}) for the result provided. This
     * walks through the query result using the items and the session's mapping
     * metadata to populate all requested relationships.
     * <p>
     * This method invoked by the
     * {@link FetchPlanListener#postExecuteQuery(SessionEvent)} event.
     * 
     * @param result
     */
    private void instantiate(ObjectLevelReadQuery query, Object result, Session session) {
        for (FetchItem item : getItems()) {
            item.instantiate(query.getReferenceClass(), result, session);
        }
    }

    @SuppressWarnings("serial")
    class FetchPlanRedirector implements QueryRedirector {

        /**
         * {@link QueryRedirector#invokeQuery(DatabaseQuery, Record, Session)}
         * implementation that executes the query and then post processes the
         * result to force all requested relationships in the {@link FetchPlan}
         * to be loaded.
         */
        public Object invokeQuery(DatabaseQuery query, Record arguments, Session session) {
            Object result = ((AbstractSession) session).internalExecuteQuery(query, (AbstractRecord) arguments);
            instantiate((ObjectLevelReadQuery) query, result, session);
            return result;
        }
    }

}
