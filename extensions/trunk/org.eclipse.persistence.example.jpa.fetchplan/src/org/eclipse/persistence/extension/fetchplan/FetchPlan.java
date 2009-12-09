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

import javax.persistence.Query;

import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionEvent;

/**
 * FetchPlan handles specifying a set of relationships in a query result that
 * need to be instantiated on a given query result. The FetchPlan is associated
 * with a query through its properties where it is stored using the FetchPlan's
 * class name as a key.
 * <p>
 * A FetchPlan is created/retrieved from a query using the
 * {@link #getFetchPlan(Query)} or {@link #getFetchPlan(ObjectLevelReadQuery)}
 * methods. Relationships that are to be loaded can then be added using
 * {@link #addFetchItem(String...)} which creates a {@link FetchItem} within the
 * plan for the requested relationship in the results graph.
 * 
 * @author dclarke
 * @since EclipseLink 1.1.2
 */
public class FetchPlan implements Serializable {

    private List<FetchItem> items;

    private ObjectLevelReadQuery query;

    private static final long serialVersionUID = 1L;

    protected FetchPlan(ObjectLevelReadQuery query) {
        this.query = query;
        this.items = new ArrayList<FetchItem>();
    }

    public ObjectLevelReadQuery getQuery() {
        return this.query;
    }

    public List<FetchItem> getItems() {
        return this.items;
    }

    /**
     * Add an item to be fetched
     * <p>
     * If a single string is supplied then it is assumed to either be an
     * attribute name for the only entity type being returned from the query or
     * it is a path expression with attribute names separated by '.' and the
     * first part represents the alias or item name in the select of the query.
     * <p>
     * If multiple strings are provided it is assumed that they are all mapped
     * attribute names and the query is returning a single entity type.
     * 
     * @param attributePath
     * @return
     */
    public FetchPlan addFetchItem(String... attributePaths) {
        if (attributePaths == null || attributePaths.length == 0) {
            throw new IllegalArgumentException("FetchPlan.addItem: " + attributePaths);
        }

        for (int index = 0; index < attributePaths.length; index++) {
            if (attributePaths[index] == null || attributePaths[index].isEmpty() || attributePaths[index].startsWith(".") || attributePaths[index].endsWith(".")) {
                throw new IllegalArgumentException("FetchPlan.addItem: " + attributePaths);
            }
        }

        FetchItem fetchItem = null;

        if (getQuery().isReportQuery()) {
            fetchItem = new ReportItemFetchItem((ReportQuery) query, attributePaths);
        } else if (getQuery().isReadAllQuery()) {
            fetchItem = new ReadAllFetchItem((ReadAllQuery) query, attributePaths);
        } else {
            fetchItem = new FetchItem(attributePaths);
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
    public void instantiate(ObjectLevelReadQuery query, Object result, Session session) {
       instantiate(query.getReferenceClass(), result, session);
    }

    public void instantiate(Class<?> entityClass, Object result, Session session) {
        for (FetchItem item : getItems()) {
            item.instantiate(entityClass, result, session);
        }
    }

    public String toString() {
        return "FetchPlan()";
    }
}
