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
 *     bdoughan - JPA DAS INCUBATOR - Enhancement 258057
 *     			 http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package demo;

import java.io.FileInputStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBContext;

import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;

import commonj.sdo.helper.XMLDocument;

public class JAXBDemo3 {

    public static void main(String[] args) throws Exception {
        
        /*
         * Step #1
         * Create the JAXBContext
         */
        JAXBContext jaxbContext = JAXBContext.newInstance("com.example.customer");
        
        /*
         * Step #2
         * Create the JPA aware SDO Helper Context
         */
        JAXBHelperContext jaxbHelperContext = new JAXBHelperContext(jaxbContext);
        
        
        /*
         * Step #3
         * Define the SDO metadata from an XML Schema
         */
        FileInputStream xsd = new FileInputStream("xsd/jpadas-customer.xsd");
        jaxbHelperContext.getXSDHelper().define(xsd, null);
        
        /*
         * Step #4
         * Unmarshal the XML document to DataObjects
         */
        FileInputStream xml = new FileInputStream("customer-1.xml");
        XMLDocument xmlDocument = jaxbHelperContext.getXMLHelper().load(xml);
        
        /*
         * Step #5
         * Marshal the DataObjects to XML
         */
        jaxbHelperContext.getXMLHelper().save(xmlDocument, System.out, null);
    }

}
