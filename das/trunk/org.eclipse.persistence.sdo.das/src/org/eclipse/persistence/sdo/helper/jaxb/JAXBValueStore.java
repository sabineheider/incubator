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
package org.eclipse.persistence.sdo.helper.jaxb;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.namespace.QName;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.oxm.MappingNodeValue;
import org.eclipse.persistence.internal.oxm.TreeObjectBuilder;
import org.eclipse.persistence.internal.oxm.XPathFragment;
import org.eclipse.persistence.internal.oxm.XPathNode;
import org.eclipse.persistence.internal.queries.ContainerPolicy;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.mappings.ContainerMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.sdo.SDODataObject;
import org.eclipse.persistence.sdo.SDOProperty;
import org.eclipse.persistence.sdo.ValueStore;
import org.eclipse.persistence.sdo.helper.ListWrapper;

import commonj.sdo.DataObject;
import commonj.sdo.Property;

public class JAXBValueStore implements ValueStore {

    private JAXBHelperContext jaxbHelperContext;
    private Object entity;
    private ClassDescriptor descriptor;
    private SDODataObject dataObject;
    private Map<Property, ListWrapper> listWrappers;

    public JAXBValueStore(JAXBHelperContext aJAXBHelperContext, QName qName) {
        this.jaxbHelperContext = aJAXBHelperContext;
        listWrappers = new WeakHashMap<Property, ListWrapper>();
        XPathFragment xPathFragment = new XPathFragment(qName.getLocalPart());
        xPathFragment.setNamespaceURI(qName.getNamespaceURI());
        JAXBContext jaxbContext = (JAXBContext) jaxbHelperContext.getJAXBContext();
        this.descriptor = jaxbContext.getXMLContext().getDescriptorByGlobalType(xPathFragment);
        this.entity = descriptor.getInstantiationPolicy().buildNewInstance();
    }

    public JAXBValueStore(JAXBHelperContext aJAXBHelperContext, Object anEntity) {
        this.jaxbHelperContext = aJAXBHelperContext;
        this.listWrappers = new WeakHashMap<Property, ListWrapper>();
        JAXBContext jaxbContext = (JAXBContext) jaxbHelperContext.getJAXBContext();
        this.descriptor = jaxbContext.getXMLContext().getSession(anEntity).getDescriptor(anEntity);
        this.entity = anEntity;
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
        SDOProperty declaredProperty = (SDOProperty) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        DatabaseMapping mapping = this.getMappignForField((XMLField) declaredProperty.getXmlMapping().getField());
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
            AbstractSession session = ((JAXBContext) jaxbHelperContext.getJAXBContext()).getXMLContext().getSession(entity);
            while(containerPolicy.hasNext(containerIterator)) {
                Object item = containerPolicy.next(containerIterator, session);
                DataObject valueDO = jaxbHelperContext.wrap(item);
                listWrapper.add(valueDO);
            }
            listWrappers.put(declaredProperty, listWrapper);
            return listWrapper;
        } else {
            return jaxbHelperContext.wrap(value);
        }
    }

    /**
     * Set the value on the underlying entity, unwrapping values as necessary.
     */
    public void setDeclaredProperty(int propertyIndex, Object value) {
        SDOProperty declaredProperty = (SDOProperty) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        DatabaseMapping mapping = this.getMappignForField((XMLField) declaredProperty.getXmlMapping().getField());
        if(declaredProperty.getType().isDataType()) {
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, value);
        } else if(declaredProperty.isMany()) {
            throw new UnsupportedOperationException();
        } else {
            value = jaxbHelperContext.unwrap((DataObject) value);
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, value);
        }
    }

    /**
     * For isMany=false properties always return true.  For collection properties
     * return true if the collection is not empty.
     */
    public boolean isSetDeclaredProperty(int propertyIndex) {
        SDOProperty declaredProperty = (SDOProperty) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        DatabaseMapping mapping = this.getMappignForField((XMLField) declaredProperty.getXmlMapping().getField());
        if(declaredProperty.isMany()) {
            Collection collection = (Collection) mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
            if(null == collection) {
                return false;
            }
            return !collection.isEmpty();
        } else {
            return null != mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
        }
    }

    /**
     * For isMany=false properties set the value to null.
     * For isMany=true set the value to an empty container of the 
     * appropriate type.
     */
    public void unsetDeclaredProperty(int propertyIndex) {
        SDOProperty declaredProperty = (SDOProperty) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        DatabaseMapping mapping = this.getMappignForField((XMLField) declaredProperty.getXmlMapping().getField());
        if(declaredProperty.isMany()) {
            ContainerMapping containerMapping = (ContainerMapping) mapping;
            Object container = containerMapping.getContainerPolicy().containerInstance();
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, container);
        } else {
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

    private DatabaseMapping getMappignForField(XMLField field) {
        TreeObjectBuilder treeObjectBuilder = (TreeObjectBuilder) descriptor.getObjectBuilder();
        XPathNode xPathNode = treeObjectBuilder.getRootXPathNode();
        XPathFragment xPathFragment = field.getXPathFragment();
        while(xPathNode != null && xPathFragment != null) {
            if(xPathFragment.isAttribute()) {
                xPathNode = (XPathNode) xPathNode.getAttributeChildrenMap().get(xPathFragment);
            } else {
                xPathNode = (XPathNode) xPathNode.getNonAttributeChildrenMap().get(xPathFragment);
            }
            xPathFragment = xPathFragment.getNextFragment();
        }
        if(null == xPathFragment && xPathNode != null) {
            MappingNodeValue mappingNodeValue = (MappingNodeValue) xPathNode.getNodeValue();
            return mappingNodeValue.getMapping();
        }
        return null;
    }

}