package testing.das;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.eclipse.persistence.config.PersistenceUnitProperties.DDL_GENERATION;
import static org.eclipse.persistence.config.PersistenceUnitProperties.DROP_AND_CREATE;

import java.io.*;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.*;

import model.Employee;

import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.BeforeClass;
import org.junit.Test;

import testing.jpa.EclipseLinkJPATest;
import testing.jpa.SamplePopulation;

import commonj.sdo.helper.*;

@PersistenceContext(unitName = "employee")
public class PopulateDatabaseFromXML extends EclipseLinkJPATest {

	private static JAXBContext jaxbContext;

	@Test
	public void test() throws Exception {
		EntityManager em = getEntityManager();
		new SchemaManager(JpaHelper.getEntityManager(em).getServerSession()).replaceSequences();

		File sampleFolder = new File("./data/samples");

		assertTrue(sampleFolder.exists());
		assertTrue(sampleFolder.isDirectory());

		File[] sampleFiles = sampleFolder.listFiles();
		assertEquals(13, sampleFiles.length);

		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		em.getTransaction().begin();

		try {

			for (int index = 0; index < sampleFiles.length; index++) {
				if (sampleFiles[index].getName().endsWith(".xml")) {
					FileInputStream xmlInputStream = new FileInputStream(sampleFiles[index]);
					XMLDocument xmlDocument = XMLHelper.INSTANCE.load(xmlInputStream);
					// DataObject employeeDO = xmlDocument.getRootObject();
					xmlInputStream.close();

					StringWriter writer = new StringWriter();
					XMLHelper.INSTANCE.save(xmlDocument, writer, null);

					System.out.println("READ> + " + sampleFiles[index] + "\n" + writer);

					Employee emp = (Employee) unmarshaller.unmarshal(new StringReader(writer.toString()));

					em.persist(emp);
				}
			}

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

	@Override
	protected Map getEMFProperties() {
		Map properties = super.getEMFProperties();
		properties.put(DDL_GENERATION, DROP_AND_CREATE);
		return properties;
	}

}
