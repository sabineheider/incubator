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

import model.Address;
import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.internal.descriptors.changetracking.AttributeChangeListener;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.Test;

import commonj.sdo.DataObject;

/**
 * 
 * @author dclarke EclipseLink 1.1
 */
public class TestEmployeeDAS_Modify extends TestEmployeeDAS {

	@Test
	public void incrementSalary_Local() {
		int empId = findMinimumEmployeeId();

		DataObject empDO = getDAS().findEmployee(empId);

		assertNotNull("No Employee DO returned for known employee id", empDO);
		Employee emp = (Employee) getSDOContext().unwrap(empDO);
		assertNotNull("Null POJO in DataObject wrapper", emp);
		

		long initialVersion = empDO.getLong("version");
		double initialSalary = empDO.getDouble("salary");
		
		// Double check values in POJO match
		assertEquals(initialVersion, emp.getVersion());
		assertEquals(initialSalary, emp.getSalary());

		if (emp instanceof ChangeTracker && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() != null && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() instanceof AttributeChangeListener) {
			assertFalse(JpaHelper.getEntityManager(getDAS().getEntityManager()).getUnitOfWork().hasChanges());
		}

		empDO.setDouble("salary", initialSalary + 1);
		
		if (emp instanceof ChangeTracker && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() != null && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() instanceof AttributeChangeListener) {
			assertTrue(JpaHelper.getEntityManager(getDAS().getEntityManager()).getUnitOfWork().hasChanges());
		}
		
		assertEquals("Salary in POJO not incremented", initialSalary + 1, emp.getSalary());
		assertEquals(initialVersion, emp.getVersion());

		DataObject empDO2 = getDAS().merge(empDO);

		assertSame(empDO, empDO2);
		assertEquals(initialVersion + 1, empDO2.getLong("version"));
		assertEquals(initialSalary + 1, empDO2.getDouble("salary"));
	}

	@Test
	public void incrementSalary_Remote() {
		int empId = findMinimumEmployeeId();

		DataObject empDO = serialize(getDAS().findEmployee(empId));

		assertNotNull("No Employee DO returned for known employee id", empDO);
		Employee emp = (Employee) getSDOContext().unwrap(empDO);
		assertNotNull("Null POJO in DataObject wrapper", emp);
		

		long initialVersion = empDO.getLong("version");
		double initialSalary = empDO.getDouble("salary");
		
		// Double check values in POJO match
		assertEquals(initialVersion, emp.getVersion());
		assertEquals(initialSalary, emp.getSalary());

		empDO.setDouble("salary", initialSalary + 1);
		assertEquals("Salary in POJO not incremented", initialSalary + 1, emp.getSalary());
		assertEquals(initialVersion, emp.getVersion());

		DataObject empDO2 = serialize(getDAS().merge(serialize(empDO)));

		assertNotSame(empDO, empDO2);
		assertEquals(initialVersion + 1, empDO2.getLong("version"));
		assertEquals(initialSalary + 1, empDO2.getDouble("salary"));
	}

	@Test
	public void testIncrementSalaryWithChangeSummary() {
		int empId = findMinimumEmployeeId();

		DataObject empDO = getDAS().findEmployee(empId);

		assertNotNull(empDO);

		try {
			empDO.getChangeSummary().beginLogging();
		} catch (NullPointerException e) {
			return;
		}
		fail("No NullPOinterException throws accessing ChangeSummary from dataObject");
	}
	
	@Test
	public void testModifyAddress() {
		int empId = findMinimumEmployeeId();
		int maxId = findMaximumEmployeeId();
		DataObject emp1DO = getDAS().findEmployee(empId);
		DataObject emp2DO = getDAS().findEmployee(maxId);
		
		Employee emp = (Employee)getSDOContext().unwrap(emp1DO);
		Employee emp2 = (Employee)getSDOContext().unwrap(emp2DO);
		//Address addrx = emp2.getAddress();
		
		DataObject addrDO = (DataObject)emp2DO.get("address");
		Address addr = (Address)getSDOContext().unwrap(addrDO);
		
		if (emp instanceof ChangeTracker && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() != null && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() instanceof AttributeChangeListener) {
			assertFalse(JpaHelper.getEntityManager(getDAS().getEntityManager()).getUnitOfWork().hasChanges());
		}
		
		emp1DO.set("address", addrDO);
		if (emp instanceof ChangeTracker && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() != null && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() instanceof AttributeChangeListener) {
			assertTrue(JpaHelper.getEntityManager(getDAS().getEntityManager()).getUnitOfWork().getCurrentChanges().getObjectChangeSetForClone(emp).hasChangeFor("address"));
			assertTrue(JpaHelper.getEntityManager(getDAS().getEntityManager()).getUnitOfWork().getCurrentChanges().getObjectChangeSetForClone(emp2).hasChangeFor("address"));
			assertTrue(JpaHelper.getEntityManager(getDAS().getEntityManager()).getUnitOfWork().getCurrentChanges().getObjectChangeSetForClone(addr).hasChangeFor("owner"));
		}
	}
	
	@Test
	public void testModifyPhoneNumbers() {
		int empId = findMinimumEmployeeId();
		int maxId = findMaximumEmployeeId();
		DataObject emp1DO = getDAS().findEmployee(empId);
		DataObject emp2DO = getDAS().findEmployee(maxId);
		
		Employee emp = (Employee)getSDOContext().unwrap(emp1DO);
		Employee emp2 = (Employee)getSDOContext().unwrap(emp2DO);
		
		DataObject phoneDO = (DataObject)emp1DO.getList("phone-number").get(0);
		PhoneNumber phone = (PhoneNumber)getSDOContext().unwrap(phoneDO);
		
		if (emp instanceof ChangeTracker && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() != null && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() instanceof AttributeChangeListener) {
			assertFalse(JpaHelper.getEntityManager(getDAS().getEntityManager()).getUnitOfWork().hasChanges());
		}

		emp2DO.getList("phone-number").add(phoneDO);
		assertFalse(emp.getPhoneNumbers().contains(phone));
		if (emp instanceof ChangeTracker && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() != null && ((ChangeTracker) emp)._persistence_getPropertyChangeListener() instanceof AttributeChangeListener) {
			assertTrue(JpaHelper.getEntityManager(getDAS().getEntityManager()).getUnitOfWork().getCurrentChanges().getObjectChangeSetForClone(emp).hasChangeFor("phoneNumbers"));
			assertTrue(JpaHelper.getEntityManager(getDAS().getEntityManager()).getUnitOfWork().getCurrentChanges().getObjectChangeSetForClone(emp2).hasChangeFor("phoneNumbers"));
			assertTrue(JpaHelper.getEntityManager(getDAS().getEntityManager()).getUnitOfWork().getCurrentChanges().getObjectChangeSetForClone(phone).hasChangeFor("owner"));
		}
	}	

}
