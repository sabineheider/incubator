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
package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.copyhelper;

import java.io.InputStream;
import java.util.List;

import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;
import org.eclipse.persistence.testing.sdo.SDOTestCase;

import commonj.sdo.DataObject;

public class CopyHelperTestCases extends SDOTestCase {

    private static final String XML_SCHEMA = "org/eclipse/persistence/testing/sdo/helper/jaxbhelper/copyhelper/CopyHelper.xsd";
    
    private JAXBHelperContext jaxbHelperContext;
    
    public CopyHelperTestCases(String name) {
        super(name);
    }
    
    public void setUp() {
        CopyHelperProject project = new CopyHelperProject();
        XMLContext xmlContext = new XMLContext(project);
        JAXBContext jaxbContext = new JAXBContext(xmlContext);
        jaxbHelperContext = new JAXBHelperContext(jaxbContext);
        
        InputStream xsd = Thread.currentThread().getContextClassLoader().getResourceAsStream(XML_SCHEMA);
        jaxbHelperContext.getXSDHelper().define(xsd, null);
    }
    
    public void testCopy() {
        DataObject rootDO = jaxbHelperContext.getDataFactory().create("urn:copy", "root");
        DataObject child1DO = rootDO.createDataObject("child1");
        DataObject child2DO = rootDO.createDataObject("child2");

        DataObject rootDOCopy = jaxbHelperContext.getCopyHelper().copy(rootDO);
        
        Root root = (Root) jaxbHelperContext.unwrap(rootDO);
        Root rootCopy = (Root) jaxbHelperContext.unwrap(rootDOCopy);
        
        assertTrue(jaxbHelperContext.getEqualityHelper().equal(rootDO, rootDOCopy));
        
        assertNotSame(root, rootCopy);
        assertNotNull(rootCopy);

        assertNotSame(root.getChild1(), rootCopy.getChild1());
        assertNotNull(rootCopy.getChild1());
        
        assertNotSame(root.getChild2(), rootCopy.getChild2());
        assertNotNull(rootCopy.getChild2());
    }

    public void tearDown() {
    }

}
