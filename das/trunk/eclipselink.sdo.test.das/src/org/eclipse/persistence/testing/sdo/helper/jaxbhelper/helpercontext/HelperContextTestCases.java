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
package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.helpercontext;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.sdo.helper.SDODataFactory;
import org.eclipse.persistence.sdo.helper.SDOXMLHelper;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;
import org.eclipse.persistence.testing.sdo.SDOTestCase;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.XMLHelper;
import commonj.sdo.helper.XSDHelper;

public class HelperContextTestCases extends SDOTestCase {

    private JAXBHelperContext jaxbHelperContext;

    public HelperContextTestCases(String name) {
        super(name);
    }

    public void setUp() {
        try {
            Class[] classes = new Class[1];
            classes[0] = Root.class;
            JAXBContext jaxbContext = JAXBContext.newInstance(classes);
            jaxbHelperContext = new JAXBHelperContext(jaxbContext);
            // jaxbHelperContext.makeDefaultContext();
            JAXBSchemaOutputResolver jsor = new JAXBSchemaOutputResolver();
            jaxbContext.generateSchema(jsor);
            String xsd = jsor.getSchema();
            System.out.println(xsd);
            jaxbHelperContext.getXSDHelper().define(xsd);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void testGetType() {
        Type pojoType = jaxbHelperContext.getType(Root.class);
        assertNotNull(pojoType);
        
        Type rootType = jaxbHelperContext.getTypeHelper().getType("urn:helpercontext", "root");
        assertSame(rootType, pojoType);
    }

    public void testDataFactory() {
        SDODataFactory sdoDataFactory = (SDODataFactory) DataFactory.INSTANCE;
        System.out.println(sdoDataFactory.getHelperContext());
    }

    public void testXMLHelper() {
        SDOXMLHelper sdoXMLHelper = (SDOXMLHelper) XMLHelper.INSTANCE;
        System.out.println(sdoXMLHelper.getHelperContext());
    }

    public void tearDown() {
    }

    private class JAXBSchemaOutputResolver extends SchemaOutputResolver {

        private StringWriter schemaWriter;
        
        public String getSchema() {
            return schemaWriter.toString();
        }

        public Result createOutput(String arg0, String arg1) throws IOException {
            schemaWriter = new StringWriter();
            return new StreamResult(schemaWriter);
        }
        
    }

}
