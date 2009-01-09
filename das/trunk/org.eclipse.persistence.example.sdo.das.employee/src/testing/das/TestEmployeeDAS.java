package testing.das;

import static junit.framework.Assert.*;
import model.Employee;
import model.Gender;

import org.eclipse.persistence.internal.descriptors.PersistenceEntity;
import org.junit.*;

import service.EmployeeDAS;

import commonj.sdo.DataObject;

/**
 * 
 * @author djclarke
 * 
 */
public class TestEmployeeDAS {

	private EmployeeDAS das;

	@Test
	public void testFind() {
		int empId = getDAS().findMinimumEmployeeId();

		DataObject empDO = getDAS().findEmployee(empId);

		assertNotNull(empDO);
		assertEquals(empId, empDO.getInt("id"));

		Employee emp = (Employee) getDAS().unWrap(empDO);
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
		DataObject empDO = getDAS().create(666);
		
		assertNotNull(empDO);
		assertEquals(666, empDO.getInt("id"));
		
		Employee emp = (Employee) getDAS().unWrap(empDO);
		assertNotNull(emp);
		assertEquals(666, emp.getId());
		
		empDO.setString("first-name", "Delete");
		empDO.setString("last-name", "Me");
		empDO.setString("gender", Gender.Male.name());
		
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
		this.das.close();
	}
}
