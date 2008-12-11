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
 *     dclarke - JPA DAS INCUBATOR - Enhancement 258057
 *     			 http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing.jaxb;

import javax.xml.bind.*;

import model.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class CreateNewEmployees {

	private static JAXBContext jaxbContext;

	@Test
	public void simple() throws JAXBException {
		Employee employee = new Employee();
		employee.setId(1);
		employee.setFirstName("John");
		employee.setLastName("Doe");
		employee.setGender(Gender.Male);
		employee.setSalary(49999.99);
		
		Address address = new Address();
		address.setId(1);
		address.setCity("Ottawa");
		address.setStreet("123 Somewhere Lane");
		address.setProvince("ON");
		address.setPostalCode("K1A1A1");
		address.setCountry("Canada");
		employee.setAddress(address);
		
		employee.addPhoneNumber("Home", "6135551212");
		employee.addPhoneNumber("Mobile", "6135552121");

		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(employee, System.out);
	}

	@BeforeClass
	public static void initializeContext() throws JAXBException {
		jaxbContext = JAXBContext.newInstance("model");
	}
}
