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
 *     dclarke - JPA DAS INCUBATOR - Enhancement 258057
 *     			 http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing.jpa;

import static org.junit.Assert.*;
import model.*;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.Test;

/**
 * Set of tests to ensure the mappings are properly populated from the provided
 * annotations/xml.
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class MappingConfigTests extends EclipseLinkJPATest {

	@Test
	public void verifyServerSession() {
		Server session = JpaHelper.getServerSession(getEMF());

		assertNotNull(session);
		assertTrue(session.isConnected());
		assertTrue(session.isServerSession());
		assertEquals(1, session.getReadConnectionPool().getMinNumberOfConnections());
		assertEquals(1, session.getDefaultConnectionPool().getMinNumberOfConnections());

		assertTrue(session.getName().startsWith("file"));
		assertTrue(session.getName().endsWith("employee"));
	}

	@Test
	public void verifyEmployeeDescriptor() {
		Server session = JpaHelper.getServerSession(getEMF());
		ClassDescriptor descriptor = session.getClassDescriptor(Employee.class);

		assertNotNull(descriptor);
		assertEquals("Employee", descriptor.getAlias());
		assertNull(descriptor.getInheritancePolicyOrNull());

		// Address Mapping
		OneToOneMapping addrMapping = (OneToOneMapping) descriptor.getMappingForAttributeName("address");
		assertNotNull(addrMapping);
		assertTrue(addrMapping.isPrivateOwned());

		// PhoenNumber Mapping
		OneToManyMapping phoneMapping = (OneToManyMapping) descriptor.getMappingForAttributeName("phoneNumbers");
		assertNotNull(phoneMapping);
		assertTrue(phoneMapping.isPrivateOwned());
	}

	@Test
	public void verifyAddressDescriptor() {
		Server session = JpaHelper.getServerSession(getEMF());
		ClassDescriptor descriptor = session.getClassDescriptor(Address.class);

		assertNotNull(descriptor);
		assertEquals("Address", descriptor.getAlias());
		assertNull(descriptor.getInheritancePolicyOrNull());
	}

	@Test
	public void verifyPhoneNumberDescriptor() {
		Server session = JpaHelper.getServerSession(getEMF());
		ClassDescriptor descriptor = session.getClassDescriptor(PhoneNumber.class);

		assertNotNull(descriptor);
		assertEquals("PhoneNumber", descriptor.getAlias());
		assertNull(descriptor.getInheritancePolicyOrNull());
	}
	
}
