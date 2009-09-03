package org.eclipse.persistence.queries;

import java.util.Collection;
import java.util.Map;

public class ReadAllFetchItem extends FetchItem {

    protected ReadAllFetchItem(ReadAllQuery query, String attributePath) {
        super(attributePath);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Object> getInitialResults(Object result) {
        if (result instanceof Collection<?>) {
            return (Collection<Object>) result;
        }
        
        if (result instanceof Map<?,?>) {
            return ((Map<Object, Object>) result).values();
        }
        
        throw new IllegalStateException("Could not convert result to collection: " + result);
    }
}
