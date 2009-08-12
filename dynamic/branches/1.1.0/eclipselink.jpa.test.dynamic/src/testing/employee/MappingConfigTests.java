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
package testing.employee;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.junit.Test;

import example.employee.EntityTypeFactory;

import testing.util.EclipseLinkJPATest;

/**
 * Set of tests to ensure the mappings are properly populated from the provided
 * annotations/xml.
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
@PersistenceContext(unitName="custom-types")
public class MappingConfigTests extends EclipseLinkJPATest {

    @Test
    public void verifyServerSession() throws Exception {
        Server session = JpaHelper.getServerSession(getEMF());

        assertNotNull(session);
        assertTrue(session.isConnected());
        assertTrue(session.isServerSession());
        assertEquals(1, session.getReadConnectionPool().getMinNumberOfConnections());
        assertEquals(1, session.getDefaultConnectionPool().getMinNumberOfConnections());

        assertTrue(session.getName().startsWith("file"));
        assertTrue(session.getName().endsWith("custom-types"));
    }

    @Test
    public void verifyEmployeeDescriptor() throws Exception {
        Server session = JpaHelper.getServerSession(getEMF());
        ClassDescriptor descriptor = session.getDescriptorForAlias("Employee");

        assertNotNull(descriptor);
        assertEquals("Employee", descriptor.getAlias());
        assertNull(descriptor.getInheritancePolicyOrNull());

        // Address Mapping
        OneToOneMapping addrMapping = (OneToOneMapping) descriptor.getMappingForAttributeName("address");
        assertNotNull(addrMapping);
        assertTrue(addrMapping.isPrivateOwned());

        // PhoenNumber Mapping
        //OneToManyMapping phoneMapping = (OneToManyMapping) descriptor.getMappingForAttributeName("phoneNumbers");
        //assertNotNull(phoneMapping);
        //assertTrue(phoneMapping.isPrivateOwned());
    }

    @Test
    public void verifyAddressDescriptor() throws Exception {
        Server session = JpaHelper.getServerSession(getEMF());
        ClassDescriptor descriptor = session.getDescriptorForAlias("Address");

        assertNotNull(descriptor);
        assertEquals("Address", descriptor.getAlias());
        assertNull(descriptor.getInheritancePolicyOrNull());
    }

    @Test
    public void verifyPhoneNumberDescriptor() {
        Server session = JpaHelper.getServerSession(getEMF());
        ClassDescriptor descriptor = session.getDescriptorForAlias("PhoneNumber");

        assertNotNull(descriptor);
        assertEquals("PhoneNumber", descriptor.getAlias());
        assertNull(descriptor.getInheritancePolicyOrNull());
    }

    @Test
    public void verifyProjectDescriptor() {
        Server session = JpaHelper.getServerSession(getEMF());
        ClassDescriptor descriptor = session.getDescriptorForAlias("Project");

        assertNotNull(descriptor);
        assertEquals("Project", descriptor.getAlias());
        assertNotNull(descriptor.getInheritancePolicyOrNull());
    }

    @Test
    public void verifySmallProjectDescriptor() {
        Server session = JpaHelper.getServerSession(getEMF());
        ClassDescriptor descriptor = session.getDescriptorForAlias("SmallProject");

        assertNotNull(descriptor);
        assertEquals("SmallProject", descriptor.getAlias());
        assertNotNull(descriptor.getInheritancePolicyOrNull());
    }

    @Test
    public void verifyLargeProjectDescriptor() {
        Server session = JpaHelper.getServerSession(getEMF());
        ClassDescriptor descriptor = session.getDescriptorForAlias("LargeProject");

        assertNotNull(descriptor);
        assertEquals("LargeProject", descriptor.getAlias());
        assertNotNull(descriptor.getInheritancePolicyOrNull());
    }

    @Override
    protected EntityManagerFactory createEMF(String unitName, Map properties) {
        EntityManagerFactory newEMF = super.createEMF(unitName, properties);
        
        Server session = JpaHelper.getServerSession(newEMF);
        if (session.getDescriptors().isEmpty()) {
            List<EntityTypeImpl> types = EntityTypeFactory.createTypes(newEMF, "model.dynamic.employee");
            EntityTypeImpl.addToSession(session, types);
            
            new DynamicSchemaManager(session).createTables((EntityType[]) null);
        }
        
        return newEMF;
    }
    
    
}
