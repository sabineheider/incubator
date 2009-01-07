package testing.sdo;

import java.io.*;

import org.junit.Test;

import commonj.sdo.DataObject;
import commonj.sdo.helper.*;

public class DynamicSDO_NewEmployee {

	@Test
	public void readSamples() throws Exception {
		InputStream xsdIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("xsd/jpadas-employee.xsd");
		XSDHelper.INSTANCE.define(xsdIn, null);

		File sampleFolder = new File("./data/samples");
		File[] sampleFiles = sampleFolder.listFiles();

		for (int index = 0; index < sampleFiles.length; index++) {
			if (sampleFiles[index].isFile()) {
				System.out.println("READING> " + sampleFiles[index].getName());
				FileInputStream xmlInputStream = new FileInputStream(sampleFiles[index]);
				XMLDocument xmlDocument = XMLHelper.INSTANCE.load(xmlInputStream);
				DataObject employeeDO = xmlDocument.getRootObject();
				System.out.println("\t> DataObject: " + employeeDO);
				xmlInputStream.close();
			}
		}
	}
}
