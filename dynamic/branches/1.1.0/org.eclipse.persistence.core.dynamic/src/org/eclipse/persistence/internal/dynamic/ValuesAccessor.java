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

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.mappings.foundation.AbstractDirectMapping;

/**
 * ValueAccessor is a specialized AttributeAccessor enabling usage of the
 * {@link DynamicEntityImpl#values} (Object[]) instead of a field/property
 * access available in static domain classes.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class ValuesAccessor extends AttributeAccessor {

    /**
     * NULL_VALUE is a singleton value used to indicate that a null value was
     * explicitly put in a given 'slot' and not just there due to the default
     * creation of an Object[].
     */
    protected final static Object NULL_VALUE = new Object();

    protected DatabaseMapping mapping;

    /**
     * {@link EntityType} used to reset Object[] size when needed.
     */
    protected EntityType type;

    /**
     * Index in the values Object[] where the owning mapping's value is stored.
     * This index assumes that the mappings remain in a static order.
     */
    protected int index;

    public ValuesAccessor(EntityType type, DatabaseMapping mapping, int index) {
        super();
        this.type = type;
        this.mapping = mapping;
        this.index = index;
    }

    public DatabaseMapping getMapping() {
        return this.mapping;
    }

    public int getIndex() {
        return this.index;
    }

    public EntityType getType() {
        return this.type;
    }

    /**
     * Access the Object[] from the {@link DynamicEntity}.
     * <p>
     * If the length of the array is incorrect this is where it will be lazily
     * fixed.
     */
    private Object[] getValues(Object entity) {
        Object[] values = ((DynamicEntityImpl) entity).values;

        if (getIndex() >= values.length) {
            Object[] newValues = new Object[getType().getNumberOfProperties()];
            System.arraycopy(values, 0, newValues, 0, values.length);
            ((DynamicEntityImpl) entity).values = newValues;
            values = newValues;
        }

        return values;
    }

    /**
     * <b>INTERNAL</b>: Direct access to the value in the Object[] for this
     * mapping. This method is provided for advanced users and can provide
     * direct access to he NULL_VALUE. All application access should be done
     * using the {@link DynamicEntity} get/set API.
     */
    public Object getRawValue(Object entity) {
        return getValues(entity)[getIndex()];
    }

    public Object getAttributeValueFromObject(Object entity) throws DescriptorException {
        Object value = getRawValue(entity);

        return value == NULL_VALUE ? null : value;
    }

    /**
     * <b>INTERNAL</b>: Direct access to the value in the Object[] for this
     * mapping. This method is provided for advanced users and BYPASSES THE USE
     * OF NULL_VALUE. All application access should be done using the
     * {@link DynamicEntity} API
     */
    public void setRawValue(Object entity, Object value) {
        getValues(entity)[getIndex()] = value;
    }

    public void setAttributeValueInObject(Object entity, Object value) throws DescriptorException {
        setRawValue(entity, value == null ? NULL_VALUE : value);
    }

    protected boolean isSet(Object entity) throws DescriptorException {
        Object[] values = getValues(entity);
        Object value = values[getIndex()];

        return value != null || value == NULL_VALUE;
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
                // If the mapping was read from project XML using the MW
                // mappings the name will be not null
                if (getMapping().isAbstractDirectMapping() && ((AbstractDirectMapping) getMapping()).getAttributeClassificationName() != null) {
                    String typeName = ((AbstractDirectMapping) getMapping()).getAttributeClassificationName().trim();

                    // Using the default conversion manager for now assuming all
                    // direct types will be standard java types and not other
                    // dynamic types. This could possibly be configured with the 
                    // AbstractDirectMapping.convertClassNamesToClasses call
                    Class<?> attrType = ConversionManager.getDefaultManager().convertClassNameToClass(typeName);
                    ((AbstractDirectMapping) getMapping()).setAttributeClassification(attrType);
                    return attrType;
                }
                return ClassConstants.OBJECT;
            }
            return getMapping().getAttributeClassification();
        }
    }
}
