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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;

import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.sdo.SDODataObject;
import org.eclipse.persistence.sdo.helper.SDOCopyHelper;
import org.eclipse.persistence.sdo.helper.SDODataHelper;
import org.eclipse.persistence.sdo.helper.SDOEqualityHelper;
import org.eclipse.persistence.sdo.helper.SDOHelperContext;
import org.eclipse.persistence.sdo.helper.delegates.SDOTypeHelperDelegate;
import org.eclipse.persistence.sdo.helper.delegates.SDOXSDHelperDelegate;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

/**
 * This helper is responsible for converting between JAXB objects and SDO
 * DataObjects.  The DataObject wraps the JAXB object.
 */
public class JAXBHelperContext extends SDOHelperContext {

    private org.eclipse.persistence.jaxb.JAXBContext jaxbContext;
    private Map<Object, SDODataObject> wrapperDataObjects;

    public JAXBHelperContext(JAXBContext aJAXBContext) {
        this(aJAXBContext, Thread.currentThread().getContextClassLoader());
    }

    public JAXBHelperContext(JAXBContext aJAXBContext, ClassLoader aClassLoader) {
        super(aClassLoader);
        wrapperDataObjects = new WeakHashMap<Object, SDODataObject>();
        jaxbContext = (org.eclipse.persistence.jaxb.JAXBContext) aJAXBContext;
    }

    protected void initialize(ClassLoader aClassLoader)  {
        copyHelper = new SDOCopyHelper(this);
        dataFactory = new JAXBDataFactory(this);
        dataHelper = new SDODataHelper(this);
        equalityHelper = new SDOEqualityHelper(this);
        xmlHelper = new JAXBXMLHelper(this, aClassLoader);
        typeHelper = new SDOTypeHelperDelegate(this);
        xsdHelper = new SDOXSDHelperDelegate(this);
    }

    public JAXBContext getJAXBContext() {
        return jaxbContext;
    }

    /**
     * Return a DataObject that wraps the JPA entity.
     * Multiple calls to wrap for the same instance entity return the same
     * instance of DataObject, in other words the following is always true:
     * jpaHelper.wrap(customer123) == jpaHelper.wrap(customer123)
     * jpaHelper.wrap(customer123) != jpaHelper.wrap(customer456)
     */
    public DataObject wrap(Object entity) {
        SDODataObject wrapperDO = wrapperDataObjects.get(entity);
        if(null != wrapperDO) {
            return wrapperDO;
        }

        XMLDescriptor entityDescriptor = (XMLDescriptor) jaxbContext.getXMLContext().getSession(entity).getDescriptor(entity);
        QName qName = entityDescriptor.getSchemaReference().getSchemaContextAsQName(entityDescriptor.getNamespaceResolver());
        Type wrapperType = getTypeHelper().getType(qName.getNamespaceURI(), qName.getLocalPart());
        wrapperDO = (SDODataObject) getDataFactory().create(wrapperType);

        JAXBValueStore jaxbValueStore = new JAXBValueStore(this, entity); 
        wrapperDO._setCurrentValueStore(jaxbValueStore);
        jaxbValueStore.initialize(wrapperDO);

        wrapperDataObjects.put(entity, wrapperDO);
        return wrapperDO;
    }

    public List<DataObject> wrap(Collection<Object> entities) {
        if(null == entities) {
            return null;
        }
        List<DataObject> dataObjects = new ArrayList<DataObject>(entities.size());
        for(Object entity: entities) {
            dataObjects.add(wrap(entity));
        }
        return dataObjects;
    }

    /**
     * Return the JPA entity that is wrapped by the DataObject.
     * Multiple calls to unwrap for the same entity instance must return the 
     * same instance of DataObject, in other words the following is always true:
     * jpaHelper.unwrap(customerDO123) == jpaHelper.wrap(customerDO123)
     * jpaHelper.unwrap(customerDO123) != jpaHelper.wrap(customerDO456)
     * customer123 == jpaHelper.unwrap(jpaHelper.wrap(customer123))
     */
    public Object unwrap(DataObject dataObject) {
        SDODataObject sdoDataObject = (SDODataObject) dataObject;
        JAXBValueStore jpaValueStore = (JAXBValueStore) sdoDataObject._getCurrentValueStore();
        return jpaValueStore.getEntity();
    }

    public List<Object> unwrap(Collection<DataObject> dataObjects) {
        if(null == dataObjects) {
            return null;
        }
        List<Object> entities = new ArrayList<Object>(dataObjects.size());
        for(DataObject dataObject: dataObjects) {
            entities.add(unwrap(dataObject));
        }
        return entities;
    }

}