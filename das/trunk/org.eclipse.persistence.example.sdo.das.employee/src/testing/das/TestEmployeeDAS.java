package testing.das;

import static junit.framework.Assert.*;

import java.math.BigInteger;

import model.Employee;
import model.Gender;

import org.eclipse.persistence.internal.descriptors.PersistenceEntity;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;
import org.junit.*;

import service.EmployeeDAS;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.impl.HelperProvider;

/**
 * 
 * @author djclarke
 * 
 */
public class TestEmployeeDAS {

	private EmployeeDAS das;

	@Test
	public void verifyDefaultContext() {
		// Note: This call also sets the JAXBHelperContext to be the default so
		// it must be made first
		JAXBHelperContext dasCtx = getDAS().getContext();
		assertNotNull(dasCtx);

		HelperContext sdoCtx = HelperProvider.getDefaultContext();
		assertNotNull(sdoCtx);

		assertSame(dasCtx, sdoCtx);
	}

	@Test
	public void verifyTypes() {
		Type employeeType = getDAS().getContext().getTypeHelper().getType("http://www.example.org/jpadas-employee", "employee-type");
		assertNotNull(employeeType);

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
		HelperContext sdoCtx = getDAS().getContext();

		Type type = sdoCtx.getTypeHelper().getType("http://www.example.org/jpadas-employee", "employee-type");
		DataObject empDO = sdoCtx.getDataFactory().create(type);

		assertNotNull(empDO);

		empDO.setInt("id", 666);
		assertEquals(666, empDO.getInt("id"));

		Employee emp = (Employee) getDAS().getContext().unwrap(empDO);
		assertNotNull(emp);
		assertEquals(666, emp.getId());

		empDO.setString("first-name", "Delete");
		empDO.setString("last-name", "Me");
		empDO.set("gender", Gender.Male);

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
