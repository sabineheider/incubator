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
 *    mmacivor - JPA DAS INCUBATOR - Enhancement 258057
 *               http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.containment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;
import org.eclipse.persistence.testing.sdo.SDOTestCase;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.mappings.Child1;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.mappings.Child2;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.mappings.MappingsProject;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.mappings.Root;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;

public class ContainmentTestCases extends SDOTestCase {
    private static final String XML_SCHEMA = "org/eclipse/persistence/testing/sdo/helper/jaxbhelper/containment/containment.xsd";
    
    private JAXBHelperContext jaxbHelperContext;
    
    public ContainmentTestCases(String name) {
        super(name);
    }
    
    public void setUp() {
        ContainmentProject project = new ContainmentProject();
        XMLContext xmlContext = new XMLContext(project);
        JAXBContext jaxbContext = new JAXBContext(xmlContext);
        jaxbHelperContext = new JAXBHelperContext(jaxbContext);
        
        InputStream xsd = Thread.currentThread().getContextClassLoader().getResourceAsStream(XML_SCHEMA);
        jaxbHelperContext.getXSDHelper().define(xsd, null);
    }
    
    public void test1() throws IOException {
    	DataObject root1 = jaxbHelperContext.getDataFactory().create("urn:containment", "root");
    	DataObject root2 = jaxbHelperContext.getDataFactory().create("urn:containment", "root");
    	
    	DataObject child1 = jaxbHelperContext.getDataFactory().create("urn:containment", "child");
    	DataObject child2 = jaxbHelperContext.getDataFactory().create("urn:containment", "child");

    	assertTrue(child1.getContainer() == null);
    	assertTrue(child2.getContainer() == null);
    	
    	root1.set("child", child1);
    	assertTrue(child1.getContainer() == root1);
    	
    	root1.unset("child");
    	assertTrue(child1.getContainer() == null);
    	
    	List list = new ArrayList();
    	list.add(child1);
    	list.add(child2);
    	root2.setList("child-many", list);

    	assertTrue(child1.getContainer() == root2);
    	assertTrue(child2.getContainer() == root2);
    	
    	root2.getList("child-many").remove(child2);
    	assertTrue(child2.getContainer() == null);
    	
    	root2.unset("child-many");
    	assertTrue(child1.getContainer() == null);
    	
    	list = new ArrayList();
    	list.add(child1);
    	list.add(child2);
    	root2.setList("child-many", list);
    	
    	root1.set("child", child1);
    	assertFalse(root2.getList("child-many").contains(child1));
    	assertTrue(child1.getContainer() == root1);
    }
    
    public void tearDown() {
    }    
    

}
