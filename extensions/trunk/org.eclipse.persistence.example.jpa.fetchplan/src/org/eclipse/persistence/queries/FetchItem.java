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
 *     dclarke - Bug ?: FetchPlan Example
 *     ssmith  - various minor edits
 ******************************************************************************/
package org.eclipse.persistence.queries;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.Session;

/**
 * FetchPlan handles specifying a set of relationships in a query result that
 * need to be instantiated on a given query result.
 * 
 * @author dclarke
 * @since EclipseLink 1.1.2
 */
public class FetchItem {

    private String[] attributeNames;

    public FetchItem(String attributePath) {
        this.attributeNames = attributePath.split("\\.");
    }

    public String[] getAttributeNames() {
        return this.attributeNames;
    }

    protected void instantiate(Class<?> entityType, Object result, Session session) {
        instantiate(entityType, getInitialResults(result), 1, session);
    }

    protected Object getEntityValue(Object entity) {
        return entity;
    }

    protected Collection<Object> getInitialResults(Object result) {
        Collection<Object> results = new HashSet<Object>();
        results.add(result);
        return results;
    }

    /**
     * Recursive method which instantiates all specified relationships
     * collection results for use in next level of graph.
     * 
     * @param entities
     * @param index
     * @param session
     */
    protected void instantiate(Class<?> entityType, Collection<Object> entities, int index, Session session) {
        // Skip out of recursion if the no entities provided or index past
        // attributeNames size
        if (entities.isEmpty() || index >= getAttributeNames().length) {
            return;
        }

        ClassDescriptor descriptor = session.getClassDescriptor(entityType);
        String attributeName = getAttributeNames()[index];
        DatabaseMapping mapping = descriptor.getMappingForAttributeName(attributeName);

        if (mapping == null) {
            throw new IllegalStateException("Could not find mapping named '" + attributeName + "' on: " + descriptor);
        }

        Collection<Object> results = Collections.emptyList();

        // If this is the last level then don't collect results
        if (index != (getAttributeNames().length - 1) && mapping.getReferenceDescriptor() != null) {
            results = new HashSet<Object>();
        }

        if (descriptor == null) {
            throw new RuntimeException("Class is not Entity--Descriptor not found for " + entityType.getName());
        }

        if (mapping == null) {
            throw new IllegalArgumentException(attributeName + " is not a attribute of " + descriptor.getAlias());
        }
        for (Object entity : entities) {
            Object result = mapping.getAttributeValueFromObject(getEntityValue(entity));

            if (result instanceof IndirectContainer) {
                result = ((IndirectContainer) result).getValueHolder();
            }
            if (result instanceof ValueHolderInterface) {
                result = ((ValueHolderInterface) result).getValue();
            }
            addResults(results, result);
        }

        if (!results.isEmpty()) {
            instantiate(mapping.getReferenceDescriptor().getJavaClass(), results, index + 1, session);
        }
    }

    private void addResults(Collection<Object> results, Object newResult) {
        if (results == Collections.EMPTY_LIST || newResult == null) {
            return;
        }

        if (newResult instanceof Collection<?>) {
            results.addAll((Collection<?>) newResult);
        } else if (newResult instanceof Map<?, ?>) {
            results.addAll(((Map<?, ?>) newResult).values());
        } else {
            results.add(newResult);
        }
    }
}
