package org.eclipse.persistence.testing.sdo.helper.jaxbhelper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.persistence.testing.sdo.helper.datahelper.DataHelperToCalendarTest;
import org.eclipse.persistence.testing.sdo.helper.datahelper.DataHelperToDateTest;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.copyhelper.CopyHelperTestCases;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.datafactory.DataFactoryTestCases;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.helpercontext.HelperContextTestCases;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.jaxb.JAXBTestCases;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.mappings.MappingsTestCases;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.oppositeproperty.OppositePropertyTestCases;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.xmlhelper.XMLHelperTestCases;
import org.eclipse.persistence.testing.sdo.helper.jaxbhelper.xsdhelper.XSDHelperTestCases;

public class JAXBHelperTestSuite extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All SDODataHelper Tests");
        suite.addTestSuite(CopyHelperTestCases.class);
        suite.addTestSuite(HelperContextTestCases.class);
        suite.addTestSuite(JAXBTestCases.class);
        suite.addTestSuite(MappingsTestCases.class);
        suite.addTestSuite(OppositePropertyTestCases.class);
        suite.addTestSuite(XMLHelperTestCases.class);
        suite.addTestSuite(XSDHelperTestCases.class);
        suite.addTestSuite(DataFactoryTestCases.class);
        return suite;
    }

}
