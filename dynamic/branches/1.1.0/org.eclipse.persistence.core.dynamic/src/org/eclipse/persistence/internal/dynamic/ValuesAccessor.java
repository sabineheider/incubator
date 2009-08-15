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

import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.mappings.*;

/**
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class ValuesAccessor extends AttributeAccessor {

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

    protected boolean isSet(Object entity) throws DescriptorException {
        Object[] values = getValues(entity);
        Object value = values[getIndex()];

        return value != null || value == NULL_ENTRY;
    }

    @Override
    public Class<?> getAttributeClass() {
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
            if (getMapping().getAttributeClassification() == null) {
                return ClassConstants.OBJECT;
            }
            return getMapping().getAttributeClassification();
        }
    }
}
