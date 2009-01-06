package testing.das;

import java.io.InputStream;

import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.junit.Test;

import commonj.sdo.helper.XSDHelper;

import testing.jpa.EclipseLinkJPATest;

@PersistenceContext(unitName = "employee")
public class ReadDataObjectsFromDB extends EclipseLinkJPATest{

	private static JAXBContext jaxbContext;

	@Test
	public void test() throws Exception {
		
	}

	@BeforeClass
	public static void initializeContext() throws JAXBException {
		jaxbContext = JAXBContext.newInstance("model");
		InputStream xsdIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("xsd/jpadas-employee.xsd");
		XSDHelper.INSTANCE.define(xsdIn, null);
	}
}
