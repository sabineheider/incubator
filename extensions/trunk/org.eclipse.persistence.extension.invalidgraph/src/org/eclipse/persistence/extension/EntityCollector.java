/*******************************************************************************
 * Copyright (c) 1998, 2008 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     dclarke - Bug 288363: Extensions Incubator - RefreshInvalidGraphListener
 *               http://wiki.eclipse.org/EclipseLink/Development/Incubator/Extensions/RefreshInvalidGraphListener
 ******************************************************************************/
package org.eclipse.persistence.extension;

import java.util.*;

import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.Session;


/**
 * An example EclipseLink utility that can walk a graph of persistent entities
 * using the provided mappings to collect the objects in the related graph that
 * match the provided criteria.
 * 
 * @author dclarke
 * @since EclipseLink 1.1.2
 */
public class EntityCollector {

    private Set<Object> visitedEntities = new HashSet<Object>();

    private Map<Class<?>, Collection<Object>> entities = new HashMap<Class<?>, Collection<Object>>();

    private MappingCollector mappings;

    /**
     * 
     * @param session
     * @param mappings
     * @param criteria
     * @param root
     */
    public EntityCollector(Session session, MappingCollector mappings, Criteria criteria, Object root) {
        this.visitedEntities = new HashSet<Object>();
        this.mappings = mappings;
        this.entities = new HashMap<Class<?>, Collection<Object>>();

        collect(session, criteria, root, null);
    }

    public Set<Object> getVisitedEntities() {
        return visitedEntities;
    }

    public Map<Class<?>, Collection<Object>> getInvalidEntities() {
        return entities;
    }

    public MappingCollector getMappings() {
        return mappings;
    }

    private void collectEntity(Object entity) {
        Collection<Object> entities = getInvalidEntities().get(entity.getClass());

        if (entities == null) {
            entities = new ArrayList<Object>();
            getInvalidEntities().put(entity.getClass(), entities);
        }
        entities.add(entity);
    }

    private Collection<DatabaseMapping> getMappings(Session session, Object entity) {
        if (entity == null) {
            return null;
        }
        return getMappings().getMappings(session, entity.getClass());
    }

    /**
     * 
     * @param session
     * @param results
     * @param mapping
     */
    @SuppressWarnings("unchecked")
    private void collect(Session session, Criteria criteria, Object results, DatabaseMapping mapping) {
        if (results instanceof Collection<?>) {
            for (Object entity : (Collection<Object>) results) {
                collect(session, criteria, entity, mapping);
            }
            return;
        }

        if (results instanceof Map<?,?>) {
            for (Object entity : ((Map<?,?>) results).values()) {
                collect(session, criteria, entity, mapping);
            }
            return;
        }

        Object entity = results;

        if (visitedEntities.contains(entity)) {
            return;
        }
        visitedEntities.add(entity);

        if (criteria.collect(session, mapping, entity)) {
            collectEntity(entity);
        }

        // Walk the mapped relationships
        Collection<DatabaseMapping> frms = getMappings(session, entity);
        if (frms != null) {
            for (DatabaseMapping relMapping : getMappings(session, entity)) {
                Object relatedEntity = relMapping.getAttributeValueFromObject(entity);

                if (relatedEntity instanceof ValueHolderInterface) {
                    ValueHolderInterface vh = (ValueHolderInterface) relatedEntity;
                    if (vh.isInstantiated() && vh.getValue() != null) {
                        collect(session, criteria, vh.getValue(), relMapping);
                    }
                } else if (relatedEntity instanceof IndirectContainer) {
                    IndirectContainer ic = (IndirectContainer) relatedEntity;
                    if (ic.isInstantiated()) {
                        collect(session, criteria, ic.getValueHolder().getValue(), relMapping);
                    }
                } else {
                    collect(session, criteria, relatedEntity, relMapping);
                }
            }
        }
    }

    public interface Criteria {

        boolean collect(Session session, DatabaseMapping mapping, Object entity);
    }
}
