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
 * 		dclarke - initial JPA Employee example using XML (bug 217884)
 ******************************************************************************/
package org.eclipse.persistence.testing.tests.dynamic.employee;

import static org.junit.Assert.*;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.testing.models.dynamic.employee.EmployeeDynamicMappings;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.junit.Test;

/**
 * Set of tests to ensure the mappings are properly populated from the provided
 * annotations/xml.
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class MappingConfigTests extends EclipseLinkORMTest {

    @Test
    public void verifySharedSession() throws Exception {

        assertNotNull(getSharedSession());
        assertTrue(getSharedSession().isConnected());
        assertTrue(getSharedSession().isServerSession());

        assertTrue(getSharedSession().getName().startsWith("file"));
        assertTrue(getSharedSession().getName().endsWith("empty"));
    }

    @Test
    public void verifyEmployeeDescriptor() throws Exception {
        Session session = getSession();
        ClassDescriptor descriptor = session.getDescriptorForAlias("Employee");

        assertNotNull(descriptor);
        assertEquals("Employee", descriptor.getAlias());
        assertNull(descriptor.getInheritancePolicyOrNull());

        // Address Mapping
        OneToOneMapping addrMapping = (OneToOneMapping) descriptor.getMappingForAttributeName("address");
        assertNotNull(addrMapping);
        assertTrue(addrMapping.isPrivateOwned());
        assertSame(session.getDescriptorForAlias("Address"), addrMapping.getReferenceDescriptor());

        // PhoenNumber Mapping
        OneToManyMapping phoneMapping = (OneToManyMapping) descriptor.getMappingForAttributeName("phoneNumbers");
        assertNotNull(phoneMapping);
        assertTrue(phoneMapping.isPrivateOwned());
        assertSame(session.getDescriptorForAlias("PhoneNumber"), phoneMapping.getReferenceDescriptor());

        // Manager Mapping
        OneToOneMapping managerMapping = (OneToOneMapping) descriptor.getMappingForAttributeName("manager");
        assertNotNull(managerMapping);
        assertFalse(managerMapping.isPrivateOwned());
        assertSame(descriptor, managerMapping.getReferenceDescriptor());

        // Managed Employees Mapping
        OneToManyMapping managedEmployeesMapping = (OneToManyMapping) descriptor.getMappingForAttributeName("managedEmployees");
        assertNotNull(managedEmployeesMapping);
        assertFalse(managedEmployeesMapping.isPrivateOwned());
        assertSame(descriptor, managedEmployeesMapping.getReferenceDescriptor());

        // Projects Mapping
        ManyToManyMapping projectsMapping = (ManyToManyMapping) descriptor.getMappingForAttributeName("projects");
        assertNotNull(projectsMapping);
        assertFalse(projectsMapping.isPrivateOwned());
        assertSame(session.getDescriptorForAlias("Project"), projectsMapping.getReferenceDescriptor());
    }

    @Test
    public void verifyAddressDescriptor() throws Exception {
        Session session = getSession();
        ClassDescriptor descriptor = session.getDescriptorForAlias("Address");

        assertNotNull(descriptor);
        assertEquals("Address", descriptor.getAlias());
        assertNull(descriptor.getInheritancePolicyOrNull());
    }

    @Test
    public void verifyPhoneNumberDescriptor() {
        Session session = getSession();
        ClassDescriptor descriptor = session.getDescriptorForAlias("PhoneNumber");

        assertNotNull(descriptor);
        assertEquals("PhoneNumber", descriptor.getAlias());
        assertNull(descriptor.getInheritancePolicyOrNull());
    }

    @Test
    public void verifyProjectDescriptor() {
        Session session = getSession();
        ClassDescriptor descriptor = session.getDescriptorForAlias("Project");

        assertNotNull(descriptor);
        assertEquals("Project", descriptor.getAlias());
        assertNotNull(descriptor.getInheritancePolicyOrNull());
    }

    @Test
    public void verifySmallProjectDescriptor() {
        Session session = getSession();
        ClassDescriptor descriptor = session.getDescriptorForAlias("SmallProject");

        assertNotNull(descriptor);
        assertEquals("SmallProject", descriptor.getAlias());
        assertNotNull(descriptor.getInheritancePolicyOrNull());
    }

    @Test
    public void verifyLargeProjectDescriptor() {
        Session session = getSession();
        ClassDescriptor descriptor = session.getDescriptorForAlias("LargeProject");

        assertNotNull(descriptor);
        assertEquals("LargeProject", descriptor.getAlias());
        assertNotNull(descriptor.getInheritancePolicyOrNull());
    }

    @Override
    protected DatabaseSession createSharedSession() {
        DatabaseSession shared = super.createSharedSession();

        if (shared.getDescriptors().isEmpty()) {
            EmployeeDynamicMappings.createTypes(shared, "model.dynamic.employee", false);
        }

        return shared;
    }

}
