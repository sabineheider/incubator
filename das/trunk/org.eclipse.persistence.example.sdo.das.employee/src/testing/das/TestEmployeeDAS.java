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

import java.util.List;

import javax.persistence.EntityManager;

import model.*;

import org.eclipse.persistence.internal.descriptors.PersistenceEntity;
import org.eclipse.persistence.sdo.helper.*;
import org.eclipse.persistence.sdo.helper.jaxb.*;
import org.junit.*;

import service.EmployeeDAS;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.*;
import commonj.sdo.impl.HelperProvider;

/**
 * 
 * @author dclarke EclipseLink 1.1
 */
public class TestEmployeeDAS {

	private EmployeeDAS das;

	/**
	 * This test is intended to verify that the DAS properly makes its
	 * JAXBHelperContext the default one.
	 */
	@Test
	public void verifyDefaultContext() {
		// Note: This call also sets the JAXBHelperContext to be the default so
		// it must be made first
		JAXBHelperContext dasCtx = getDAS().getContext();
		assertNotNull(dasCtx);

		HelperContext sdoCtx = HelperProvider.getDefaultContext();
		assertNotNull(sdoCtx);

		DataFactory dataFactory = sdoCtx.getDataFactory();
		assertTrue(dataFactory instanceof SDODataFactory);
		assertTrue(((SDODataFactory) dataFactory).getHelperContext() instanceof JAXBHelperContext);

		XMLHelper xmlHelper = sdoCtx.getXMLHelper();
		assertTrue(xmlHelper instanceof SDOXMLHelper);
		assertTrue(((SDOXMLHelper) xmlHelper).getHelperContext() instanceof JAXBHelperContext);
	}

	@Test
	public void verifyTypes() {
		Type employeeType = getDAS().getContext().getTypeHelper().getType("http://www.example.org/jpadas-employee", "employee-type");
		assertNotNull(employeeType);
		assertSame(employeeType, getDAS().getContext().getType(Employee.class));

		Type addressType = getDAS().getContext().getTypeHelper().getType("http://www.example.org/jpadas-employee", "address-type");
		assertNotNull(addressType);
		assertSame(addressType, getDAS().getContext().getType(Address.class));

		Type phoneType = getDAS().getContext().getTypeHelper().getType("http://www.example.org/jpadas-employee", "phone-type");
		assertNotNull(phoneType);
		assertSame(phoneType, getDAS().getContext().getType(PhoneNumber.class));
	}

	@Test
	public void testFind() {
		int empId = getDAS().findMinimumEmployeeId();

		DataObject empDO = getDAS().findEmployee(empId);

		assertNotNull(empDO);
		assertEquals(empId, empDO.getInt("id"));

		Employee emp = (Employee) getDAS().getContext().unwrap(empDO);
		assertNotNull(emp);

		assertTrue(emp instanceof PersistenceEntity);
	}

	@Test
	public void testIncrementSalary() {
		int empId = getDAS().findMinimumEmployeeId();

		DataObject empDO = getDAS().findEmployee(empId);

		assertNotNull(empDO);

		long initialVersion = empDO.getLong("version");
		int initialSalary = empDO.getInt("salary");

		empDO.setInt("salary", initialSalary + 1);

		DataObject empDO2 = getDAS().merge(empDO);

		assertNotSame(empDO, empDO2);
		assertEquals(initialVersion + 1, empDO2.getLong("version"));
		assertEquals(initialSalary + 1, empDO2.getInt("salary"));
	}

	@Test
	public void testIncrementSalaryWithChangeSummary() {
		int empId = getDAS().findMinimumEmployeeId();

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
	public void testCreateNewEmployee() {
		Type type = getDAS().getContext().getType(Employee.class);
		DataObject empDO = getDAS().getContext().getDataFactory().create(type);

		assertNotNull(empDO);

		empDO.setInt("id", 666);
		assertEquals(666, empDO.getInt("id"));

		Employee emp = (Employee) getDAS().getContext().unwrap(empDO);
		assertNotNull(emp);
		assertEquals(666, emp.getId());

		empDO.setString("first-name", "Delete");
		empDO.setString("last-name", "Me");
		empDO.setString("gender", Gender.Male.name());

		assertEquals("Delete", emp.getFirstName());
		assertEquals("Me", emp.getLastName());
		assertEquals(Gender.Male, emp.getGender());

		getDAS().merge(empDO);
	}

	public EmployeeDAS getDAS() {
		return this.das;
	}

	@Before
	public void initializeDAS() {
		this.das = new EmployeeDAS();
	}

	@After
	public void shutdown() {
		EntityManager em = getDAS().getEMF().createEntityManager();
		em.getTransaction().begin();
		List<Employee> deleteEmps = em.createQuery("SELECT e FROM Employee e WHERE e.firstName = 'Delete' and e.lastName = 'Me'").getResultList();
		for (Employee emp : deleteEmps) {
			em.remove(emp);
		}
		em.getTransaction().commit();

		this.das.close();
	}
}
