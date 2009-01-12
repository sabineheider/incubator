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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import model.Employee;

import org.eclipse.persistence.internal.helper.SerializationHelper;
import org.junit.Test;

import testing.SamplePopulation;

import commonj.sdo.DataObject;

/**
 * 
 * @author dclarke EclipseLink 1.1
 */
public class TestEmployeeDAS_Queries extends TestEmployeeDAS {
	@Test
	public void testFind() {
		int empId = findMinimumEmployeeId();
		DataObject empDO = findEmployee(this, empId);

		assertNotNull(empDO);
		assertEquals(empId, empDO.getInt("id"));

		Employee emp = (Employee) getDAS().getContext().unwrap(empDO);
		assertNotNull(emp);
	}

	@Test
	public void serializeExisting() throws Exception {
		int empId = findMinimumEmployeeId();
		DataObject empDO = findEmployee(this, empId);

		Employee emp = (Employee) getSDOContext().unwrap(empDO);
		assertEquals(empId, emp.getId());

		DataObject serializedDO = (DataObject) SerializationHelper.deserialize(SerializationHelper.serialize(empDO));

		assertNotNull(serializedDO);
		assertEquals(empId, serializedDO.getInt("id"));

		Employee serializedEmp = (Employee) getSDOContext().unwrap(serializedDO);

		assertNotNull(serializedEmp);

		SamplePopulation.compare(emp, serializedEmp);
	}

	/**
	 * 
	 * @param test
	 * @param id
	 * @return
	 */
	public static DataObject findEmployee(TestEmployeeDAS test, int id) {
		DataObject empDO = test.getDAS().findEmployee(id);

		assertNotNull(empDO);
		assertEquals(id, empDO.getInt("id"));

		return empDO;
	}
}
