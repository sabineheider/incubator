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
 ******************************************************************************/
package org.eclipse.persistence.extension.fetchplan;

import java.util.*;

import org.eclipse.persistence.internal.queries.ReportItem;
import org.eclipse.persistence.queries.ReportQuery;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.1.2
 */
public class ReportItemFetchItem extends FetchItem {

    private ReportItem item;

    protected ReportItemFetchItem(ReportQuery query, String... attributePaths) {
        super(attributePaths);
        this.item = matchReportItem(query, attributePaths);

        if (this.item == null || this.attributeNames.length == 0) {
            throw new IllegalArgumentException("Cannot create FetchPlan item: " + attributePaths);
        }

    }

    public ReportItem getItem() {
        return this.item;
    }

    @Override
    public Object getEntityValue(Object entity) {
        if (entity instanceof Object[]) {
            Object[] values = (Object[]) entity;
            return values[getItem().getResultIndex()];
        }
        return super.getEntityValue(entity);
    }

    /**
     * Locate the ReportItem for the given path.
     */
    private ReportItem matchReportItem(ReportQuery query, String... attributePaths) {
        // TODO: Can we avoid doing this collection of entity item types on each
        // fetch item?
        List<ReportItem> entityItems = new ArrayList<ReportItem>();
        for (Iterator<?> i = query.getItems().iterator(); i.hasNext();) {
            ReportItem ri = (ReportItem) i.next();
            // We'll assume that ReportItem with descriptors are for entities
            if (ri.getDescriptor() != null) {
                entityItems.add(ri);
            }
        }

        if (entityItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot create FetchPlan item for query that does not return any entities: " + query);
        }

        if (entityItems.size() == 1) {
            ReportItem reportItem = entityItems.get(0);

            if (attributePaths.length == 1) {
                if (attributePaths[0].startsWith(reportItem.getName())) {
                    this.attributeNames = attributePaths[0].substring(reportItem.getName().length()).split("\\.");
                }
            }
            return reportItem;
        }

        if (attributePaths.length > 1) {
            throw new IllegalArgumentException("Cannot specify multiple attributes when query has multiple entity return types.");
        }

        for (ReportItem ri : entityItems) {
            if (attributePaths[0].startsWith(ri.getName())) {
                this.attributeNames = attributePaths[0].substring(ri.getName().length()).split("\\.");
                return ri;
            }
        }

        throw new IllegalArgumentException("Cannot match '" + attributePaths[0] + "' in: " + query);
    }

}
