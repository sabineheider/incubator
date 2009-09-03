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

import java.util.Collection;

import org.eclipse.persistence.internal.queries.ReportItem;
import org.eclipse.persistence.queries.ReportQuery;

public class ReportItemFetchItem extends FetchItem {

    private ReportItem item;

    protected ReportItemFetchItem(ReportQuery query, String attributePath) {
        super(attributePath);
        this.item = matchReportItem(query, attributePath);
    }

    public ReportItem getItem() {
        return this.item;
    }

    @Override
    protected Object getEntityValue(Object entity) {
        if (entity instanceof Object[]) {
            Object[] values = (Object[]) entity;
            return values[getItem().getResultIndex()];
        }
        return super.getEntityValue(entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Object> getInitialResults(Object result) {
        return (Collection<Object>) result;
    }

    private ReportItem matchReportItem(ReportQuery query, String attributePath) {
        ReportItem reportItem = null;

        if (query.getItems().size() == 1) {
            reportItem = (ReportItem) query.getItems().get(0);
        } else {
            String alias = attributePath.substring(0, attributePath.indexOf('.'));

            for (int index = 0; reportItem == null && index < query.getItems().size(); index++) {
                ReportItem item = (ReportItem) query.getItems().get(index);
                if (item.getName().equals(alias)) {
                    reportItem = item;
                }
            }
        }

        if (reportItem == null) {
            throw new RuntimeException("Could not find selected item for fetch item: " + attributePath);
        }

        return reportItem;
    }

}
