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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.Transient;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.exceptions.QueryException;
import org.eclipse.persistence.internal.descriptors.changetracking.ObjectChangeListener;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.ObjectChangeSet;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.sessions.ObjectCopyingPolicy;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;

/**
 * A FetchPlan ... TODO FetchPlan handles specifying a set of relationships in a
 * query result that need to be instantiated on a given query result. The
 * FetchPlan is associated with a query through its properties where it is
 * stored using the FetchPlan's class name as a key.
 * <p>
 * A FetchPlan is created/retrieved from a query using the
 * {@link #getFetchPlan(Query)} or {@link #getFetchPlan(ObjectLevelReadQuery)}
 * methods. Relationships that are to be loaded can then be added using
 * {@link #addFetchItem(String...)} which creates a {@link FetchItem} within the
 * plan for the requested relationship in the results graph.
 * 
 * @author dclarke
 * @since EclipseLink 2.1
 */
public class FetchPlan {

    /**
     * Map of items where each item represents an attribute to be fetched/copied
     * depending on the usage of the plan.
     * 
     * @see FetchItem
     */
    private Map<String, FetchItem> items;

    private Class<?> entityClass;

    @Transient
    private ClassDescriptor descriptor;

    public FetchPlan(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.items = new HashMap<String, FetchItem>();
    }

    public Class<?> getEntityClass() {
        return this.entityClass;
    }

    /**
     * Used in {@link FetchItem#initialize(Session)}
     */
    protected void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    protected Map<String, FetchItem> getItems() {
        return this.items;
    }

    /**
     * 
     * @return a read-only collection of {@link FetchItem} in this plan.
     */
    public Collection<FetchItem> getFetchItems() {
        return getItems().values();
    }

    public boolean containsAttribute(String... attributeNameOrPath) {
        // TODO
        return false;
    }

    /**
     * Initialize the FetchPlan and all of its FetchItem and nested FetchPlan to
     * lookup and hold their corresponding descriptors and mappings. If any of
     * the FetchPlan's state does not match the mappings an exception will be
     * raised.
     * <p>
     * This is used within {@link #getDescriptor(Session)} to lazily lookup the
     * {@link ClassDescriptor} but it can also be used externally to validate
     * the configuration of the FetchPlan.
     * 
     * @throws IllegalStateException
     *             if {@link #entityClass} is null or no descriptor can be found
     *             for the provided class
     * @throws QueryException
     *             if any of the items or nested items cannot be associated with
     *             a mapping
     */
    public void initialize(Session session) {
        if (this.entityClass == null) {
            throw new IllegalStateException("FetchPlan.initialize: Null entityClass found");
        }

        this.descriptor = session.getClassDescriptor(getEntityClass());

        if (this.descriptor == null) {
            throw new IllegalStateException("No descriptor found for class: " + getEntityClass());
        }

        for (FetchItem item : getItems().values()) {
            item.initialize(session);
        }
    }

    protected ClassDescriptor getDescriptor(Session session) {
        if (this.descriptor == null) {
            initialize(session);
        }
        return this.descriptor;
    }

    /**
     * Add an item to be fetched
     * <p>
     * If a single string is supplied then it is assumed to either be an
     * attribute name for the only entity type being returned from the query or
     * it is a path expression with attribute names separated by '.' and the
     * first part represents the alias or item name in the select of the query.
     * <p>
     * If multiple strings are provided it is assumed that they are all mapped
     * attribute names and the query is returning a single entity type.
     * 
     * @param attributePath
     * @return
     */
    public void addAttribute(String... nameOrPath) {
        String[] attributePaths = convert(nameOrPath);
        FetchPlan currentFP = this;

        for (int index = 0; index < attributePaths.length; index++) {
            String attrName = attributePaths[index];
            FetchItem item = (FetchItem) currentFP.getItems().get(attrName);

            if (item == null) {
                item = new FetchItem(currentFP, attrName);
                currentFP.getItems().put(attrName, item);
            }

            currentFP = item.getFetchPlan();
            if (currentFP == null && index < (attributePaths.length - 1)) {
                currentFP = new FetchPlan(null);
                item.setFetchPlan(currentFP);
            }
        }
    }

    /**
     * Convert a provided name or path which could be a single attributeName, a
     * single string with dot separated attribute names, or an array of
     * attribute names defining the path.
     */
    private String[] convert(String... nameOrPath) {
        if (nameOrPath == null || nameOrPath.length == 0 || (nameOrPath.length == 1 && (nameOrPath[0] == null || nameOrPath[0].isEmpty()))) {
            throw new IllegalArgumentException("FetchPlan: illegal attribute name or path: '" + nameOrPath + "'");
        }

        String[] path = nameOrPath;
        if (nameOrPath.length > 1 || !nameOrPath[0].contains(".")) {
            path = nameOrPath;
        } else {
            if (nameOrPath[0].endsWith(".")) {
                throw new IllegalArgumentException("Invalid path: " + nameOrPath[0]);
            }
            path = nameOrPath[0].split("\\.");
        }

        if (path.length == 0) {
            throw new IllegalArgumentException("Invalid path: " + nameOrPath[0]);
        }

        for (int index = 0; index < path.length; index++) {
            if (path[index] == null || path[index].length() == 0 || !path[index].trim().equals(path[index])) {
                throw new IllegalArgumentException("Invalid path: " + nameOrPath[0]);
            }
        }
        return path;
    }

    /**
     * Instantiate all items ({@link FetchItem}) for the result provided. This
     * walks through the query result using the items and the session's mapping
     * metadata to populate all requested attributes and relationships.
     * <p>
     * This method ensures that the provided entity has at least the attributes
     * specified in the plan loaded. Additional attributes may already have been
     * loaded or will be loaded through interaction with {@link FetchGroup}
     * behavior.
     */
    public void fetch(Object entity, AbstractSession session) {
        if (entity instanceof Collection<?>) {
            for (Object e : (Collection<?>) entity) {
                if (e instanceof Object[]) {
                    for (int index = 0; index < ((Object[]) e).length; index++) {
                        if (((Object[]) e)[index].getClass() == getEntityClass()) {
                            fetch(((Object[]) e)[index], session);
                        }
                    }
                } else {
                    fetch(e, session);
                }
            }
        } else {
            for (Map.Entry<String, FetchItem> entry : getItems().entrySet()) {
                entry.getValue().fetch(entity, session);
            }
        }
    }

    /**
     * Perform fetch on all entities in collection.
     * 
     * @see #fetch(Object, AbstractSession)
     */
    public void fetch(Collection<?> entities, AbstractSession session) {
        for (Object entity : entities) {
            fetch(entity, session);
        }
    }

    /**
     * Perform fetch on all entities in collection.
     * 
     * @see #fetch(Object, AbstractSession)
     */
    public void fetch(Collection<Object[]> entities, int resultIndex, AbstractSession session) {
        for (Object[] entity : entities) {
            fetch(entity[resultIndex], session);
        }
    }
    @SuppressWarnings("unchecked")
    public <T> T copy(T source, AbstractSession session) {
        if (source instanceof Collection) {
            throw new IllegalArgumentException("FetchPlan.copy does not support collections");
        }
        
        T copy = (T) getDescriptor(session).getInstantiationPolicy().buildNewInstance();
        ObjectCopyingPolicy policy = new ObjectCopyingPolicy();
        policy.setShouldResetPrimaryKey(false);
        policy.setSession(session);

        for (Map.Entry<String, FetchItem> entry : getItems().entrySet()) {
            entry.getValue().copy(source, copy, session, policy);
        }

        return copy;
    }
    
    /**
     * Do a partial merge of the provided entity into the UnitOfWork using the
     * items specified in this FetchPlan. The entity returned will be the
     */
    @SuppressWarnings("unchecked")
    public <T> T merge(T entity, UnitOfWork uow) {
        T workingCopy = (T) uow.readObject(entity);

        for (Map.Entry<String, FetchItem> entry : getItems().entrySet()) {
            entry.getValue().merge(entity, workingCopy, (UnitOfWorkImpl) uow);
        }

        return workingCopy;
    }

    private boolean canBePruned(Object entity) {
        return entity instanceof FetchGroupTracker;
    }

    /**
     * Walk through the graph starting from the entity and remove all items not
     * specified in this plan
     */
    public <T> T prune(T entity, Session session) {
        return (T) prune(entity, this, session);
    }

    protected <T> T prune(T entity, FetchPlan fetchPlan, Session session) {
        if (!canBePruned((T) entity)) {
            throw new IllegalArgumentException("TODO");
        }

        FetchGroup fg = ((FetchGroupTracker) entity)._persistence_getFetchGroup();
        if (fg == null) {
            fg = new FetchGroup();
            ((FetchGroupTracker) entity)._persistence_setFetchGroup(fg);
            ((FetchGroupTracker) entity)._persistence_setSession(session);
        }

        boolean usesChangeTracking = entity instanceof ChangeTracker && ((ChangeTracker) entity)._persistence_getPropertyChangeListener() != null;
        if (usesChangeTracking) {
            ((ObjectChangeListener) ((ChangeTracker) entity)._persistence_getPropertyChangeListener()).ignoreEvents();
        }

        for (DatabaseMapping mapping : getDescriptor(session).getMappings()) {
            if (!containsAttribute(mapping.getAttributeName()) && fg.containsAttribute(mapping.getAttributeName())) {
                mapping.setAttributeValueInObject(entity, null);
            }

            if ((mapping.isAggregateObjectMapping() || mapping.isForeignReferenceMapping()) && containsAttribute(mapping.getAttributeName())) {
                Object value = mapping.getRealAttributeValueFromObject(entity, (AbstractSession) session);
                prune(value, getItems().get(mapping.getAttributeName()).getFetchPlan(), session);
            }
        }

        if (usesChangeTracking) {
            ((ObjectChangeListener) ((ChangeTracker) entity)._persistence_getPropertyChangeListener()).processEvents();
        }

        return entity;
    }

    /**
     * Helper method that will set a FetchGroup on the provided query for the
     * immediate items. FetchGroups in the EclipseLInk 2.0 and earlier releases
     * only control attributes on the entity being queries and cannot be nested
     * to effect relationships.
     */
    public void setFetchGroup(Query query) {
        FetchGroup group = new FetchGroup(this.toString());
        for (String attrName : getItems().keySet()) {
            group.addAttribute(attrName);
        }
        query.setHint(QueryHints.FETCH_GROUP, group);
    }

    public String toString() {
        return "FetchPlan()";
    }

}
