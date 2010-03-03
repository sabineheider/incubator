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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Query;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.exceptions.QueryException;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.queries.FetchGroup;
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
     * Name for this FetchPlan. Offers some assistance with debugging as name is
     * used in log messages. Also align with future FetchGroup enhancements
     * where named fetchGroups are stored and available for use.
     */
    private String name;

    /**
     * Map of items where each item represents an attribute to be fetched/copied
     * depending on the usage of the plan.
     * 
     * @see FetchItem
     */
    private Map<String, FetchItem> items;

    private Class<?> entityClass;

    /**
     * 
     */
    private ClassDescriptor descriptor;

    /**
     * Indicates if the minimal attributes required of a {@link FetchGroup}
     * should be added to this FetchPlan automatically when initialized.
     */
    private boolean addRequiredAttributes = true;

    /**
     * {@link SessionLog} category used for messages logged during the use of a
     * FetchPlan. To enable all messages in this category configure the
     * persistence unit property:
     * <p>
     * <code>
     * <property name =eclipselink.logging.level.fetch_plan" value="ALL" />
     * </code>
     */
    public static final String LOG_CATEGORY = "fetch_plan";

    public FetchPlan(String name, Class<?> entityClass, boolean addRequiredAttributes) {
        this.name = name;
        this.entityClass = entityClass;
        this.addRequiredAttributes = addRequiredAttributes;
        this.items = new HashMap<String, FetchItem>();
    }

    public FetchPlan(String name, Class<?> entityClass) {
        this(name, entityClass, true);
    }

    public FetchPlan(Class<?> entityClass) {
        this(null, entityClass);
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getEntityClass() {
        return this.entityClass;
    }

    public boolean addRequiredAttributes() {
        return this.addRequiredAttributes;
    }

    public void setAddRequiredAttributes(boolean value) {
        this.addRequiredAttributes = value;
    }

    /**
     * Used in {@link FetchItem#initialize(Session)} to populate the target
     * entity type of relationships from the mapping.
     */
    protected void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    protected Map<String, FetchItem> getItems() {
        return this.items;
    }

    /**
     * @return a read-only collection of {@link FetchItem} in this plan.
     */
    public Collection<FetchItem> getFetchItems() {
        return getItems().values();
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
        if (this.descriptor != null) {
            return;
        }

        if (this.entityClass == null) {
            throw new IllegalStateException("FetchPlan.initialize: Null entityClass found");
        }

        this.descriptor = session.getClassDescriptor(getEntityClass());
        if (this.descriptor == null) {
            throw new IllegalStateException("No descriptor found for class: " + getEntityClass());
        }

        if (session.getSessionLog().shouldLog(SessionLog.FINER, LOG_CATEGORY)) {
            SessionLogEntry entry = new SessionLogEntry(SessionLog.FINEST, LOG_CATEGORY, (AbstractSession) session, "FetchPlan: initializing {0}", new Object[] { this }, null, false);
            session.getSessionLog().log(entry);
        }

        // Add identifier and optimistic locking version attributes
        if (addRequiredAttributes()) {
            for (DatabaseMapping mapping : this.descriptor.getObjectBuilder().getPrimaryKeyMappings()) {
                if (!getItems().containsKey(mapping.getAttributeName())) {
                    addAttribute(mapping.getAttributeName());

                    if (session.getSessionLog().shouldLog(SessionLog.FINEST, LOG_CATEGORY)) {
                        SessionLogEntry entry = new SessionLogEntry(SessionLog.FINEST, LOG_CATEGORY, (AbstractSession) session, "FetchPlan: Added required id attribute {0} to {1}", new Object[] { mapping.getAttributeName(), this }, null, false);
                        session.getSessionLog().log(entry);
                    }
                }
            }

            if (this.descriptor.usesOptimisticLocking()) {
                DatabaseField lockField = this.descriptor.getOptimisticLockingPolicy().getWriteLockField();
                if (lockField != null) {
                    DatabaseMapping lockMapping = this.descriptor.getObjectBuilder().getMappingForField(lockField);
                    if (lockMapping != null && !getItems().containsKey(lockMapping.getAttributeName())) {
                        addAttribute(lockMapping.getAttributeName());

                        if (session.getSessionLog().shouldLog(SessionLog.FINEST, LOG_CATEGORY)) {
                            SessionLogEntry entry = new SessionLogEntry(SessionLog.FINEST, LOG_CATEGORY, (AbstractSession) session, "FetchPlan: Added required lock attribute {0} to {1}", new Object[] { lockMapping.getAttributeName(), this }, null, false);
                            session.getSessionLog().log(entry);
                        }
                    }
                }
            }
        }

        for (FetchItem item : getItems().values()) {
            item.initialize(session);
        }
    }

    protected ClassDescriptor getDescriptor(Session session) {
        initialize(session);
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
    public FetchItem addAttribute(String... nameOrPath) {
        String[] attributePaths = convert(nameOrPath);
        FetchPlan currentFP = this;
        FetchItem item = null;

        for (int index = 0; index < attributePaths.length; index++) {
            String attrName = attributePaths[index];
            item = (FetchItem) currentFP.getItems().get(attrName);

            if (item == null) {
                item = new FetchItem(currentFP, attrName);
                currentFP.getItems().put(attrName, item);
            }

            currentFP = item.getFetchPlan();
            if (currentFP == null && index < (attributePaths.length - 1)) {
                currentFP = new FetchPlan(getName() + "-" + attrName, null, addRequiredAttributes());
                item.setFetchPlan(currentFP);
            }
        }

        return item;
    }

    /**
     * TODO
     */
    public boolean containsAttribute(String... attributeNameOrPath) {
        // TODO
        return false;
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
        initialize(session);

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

    /**
     * Make a copy of the provided source (entity or collection of
     * entities)copying only the attributes specified in this plan. If a
     * relationship does not specify a target
     * 
     * @param <T>
     * @param source
     * @param session
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T copy(T source, AbstractSession session) {
        initialize(session);

        if (source == null) {
            return null;
        }

        return (T) copy(source, session, new HashMap<Object, Object>());
    }

    /**
     * Create a new collection of the same type with the same size. This is done
     * using reflection
     */
    protected static Collection<?> createEmptyContainer(Collection<?> source) {
        Constructor<?> constructor = null;

        try {
            constructor = source.getClass().getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("FetchPlan.copy: Cannot create new collection of type: " + source.getClass(), e);
        }

        Collection<?> newCollection = null;

        try {
            newCollection = (Collection<?>) constructor.newInstance(source.size());
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return newCollection;
    }

    /**
     * TODO
     * 
     * @param source
     * @param session
     * @param copies
     * @return
     */
    protected Object copy(Object source, AbstractSession session, Map<Object, Object> copies) {
        if (source instanceof Object[]) {
            throw new IllegalArgumentException("Fetchplan.copy does not support Object[]");
        }

        Object copy = copies.get(source);
        if (copy != null) {
            return copy;
        }

        if (source instanceof Collection<?>) {
            Collection copiesCollection = createEmptyContainer((Collection<?>) source);

            for (Object entity : (Collection<?>) source) {
                copy = copy(entity, session, copies);
                copiesCollection.add(copy);
            }
            copies.put(source, copiesCollection);
            return copiesCollection;
        }

        copy = getDescriptor(session).getInstantiationPolicy().buildNewInstance();
        copies.put(source, copy);

        ObjectCopyingPolicy policy = new ObjectCopyingPolicy();
        policy.setShouldResetPrimaryKey(false);
        policy.setSession(session);

        for (Map.Entry<String, FetchItem> entry : getItems().entrySet()) {
            entry.getValue().copy(source, copy, session, policy, copies);
        }

        return copy;
    }

    /**
     * Do a partial merge of the provided entity into the UnitOfWork using the
     * items specified in this FetchPlan.
     * 
     * @return the working copy for the entity that may include additional
     *         attributes then what was merged.
     */
    @SuppressWarnings("unchecked")
    public <T> T merge(T entity, UnitOfWork uow) {
        T workingCopy = (T) uow.readObject(entity);

        for (Map.Entry<String, FetchItem> entry : getItems().entrySet()) {
            entry.getValue().merge(entity, workingCopy, (UnitOfWorkImpl) uow);
        }

        return workingCopy;
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
        return "FetchPlan(" + getName() + ")";
    }

}
