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
 *     dclarke - Dynamic Persistence INCUBATION - Enhancement 200045
 *     			 http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.internal.dynamic;

import java.beans.PropertyChangeListener;
import java.io.StringWriter;
import java.util.*;

import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.descriptors.PersistenceEntity;
import org.eclipse.persistence.internal.identitymaps.CacheKey;
import org.eclipse.persistence.internal.localization.ExceptionLocalization;
import org.eclipse.persistence.internal.weaving.PersistenceWeavedLazy;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.queries.*;
import org.eclipse.persistence.sessions.Session;

/**
 * This abstract class is used to represent an entity which typically is not
 * realized in Java code. In combination with the DynamicClassLoader ASM is used
 * to generate subclasses that will work within EclipseLink's framework. Since
 * no concrete fields or methods exist on this class the mappings used must be
 * customized to use a custom AttributeAccessor (
 * {@link EntityPropertyImpl.DynamicAttributeAccessor}).
 * <p>
 * <b>Type/Property Meta-model</b>: This dynamic entity approach also includes a
 * meta-model facade to simplify access to the types and property information so
 * that clients can more easily understand the model. Each
 * {@link EntityTypeImpl} wraps the underlying EclipseLink relational-descriptor
 * and the {@link EntityPropertyImpl} wraps each mapping. The client application
 * can use these types and properties to facilitate generic access to the entity
 * instances and are required for creating new instances as well as for
 * accessing the Java class needed for JPA and EclipseLink native API calls.
 * 
 * @see DynamicEntity
 * @See DynamicAttributeAccessor
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public abstract class DynamicEntityImpl implements DynamicEntity, ChangeTracker, PersistenceEntity, FetchGroupTracker, PersistenceWeavedLazy, Cloneable {
    /**
     * The persistent values indexed by the descriptor's mappings and the
     * EntityType's corresponding property list.
     */
    protected Object[] values;

    private EntityTypeImpl type;

    /**
     * ChangeListener used for attribute change tracking processed in the
     * property
     */
    private PropertyChangeListener changeListener = null;

    /**
     * State required for fetch group support
     * 
     * @see FetchGroupTracker
     */
    private FetchGroup fetchGroup;
    private Session session;
    private boolean refreshFetchGroup = false;

    /**
     * Cache the CacheKey within the entity
     * 
     * @see PersistenceEntity
     */
    private CacheKey cacheKey;

    /**
     * Cache the primary key within the entity
     * 
     * @see PersistenceEntity
     */
    private Vector primaryKey;

    protected DynamicEntityImpl(EntityTypeImpl type) {
        this.type = type;
        this.values = new Object[type.getNumberOfProperties()];
    }

    public EntityType getType() {
        return this.type;
    }

    public Object get(String propertyName) {
        return get(getType().getMapping(propertyName));
    }

    public <T> T get(String propertyName, Class<T> type) {
        return (T) get(propertyName);
    }

    public Object get(int propertyIndex) {
        return get(getType().getMapping(propertyIndex));
    }

    public <T> T get(int propertyIndex, Class<T> type) {
        return (T) get(propertyIndex);
    }

    public Object get(DatabaseMapping mapping) {
        return mapping.getAttributeValueFromObject(this);
    }

    public <T> T get(DatabaseMapping mapping, Class<T> type) {
        return (T) mapping.getAttributeValueFromObject(this);
    }

    public DynamicEntity set(int propertyIndex, Object value) {
        return set(getType().getMapping(propertyIndex), value);
    }

    public DynamicEntity set(String propertyName, Object value) {
        return set(getType().getMapping(propertyName), value);
    }

    // TODO: Ensure value is appropriate for mapping type?
    public DynamicEntity set(DatabaseMapping mapping, Object value) {
        mapping.setAttributeValueInObject(this, value);
        return this;
    }

    private Collection getCollection(DatabaseMapping mapping) {
        if (!mapping.isCollectionMapping() || Collection.class.isAssignableFrom(((CollectionMapping) mapping).getContainerPolicy().getContainerClass())) {
            throw DynamicEntityException.propertyNotCollection(mapping);
        }
        return (Collection) get(mapping);
    }

    private Map getMap(DatabaseMapping mapping) {
        if (!mapping.isCollectionMapping() || Map.class.isAssignableFrom(((CollectionMapping) mapping).getContainerPolicy().getContainerClass())) {
            throw DynamicEntityException.propertyNotCollection(mapping);
        }
        return (Map) get(mapping);
    }

    public Object add(String propertyName, Object value) {
        Collection collection = getCollection(getType().getMapping(propertyName));
        return collection.add(value);
    }

    public Object remove(String propertyName, Object value) {
        Collection collection = getCollection(getType().getMapping(propertyName));
        return collection.remove(value);
    }

    public Object get(String propertyName, Object key) {
        Map map = getMap(getType().getMapping(propertyName));
        return map.get(key);
    }

    public Object put(String propertyName, Object key, Object value) {
        Map map = getMap(getType().getMapping(propertyName));
        return map.put(key, value);
    }

    public PropertyChangeListener _persistence_getPropertyChangeListener() {
        return this.changeListener;
    }

    public void _persistence_setPropertyChangeListener(PropertyChangeListener listener) {
        this.changeListener = listener;
    }

    /**
     * String representation of the dynamic entity using the entity type name
     * and the primary key values.
     */
    public String toString() {
        StringWriter writer = new StringWriter();

        writer.write(getType().getName());
        writer.write("(");

        for (DatabaseMapping mapping : getType().getMappings()) {
            if (getType().getDescriptor().getObjectBuilder().getPrimaryKeyMappings().contains(mapping)) {
                writer.write(mapping.getAttributeName());
                writer.write("=" + mapping.getAttributeValueFromObject(this));
            }
        }

        writer.write(")");
        return writer.toString();
    }

    public FetchGroup _persistence_getFetchGroup() {
        return this.fetchGroup;
    }

    public Session _persistence_getSession() {
        return this.session;
    }

    public boolean _persistence_isAttributeFetched(String attribute) {
        return this.fetchGroup == null || this.fetchGroup.containsAttribute(attribute);
    }

    public void _persistence_resetFetchGroup() {
        // TODO: What do we do here?
        this.refreshFetchGroup = true;
    }

    public void _persistence_setFetchGroup(FetchGroup group) {
        this.fetchGroup = group;
    }

    public void _persistence_setSession(Session session) {
        this.session = session;
    }

    public void _persistence_setShouldRefreshFetchGroup(boolean shouldRefreshFetchGroup) {
        this.refreshFetchGroup = shouldRefreshFetchGroup;
    }

    public boolean _persistence_shouldRefreshFetchGroup() {
        return this.refreshFetchGroup;
    }

    /**
     * 
     * @param attribute
     */
    protected void _persistence_checkFetched(String attribute) {
        if (!_persistence_isAttributeFetched(attribute)) {
            ReadObjectQuery query = new ReadObjectQuery(this);
            query.setShouldUseDefaultFetchGroup(false);
            Object result = _persistence_getSession().executeQuery(query);
            if (result == null) {
                Object[] args = { query.getSelectionKey() };
                String message = ExceptionLocalization.buildMessage("no_entities_retrieved_for_get_reference", args);
                throw new javax.persistence.EntityNotFoundException(message);
            }
        }
    }

    // Methods for PersistenceEntity
    public CacheKey _persistence_getCacheKey() {
        return this.cacheKey;
    }

    public void _persistence_setCacheKey(CacheKey key) {
        this.cacheKey = key;
    }

    public Vector _persistence_getPKVector() {
        return this.primaryKey;
    }

    public void _persistence_setPKVector(Vector pk) {
        this.primaryKey = pk;
    }

    public Object _persistence_shallow_clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("DynamicEntity._persistence_shallow_clone failed on super.clone", e);
        }
    }

    protected static class ValuesAccessor extends AttributeAccessor {

        protected final static Object NULL_ENTRY = new Object();

        private DatabaseMapping mapping;

        private int index;

        public ValuesAccessor(DatabaseMapping mapping, int index) {
            super();
            this.mapping = mapping;
            this.index = index;
        }

        public DatabaseMapping getMapping() {
            return this.mapping;
        }

        public int getIndex() {
            return this.index;
        }

        private Object[] getValues(Object entity) {
            return ((DynamicEntityImpl) entity).values;
        }

        public Object getAttributeValueFromObject(Object entity) throws DescriptorException {
            Object[] values = getValues(entity);
            Object value = values[getIndex()];

            if (value == NULL_ENTRY) {
                value = null;
            }
            return value;
        }

        public void setAttributeValueInObject(Object entity, Object value) throws DescriptorException {
            Object[] values = getValues(entity);
            values[getIndex()] = value == null ? NULL_ENTRY : value;
        }

        @Override
        public Class getAttributeClass() {
            if (getMapping().isForeignReferenceMapping()) {
                ForeignReferenceMapping refMapping = (ForeignReferenceMapping) getMapping();

                if (refMapping.isCollectionMapping()) {
                    return ((CollectionMapping) refMapping).getContainerPolicy().getContainerClass();
                }
                if (refMapping.usesIndirection()) {
                    return ValueHolderInterface.class;
                }
                return refMapping.getReferenceClass();
            } else {
                return getMapping().getAttributeClassification();
            }
        }

    }
}
