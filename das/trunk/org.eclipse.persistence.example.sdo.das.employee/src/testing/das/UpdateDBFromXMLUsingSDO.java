package testing.das;

import static junit.framework.Assert.assertTrue;

import java.io.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.*;

import model.Employee;

import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.*;

import testing.jpa.EclipseLinkJPATest;
import testing.jpa.SamplePopulation;

import commonj.sdo.DataObject;
import commonj.sdo.helper.*;

@PersistenceContext(unitName = "employee")
public class UpdateDBFromXMLUsingSDO extends EclipseLinkJPATest {

	private static JAXBContext jaxbContext;

	@Test
	public void incrementSalary() throws Exception {
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		EntityManager em = getEntityManager();
		new SchemaManager(JpaHelper.getEntityManager(em).getServerSession()).replaceSequences();

		File sampleFile = new File("./data/samples/employee-1.xml");

		assertTrue(sampleFile.exists());
		assertTrue(sampleFile.isFile());

		em.getTransaction().begin();

		try {

			FileInputStream xmlInputStream = new FileInputStream(sampleFile);
			XMLDocument xmlDocument = XMLHelper.INSTANCE.load(xmlInputStream);
			xmlInputStream.close();

			DataObject employeeDO = xmlDocument.getRootObject();

			// Modify the DataObject model with change logging on
			employeeDO.getChangeSummary().beginLogging();
			employeeDO.set("salary", ((Number) employeeDO.get("salary")).doubleValue() + 1);

			assertTrue(employeeDO.getChangeSummary().isModified(employeeDO));

			StringWriter writer = new StringWriter();
			XMLHelper.INSTANCE.save(xmlDocument, writer, null);

			System.out.println("READ> + " + sampleFile + "\n" + writer);

			Employee emp = (Employee) unmarshaller.unmarshal(new StringReader(writer.toString()));

			em.merge(emp);

			em.getTransaction().commit();

			SamplePopulation.population.verifyCounts(em);

		} finally {
			if (em != null && em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
		}
	}

	@BeforeClass
	public static void initializeContext() throws JAXBException {
		jaxbContext = JAXBContext.newInstance("model");
		InputStream xsdIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("xsd/jpadas-employee.xsd");
		XSDHelper.INSTANCE.define(xsdIn, null);
	}
	
	@After
	public void resetDatabase() throws Exception {
		JpaHelper.getServerSession(getEMF()).executeNonSelectingSQL("UPDATE JPADAS_SALARY SET SALARY = 35000");
		JpaHelper.getServerSession(getEMF()).executeNonSelectingSQL("UPDATE JPADAS_EMPLOYEE SET VERSION = 1");
	}
}
