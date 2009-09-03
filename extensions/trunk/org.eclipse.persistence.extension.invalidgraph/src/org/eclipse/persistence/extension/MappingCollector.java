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

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.Session;

/**
 * A simple utility to collect mappings based on specified criteria into a cache
 * for more optimal use in other processing. Typically used in event processing.
 * 
 * @author dclarke
 * @since EclipseLink Extensions Incubator using 1.1.2
 */
public class MappingCollector {

    /**
     * Cache of relationship mappings by persistent type.
     */
    private HashMap<Class<?>, Collection<DatabaseMapping>> mappings;

    /**
     * Criteria provided. This is held in case lazy collecting is required for
     * descriptors added to the session after login.
     */
    private Criteria criteria;

    public MappingCollector(Session session, Criteria criteria) {
        this.mappings = new HashMap<Class<?>, Collection<DatabaseMapping>>();
        this.criteria = criteria;
        collectMappings(session);
    }

    public HashMap<Class<?>, Collection<DatabaseMapping>> getMappings() {
        return this.mappings;
    }

    public Criteria getCriteria() {
        return this.criteria;
    }

    /**
     * Lookup the relationship mappings for the provided entity. If there are
     * none in the cache lazily create the collection of mappings. The lazy
     * creation of the collection should only happen for descriptors that were
     * added to the session after login, which is not very typical but allowed
     * to support some advanced dynamic persistence scenarios.
     */
    public Collection<DatabaseMapping> getMappings(Session session, Class<?> entityClass) {
        Collection<DatabaseMapping> mappings = getMappings().get(entityClass);

        if (mappings == null) {
            ClassDescriptor descriptor = session.getClassDescriptor(entityClass);

            if (descriptor != null) {
                collectMappings(descriptor);
            }
        }

        return mappings;
    }

    /**
     * 
     * @param session
     */
    private void collectMappings(Session session) {
        for (Iterator<?> i = session.getDescriptors().values().iterator(); i.hasNext();) {
            ClassDescriptor descriptor = (ClassDescriptor) i.next();

            if (getCriteria().collect(descriptor)) {
                collectMappings(descriptor);
            }
        }
    }

    /**
     * Collect mappings for the specified descriptor based on
     * {@link Criteria#collect(DatabaseMapping)}
     */
    private void collectMappings(ClassDescriptor descriptor) {
        Collection<DatabaseMapping> mappings = new ArrayList<DatabaseMapping>();
        for (DatabaseMapping mapping : descriptor.getMappings()) {
            if (getCriteria().collect(mapping)) {
                mappings.add(mapping);
            }
        }
        getMappings().put(descriptor.getJavaClass(), mappings);
    }

    /**
     * 
     */
    public interface Criteria {
        /**
         * Determines if the provided descriptor should be traversed collection
         * mappings. If true then a collection of mappings is collected for the
         * descriptor. If false an empty collection is placed in the collected
         * map.
         */
        boolean collect(ClassDescriptor descriptor);

        /**
         * Determines if the provided mapping should be collected.
         */
        boolean collect(DatabaseMapping mapping);
    }
}
