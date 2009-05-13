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
import org.eclipse.persistence.dynamic.EntityReferenceProperty;
import org.eclipse.persistence.indirection.ValueHolder;
import org.eclipse.persistence.mappings.*;

/**
 * 
 * @author Doug Clarke
 * @since EclipseLink 1.1 INCUBATION
 */
public class EntityReferencePropertyImpl extends EntityPropertyImpl implements EntityReferenceProperty {

	private EntityTypeImpl referenceType = null;

	protected EntityReferencePropertyImpl(EntityTypeImpl type, ForeignReferenceMapping mapping) {
		super(type, mapping);
	}

	private ForeignReferenceMapping getForeignReferenceMapping() {
		return (ForeignReferenceMapping) super.getMapping();
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
		return getCollection(entity).add(value);
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

	protected boolean requiresInitialization() {
		return ((ForeignReferenceMapping) getMapping()).usesIndirection();
	}

	protected void initializeValue(DynamicEntityImpl entity) {
		if (getForeignReferenceMapping().usesIndirection()) {
			putRawInEntity(entity, new ValueHolder());
		}
	}

	@Override
	public boolean isReference() {
		return true;
	}

	public boolean isCollection() {
		return false;
	}

	public EntityTypeImpl getReferenceType() {
		if (this.referenceType == null) {
			this.referenceType = EntityTypeImpl.getType(getForeignReferenceMapping().getReferenceDescriptor());
		}
		return this.referenceType;
	}

	public Class getReferenceClass() {
		if (this.referenceType == null) {
			return getForeignReferenceMapping().getReferenceClass();
		}
		return this.referenceType.getJavaClass();
	}

	public ForeignReferenceMapping getReferenceMapping() {
		return (ForeignReferenceMapping) getMapping();
	}
}
