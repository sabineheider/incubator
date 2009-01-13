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
 *    dclarke - JPA DAS INCUBATOR - Enhancement 258057
 *              http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing.das;

import static junit.framework.Assert.*;
import model.Employee;
import model.Gender;

import org.junit.Test;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

/**
 * 
 * @author dclarke EclipseLink 1.1
 */
public class TestEmployeeDAS_Create extends TestEmployeeDAS {

	@Test
	public void createNewEmployee() {
		createNewEmployee(this, "Delete", "Me", Gender.Male);
	}

	@Test
	public void createNewEmplyeeWithAddress() {
		Type type = getDAS().getContext().getType(Employee.class);
		DataObject empDO = getDAS().getContext().getDataFactory().create(type);

		assertNotNull(empDO);

		assertEquals(0, empDO.getInt("id"));

		Employee emp = (Employee) getSDOContext().unwrap(empDO);
		assertNotNull(emp);
		assertEquals(0, emp.getId());

		empDO.setString("first-name", "Delete");
		empDO.setString("last-name", "Me");
		empDO.setString("gender", Gender.Female.name());

		DataObject addressDO = empDO.createDataObject("address");
		addressDO.setString("city", "Ottawa");
		
		getTracker().reset();
		
		getDAS().merge(empDO);
		
		assertEquals(3, getTracker().getTotalSQLINSERTCalls());
	}

	@Test
	public void createNewEmplyeeWithAddressAndPhone() {
		Type type = getDAS().getContext().getType(Employee.class);
		DataObject empDO = getDAS().getContext().getDataFactory().create(type);

		assertNotNull(empDO);

		assertEquals(0, empDO.getInt("id"));

		Employee emp = (Employee) getSDOContext().unwrap(empDO);
		assertNotNull(emp);
		assertEquals(0, emp.getId());

		empDO.setString("first-name", "Delete");
		empDO.setString("last-name", "Me");
		empDO.setString("gender", Gender.Female.name());

		DataObject addressDO = empDO.createDataObject("address");
		addressDO.setString("city", "Ottawa");
		
		DataObject phoneDO =  empDO.createDataObject("phone-number");
		phoneDO.setString("number", "6135551212");
		phoneDO.setString("type", "cell");
		
		getTracker().reset();
		
		getDAS().merge(empDO);
		
		assertEquals(4, getTracker().getTotalSQLINSERTCalls());
	}

	public static DataObject createNewEmployee(TestEmployeeDAS test, String firstName, String lastName, Gender gender) {
		Type type = test.getDAS().getContext().getType(Employee.class);
		DataObject empDO = test.getDAS().getContext().getDataFactory().create(type);

		assertNotNull(empDO);

		assertEquals(0, empDO.getInt("id"));

		Employee emp = (Employee) test.getSDOContext().unwrap(empDO);
		assertNotNull(emp);
		assertEquals(0, emp.getId());

		empDO.setString("first-name", firstName);
		empDO.setString("last-name", lastName);
		empDO.setString("gender", gender.name());

		assertEquals(firstName, empDO.getString("first-name"));
		assertEquals(lastName, empDO.getString("last-name"));
		assertEquals(gender.name(), empDO.getString("gender"));

		assertEquals(firstName, emp.getFirstName());
		assertEquals(lastName, emp.getLastName());
		assertEquals(gender, emp.getGender());
		assertNull(emp.getAddress());
		assertTrue(emp.getPhoneNumbers().isEmpty());

		DataObject persistedDO = test.getDAS().merge(empDO);

		assertNotNull("Null DataObject returned from DAS merge", persistedDO);
		int maxId = test.findMaximumEmployeeId();
		assertEquals(maxId, persistedDO.getInt("id"));

		return persistedDO;
	}
}
