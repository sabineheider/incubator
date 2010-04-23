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

import javax.persistence.Transient;

import org.eclipse.persistence.exceptions.QueryException;
import org.eclipse.persistence.extension.fetchplan.FetchPlan.CopyTrace;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.ObjectCopyingPolicy;
import org.eclipse.persistence.sessions.Session;

/**
 * A FetchItem refers to a single attribute in a FetchPlan that is used when
 * fetching, copying, or merging entities. A FetchItem can be used for a basic
 * (direct-to-field) attribute as well as relationships (entity->entity or
 * entity->embedded) with the FetchItem optionally holding a FetchGroup
 * indicating how that related entity or embeddable should be treated.
 * <p>
 * This class is mostly internal to the FetchPlan but is made public so that
 * users can interrogate the state of the FetchPlan and its nested FetchItems.
 * These can only be created through the use of
 * {@link FetchPlan#addAttribute(String...)}
 * 
 * @author dclarke
 * @since EclipseLink 1.2
 */
public class FetchItem {

    /**
     * @see #getName()
     */
    private String name;

    /**
     * @see #getParent()
     */
    private FetchPlan parent;

    /**
     * @see #getFetchPlan()
     */
    private FetchPlan fetchPlan;

    /**
     * Optimization to hold a reference to the database mapping for the
     * {@link #name} on the descriptor of the {@link #parent}.
     */
    @Transient
    private DatabaseMapping mapping;

    protected FetchItem(FetchPlan parent, String name) {
        this.parent = parent;
        this.name = name;
    }
    
    protected FetchItem(FetchPlan parent, DatabaseMapping mapping) {
        this(parent, mapping.getAttributeName());
        this.mapping = mapping;
    }
    
    /**
     * Name of the attribute. This is a simple name corresponding to a mapped
     * attribute of the entity type this FetchItem is applied to.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Reference to the owning {@link FetchItem} which has the attribute mapped
     * for {@link #getName()}
     */
    public FetchPlan getParent() {
        return parent;
    }

    /**
     * Optional FetchPlan that is used to describe how a related entity or
     * embeddable should be fetched. This FetchPlan is only used in
     * relationships and is ignored in basic (direct-to-field) mapping scenarios
     * or in operations where the entity being processed has a null or empty
     * collection.
     */
    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }

    public void setFetchPlan(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
    }

    protected DatabaseMapping getMapping(Session session) {
        if (this.mapping == null) {
            initialize(session);
        }
        return mapping;
    }

    /*
     * Initialize this item to lookup its mapping and cascade initialize its
     * nested FetchPlan
     */
    protected void initialize(Session session) {
        this.mapping = getParent().getDescriptor(session).getMappingForAttributeName(getName());

        if (this.mapping == null) {
            throw QueryException.fetchGroupAttributeNotMapped(getName());
        }

        if (getFetchPlan() != null) {
            // If there is no ref descriptor then there should not be a
            // FetchPlan
            if (this.mapping.getReferenceDescriptor() == null) {
                throw new RuntimeException("FetchPlan initialize: FetchItem found with FetchPlan for " + this.mapping);
            }

            getFetchPlan().setEntityClass(this.mapping.getReferenceDescriptor().getJavaClass());
            getFetchPlan().initialize(session);
        } 
    }

    /**
     * Force the mapped attribute to be loaded if it is not null and is
     * assignable to the type of the item's plan.
     */
    protected void fetch(Object entity, AbstractSession session) {
        if (entity != null && getParent().getEntityClass().isAssignableFrom(entity.getClass())) {
            getMapping(session).instantiateAttribute(entity, session);

            if (getFetchPlan() != null) {
                Object target = mapping.getRealAttributeValueFromObject(entity, session);
                if (target != null) {
                    getFetchPlan().fetch(target, session);
                }
            }
        }
    }

    /**
     * Populate the copy with a copy or value (depending on type) of this mapped
     * attribute
     */
    protected void copy(Object source, Object target, AbstractSession session, ObjectCopyingPolicy policy, CopyTrace copies) {
        copy(source, target, getMapping(session), getFetchPlan(), session, policy, copies);
    }

    /**
     * 
     */
    private void copy(Object source, Object target, DatabaseMapping mapping, FetchPlan targetFetchPlan, AbstractSession session, ObjectCopyingPolicy policy, CopyTrace copies) {
        if (mapping.getReferenceDescriptor() != null) {
            Object sourceValue = mapping.getRealAttributeValueFromObject(source, session);
            Object copyValue = null;

            if (sourceValue != null) {
                if (targetFetchPlan == null) {
                    targetFetchPlan = FetchPlan.defaultFetchPlan(mapping);
                }
                copyValue = targetFetchPlan.copy(sourceValue, session, copies);
                mapping.setRealAttributeValueInObject(target, copyValue);
            }
        } else {
            mapping.buildCopy(target, source, policy);
        }
    }

    public String toString() {
        return "FetchItem(" + getName() + ")";
    }

}
