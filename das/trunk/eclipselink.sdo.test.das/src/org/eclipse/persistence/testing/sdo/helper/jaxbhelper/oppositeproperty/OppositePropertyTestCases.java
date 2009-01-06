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
package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.oppositeproperty;

import java.io.InputStream;
import java.util.List;

import org.eclipse.persistence.testing.sdo.SDOTestCase;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;

import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.sdo.helper.SDOHelperContext;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;

public class OppositePropertyTestCases extends SDOTestCase {

    private static final String XML_SCHEMA = "org/eclipse/persistence/testing/sdo/helper/jaxbhelper/oppositeproperty/OppositeProperty.xsd";
    
    private JAXBHelperContext jaxbHelperContext;
    
    public OppositePropertyTestCases(String name) {
        super(name);
    }

    public void setUp() {
        OppositeProject project = new OppositeProject();
        XMLContext xmlContext = new XMLContext(project);
        JAXBContext jaxbContext = new JAXBContext(xmlContext);
        jaxbHelperContext = new JAXBHelperContext(jaxbContext);
        
        InputStream xsd = Thread.currentThread().getContextClassLoader().getResourceAsStream(XML_SCHEMA);
        jaxbHelperContext.getXSDHelper().define(xsd, null);
    }
    
    public void testOppositePropertySet() {
        DataObject rootDO = jaxbHelperContext.getDataFactory().create("urn:opposite", "root");
        DataObject child1DO = rootDO.createDataObject("child1");
        DataObject child2DO = rootDO.createDataObject("child2");

        Property child2Property = child1DO.getType().getProperty("child2");
        child1DO.set(child2Property, child2DO);
        this.assertEquals(child1DO, child2DO.get("child1"));
        
        Child2 child2 = (Child2) jaxbHelperContext.unwrap(child2DO);
        this.assertNotNull(child2.getChild1());
    }
    
    public void testOppositePropertyCleared1() {
        DataObject rootDO = jaxbHelperContext.getDataFactory().create("urn:opposite", "root");
        DataObject child1DO = rootDO.createDataObject("child1");
        DataObject child2DO = rootDO.createDataObject("child2");
        DataObject newChild2DO =  rootDO.createDataObject("child2");
        
        Property child2Property = child1DO.getType().getProperty("child2");
        child1DO.set(child2Property, child2DO);
        child1DO.set(child2Property, newChild2DO);
        
        this.assertNull(child2DO.get("child1"));
        this.assertEquals(child1DO, newChild2DO.get("child1"));
        
        Child2 child2 = (Child2) jaxbHelperContext.unwrap(child2DO);
        this.assertNull(child2.getChild1());
        
        Child2 newChild2 = (Child2) jaxbHelperContext.unwrap(newChild2DO);
        this.assertNotNull(newChild2.getChild1());        
    }

    public void testOppositePropertyCleared2() {
        DataObject rootDO = jaxbHelperContext.getDataFactory().create("urn:opposite", "root");
        DataObject child1DO = jaxbHelperContext.getDataFactory().create("urn:opposite", "child1");
        DataObject child2DO = jaxbHelperContext.getDataFactory().create("urn:opposite", "child2");
        DataObject newChild2DO =  jaxbHelperContext.getDataFactory().create("urn:opposite", "child2");
        
        Property child2Property = child1DO.getType().getProperty("child2");
        child1DO.set(child2Property, child2DO);
        child1DO.set(child2Property, newChild2DO);
        
        this.assertNull(child2DO.get("child1"));
        this.assertEquals(child1DO, newChild2DO.get("child1"));
        
        Child2 child2 = (Child2) jaxbHelperContext.unwrap(child2DO);
        this.assertNull(child2.getChild1());
        
        Child2 newChild2 = (Child2) jaxbHelperContext.unwrap(newChild2DO);
        this.assertNotNull(newChild2.getChild1());        
    }
    
    public void tearDown() {
        
    }
}