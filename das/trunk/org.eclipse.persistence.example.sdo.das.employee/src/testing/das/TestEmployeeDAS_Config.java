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
 *    dclarke - JPA DAS INCUBATOR - Enhancement 258057
 *              http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing.das;

import static junit.framework.Assert.*;

import java.util.Iterator;

import model.*;
import model.persistence.PersistenceHelper;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.internal.descriptors.PersistenceEntity;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sdo.helper.SDODataFactory;
import org.eclipse.persistence.sdo.helper.SDOXMLHelper;
import org.eclipse.persistence.sdo.helper.jaxb.JAXBHelperContext;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.Test;

import commonj.sdo.Type;
import commonj.sdo.helper.*;
import commonj.sdo.impl.HelperProvider;

/**
 * 
 * @author dclarke EclipseLink 1.1
 */
public class TestEmployeeDAS_Config extends TestEmployeeDAS {

	/**
	 * This test is intended to verify that the DAS properly makes its
	 * JAXBHelperContext the default one.
	 */
	@Test
	public void verifyConfig_JAXBHelperContext() {
		// Note: This call also sets the JAXBHelperContext to be the default so
		// it must be made first
		JAXBHelperContext dasCtx = getDAS().getContext();
		assertNotNull(dasCtx);

		HelperContext sdoCtx = HelperProvider.getDefaultContext();
		assertNotNull(sdoCtx);

		DataFactory dataFactory = sdoCtx.getDataFactory();
		assertTrue(dataFactory instanceof SDODataFactory);
		assertTrue(((SDODataFactory) dataFactory).getHelperContext() instanceof JAXBHelperContext);

		XMLHelper xmlHelper = sdoCtx.getXMLHelper();
		assertTrue(xmlHelper instanceof SDOXMLHelper);
		assertTrue(((SDOXMLHelper) xmlHelper).getHelperContext() instanceof JAXBHelperContext);
	}

	@Test
	public void verifyConfig_JPA() {
		assertNotNull(getEMF());
		Server session = JpaHelper.getServerSession(getEMF());

		assertNotNull(session);

		for (Iterator dI = session.getDescriptors().values().iterator(); dI.hasNext();) {
			ClassDescriptor descriptor = (ClassDescriptor) dI.next();
			Class javaClass = descriptor.getJavaClass();

			if (!descriptor.isAggregateDescriptor()) {
				assertTrue("Entity class not Woven - PersistenceEntity: " + javaClass, PersistenceEntity.class.isAssignableFrom(javaClass));
				assertTrue("Entity class not Woven - ChangeTracker: " + javaClass, ChangeTracker.class.isAssignableFrom(javaClass));
			}
		}
	}

	@Test
	public void verifyConfig_SDOTypes() {
		Type employeeType = getDAS().getContext().getTypeHelper().getType(PersistenceHelper.URI, PersistenceHelper.EMPLOYEE_TYPE);
		assertNotNull(employeeType);
		assertSame(employeeType, getDAS().getContext().getType(Employee.class));

		Type addressType = getDAS().getContext().getTypeHelper().getType(PersistenceHelper.URI, PersistenceHelper.ADDRESS_TYPE);
		assertNotNull(addressType);
		assertSame(addressType, getDAS().getContext().getType(Address.class));

		Type phoneType = getDAS().getContext().getTypeHelper().getType(PersistenceHelper.URI, PersistenceHelper.PHONE_TYPE);
		assertNotNull(phoneType);
		assertSame(phoneType, getDAS().getContext().getType(PhoneNumber.class));
	}

}
