/*******************************************************************************
 * Copyright (c) 1998, 2008 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    bdoughan - JPA DAS INCUBATOR - Enhancement 258057
 *               http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
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
