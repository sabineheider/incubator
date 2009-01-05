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
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBContext;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;
import com.example.customer.Customer;

import commonj.sdo.DataObject;

public class JAXBDemo {

    public static void main(String[] args) throws Exception {
        
        /*
         * Step #1
         * Create the JPA EntityManager 
         */
        EntityManagerFactory emf = 
            Persistence.createEntityManagerFactory("MeetInTheMiddle");      
        EntityManager em = emf.createEntityManager();
        
        /*
         * Step #2
         * Create the JAXBContext
         */
        JAXBContext jaxbContext = JAXBContext.newInstance("com.example.customer");
        
        /*
         * Step #3
         * Create the JAXB aware SDO Helper Context
         */
        JAXBHelperContext jaxbHelperContext = new JAXBHelperContext(jaxbContext);

        /*
         * Step #4
         * Define the SDO metadata from an XML Schema
         */
        FileInputStream xsd = new FileInputStream("xsd/jpadas-customer.xsd");
        jaxbHelperContext.getXSDHelper().define(xsd, null);
        
        /*
         * Step #5
         * Query the entities from the database using JPA APIs.
         */
        Customer customer = 
            (Customer) em.createQuery("select c from Customer c where c.id = 1").getSingleResult();
    
        /*
         * Step #6
         * Wrap the JPA entity in a SDO data object.
         */
        DataObject customerDO = jaxbHelperContext.wrap(customer);
        
        /*
         * Step #7
         * Interact with the SDO data object.
         */
        System.out.println(customerDO.getString("billing-address/street"));
        customerDO.set("billing-address/street", new Date().getTime() + " Any Street");
        String xml = jaxbHelperContext.getXMLHelper().save(customerDO, "http://www.example.com/customer", "ROOT");
        System.out.println(xml);
        
        /*
         * Step #8 (Optional)
         * If necessary, unwrap the SDO data object to get the JPA entity.
         * Since the data object never left the VM it is still wrapping the 
         * original entity.
         */
        System.out.println(customer == jaxbHelperContext.unwrap(customerDO));
        
        /*
         * Step #9
         * Commit the JPA entity to the database.
         */
        em.getTransaction().begin();
        em.persist(jaxbHelperContext.unwrap(customerDO));
        em.getTransaction().commit();   
        
    }
}