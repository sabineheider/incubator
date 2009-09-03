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
 *     dclarke - Bug 273057: NestedFetchGroup Example
 ******************************************************************************/
package org.eclipse.persistence.queries;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.*;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.indirection.QueryBasedValueHolder;
import org.eclipse.persistence.internal.indirection.UnitOfWorkValueHolder;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.sessions.*;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.1.2
 */
@SuppressWarnings("serial")
public class NestedFetchGroup extends FetchGroup {

    private static final Class<?> NESTED_FG_CLASS = NestedFetchGroup.class;

    /** */
    private Map<String, FetchGroup> nestedGroups;

    public NestedFetchGroup() {
        this("");
    }

    /**
     * Constructor with a group name.
     */
    public NestedFetchGroup(String name) {
        super(name);
        this.nestedGroups = new HashMap<String, FetchGroup>();
    }

    public Map<String, FetchGroup> getNestedGroups() {
        return this.nestedGroups;
    }

    /**
     * Add a nested FetchGroup.
     * 
     * @param name
     *            is the name of the relationship attribute to apply the
     *            FetchGroup to.
     * @param group
     *            can be a valid FetchGroup for the related object or null to
     *            disable the use of the default FetchGroup
     * @return
     */
    public FetchGroup addGroup(String name, FetchGroup group) {
        getNestedGroups().put(name, group);
        addAttribute(name);
        return group;
    }

    public NestedFetchGroup addGroup(String name, NestedFetchGroup group) {
        getNestedGroups().put(name, group);
        addAttribute(name);
        return group;
    }

    private void addDescriptorListener(ClassDescriptor descriptor) {
        if (!descriptor.getEventManager().getDefaultEventListeners().contains(DESCRIPTOR_LISTENER)) {
            descriptor.getEventManager().addListener(DESCRIPTOR_LISTENER);
        }
    }

    public void setAsDefault(Session session, Class<?> entityClass) {
        ClassDescriptor descriptor = session.getClassDescriptor(entityClass);

        if (descriptor == null) {
            throw new IllegalArgumentException("No descriptor found for class: '" + entityClass + "' on session: " + session);
        }

        setAsDefault(descriptor);
    }

    public void setAsDefault(ClassDescriptor descriptor) {
        FetchGroupManager fgm = descriptor.getFetchGroupManager();
        if (fgm == null) {
            fgm = new FetchGroupManager();
            descriptor.setFetchGroupManager(fgm);
        }

        fgm.setDefaultFetchGroup(this);

        ObjectLevelReadQuery query = descriptor.getDescriptorQueryManager().getReadObjectQuery();
        if (query != null) {
            query.setFetchGroup(this);
        }

        query = descriptor.getDescriptorQueryManager().getReadAllQuery();
        if (query != null) {
            query.setFetchGroup(this);
        }
    }

    /**
     * Populate nested {@link FetchGroup} into the relationship (target) query.
     * this method is called from ForeignReferenceMapping when the objects are
     * being built from rows.
     * 
     * @see ForeignReferenceMapping#valueFromRowInternal
     */
    private void applyNestedGroups(ClassDescriptor descriptor, Object source) {
        for (String attributeName : getNestedGroups().keySet()) {
            FetchGroup nestedGroup = getNestedGroups().get(attributeName);
            DatabaseMapping mapping = descriptor.getMappingForAttributeName(attributeName);

            if (mapping.isForeignReferenceMapping() && ((ForeignReferenceMapping) mapping).usesIndirection()) {
                Object indirect = mapping.getAttributeValueFromObject(source);
                ObjectLevelReadQuery query = null;

                if (indirect instanceof IndirectContainer) {
                    indirect = ((IndirectContainer) indirect).getValueHolder();
                }

                if (indirect instanceof UnitOfWorkValueHolder) {
                    indirect = ((UnitOfWorkValueHolder) indirect).getWrappedValueHolder();
                }

                if (indirect instanceof QueryBasedValueHolder && !((ValueHolderInterface) indirect).isInstantiated()) {
                    ReadQuery rq = ((QueryBasedValueHolder) indirect).getQuery();
                    if (rq.isObjectLevelReadQuery()) {
                        query = (ObjectLevelReadQuery) rq;
                    }
                }

                if (query != null) {
                    query.setFetchGroup(nestedGroup);

                    // For cases where the nested FetchGroup is null this
                    // setting will prevent the default FetchGroup from being
                    // used.
                    query.setShouldUseDefaultFetchGroup(false);
                } else {
                    // TODO: Could not get query
                }
            } else {
                // TODO: Log warning that nested FG could not be applied since
                // mapping is not a relationship or it does not use indirection
            }
        }
    }

    private static final NestedFetchGroupListener DESCRIPTOR_LISTENER = new NestedFetchGroupListener();

    static class NestedFetchGroupListener extends DescriptorEventAdapter {

        /**
         * @see org.eclipse.persistence.descriptors.DescriptorEventAdapter#postBuild(org.eclipse.persistence.descriptors.DescriptorEvent)
         */
        @Override
        public void postBuild(DescriptorEvent event) {
            if (event.getQuery().isObjectLevelReadQuery() && ((ObjectLevelReadQuery) event.getQuery()).getFetchGroup() != null
                    && ((ObjectLevelReadQuery) event.getQuery()).getFetchGroup().getClass() == NESTED_FG_CLASS) {
                NestedFetchGroup fetchGroup = (NestedFetchGroup) ((ObjectLevelReadQuery) event.getQuery()).getFetchGroup();
                fetchGroup.applyNestedGroups(event.getDescriptor(), event.getSource());
            }
        }

    }

    static class SessionListener extends SessionEventAdapter {

        @Override
        public void preExecuteQuery(SessionEvent event) {
            if (event.getQuery().isObjectLevelReadQuery()) {
                ObjectLevelReadQuery readQuery = (ObjectLevelReadQuery) event.getQuery();

                if (readQuery.getFetchGroup() != null && readQuery.getFetchGroup().getClass() == NESTED_FG_CLASS) {
                    ((NestedFetchGroup) readQuery.getFetchGroup()).addDescriptorListener(readQuery.getDescriptor());
                } else if (readQuery.getFetchGroupName() != null) {
                    FetchGroupManager manager = event.getSession().getClassDescriptor(readQuery.getReferenceClass()).getFetchGroupManager();

                    if (manager != null && manager.getFetchGroup(readQuery.getFetchGroupName()) != null && manager.getFetchGroup(readQuery.getFetchGroupName()).getClass() == NESTED_FG_CLASS) {
                        ((NestedFetchGroup) manager.getFetchGroup(readQuery.getFetchGroupName())).addDescriptorListener(readQuery.getDescriptor());
                    }
                }
            }
        }

    }

    private static final SessionListener SESSION_LISTENER = new SessionListener();

    public static class Customizer implements SessionCustomizer {

        public void customize(Session session) {
            if (!session.getEventManager().getListeners().contains(SESSION_LISTENER)) {
                session.getEventManager().addListener(SESSION_LISTENER);
            }
        }

    }
}
