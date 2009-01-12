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
package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.xmlhelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;
import org.eclipse.persistence.testing.sdo.SDOTestCase;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;
import commonj.sdo.helper.XSDHelper;

public class XMLHelperTestCases extends SDOTestCase {

    private static final String XML_SCHEMA = "org/eclipse/persistence/testing/sdo/helper/jaxbhelper/xmlhelper/GlobalElement.xsd";
    private static final String XML_INPUT = "org/eclipse/persistence/testing/sdo/helper/jaxbhelper/xmlhelper/GlobalElement.xml";
    private static final String XML_INPUT_UTF16 = "org/eclipse/persistence/testing/sdo/helper/jaxbhelper/xmlhelper/GlobalElement_UTF16.xml";

    private JAXBHelperContext jaxbHelperContext;

    public XMLHelperTestCases(String name) {
        super(name);
    }

    public void setUp() {
        XMLHelperProject project = new XMLHelperProject();
        XMLContext xmlContext = new XMLContext(project);
        JAXBContext jaxbContext = new JAXBContext(xmlContext);
        jaxbHelperContext = new JAXBHelperContext(jaxbContext);
        
        InputStream xsd = Thread.currentThread().getContextClassLoader().getResourceAsStream(XML_SCHEMA);
        jaxbHelperContext.getXSDHelper().define(xsd, null);
    }

    public void testTypes() {
        Type rootTypeType = jaxbHelperContext.getTypeHelper().getType("urn:xml", "root-type");
        assertNotNull(rootTypeType);

        Type rootType = jaxbHelperContext.getTypeHelper().getType("urn:xml", "root");
        assertNull(rootType);
    }

    public void testCreateTypeFromGlobalComplexType() {
        try {
            InputStream xml = Thread.currentThread().getContextClassLoader().getResourceAsStream(XML_INPUT);
            XMLDocument xmlDocument = jaxbHelperContext.getXMLHelper().load(xml);
            assertNotNull(xmlDocument);
            assertNotNull(xmlDocument.getRootObject());
        } catch(IOException e) {
            fail();
        }
    }

    public void testLoadUTF16() {
        try {
            InputStream xml = Thread.currentThread().getContextClassLoader().getResourceAsStream(XML_INPUT_UTF16);
            XMLDocument xmlDocument = jaxbHelperContext.getXMLHelper().load(xml);
            assertEquals("UTF-16LE", xmlDocument.getEncoding());
            assertEquals("1.1", xmlDocument.getXMLVersion());
        } catch(IOException e) {
            fail();
        }
    }

    public void tearDown() {
    }

}
