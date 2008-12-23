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


import com.example.customer.Customer;
import commonj.sdo.DataObject;

public class JAXBDemo2 {
    
    public static void main(String[] args) throws Exception {

        /*
         * Step #1
         * Create the JAXBContext
         */
        JAXBContext jaxbContext = JAXBContext.newInstance("com.example.customer");

        /*
        *Step #2
        * Create the JAXB aware SDO Helper Context
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
         * Create new DataObjects
         */
        DataObject customerDO = jaxbHelperContext.getDataFactory().create("urn:customer", "customer");
        customerDO.set("first-name", "Jane");
        Customer customer = (Customer) jaxbHelperContext.unwrap(customerDO);
        System.out.println(customer.getFirstName());
    
        DataObject billingAddressDO = customerDO.createDataObject("billing-address");
        billingAddressDO.set("street", "123 Any Street");
        System.out.println(customer.getBillingAddress().getStreet());
    
        DataObject phoneNumberDO = jaxbHelperContext.getDataFactory().create("urn:customer", "phone-number");
        customerDO.getList("phone-number").add(phoneNumberDO);
    
        customerDO.unset("billing-address");
        customerDO.unset("phone-number");
    }
 

}
