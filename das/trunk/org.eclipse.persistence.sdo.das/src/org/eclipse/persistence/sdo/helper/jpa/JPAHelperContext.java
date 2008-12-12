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

import commonj.sdo.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import javax.persistence.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.sdo.SDODataObject;
import org.eclipse.persistence.sdo.helper.SDOCopyHelper;
import org.eclipse.persistence.sdo.helper.SDODataHelper;
import org.eclipse.persistence.sdo.helper.SDOEqualityHelper;
import org.eclipse.persistence.sdo.helper.SDOHelperContext;
import org.eclipse.persistence.sdo.helper.delegates.SDOTypeHelperDelegate;
import org.eclipse.persistence.sdo.helper.delegates.SDOXSDHelperDelegate;

/**
 * This helper is responsible for converting between JPA entities and SDO
 * DataObjects.  The DataObject wraps the JPA entity.
 */
public class JPAHelperContext extends SDOHelperContext {

    private JAXBContext jaxbContext;
    private EntityManagerImpl jpaContext;
    private Map<Object, SDODataObject> wrapperDataObjects;

    public JPAHelperContext(EntityManager anEntityManager) throws Exception {
        this(anEntityManager, Thread.currentThread().getContextClassLoader());
    }

    public JPAHelperContext(EntityManager anEntityManager, ClassLoader aClassLoader) throws Exception {
        super(aClassLoader);
        wrapperDataObjects = new WeakHashMap<Object, SDODataObject>();
        jpaContext = (EntityManagerImpl) anEntityManager;
        
        Collection descriptors = jpaContext.getSession().getDescriptors().values();
        Class[] classes = new Class[descriptors.size()]; 
        Iterator descriptorIterator = descriptors.iterator();
        int index = 0;
        while(descriptorIterator.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor) descriptorIterator.next();
            classes[index++] = descriptor.getJavaClass();
        }
        jaxbContext = JAXBContext.newInstance(classes);
        JAXBSchemaOutputResolver schemaOutputResolver = new JAXBSchemaOutputResolver();
        jaxbContext.generateSchema(schemaOutputResolver);
        String xsd = schemaOutputResolver.getXMLSchema();
        getXSDHelper().define(xsd);
    }

    protected void initialize(ClassLoader aClassLoader)  {
        copyHelper = new SDOCopyHelper(this);
        dataFactory = new JPADataFactory(this);
        dataHelper = new SDODataHelper(this);
        equalityHelper = new SDOEqualityHelper(this);
        xmlHelper = new JPAXMLHelper(this, aClassLoader);
        typeHelper = new SDOTypeHelperDelegate(this);
        xsdHelper = new SDOXSDHelperDelegate(this);        
    }

    public JAXBContext getJAXBContext() {
        return jaxbContext;
    }

    public EntityManager getEntityManager() {
        return jpaContext;
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
        Type wrapperType = getTypeHelper().getType(entity.getClass());
        wrapperDO = (SDODataObject) getDataFactory().create(wrapperType);

        JPAValueStore jpaValueStore = new JPAValueStore(this, entity);
        jpaValueStore.initialize(wrapperDO);
        wrapperDO._setCurrentValueStore(jpaValueStore);
        wrapperDataObjects.put(entity, wrapperDO);
        return wrapperDO;
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
        JPAValueStore jpaValueStore = (JPAValueStore) sdoDataObject._getCurrentValueStore();
        return jpaValueStore.getEntity();
    }

    private static class JAXBSchemaOutputResolver extends SchemaOutputResolver {

        private StringWriter stringWriter;
        
        public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {
            stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            result.setSystemId(suggestedFileName);
            return result;
        }
        
        public String getXMLSchema() {
            if(null == stringWriter) {
                return null;
            }
            return stringWriter.toString();
        }
        
    }
    
}