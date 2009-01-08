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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.*;

import model.*;

import org.junit.BeforeClass;
import org.junit.Test;
import static junit.framework.Assert.*;

public class CreateNewEmployees {

	private static JAXBContext jaxbContext;

	@Test
	public void simple() throws Exception {
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
		
		PhoneNumber p1 = employee.addPhoneNumber("Home", "6135551212");
		assertSame(employee, p1.getOwner());
		PhoneNumber p2 = employee.addPhoneNumber("Mobile", "6135552121");
		assertSame(employee, p2.getOwner());

		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter writer = new StringWriter();
		marshaller.marshal(employee, writer);
		
		System.out.println("XML::\n" + writer.toString());
		
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		
		Employee emp2 = (Employee) unmarshaller.unmarshal(new StringReader(writer.toString()));
		
		assertNotNull(emp2);
		assertNotSame(employee, emp2);
		assertEquals(employee.getId(), emp2.getId());
		assertEquals(employee.getFirstName(), emp2.getFirstName());
		assertEquals(employee.getLastName(), emp2.getLastName());
		assertEquals(employee.getGender(), emp2.getGender());
		assertEquals(employee.getSalary(), emp2.getSalary());
		assertEquals(employee.getVersion(), emp2.getVersion());
		
		assertNotNull(emp2.getAddress());
		assertEquals(employee.getAddress().getId(), emp2.getId());
		assertEquals(employee.getAddress().getCity(), emp2.getAddress().getCity());
		assertEquals(employee.getAddress().getStreet(), emp2.getAddress().getStreet());
		assertEquals(employee.getAddress().getProvince(), emp2.getAddress().getProvince());
		assertEquals(employee.getAddress().getPostalCode(), emp2.getAddress().getPostalCode());
		assertEquals(employee.getAddress().getCountry(), emp2.getAddress().getCountry());
		
		assertEquals(2, emp2.getPhoneNumbers().size());
		assertSame(emp2, emp2.getPhoneNumbers().get(0).getOwner());
		assertSame(emp2, emp2.getPhoneNumbers().get(1).getOwner());
	}

	@BeforeClass
	public static void initializeContext() throws JAXBException {
		jaxbContext = JAXBContext.newInstance("model");
	}
}
