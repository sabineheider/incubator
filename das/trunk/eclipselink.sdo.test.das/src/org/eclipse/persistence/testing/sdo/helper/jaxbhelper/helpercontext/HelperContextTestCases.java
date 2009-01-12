package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.helpercontext;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.sdo.helper.SDODataFactory;
import org.eclipse.persistence.sdo.helper.SDOXMLHelper;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;
import org.eclipse.persistence.testing.sdo.SDOTestCase;

import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.XMLHelper;
import commonj.sdo.helper.XSDHelper;

public class HelperContextTestCases extends SDOTestCase {
    
    private JAXBHelperContext jaxbHelperContext;
    
    public HelperContextTestCases(String name) {
        super(name);
    }
    
    public void setUp() {
        try {
            Class[] classes = new Class[1];
            classes[0] = Root.class;
            JAXBContext jaxbContext = JAXBContext.newInstance(classes);
            jaxbHelperContext = new JAXBHelperContext(jaxbContext);
            jaxbHelperContext.makeDefaultContext();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void testDataFactory() {
        SDODataFactory sdoDataFactory = (SDODataFactory) DataFactory.INSTANCE;
        System.out.println(sdoDataFactory.getHelperContext());
    }

    public void testXMLHelper() {
        SDOXMLHelper sdoXMLHelper = (SDOXMLHelper) XMLHelper.INSTANCE;
        System.out.println(sdoXMLHelper.getHelperContext());
    }
    
    public void tearDown() {
    }

    private class JAXBSchemaOutputResolver extends SchemaOutputResolver {

        private StringWriter schemaWriter;
        
        public String getSchema() {
            return schemaWriter.toString();
        }

        public Result createOutput(String arg0, String arg1) throws IOException {
            schemaWriter = new StringWriter();
            return new StreamResult(schemaWriter);
        }
        
    }
    
}
