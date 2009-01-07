package testing.das;

import org.junit.*;

import service.EmployeeDAS;

import commonj.sdo.DataObject;

public class TestEmployeeDAS {

	private EmployeeDAS das;

	@Test
	public void testFind() {
		int empId = getDAS().findMinimumEmployeeId();

		DataObject empDO = getDAS().findEmployee(empId);

		Assert.assertNotNull(empDO);
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
