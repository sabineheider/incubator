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

import static org.eclipse.persistence.internal.dynamic.EntityPropertyImpl.NULL_ENTRY;

import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.descriptors.InstanceVariableAttributeAccessor;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;

/**
 * <b>Purpose</b>: Provide mechanism to access the property values from a
 * dynamic Entity
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li>Extending field access provide reflective access to DynamicEntityImpl's
 * values (Object[]).
 * <li>
 * </ul>
 * 
 * @author djclarke
 * @since EclipseLink 1.1
 */
public class DynamicAttributeAccessor extends InstanceVariableAttributeAccessor {
	private EntityPropertyImpl property;

	public DynamicAttributeAccessor(EntityPropertyImpl property) {
		super();
		this.property = property;
	}

	public void initializeAttributes(Class theJavaClass) throws DescriptorException {
		try {
			setAttributeField(PrivilegedAccessHelper.getField(theJavaClass, "values", true));
		} catch (NoSuchFieldException exception) {
			throw DescriptorException.noSuchFieldWhileInitializingAttributesInInstanceVariableAccessor(getAttributeName(), theJavaClass.getName(), exception);
		} catch (SecurityException exception) {
			throw DescriptorException.securityWhileInitializingAttributesInInstanceVariableAccessor(getAttributeName(), theJavaClass.getName(), exception);
		}
	}

	public Object getAttributeValueFromObject(Object anObject) throws DescriptorException {
		Object[] values = (Object[]) super.getAttributeValueFromObject(anObject);
		Object value = values[getProperty().getIndex()];

		if (value == NULL_ENTRY) {
			value = null;
		}
		return value;
	}

	public void setAttributeValueInObject(Object anObject, Object value) throws DescriptorException {
		Object[] values = (Object[]) super.getAttributeValueFromObject(anObject);
		values[getProperty().getIndex()] = value == null ? NULL_ENTRY : value;
	}

	public EntityPropertyImpl getProperty() {
		return this.property;
	}

	@Override
	public Class getAttributeType() {
		if (getProperty().isReference()) {
			EntityReferencePropertyImpl refProperty = (EntityReferencePropertyImpl) getProperty();

			if (refProperty.isCollection()) {
				return ((EntityCollectionPropertyImpl) refProperty).getCollectionMapping().getContainerPolicy().getContainerClass();
			}
			if (refProperty.getReferenceMapping().usesIndirection()) {
				return ValueHolderInterface.class;
			}
			return refProperty.getReferenceClass();
		} else {
			return getProperty().getAttributeType();
		}
	}

}
