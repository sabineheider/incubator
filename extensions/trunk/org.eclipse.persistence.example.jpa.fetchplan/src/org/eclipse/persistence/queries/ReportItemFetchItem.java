package org.eclipse.persistence.queries;

import java.util.Collection;

import org.eclipse.persistence.internal.queries.ReportItem;

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
