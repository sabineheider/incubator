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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.namespace.QName;

import org.eclipse.persistence.internal.oxm.MappingNodeValue;
import org.eclipse.persistence.internal.oxm.TreeObjectBuilder;
import org.eclipse.persistence.internal.oxm.XPathFragment;
import org.eclipse.persistence.internal.oxm.XPathNode;
import org.eclipse.persistence.internal.queries.ContainerPolicy;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.mappings.ContainerMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.oxm.mappings.XMLCompositeCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLCompositeObjectMapping;
import org.eclipse.persistence.oxm.mappings.XMLObjectReferenceMapping;
import org.eclipse.persistence.sdo.SDODataObject;
import org.eclipse.persistence.sdo.SDOProperty;
import org.eclipse.persistence.sdo.ValueStore;
import org.eclipse.persistence.sdo.helper.ListWrapper;

import commonj.sdo.DataObject;
import commonj.sdo.Property;

public class JAXBValueStore implements ValueStore {

    private JAXBHelperContext jaxbHelperContext;
    private Object entity;
    private XMLDescriptor descriptor;
    private SDODataObject dataObject;
    private Map<Property, ListWrapper> listWrappers;

    public JAXBValueStore(JAXBHelperContext aJAXBHelperContext, QName qName) {
        this.jaxbHelperContext = aJAXBHelperContext;
        listWrappers = new WeakHashMap<Property, ListWrapper>();
        XPathFragment xPathFragment = new XPathFragment(qName.getLocalPart());
        xPathFragment.setNamespaceURI(qName.getNamespaceURI());
        JAXBContext jaxbContext = (JAXBContext) jaxbHelperContext.getJAXBContext();
        this.descriptor = jaxbContext.getXMLContext().getDescriptorByGlobalType(xPathFragment);
        if(this.descriptor == null) {
            this.descriptor = jaxbContext.getXMLContext().getDescriptor(qName);
        }
        this.entity = descriptor.getInstantiationPolicy().buildNewInstance();
    }

    public JAXBValueStore(JAXBHelperContext aJAXBHelperContext, Object anEntity) {
        this.jaxbHelperContext = aJAXBHelperContext;
        this.listWrappers = new WeakHashMap<Property, ListWrapper>();
        JAXBContext jaxbContext = (JAXBContext) jaxbHelperContext.getJAXBContext();
        this.descriptor = (XMLDescriptor) jaxbContext.getXMLContext().getSession(anEntity).getDescriptor(anEntity);
        this.entity = anEntity;
    }

    SDODataObject getDataObject() {
        return dataObject;
    }

    Object getEntity() {
        return entity;
    }

    XMLDescriptor getEntityDescriptor() {
        return descriptor;
    }

    JAXBHelperContext getJAXBHelperContext() {
        return jaxbHelperContext;
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
        DatabaseMapping mapping = this.getJAXBMappingForProperty(declaredProperty);
        Object value = mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
        if(null == value || declaredProperty.getType().isDataType()) {
            if(declaredProperty.isMany()) {
                return new JAXBListWrapper(this, declaredProperty);
            } else {
                return value;
            }
        } else if(declaredProperty.isMany()) {
            ListWrapper listWrapper = listWrappers.get(declaredProperty);
            if(null != listWrapper) {
                return listWrapper;
            }
            listWrapper = new JAXBListWrapper(this, declaredProperty);
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
        DatabaseMapping mapping = this.getJAXBMappingForProperty(declaredProperty);
        if(declaredProperty.getType().isDataType()) {
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, value);
        } else if(declaredProperty.isMany()) {
            //Get a ListWrapper and set it's current elements
            ListWrapper listWrapper = (ListWrapper)getDeclaredProperty(propertyIndex);
            listWrapper.setCurrentElements((List)value);
        } else {
            // OLD VALUE
            Object oldValue = mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
            if(mapping.isAbstractCompositeObjectMapping()) {
                XMLCompositeObjectMapping compositeMapping = (XMLCompositeObjectMapping) mapping;
                if(oldValue != null && compositeMapping.getContainerAccessor() != null) {
                    compositeMapping.getContainerAccessor().setAttributeValueInObject(oldValue, null);
                }
            }

            // NEW VALUE
            value = jaxbHelperContext.unwrap((DataObject) value);
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, value);
            if(mapping.isAbstractCompositeObjectMapping()) {
                XMLCompositeObjectMapping compositeMapping = (XMLCompositeObjectMapping) mapping;
                if(value != null && compositeMapping.getContainerAccessor() != null) {
                    compositeMapping.getContainerAccessor().setAttributeValueInObject(value, entity);
                }
            }
        }
    }

    /**
     * For isMany=false properties always return true.  For collection properties
     * return true if the collection is not empty.
     */
    public boolean isSetDeclaredProperty(int propertyIndex) {
        SDOProperty declaredProperty = (SDOProperty) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        DatabaseMapping mapping = this.getJAXBMappingForProperty(declaredProperty);
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
        DatabaseMapping mapping = this.getJAXBMappingForProperty(declaredProperty);
        if(declaredProperty.isMany()) {
            ContainerMapping containerMapping = (ContainerMapping) mapping;
            ContainerPolicy containerPolicy = containerMapping.getContainerPolicy();

            // OLD VALUE
            if(mapping.isAbstractCompositeCollectionMapping()) {
                XMLCompositeCollectionMapping compositeMapping = (XMLCompositeCollectionMapping) mapping;
                if(compositeMapping.getContainerAccessor() != null) {
                    Object oldContainer = mapping.getAttributeValueFromObject(entity);
                    if(oldContainer != null) {
                        AbstractSession session = ((JAXBContext) jaxbHelperContext.getJAXBContext()).getXMLContext().getSession(entity);
                        Object iterator = containerPolicy.iteratorFor(oldContainer);
                        while(containerPolicy.hasNext(iterator)) {
                            Object oldValue =  containerPolicy.next(iterator, session);
                            compositeMapping.getContainerAccessor().setAttributeValueInObject(oldValue, null);                            
                        }
                    }
                }
            }

            // NEW VALUE
            Object container = containerPolicy.containerInstance();
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, container);
        } else {
            // OLD VALUE
            if(mapping.isAbstractCompositeObjectMapping()) {
                XMLCompositeObjectMapping compositeMapping = (XMLCompositeObjectMapping) mapping;
                if(compositeMapping.getContainerAccessor() != null) {
                    Object oldValue = mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
                    if(oldValue != null) {
                        compositeMapping.getContainerAccessor().setAttributeValueInObject(oldValue, null);
                    }
                }
            }

            // NEW VALUE
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

    public void setManyProperty(Property property, Object value) {
        DatabaseMapping mapping = this.getJAXBMappingForProperty((SDOProperty) property);
        ContainerMapping containerMapping = (ContainerMapping) mapping;
        ContainerPolicy containerPolicy = containerMapping.getContainerPolicy();
        AbstractSession session = ((JAXBContext) jaxbHelperContext.getJAXBContext()).getXMLContext().getSession(entity);
        Collection collection = (Collection) value;
        if(!property.getType().isDataType()) {
            collection = getJAXBHelperContext().unwrap(collection);
        }

        Iterator collectionIterator = collection.iterator();
        Object container = containerMapping.getContainerPolicy().containerInstance();
        while(collectionIterator.hasNext()) {
            Object collectionValue = collectionIterator.next();
            containerPolicy.addInto(collectionValue, container, session);
        }
        mapping.setAttributeValueInObject(entity, container);
    }

    public ValueStore copy() {
        throw new UnsupportedOperationException();
    }

    DatabaseMapping getJAXBMappingForProperty(SDOProperty sdoProperty) {
        DatabaseMapping sdoMapping = sdoProperty.getXmlMapping();
        XMLField field;
        if(sdoMapping instanceof XMLObjectReferenceMapping) {
            XMLObjectReferenceMapping referenceMapping = (XMLObjectReferenceMapping) sdoMapping;
            field = (XMLField) referenceMapping.getFields().get(0);
        } else {
            field = (XMLField) sdoMapping.getField();
        }
        TreeObjectBuilder treeObjectBuilder = (TreeObjectBuilder) descriptor.getObjectBuilder();
        XPathNode xPathNode = treeObjectBuilder.getRootXPathNode();
        XPathFragment xPathFragment = field.getXPathFragment();
        while(xPathNode != null && xPathFragment != null) {
            if(xPathFragment.isAttribute()) {
                if(sdoProperty.isMany() && !sdoProperty.isContainment() && !sdoProperty.getType().isDataType()) {
                    xPathFragment = null;
                    break;
                }
                Map attributeChildrenMap = xPathNode.getAttributeChildrenMap();
                if(null == attributeChildrenMap) {
                    xPathNode = null;
                } else {
                    xPathNode = (XPathNode) xPathNode.getAttributeChildrenMap().get(xPathFragment);
                }
            } else {
                Map nonAttributeChildrenMap = xPathNode.getNonAttributeChildrenMap();
                if(null == nonAttributeChildrenMap) {
                    xPathNode = null;
                } else {
                    xPathNode = (XPathNode) xPathNode.getNonAttributeChildrenMap().get(xPathFragment);
                }
            }
            xPathFragment = xPathFragment.getNextFragment();
            if(xPathFragment != null && xPathFragment.nameIsText()) {
                if(sdoProperty.isMany() && !sdoProperty.isContainment() && !sdoProperty.getType().isDataType()) {
                    xPathFragment = null;
                    break;
                }
            }
        }
        if(null == xPathFragment && xPathNode != null) {
            MappingNodeValue mappingNodeValue = (MappingNodeValue) xPathNode.getNodeValue();
            return mappingNodeValue.getMapping();
        }
        throw new RuntimeException("A JAXB Mapping could not be found for the XPath:  " + field.getXPath());
    }

}