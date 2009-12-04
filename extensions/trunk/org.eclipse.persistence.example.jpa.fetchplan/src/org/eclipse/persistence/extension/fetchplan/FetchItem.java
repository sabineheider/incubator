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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.indirection.BatchValueHolder;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.sessions.Session;

/**
 * A FetchItem represents a single relationship within a {@link FetchPlan} that
 * is to be loaded after the query is executed.
 * 
 * @see FetchPlan
 * 
 * @author dclarke
 * @since EclipseLink 1.1.2
 */
public class FetchItem {

    protected String[] attributeNames;

    private transient DatabaseMapping[] mappings;

    public FetchItem(String... attributePaths) {
        if (attributePaths.length == 1 && attributePaths[0].contains(".")) {
            this.attributeNames = attributePaths[0].split("\\.");
        } else {
            this.attributeNames = attributePaths;
        }
    }

    public String[] getAttributeNames() {
        return this.attributeNames;
    }

    protected void instantiate(Class<?> entityType, Object result, Session session) {
        Collection<Object> results = getInitialResults(result);
        DatabaseMapping[] mappings = getMappings(entityType, session);

        Collection<Object> currentEntities = results;
        for (int index = 0; index < mappings.length; index++) {
            DatabaseMapping mapping = mappings[index];
            currentEntities = instantiate(mapping, currentEntities, index == (mappings.length - 1));
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> getInitialResults(Object result) {
        if (result instanceof Collection<?>) {
            return (Collection<Object>) result;
        }

        if (result instanceof Map<?, ?>) {
            return ((Map<Object, Object>) result).values();
        }

        Collection<Object> results = new ArrayList<Object>();
        results.add(result);
        return results;
    }

    /**
     * TODO: ?
     * 
     * @param entity
     * @return
     */
    protected Object getEntityValue(Object entity) {
        return entity;
    }

    /**
     * TODO
     */
    private Collection<Object> instantiate(DatabaseMapping mapping, Collection<Object> entities, boolean isLast) {
        HashSet<Object> results = new HashSet<Object>();

        for (Object current : entities) {
            Object result = mapping.getAttributeValueFromObject(getEntityValue(current));
            boolean batching = false;

            if (result instanceof IndirectContainer) {
                result = ((IndirectContainer) result).getValueHolder();
            }
            if (result instanceof ValueHolderInterface) {
                batching = result instanceof BatchValueHolder;
                result = ((ValueHolderInterface) result).getValue();
            }
            if (mapping.isForeignReferenceMapping()) {
                if (result instanceof Collection<?>) {
                    // If batching was used and this is the last mapping then
                    // only add the first result
                    if (batching && isLast && ((Collection<?>) result).size() > 0) {
                        // TODO: Optimize?
                        results.add(((Collection<?>) result).iterator().next());
                    } else {
                        results.addAll((Collection<?>) result);
                    }
                } else if (result != null) {
                    results.add(result);
                }
            }
        }

        return results;
    }

    /**
     * NOTE: Only returns the mappings after they have been lazily initialized
     * by {@link #instantiate(Class, Collection, Session)}
     * 
     * @return
     */
    public DatabaseMapping[] getMappings() {
        return this.mappings;

    }

    /**
     * Collect the mappings ... TODO
     * 
     * @param entityType
     * @param session
     * @return
     */
    protected DatabaseMapping[] getMappings(Class<?> entityType, Session session) {
        if (this.mappings != null) {
            return this.mappings;
        }

        ClassDescriptor descriptor = session.getClassDescriptor(entityType);
        // TODO - remove alias assumption
        DatabaseMapping[] mappingsFound = new DatabaseMapping[getAttributeNames().length - 1];

        for (int index = 1; index < getAttributeNames().length; index++) {
            String attributeName = getAttributeNames()[index];
            DatabaseMapping mapping = descriptor.getMappingForAttributeName(attributeName);

            if (mapping == null) {
                throw new IllegalStateException("Could not find mapping named '" + attributeName + "' on: " + descriptor);
            }

            if (mapping.isForeignReferenceMapping()) {
                descriptor = ((ForeignReferenceMapping) mapping).getReferenceDescriptor();
            } else {
                if (index < (getAttributeNames().length - 1)) {
                    throw new IllegalStateException("Mapping not a relationship: '" + attributeName + "' on: " + descriptor);
                }
            }
            mappingsFound[index - 1] = mapping;
        }

        this.mappings = mappingsFound;
        return this.mappings;
    }

}
