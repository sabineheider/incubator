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
import org.eclipse.persistence.exceptions.DynamicException;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.descriptors.PersistenceEntity;
import org.eclipse.persistence.internal.identitymaps.CacheKey;
import org.eclipse.persistence.internal.localization.ExceptionLocalization;
import org.eclipse.persistence.internal.weaving.PersistenceWeavedLazy;
import org.eclipse.persistence.mappings.DatabaseMapping;
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
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public abstract class DynamicEntityImpl implements DynamicEntity, ChangeTracker, PersistenceEntity, FetchGroupTracker, PersistenceWeavedLazy, Cloneable {
    /**
     * The persistent values indexed by the descriptor's mappings and the
     * EntityType's corresponding property list.
     */
    protected Object[] values;

    /**
     * Type of this entity
     */
    private EntityTypeImpl type;

    /**
     * ChangeListener used for attribute change tracking processed in the
     * property. Set through {@link ChangeTracker#_persistence_setPropertyChangeListener(PropertyChangeListener)}
     */
    private PropertyChangeListener changeListener = null;

    /**
     * FetchGroup cached by {@link FetchGroupTracker#_persistence_setFetchGroup(FetchGroup)}
     */
    private FetchGroup fetchGroup;

    /**
     * Session cached by {@link FetchGroupTracker#_persistence_setSession(Session)}
     */
    private Session session;
    
    /**
     * {@link FetchGroupTracker#_persistence_setShouldRefreshFetchGroup(boolean)}
     */
    private boolean refreshFetchGroup = false;

    /**
     * Cache the CacheKey within the entity
     * 
     * @see PersistenceEntity#_persistence_setCacheKey(CacheKey)
     */
    private CacheKey cacheKey;

    /**
     * Cache the primary key within the entity
     * 
     * @see PersistenceEntity#_persistence_setPKVector(Vector)
     */
    private Vector<Object> primaryKey;

    
    protected DynamicEntityImpl(EntityTypeImpl type) {
        this.type = type;
        this.values = new Object[type.getNumberOfProperties()];
    }

    protected EntityTypeImpl getTypeImpl() {
        return this.type;
    }

    public EntityType getType() {
        return getTypeImpl();
    }

    public Object get(String propertyName) {
        return get(getTypeImpl().getMapping(propertyName), Object.class);
    }

    public <T> T get(String propertyName, Class<T> type) {
        return (T) get(getTypeImpl().getMapping(propertyName), type);
    }

    public Object get(int propertyIndex) {
        return get(getTypeImpl().getMapping(propertyIndex), Object.class);
    }

    public <T> T get(int propertyIndex, Class<T> type) {
        return get(getTypeImpl().getMapping(propertyIndex), type);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(DatabaseMapping mapping, Class<T> type) {
        Object value = mapping.getAttributeValueFromObject(this);

        if (value instanceof ValueHolderInterface) {
            value = ((ValueHolderInterface) value).getValue();
        }

        try {
            return (T) value;
        } catch (ClassCastException cce) {
            throw DynamicException.invalidPropertyType(mapping, type);
        }
    }

    public DynamicEntity set(int propertyIndex, Object value) {
        return set(getTypeImpl().getMapping(propertyIndex), value);
    }

    public DynamicEntity set(String propertyName, Object value) {
        return set(getTypeImpl().getMapping(propertyName), value);
    }

    public DynamicEntity set(DatabaseMapping mapping, Object value) {
        Object currentValue = mapping.getAttributeValueFromObject(this);

        if (currentValue instanceof ValueHolderInterface) {
            ((ValueHolderInterface) currentValue).setValue(value);
        } else {
            mapping.setAttributeValueInObject(this, value);
        }
        return this;
    }

    public boolean isSet(DatabaseMapping mapping) {
        ValuesAccessor accessor = (ValuesAccessor) mapping.getAttributeAccessor();
        return accessor.isSet(this);
    }

    public boolean isSet(int propertyIndex) {
        return isSet(getTypeImpl().getMapping(propertyIndex));
    }

    public boolean isSet(String propertyName) {
        return isSet(getTypeImpl().getMapping(propertyName));
    }

    /**
     * String representation of the dynamic entity using the entity type name
     * and the primary key values.
     */
    public String toString() {
        StringWriter writer = new StringWriter();

        writer.write(getType().getName());
        writer.write("(");

        for (DatabaseMapping mapping : getType().getDescriptor().getMappings()) {
            if (getTypeImpl().getDescriptor().getObjectBuilder().getPrimaryKeyMappings().contains(mapping)) {
                writer.write(mapping.getAttributeName());
                writer.write("=" + mapping.getAttributeValueFromObject(this));
            }
        }

        writer.write(")");
        return writer.toString();
    }

    public PropertyChangeListener _persistence_getPropertyChangeListener() {
        return this.changeListener;
    }

    public void _persistence_setPropertyChangeListener(PropertyChangeListener listener) {
        this.changeListener = listener;
    }

    public FetchGroup _persistence_getFetchGroup() {
        return this.fetchGroup;
    }

    public Session _persistence_getSession() {
        return this.session;
    }

    /**
     * Return true if the attribute is in the fetch group being tracked.
     */
    public boolean _persistence_isAttributeFetched(String attribute) {
        return this.fetchGroup == null || this.fetchGroup.containsAttribute(attribute);
    }

    /**
     * Reset all attributes of the tracked object to the un-fetched state with
     * initial default values.
     */
    public void _persistence_resetFetchGroup() {
        // TODO: Clear all values that are not to be fetched ensuring that any
        // values that require initialization are properly initialized
        throw new UnsupportedOperationException("DynamicEntityImpl._persistence_resetFetchGroup:: NOT SUPPORTED");
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

    @SuppressWarnings("unchecked")
    public Vector _persistence_getPKVector() {
        return this.primaryKey;
    }

    @SuppressWarnings("unchecked")
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

}
