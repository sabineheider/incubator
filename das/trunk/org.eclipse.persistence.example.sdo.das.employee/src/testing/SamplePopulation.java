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
package testing;

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.framework.Assert;
import model.*;

import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.*;

public class SamplePopulation {

	public static SamplePopulation population = new SamplePopulation();

	private Employee[] employees = { basicEmployeeExample1(), basicEmployeeExample2(), basicEmployeeExample3(), basicEmployeeExample4(), basicEmployeeExample5(), basicEmployeeExample6(),
			basicEmployeeExample7(), basicEmployeeExample8(), basicEmployeeExample9(), basicEmployeeExample10(), basicEmployeeExample11(), basicEmployeeExample12() };

	private SamplePopulation() {
		addManagedEmployees(0, new int[] { 2, 3, 4 });
		addManagedEmployees(1, new int[] { 5, 0 });
		addManagedEmployees(2, new int[] {});
		addManagedEmployees(3, new int[] {});
		addManagedEmployees(4, new int[] {});
		addManagedEmployees(5, new int[] {});
		addManagedEmployees(6, new int[] {});
		addManagedEmployees(7, new int[] {});
		addManagedEmployees(8, new int[] {});
		addManagedEmployees(9, new int[] { 11 });
		addManagedEmployees(10, new int[] { 6 });
		addManagedEmployees(11, new int[] { 1 });
	}

	public Employee basicEmployeeExample1() {
		Employee employee = new Employee();

		employee.setFirstName("Bob");
		employee.setLastName("Smith");
		employee.setGender(Gender.Male);
		employee.setSalary(35000);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setEndDate(1996, 0, 1);
		employmentPeriod.setStartDate(1993, 0, 1);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Toronto");
		address.setPostalCode("L5J2B5");
		address.setProvince("ONT");
		address.setStreet("1450 Acme Cr., Suite 4");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.addResponsibility("Water the office plants.");
		employee.addResponsibility("Maintain the kitchen facilities.");
		employee.addPhoneNumber("Work", "6135558812");

		return employee;
	}

	public Employee basicEmployeeExample10() {
		Employee employee = new Employee();

		employee.setFirstName("Jill");
		employee.setLastName("May");
		employee.setGender(Gender.Female);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setStartDate(1991, 10, 11);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Calgary");
		address.setPostalCode("J5J2B5");
		address.setProvince("AB");
		address.setStreet("1111 Mooseland Rd.");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.setSalary(56232);
		employee.addPhoneNumber("Work", "6135558812");
		employee.addPhoneNumber("Work Fax", "6135555943");

		return employee;
	}

	public Employee basicEmployeeExample11() {
		Employee employee = new Employee();

		employee.setFirstName("Sarah-Lou");
		employee.setLastName("Smitty");
		employee.setGender(Gender.Female);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setEndDate(1996, 0, 1);
		employmentPeriod.setStartDate(1993, 0, 1);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Arnprior");
		address.setPostalCode("W1A2B5");
		address.setProvince("ONT");
		address.setStreet("1 Hawthorne Drive");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.setSalary(75000);
		employee.addPhoneNumber("Work Fax", "6135555943");
		employee.addPhoneNumber("Home", "6135551234");
		employee.addPhoneNumber("Cellular", "4165551111");

		return employee;
	}

	public Employee basicEmployeeExample12() {
		Employee employee = new Employee();

		employee.setFirstName("Jim-Bob");
		employee.setLastName("Jefferson");
		employee.setGender(Gender.Male);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setEndDate(2001, 11, 31);
		employmentPeriod.setStartDate(1995, 0, 12);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Yellowknife");
		address.setPostalCode("Y5J2N5");
		address.setProvince("YK");
		address.setStreet("1112 Gold Rush Rd.");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.setSalary(50000);
		employee.addPhoneNumber("Home", "6135551234");
		employee.addPhoneNumber("Cellular", "4165551111");

		return employee;
	}

	public Employee basicEmployeeExample2() {
		Employee employee = new Employee();

		employee.setFirstName("John");
		employee.setLastName("Way");
		employee.setGender(Gender.Male);
		employee.setSalary(53000);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setStartDate(1991, 10, 11);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Ottawa");
		address.setPostalCode("K5J2B5");
		address.setProvince("ONT");
		address.setStreet("12 Merivale Rd., Suite 5");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.addResponsibility("Hire people when more people are required.");
		employee.addResponsibility("Lay off employees when less people are required.");
		employee.addPhoneNumber("Work", "6135558812");
		employee.addPhoneNumber("ISDN", "9055553691");

		return employee;
	}

	public Employee basicEmployeeExample3() {
		Employee employee = new Employee();

		employee.setFirstName("Charles");
		employee.setLastName("Chanley");
		employee.setGender(Gender.Male);
		employee.setSalary(43000);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setEndDate(2001, 11, 31);
		employmentPeriod.setStartDate(1995, 0, 1);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Montreal");
		address.setPostalCode("Q2S5Z5");
		address.setProvince("QUE");
		address.setStreet("1 Canadien Place");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.addResponsibility("Perform code reviews as required.");
		employee.addPhoneNumber("Pager", "9765556666");
		employee.addPhoneNumber("ISDN", "9055553691");

		return employee;
	}

	public Employee basicEmployeeExample4() {
		Employee employee = new Employee();

		employee.setFirstName("Emanual");
		employee.setLastName("Smith");
		employee.setGender(Gender.Male);
		employee.setSalary(49631);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setEndDate(2001, 11, 31);
		employmentPeriod.setStartDate(1995, 0, 1);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Vancouver");
		address.setPostalCode("N5J2N5");
		address.setProvince("BC");
		address.setStreet("20 Mountain Blvd., Floor 53, Suite 6");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.addResponsibility("Have to fix the Database problem.");
		employee.addPhoneNumber("Work Fax", "6135555943");
		employee.addPhoneNumber("Cellular", "4165551111");
		employee.addPhoneNumber("Pager", "9765556666");
		employee.addPhoneNumber("ISDN", "9055553691");

		return employee;
	}

	public Employee basicEmployeeExample5() {
		Employee employee = new Employee();

		employee.setFirstName("Sarah");
		employee.setLastName("Way");
		employee.setGender(Gender.Female);
		employee.setSalary(87000);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setEndDate(2001, 6, 31);
		employmentPeriod.setStartDate(1995, 4, 1);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Prince Rupert");
		address.setPostalCode("K3K5D5");
		address.setProvince("BC");
		address.setStreet("3254 Parkway Place");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.addResponsibility("Write code documentation.");
		employee.addPhoneNumber("Work", "6135558812");
		employee.addPhoneNumber("ISDN", "9055553691");
		employee.addPhoneNumber("Home", "6135551234");

		return employee;
	}

	public Employee basicEmployeeExample6() {
		Employee employee = new Employee();

		employee.setFirstName("Marcus");
		employee.setLastName("Saunders");
		employee.setGender(Gender.Male);
		employee.setSalary(54300);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setEndDate(2001, 11, 31);
		employmentPeriod.setStartDate(1995, 0, 12);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Perth");
		address.setPostalCode("Y3Q2N9");
		address.setProvince("ONT");
		address.setStreet("234 Caledonia Lane");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.addResponsibility("Write user specifications.");
		employee.addPhoneNumber("ISDN", "9055553691");
		employee.addPhoneNumber("Work", "6135558812");

		return employee;
	}

	public Employee basicEmployeeExample7() {
		Employee employee = new Employee();

		employee.setFirstName("Nancy");
		employee.setLastName("White");
		employee.setGender(Gender.Female);
		employee.setSalary(31000);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setEndDate(1996, 0, 1);
		employmentPeriod.setStartDate(1993, 0, 1);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Metcalfe");
		address.setPostalCode("Y4F7V6");
		address.setProvince("ONT");
		address.setStreet("2 Anderson Rd.");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.addPhoneNumber("Home", "6135551234");

		return employee;
	}

	public Employee basicEmployeeExample8() {
		Employee employee = new Employee();

		employee.setFirstName("Fred");
		employee.setLastName("Jones");
		employee.setGender(Gender.Male);
		employee.setSalary(500000);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setEndDate(2001, 11, 31);
		employmentPeriod.setStartDate(1995, 0, 1);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Victoria");
		address.setPostalCode("Z5J2N5");
		address.setProvince("BC");
		address.setStreet("382 Hyde Park Blvd.");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.addPhoneNumber("Cellular", "4165551111");
		employee.addPhoneNumber("ISDN", "9055553691");

		return employee;
	}

	public Employee basicEmployeeExample9() {
		Employee employee = new Employee();

		employee.setFirstName("Betty");
		employee.setLastName("Jones");
		employee.setGender(Gender.Female);
		employee.setSalary(500001);

		EmploymentPeriod employmentPeriod = new EmploymentPeriod();
		employmentPeriod.setStartDate(2001, 11, 31);
		employmentPeriod.setEndDate(1995, 0, 1);
		employee.setPeriod(employmentPeriod);

		Address address = new Address();
		address.setCity("Smith Falls");
		address.setPostalCode("C6C6C6");
		address.setProvince("ONT");
		address.setStreet("89 Chocolate Drive");
		address.setCountry("Canada");
		employee.setAddress(address);

		employee.addPhoneNumber("Work", "6135558812");
		employee.addPhoneNumber("ISDN", "9055553691");

		return employee;
	}

	private void addManagedEmployees(int managerIndex, int[] employeeIndeces) {
		Employee manager = this.employees[managerIndex];

		if (manager.getManagedEmployees().isEmpty()) {
			for (int index = 0; index < employeeIndeces.length; index++) {
				manager.addManagedEmployee(this.employees[employeeIndeces[index]]);
			}
		}
	}

	/**
	 * Register all of the population in the provided EntityManager to be
	 * persisted This method should only be called from within a test case. It
	 * asserts that the provided EntityManager is in a transaction and that the
	 * database tables are empty.
	 */
	public void persistAll(EntityManager em) {
		Assert.assertTrue("EntityManager not in Transaction", em.getTransaction().isActive());

		// Verify that the database tables are empty
		assertCount(em, Employee.class, 0);
		assertCount(em, Address.class, 0);
		assertCount(em, PhoneNumber.class, 0);

		for (int index = 0; index < this.employees.length; index++) {
			em.persist(this.employees[index]);
		}

		em.flush();
		verifyCounts(em);
	}

	public void verifyCounts(EntityManager em) {
		assertCount(em, Employee.class, this.employees.length);
		assertCount(em, Address.class, this.employees.length);
	}

	/**
	 * Verify that the provided entity type has no rows in the database using a
	 * native ReportQuery.
	 * 
	 * @param entityClass
	 * @param count
	 */
	public void assertCount(EntityManager em, Class entityClass, int count) {
		ReportQuery query = new ReportQuery(entityClass, new ExpressionBuilder());
		query.addCount();
		query.setShouldReturnSingleValue(true);

		int dbCount = ((Number) JpaHelper.getEntityManager(em).getUnitOfWork().executeQuery(query)).intValue();
		Assert.assertEquals("Incorrect quantity found of " + entityClass, count, dbCount);
	}

	/**
	 * Verify that the provided list of Employee instances matches the sample
	 * population.
	 * 
	 * @param employees
	 */
	public void assertSame(List<Employee> dbEmps) {
		Assert.assertEquals("Incorrect quantity of employees", this.employees.length, dbEmps.size());

		Collections.sort(dbEmps, new EmployeeComparator());

		for (int index = 0; index < this.employees.length; index++) {
			Employee emp = employees[index];
			Employee dbEmp = dbEmps.get(index);

			Assert.assertEquals("First name does not match on employees[" + index + "]", emp.getFirstName(), dbEmp.getFirstName());
			Assert.assertEquals("Last name does not match on employees[" + index + "]", emp.getLastName(), dbEmp.getLastName());
			Assert.assertEquals("Salary does not match on employees[" + index + "]", emp.getSalary(), dbEmp.getSalary());
		}
	}

	/**
	 * Simple comparator used to order the employees for use within assertSame
	 */
	class EmployeeComparator implements Comparator<Employee> {

		public int compare(Employee emp1, Employee emp2) {
			return emp1.getId() - emp2.getId();
		}

	}

	/**
	 * Extract the id's from the sample Employee instances.
	 * 
	 * @param em
	 * @return
	 */
	public int[] getEmployeeIds(EntityManager em) {
		int[] ids = new int[this.employees.length];

		for (int index = 0; index < this.employees.length; index++) {
			if (this.employees[index].getId() <= 0) {
				Employee emp = queryByExample(em, this.employees[index]);

				if (emp == null) {
					throw new RuntimeException("Could not find Employee: " + this.employees[index]);
				}
				this.employees[index].setId(emp.getId());
			}
			ids[index] = this.employees[index].getId();
		}

		return ids;
	}

	/**
	 * Reset the database so that only the sample population exists.
	 * 
	 * @param em
	 */
	public void resetDatabase(EntityManager em) {
		em.getTransaction().begin();

		DeleteAllQuery deleteEmpsQuery = new DeleteAllQuery(Employee.class);
		ExpressionBuilder eb = deleteEmpsQuery.getExpressionBuilder();
		deleteEmpsQuery.setSelectionCriteria(eb.get("id").notIn(getEmployeeIds(em)));
		deleteEmpsQuery.setFlushOnExecute(true);

		JpaHelper.getEntityManager(em).getUnitOfWork().executeQuery(deleteEmpsQuery);

		em.getTransaction().commit();
	}

	/**
	 * Example of EclipseLink's native query-by-example support.
	 * 
	 * @param em
	 * @param sampleEmployee
	 * @return
	 */
	public Employee queryByExample(EntityManager em, Employee sampleEmployee) {
		QueryByExamplePolicy policy = new QueryByExamplePolicy();
		policy.excludeDefaultPrimitiveValues();
		ReadObjectQuery roq = new ReadObjectQuery(sampleEmployee, policy);
		// Wrap the native query in a JPA Query and execute it.
		Query query = new EJBQueryImpl(roq, (EntityManagerImpl) JpaHelper.getEntityManager(em));
		return (Employee) query.getSingleResult();
	}
	
	public static void compare(Employee emp1, Employee emp2) {
		
		return;
	}

}
