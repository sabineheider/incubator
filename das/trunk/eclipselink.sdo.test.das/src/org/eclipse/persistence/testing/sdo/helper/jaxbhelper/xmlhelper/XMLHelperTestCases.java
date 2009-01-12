package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.xmlhelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;
import org.eclipse.persistence.testing.sdo.SDOTestCase;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.XMLDocument;
import commonj.sdo.helper.XSDHelper;

public class XMLHelperTestCases extends SDOTestCase {

    private static final String XML_SCHEMA = "org/eclipse/persistence/testing/sdo/helper/jaxbhelper/xmlhelper/GlobalElement.xsd";
    private static final String XML_INPUT = "org/eclipse/persistence/testing/sdo/helper/jaxbhelper/xmlhelper/GlobalElement.xml";
    private static final String XML_INPUT_UTF16 = "org/eclipse/persistence/testing/sdo/helper/jaxbhelper/xmlhelper/GlobalElement_UTF16.xml";

    private JAXBHelperContext jaxbHelperContext;

    public XMLHelperTestCases(String name) {
        super(name);
    }

    public void setUp() {
        XMLHelperProject project = new XMLHelperProject();
        XMLContext xmlContext = new XMLContext(project);
        JAXBContext jaxbContext = new JAXBContext(xmlContext);
        jaxbHelperContext = new JAXBHelperContext(jaxbContext);
        
        InputStream xsd = Thread.currentThread().getContextClassLoader().getResourceAsStream(XML_SCHEMA);
        jaxbHelperContext.getXSDHelper().define(xsd, null);
    }

    public void testTypes() {
        Type rootTypeType = jaxbHelperContext.getTypeHelper().getType("urn:xml", "root-type");
        assertNotNull(rootTypeType);

        Type rootType = jaxbHelperContext.getTypeHelper().getType("urn:xml", "root");
        assertNull(rootType);
    }

    public void testCreateTypeFromGlobalComplexType() {
        try {
            InputStream xml = Thread.currentThread().getContextClassLoader().getResourceAsStream(XML_INPUT);
            XMLDocument xmlDocument = jaxbHelperContext.getXMLHelper().load(xml);
            assertNotNull(xmlDocument);
            assertNotNull(xmlDocument.getRootObject());
        } catch(IOException e) {
            fail();
        }
    }

    public void testLoadUTF16() {
        try {
            InputStream xml = Thread.currentThread().getContextClassLoader().getResourceAsStream(XML_INPUT_UTF16);
            XMLDocument xmlDocument = jaxbHelperContext.getXMLHelper().load(xml);
            assertEquals("UTF-16LE", xmlDocument.getEncoding());
            assertEquals("1.1", xmlDocument.getXMLVersion());
        } catch(IOException e) {
            fail();
        }
    }

    public void tearDown() {
    }

}
