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
 *    bdoughan - JPA DAS INCUBATOR - Enhancement 258057
 *               http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.sdo.helper.jpa;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.queries.ContainerPolicy;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.ContainerMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sdo.SDODataObject;
import org.eclipse.persistence.sdo.ValueStore;
import org.eclipse.persistence.sdo.helper.ListWrapper;

import commonj.sdo.DataObject;
import commonj.sdo.Property;

public class JPAValueStore implements ValueStore {

    private SDOJPAHelper sdoJPAHelper;
    private Object entity;
    private ClassDescriptor descriptor;
    private SDODataObject dataObject;
    private Map<Property, ListWrapper> listWrappers;

    public JPAValueStore(SDOJPAHelper aSDOJPAHelper, Object anEntity) {
        this.sdoJPAHelper = aSDOJPAHelper;
        this.entity = anEntity;
        EntityManagerImpl entityManager = (EntityManagerImpl) sdoJPAHelper.getEntityManager();
        descriptor = entityManager.getSession().getClassDescriptor(entity);
        listWrappers = new WeakHashMap<Property, ListWrapper>();
    }

	public Object getEntity() {
        return entity;
    }

    public void initialize(DataObject aDataObject) {
        this.dataObject = (SDODataObject) aDataObject;
    }

    /**
     * Get the value from the wrapped JPA entity, wrapping in DataObjects
     * as necessary.
     */
    public Object getDeclaredProperty(int propertyIndex) {
        Property declaredProperty = (Property) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        DatabaseMapping mapping = descriptor.getMappingForAttributeName(declaredProperty.getName());
        Object value = mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
        if(null == value || declaredProperty.getType().isDataType()) {
            return value;
        } else if(declaredProperty.isMany()) {
            ListWrapper listWrapper = listWrappers.get(declaredProperty);
            if(null != listWrapper) {
                return listWrapper;
            }
            listWrapper = new ListWrapper(dataObject, declaredProperty);
            ContainerMapping containerMapping = (ContainerMapping) mapping;
            ContainerPolicy containerPolicy = containerMapping.getContainerPolicy();
            Object containerIterator = containerPolicy.iteratorFor(value);
            EntityManagerImpl entityManager = (EntityManagerImpl) sdoJPAHelper.getEntityManager();
            while(containerPolicy.hasNext(containerIterator)) {
                Object item = containerPolicy.next(containerIterator, (AbstractSession) entityManager.getSession());
                DataObject valueDO = sdoJPAHelper.wrap(item);
                listWrapper.add(valueDO);
            }
            listWrappers.put(declaredProperty, listWrapper);
            return listWrapper;
        } else {
            return sdoJPAHelper.wrap(value);
        }
    }

    /**
     * Set the value on the underlying entity, unwrapping values as necessary.
     */
    public void setDeclaredProperty(int propertyIndex, Object value) {
        Property declaredProperty = (Property) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        DatabaseMapping mapping = descriptor.getMappingForAttributeName(declaredProperty.getName());
        if(declaredProperty.getType().isDataType()) {
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, value);
        } else if(declaredProperty.isMany()) {
            throw new UnsupportedOperationException();
        } else {
            value = sdoJPAHelper.unwrap((DataObject) value);
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, value);
        }
    }

    /**
     * For isMany=false properties always return true.  For collection properties
     * return true if the collection is not empty.
     */
    public boolean isSetDeclaredProperty(int propertyIndex) {
        Property declaredProperty = (Property) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        if(declaredProperty.isMany()) {
            DatabaseMapping mapping = descriptor.getMappingForAttributeName(declaredProperty.getName());
            Collection collection = (Collection) mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
            if(null == collection) {
                return false;
            }
            return !collection.isEmpty();
        } else {
            return true;
        }
    }

    /**
     * For isMany=false properties set the value to null.
     * For isMany=true set the value to an empty container of the 
     * appropriate type.
     */
    public void unsetDeclaredProperty(int propertyIndex) {
        Property declaredProperty = (Property) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        if(declaredProperty.isMany()) {
            DatabaseMapping mapping = descriptor.getMappingForAttributeName(declaredProperty.getName());
            ContainerMapping containerMapping = (ContainerMapping) mapping;
            Object container = containerMapping.getContainerPolicy().containerInstance();
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, container);
        } else {
            DatabaseMapping mapping = descriptor.getMappingForAttributeName(declaredProperty.getName());
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, null);
        }
    }

    public Object getOpenContentProperty(Property property) {
        throw new UnsupportedOperationException();
    }

    public void setOpenContentProperty(Property property, Object value) {
        throw new UnsupportedOperationException();
    }

    public boolean isSetOpenContentProperty(Property property) {
        throw new UnsupportedOperationException();
    }

    public void unsetOpenContentProperty(Property property) {
        throw new UnsupportedOperationException();
    }

    public void setManyProperty(Property propertyName, Object value) {
        throw new UnsupportedOperationException();
    }

    public ValueStore copy() {
        throw new UnsupportedOperationException();
    }

}