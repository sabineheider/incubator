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

import java.beans.PropertyChangeEvent;

import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.indirection.*;
import org.eclipse.persistence.internal.dynamic.DynamicAttributeAccessor;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.helper.Helper;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.mappings.foundation.AbstractDirectMapping;

/**
 * <b>Purpose</b>: Metadata model for persistent property of a dynamic entity.
 * <p>
 * The property is used within the EntityType metadata model to represent a
 * persistent property wrapping the underlying TopLink mapping. It can be used
 * by an application to access the structure of the dynamic entity model as well
 * as provide more optimal access to data values within an entity.
 * <p>
 * 
 * @author dclarke
 * @since EclipseLink 1.1 Incubation
 */
public class EntityPropertyImpl implements EntityProperty {
	private EntityTypeImpl type;
	private DatabaseMapping mapping;
	private int index;

	protected EntityPropertyImpl(EntityTypeImpl type, DatabaseMapping mapping) {
		initialize(type, mapping);
	}

	protected EntityPropertyImpl(EntityTypeImpl type, String name, String columnName, Class javaType, boolean primaryKey) {
		AbstractDirectMapping directMapping = (AbstractDirectMapping) type.getDescriptor().addDirectMapping(name, columnName);
		directMapping.getField().setType(javaType);

		directMapping.setAttributeClassification(javaType);
		if (primaryKey) {
			directMapping.setIsPrimaryKeyMapping(primaryKey);
			type.getDescriptor().addPrimaryKeyFieldName(columnName);
		}
		initialize(type, directMapping);
	}

	protected EntityPropertyImpl initialize(EntityTypeImpl type, DatabaseMapping mapping) {
		this.type = type;
		setMapping(mapping);
		this.index = type.getDescriptor().getMappings().indexOf(mapping);
		return this;
	}

	public EntityTypeImpl getType() {
		return this.type;
	}

	public String getName() {
		return getMapping().getAttributeName();
	}

	public Class getAttributeType() {
		Class type = getMapping().getAttributeClassification();

		if (type == null) {
			if (getMapping().isAbstractDirectMapping()) {
				type = Object.class;
				((AbstractDirectMapping) getMapping()).setAttributeClassification(type);
			} else {
				// TODO: Can we reach this?
				throw new RuntimeException("Cannot return null for attribute type");
			}
		}

		return type;
	}

	public EntityTypeImpl getEntityType() {
		return this.type;
	}

	public DatabaseMapping getMapping() {
		return this.mapping;
	}

	protected void setMapping(DatabaseMapping mapping) {
		this.mapping = mapping;
		AttributeAccessor accessor = new DynamicAttributeAccessor(this);
		mapping.setAttributeAccessor(accessor);

		if (!getType().getDescriptor().getMappings().contains(mapping)) {
			getType().getDescriptor().addMapping(mapping);
		}
		accessor.initializeAttributes(DynamicEntityImpl.class);
	}

	public int getIndex() {
		return this.index;
	}

	public boolean isPrimaryKey() {
		return getMapping().isPrimaryKeyMapping();
	}

	public boolean isReference() {
		return false;
	}

	protected void putRawInEntity(DynamicEntityImpl entity, Object value) {
		getMapping().setAttributeValueInObject(entity, value);
	}

	protected DynamicEntityImpl putInEntity(DynamicEntityImpl entity, Object value) {
		Object currentValue = getRawFromEntity(entity);

		if (currentValue instanceof IndirectContainer) {
			currentValue = ((IndirectContainer) currentValue).getValueHolder();
		}

		if (entity._persistence_getPropertyChangeListener() != null) {
			PropertyChangeEvent event = new PropertyChangeEvent(entity, getName(), currentValue, value);
			entity._persistence_getPropertyChangeListener().propertyChange(event);
		}

		if (currentValue instanceof ValueHolderInterface) {
			((ValueHolderInterface) currentValue).setValue(value);
		} else {
			putRawInEntity(entity, value);
		}
		return entity;
	}

	protected Object getRawFromEntity(DynamicEntityImpl entity) {
		entity._persistence_checkFetched(getName());
		return getMapping().getAttributeValueFromObject(entity);
	}

	protected Object getFromEntity(DynamicEntityImpl entity) {
		Object currentValue = getRawFromEntity(entity);

		if (currentValue == NULL_ENTRY) {
			return null;
		}
		if (currentValue instanceof ValueHolderInterface) {
			return ((ValueHolderInterface) currentValue).getValue();
		}
		if (currentValue instanceof IndirectContainer) {
			return ((IndirectContainer) currentValue).getValueHolder().getValue();
		}
		return currentValue;

	}

	/**
	 * Allows property assign value for new instances. For DirectMappings this
	 * does nothing. Provided to allow subclasses to initialize relationships.
	 */
	protected void initializeValue(DynamicEntityImpl entity) {
		if (getAttributeType().isPrimitive()) {
			Object value = null;

			if (getAttributeType() == ClassConstants.PBOOLEAN) {
				value = true;
			} else if (getAttributeType() == ClassConstants.PINT) {
				value = 0;
			} else if (getAttributeType() == ClassConstants.PSHORT) {
				value = 0;
			} else if (getAttributeType() == ClassConstants.PLONG) {
				value = 0l;
			} else if (getAttributeType() == ClassConstants.PDOUBLE) {
				value = 0d;
			} else if (getAttributeType() == ClassConstants.PFLOAT) {
				value = 0f;
			}

			if (getMapping().isLazy()) {
				value = new ValueHolder(value);
			}

			if (value == null) {
				throw new DynamicEntityException("EntityProperty could not initialize default value for primitive: " + getAttributeType());
			}
			putInEntity(entity, value);
		}
	}

	/**
	 * Returns true if property requires initialization on new instance
	 * creation. Allows for indirect relationships to create the necessary
	 * value-holder
	 */
	protected boolean requiresInitialization() {
		return getAttributeType().isPrimitive() || getMapping().isLazy();
	}

	public boolean isLazy() {
		return getMapping().isLazy();
	}

	public String toString() {
		return Helper.getShortClassName(this) + "[" + getType().getName() + "." + getName() + "] -> " + getMapping();
	}

	public static final NullEntry NULL_ENTRY = new NullEntry();

	public static class NullEntry {
	}

}
