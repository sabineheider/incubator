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
 ******************************************************************************/
package org.eclipse.persistence.extension.fetchplan;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.exceptions.QueryException;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.queries.QueryRedirector;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.ObjectCopyingPolicy;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;

/**
 * A FetchPlan is an extension to EclipseLink that allows a required graph of
 * entities to be returned from a query execution or copy operation as well as
 * being used to do a sparse merge of an unmanaged graph of entities into a
 * persistence context.
 * <p>
 * A FetchPlan is created/retrieved from a query using the
 * {@link #getFetchPlan(Query)} or {@link #getFetchPlan(ObjectLevelReadQuery)}
 * methods. Relationships that are to be loaded can then be added using
 * {@link #addFetchItem(String...)} which creates a {@link FetchItem} within the
 * plan for the requested relationship in the results graph.
 * 
 * @author dclarke
 * @since TBD
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

    /**
     * The entity type this fetchPlan will be used against.
     */
    private Class<?> entityClass;

    /**
     * Descriptor cached for the entity class of this FetchPlan.
     */
    private ClassDescriptor descriptor;

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

    public FetchPlan(String name, Class<?> entityClass) {
        this.name = name;
        this.entityClass = entityClass;
        this.items = new HashMap<String, FetchItem>();
    }

    public FetchPlan(Class<?> entityClass) {
        this(null, entityClass);
    }

    /**
     * TODO Create an initialized {@link FetchPlan} (meaning its descriptor,
     * required mappings, and items are configured when returned so that
     * {@link #initialize(Session)} will not do anything).
     */
    protected FetchPlan(ClassDescriptor descriptor) {
        this(descriptor.getJavaClass());
        this.descriptor = descriptor;
    }

    /**
     * TODO
     */
    @SuppressWarnings("unchecked")
    protected void addDefaultMappings() {
        if (descriptor.hasFetchGroupManager() && descriptor.getFetchGroupManager().getDefaultFetchGroup() != null) {
            FetchGroup fg = descriptor.getFetchGroupManager().getDefaultFetchGroup();

            for (String attr : (Set<String>) fg.getAttributes()) {
                addAttribute(descriptor.getMappingForAttributeName(attr));
            }
            return;
        }

        for (DatabaseMapping mapping : descriptor.getMappings()) {
            if (!mapping.isLazy()) {
                addAttribute(mapping);
            }
        }

        // Add identifier and optimistic locking version attributes
        if (!descriptor.isAggregateDescriptor()) {
            for (DatabaseMapping pkMapping : descriptor.getObjectBuilder().getPrimaryKeyMappings()) {
                if (!getItems().containsKey(pkMapping.getAttributeName())) {
                    addAttribute(pkMapping);
                }
            }

            if (descriptor.usesOptimisticLocking()) {
                DatabaseField lockField = descriptor.getOptimisticLockingPolicy().getWriteLockField();
                if (lockField != null) {
                    DatabaseMapping lockMapping = descriptor.getObjectBuilder().getMappingForField(lockField);
                    if (lockMapping != null && !getItems().containsKey(lockMapping.getAttributeName())) {
                        addAttribute(lockMapping);
                    }
                }
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getEntityClass() {
        return this.entityClass;
    }

    /*
     * Used in {@link FetchItem#initialize(Session)} to populate the target
     * entity type of relationships from the mapping.
     */
    protected void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public Map<String, FetchItem> getItems() {
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

        for (FetchItem item : getItems().values()) {
            item.initialize(session);
        }
    }

    protected ClassDescriptor getDescriptor(Session session) {
        initialize(session);
        return this.descriptor;
    }

    /**
     * Add an item to be fetched. This can be either a mapped attribute name of
     * the current {@link #entityClass} or it can be a path of mapped attribute
     * names '.' separated. The values are not validated against the mappings
     * when added but are instead validated during {@link #initialize(Session)}
     * when the plan is used in a fetch/copy/merge operation.
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
                currentFP = new FetchPlan(getName() + "-" + attrName, null);
                item.setFetchPlan(currentFP);
            }
        }

        return item;
    }

    private FetchItem addAttribute(DatabaseMapping mapping) {
        FetchItem item = new FetchItem(this, mapping);
        getItems().put(mapping.getAttributeName(), item);
        return item;
    }

    /**
     * Add all of the attributes from the provided FetchGroup.
     */
    @SuppressWarnings("unchecked")
    public void addAttributes(FetchGroup fetchGroup) {
        for (String attribute : (Set<String>) fetchGroup.getAttributes()) {
            addAttribute(attribute);
        }
    }

    /**
     * Identifies if the attribute name or path exists in the plan.
     */
    public boolean containsAttribute(String... attributeNameOrPath) {
        String[] attributePaths = convert(attributeNameOrPath);
        FetchPlan currentFP = this;
        FetchItem item = null;

        for (int index = 0; index < attributePaths.length; index++) {
            String attrName = attributePaths[index];
            item = (FetchItem) currentFP.getItems().get(attrName);

            if (item == null) {
                return false;
            }

            currentFP = item.getFetchPlan();
            if (currentFP == null && index < (attributePaths.length - 1)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Convert a provided name or path which could be a single attributeName, a
     * single string with dot separated attribute names, or an array of
     * attribute names defining the path.
     */
    private String[] convert(String... nameOrPath) {
        if (nameOrPath == null || nameOrPath.length == 0 || (nameOrPath.length == 1 && (nameOrPath[0] == null || nameOrPath[0].length() == 0))) {
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
     * <p>
     * In the case of composite results (Collection<Object[]) all result
     * elements of the array have fetch performed on them.
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
     * Special fetch operation for composite query results where the SELECT
     * returned multiple items and each result is an array of objects. This
     * method allows an index to be specified so that only the result in
     * question is used in the fetch operation.
     * 
     * @see #fetch(Object, AbstractSession)
     */
    public void fetch(Collection<Object[]> entities, int resultIndex, AbstractSession session) {
        for (Object[] entity : entities) {
            fetch(entity[resultIndex], session);
        }
    }

    /**
     * Configure a redirector on the query so that this {@link FetchPlan} is
     * applied on the result before it is returned. This cannot be used in
     * conjunction with other redirectors or queries that return results that
     * are not a single entity or collection of entities (No {@link ReportQuery}
     * ).
     */
    public void fetchOnExecute(ObjectLevelReadQuery query) {
        query.setRedirector(new QueryRedirector() {
            public Object invokeQuery(DatabaseQuery query, Record arguments, Session session) {
                Object result = query.execute((AbstractSession) session, (AbstractRecord) arguments);
                fetch(result, (AbstractSession) session);
                return result;
            }
        });
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

        return (T) copy(source, session, new CopyTrace());
    }

    /**
     * Create a copy of the entity or collection of entities ensure identity
     * through maintenance of a map of copies (original -> copy).
     */
    protected Object copy(Object source, AbstractSession session, CopyTrace copies) {
        if (source instanceof Object[]) {
            throw new IllegalArgumentException("Fetchplan.copy does not support Object[]");
        }

        CopyFetchPlans copyPlans = copies.get(source);
        if (copyPlans.isCopied(this)) {
            return copyPlans.getCopy();
        }

        if (source instanceof Collection<?>) {
            return copyAll((Collection<?>) source, session, copies);
        }

        ClassDescriptor descriptor = getDescriptor(session);

        Object copy = copyPlans.getCopy();

        if (copy == null) {
            copy = descriptor.getInstantiationPolicy().buildNewInstance();
            copyPlans.setCopy(copy);
        }
        copyPlans.addCopied(this);

        ObjectCopyingPolicy policy = new ObjectCopyingPolicy();
        policy.setShouldResetPrimaryKey(false);
        policy.setSession(session);

        // Copy all specified items
        for (Map.Entry<String, FetchItem> entry : getItems().entrySet()) {
            entry.getValue().copy(source, copy, session, policy, copies);
        }

        return copy;
    }

    /**
     * Create copy of a collection
     */
    @SuppressWarnings("unchecked")
    protected Object copyAll(Collection<?> source, AbstractSession session, CopyTrace copies) {
        CopyFetchPlans copyPlans = copies.get(source);
        Collection<Object> copiesCollection = (Collection<Object>) copyPlans.getCopy();
        boolean newCollection = false;

        if (copiesCollection == null) {
            try {
                Constructor<?> constructor = source.getClass().getConstructor(int.class);
                copiesCollection = (Collection<Object>) constructor.newInstance(source.size());
            } catch (Exception e) {
                throw new RuntimeException("FetchPlan.copy: failed to create copy of result container for: " + source.getClass(), e);
            }
            newCollection = true;
        }

        for (Object entity : source) {
            Object copy = copy(entity, session, copies);
            if (newCollection) {
                copiesCollection.add(copy);
            }
        }

        copyPlans.setCopy(copiesCollection);
        copyPlans.addCopied(this);

        return copiesCollection;
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
        initialize(uow);

        FetchPlanMergeManager mergeManager = new FetchPlanMergeManager((AbstractSession) uow);
        mergeManager.getFetchPlans().put(entity, this);

        return (T) ((UnitOfWorkImpl) uow).mergeCloneWithReferences(entity, mergeManager);
    }

    /**
     * Create a dynamic {@link FetchGroup} that includes all of the mapped
     * attributes on the {@link #entityClass}. The returned FetchGroup will not
     * cause relationships to be loaded or any nested FetchItems to be applied.
     * This is generally used in combination with the fetch/copy methods on
     * FetchPlan or {@link JpaFetchPlanHelper} to post process the query results
     * ensuring that all requested attributes in the graph are loaded (or
     * copied).
     * <p>
     * <b>JPA Usage Example</b>:
     * <p>
     * <code>
     * query.setHint(QueryHints.FETCH_GROUP, fetchPlan.createFetchGroup());<br/>
     * List<Employee> emps = query.getResultList();<br/>
     * JpaFetchPlanHelper.fetch(em, fetchPlan, emps);<br/>
     * </code>
     */
    public FetchGroup createFetchGroup() {
        FetchGroup group = new FetchGroup(toString() + "_fetch-group");
        for (String attrName : getItems().keySet()) {
            group.addAttribute(attrName);
        }
        return group;
    }

    public String toString() {
        return "FetchPlan(" + getName() + ")";
    }

    /**
     * Utility class that tracks the copying of the graph to ensure the identity
     * of the entities based on source so that only one copy per source is
     * created
     */
    protected class CopyTrace {

        private Map<Object, CopyFetchPlans> copies = new HashMap<Object, CopyFetchPlans>();

        public CopyFetchPlans get(Object source) {
            CopyFetchPlans copyFetchPlans = this.copies.get(source);

            if (copyFetchPlans == null) {
                copyFetchPlans = new CopyFetchPlans();
                this.copies.put(source, copyFetchPlans);
            }
            return copyFetchPlans;
        }

    }

    /**
     * TODO
     */
    protected class CopyFetchPlans {

        /**
         * Entity being copied
         */
        private Object copy;

        /**
         * Plans processed for this copy
         */
        private Set<FetchPlan> plans;

        CopyFetchPlans() {
            this.plans = new HashSet<FetchPlan>();
        }

        protected Object getCopy() {
            return this.copy;
        }

        protected boolean hasCopy() {
            return this.copy != null;
        }

        protected void setCopy(Object copy) {
            this.copy = copy;
        }

        protected boolean isCopied(FetchPlan fetchPlan) {
            return hasCopy() && this.plans.contains(fetchPlan);
        }

        protected void addCopied(FetchPlan fetchPlan) {
            this.plans.add(fetchPlan);
        }
    }

    private static final String PROPERTY = "DEFAULT-FETCH-PLAN";

    /**
     * TODO
     */
    @SuppressWarnings("unchecked")
    protected static FetchPlan defaultFetchPlan(DatabaseMapping mapping) {
        if (mapping.getReferenceDescriptor() == null) {
            return null;
        }

        ClassDescriptor targetDescriptor = mapping.getReferenceDescriptor();
        FetchPlan fp = (FetchPlan) mapping.getProperty(PROPERTY);

        if (fp == null) {
            fp = new FetchPlan(targetDescriptor);
            mapping.setProperty(PROPERTY, fp);

            if (mapping.isForeignReferenceMapping()) {
                FetchGroup mappingFG = null;
                ForeignReferenceMapping frMapping = (ForeignReferenceMapping) mapping;
                ObjectLevelReadQuery mappingQuery = (ObjectLevelReadQuery) frMapping.getSelectionQuery();

                if (mappingQuery.getFetchGroup() != null) {
                    mappingFG = mappingQuery.getFetchGroup();
                } else if (mappingQuery.getFetchGroupName() != null && targetDescriptor.hasFetchGroupManager()) {
                    mappingFG = targetDescriptor.getFetchGroupManager().getFetchGroup(mappingQuery.getFetchGroupName());
                }

                if (mappingFG != null) {
                    for (String attr : (Set<String>) mappingFG.getAttributes()) {
                        fp.addAttribute(targetDescriptor.getMappingForAttributeName(attr));
                    }
                    return fp;
                }
            }
            fp.addDefaultMappings();
        }
        return fp;
    }
}
