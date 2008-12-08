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

import java.util.Collection;
import java.util.Map;

import org.eclipse.persistence.dynamic.DynamicEntityException;
import org.eclipse.persistence.dynamic.EntityCollectionProperty;
import org.eclipse.persistence.mappings.CollectionMapping;

/**
 * Property used to represent an attribute mapped by either a 1:M or M:M mapping
 * with either a Map or Collection (Set/List) container type
 * 
 * @author Doug Clarke
 * @since EclipseLink 1.0
 */
public class EntityCollectionPropertyImpl extends EntityReferencePropertyImpl implements EntityCollectionProperty {
	private EntityReferencePropertyImpl mappedByProperty;

	protected EntityCollectionPropertyImpl(EntityTypeImpl type, CollectionMapping mapping) {
		super(type, mapping);
	}

	public boolean isMap() {
		return getMapping().getContainerPolicy().isMapPolicy();
	}

	protected CollectionMapping getCollectionMapping() {
		return (CollectionMapping) getMapping();
	}

	private Collection getCollection(DynamicEntityImpl entity) {
		if (isMap()) {
			throw new DynamicEntityException("Cannot retieve Map from collection mapping: " + getType().getName() + "." + getName());
		}

		Object currentValue = super.getFromEntity(entity);

		if (currentValue == null) {
			currentValue = getMapping().getContainerPolicy().containerInstance();
			putRawInEntity(entity, currentValue);
		}

		return (Collection) currentValue;
	}

	public Object addToCollection(DynamicEntityImpl entity, Object value) {
		Object result = getCollection(entity).add(value);

		if (getMappedByProperty() != null) {
			getMappedByProperty().putInEntity((DynamicEntityImpl) value, entity);
		}

		return result;
	}

	public Object removeFromCollection(DynamicEntityImpl entity, Object value) {
		return getCollection(entity).remove(value);
	}

	private Map getMap(DynamicEntityImpl entity) {
		if (!isMap()) {
			throw new DynamicEntityException("Cannot retieve non-Map from map mapping: " + getType().getName() + "." + getName());
		}

		Object currentValue = super.getFromEntity(entity);

		if (currentValue == null) {
			currentValue = getMapping().getContainerPolicy().containerInstance();
			putRawInEntity(entity, currentValue);
		}

		return (Map) currentValue;
	}

	public Object getFromMap(DynamicEntityImpl entity, Object key) {
		return getMap(entity).get(key);
	}

	public Object putInMap(DynamicEntityImpl entity, Object key, Object value) {
		return getMap(entity).put(key, value);
	}

	public Object removeKeyFromMap(DynamicEntityImpl entity, Object key) {
		return getMap(entity).remove(key);
	}

	public boolean isCollection() {
		return getMapping().getContainerPolicy().isCollectionPolicy();
	}

	protected Object initialValue() {
		Object init = getCollectionMapping().getContainerPolicy().containerInstance();
		return init;
	}

	/**
	 * This method is called during the creation of a collection relationship
	 * where initialization was indicated. This should therefore only be called
	 * if indirection is in use and currently on Basic indirection (ValueHolder)
	 * is supported.
	 * 
	 * @see EntityReferencePropertyImpl#requiresInitialization()
	 */
	@Override
	protected void initializeValue(DynamicEntityImpl entity) {
		putRawInEntity(entity, initialValue());
	}

	public EntityReferencePropertyImpl getMappedByProperty() {
		return this.mappedByProperty;
	}

	public void setMappedByProperty(EntityReferencePropertyImpl mappedByProperty) {
		this.mappedByProperty = mappedByProperty;
	}

}
