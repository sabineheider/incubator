package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.copyhelper;

import java.io.InputStream;
import java.util.List;

import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;
import org.eclipse.persistence.testing.sdo.SDOTestCase;

import commonj.sdo.DataObject;

public class CopyHelperTestCases extends SDOTestCase {

    private static final String XML_SCHEMA = "org/eclipse/persistence/testing/sdo/helper/jaxbhelper/copyhelper/CopyHelper.xsd";
    
    private JAXBHelperContext jaxbHelperContext;
    
    public CopyHelperTestCases(String name) {
        super(name);
    }
    
    public void setUp() {
        CopyHelperProject project = new CopyHelperProject();
        XMLContext xmlContext = new XMLContext(project);
        JAXBContext jaxbContext = new JAXBContext(xmlContext);
        jaxbHelperContext = new JAXBHelperContext(jaxbContext);
        
        InputStream xsd = Thread.currentThread().getContextClassLoader().getResourceAsStream(XML_SCHEMA);
        jaxbHelperContext.getXSDHelper().define(xsd, null);
    }
    
    public void testCopy() {
        DataObject rootDO = jaxbHelperContext.getDataFactory().create("urn:copy", "root");
        DataObject child1DO = rootDO.createDataObject("child1");
        DataObject child2DO = rootDO.createDataObject("child2");

        DataObject rootDOCopy = jaxbHelperContext.getCopyHelper().copy(rootDO);
        
        Root root = (Root) jaxbHelperContext.unwrap(rootDO);
        Root rootCopy = (Root) jaxbHelperContext.unwrap(rootDOCopy);
        
        assertTrue(jaxbHelperContext.getEqualityHelper().equal(rootDO, rootDOCopy));
        
        assertNotSame(root, rootCopy);
        assertNotNull(rootCopy);

        assertNotSame(root.getChild1(), rootCopy.getChild1());
        assertNotNull(rootCopy.getChild1());
        
        assertNotSame(root.getChild2(), rootCopy.getChild2());
        assertNotNull(rootCopy.getChild2());
    }

    public void tearDown() {
    }

}
