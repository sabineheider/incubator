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
 *     dclarke - SimpleDynamicMap Example - Bug 277731
 *               http://wiki.eclipse.org/EclipseLink/Examples/JPA/Dynamic/SimpleDynamicMap
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.Test;

import example.SimpleDynamicMap_JPAExample;

public class SimpleDynamicMap_JPAExample_Tests {

    private static SimpleDynamicMap_JPAExample example = new SimpleDynamicMap_JPAExample();

    private static EntityManagerFactory emf;

    private static EntityManagerFactory getEMF() {
        if (emf == null) {
            emf = example.createEMF();
        }
        assertNotNull("No Entitymanagerfactory returned from createEMF", emf);
        return emf;
    }

    private static ClassDescriptor getDescriptor(String alias) {
        ClassDescriptor descriptor = JpaHelper.getServerSession(getEMF()).getClassDescriptorForAlias(alias);
        assertNotNull("No descriptor found for alias: " + alias, descriptor);
        return descriptor;
    }

    @Test
    public void createSession() {
        EntityManagerFactory emf = getEMF();

        assertNotNull(emf);
        assertTrue(emf.isOpen());

        Server session = JpaHelper.getServerSession(emf);
        assertNotNull(session);
        assertTrue(session.isConnected());
        assertEquals(0, session.getDescriptors().size());
    }

    @Test
    public void createSimpleType() {
        ClassDescriptor descriptor = example.createDynamicType(getEMF());
        Server session = JpaHelper.getServerSession(emf);

        assertNotNull(descriptor);
        assertEquals(1, session.getDescriptors().size());
        assertEquals("model.SimpleType", descriptor.getJavaClassName());
        assertEquals("SimpleType", descriptor.getAlias());
        assertEquals("DYNAMIC_SIMPLE", descriptor.getTableName());
        assertEquals(1, descriptor.getPrimaryKeyFieldNames().size());
        assertEquals("DYNAMIC_SIMPLE.ID", descriptor.getPrimaryKeyFieldNames().get(0));
        assertEquals(2, descriptor.getMappings().size());
        
        DirectToFieldMapping mapping = (DirectToFieldMapping) descriptor.getMappingForAttributeName("id");
        assertNotNull(mapping);
        assertEquals("DYNAMIC_SIMPLE.ID", mapping.getFieldName());
        assertEquals(Integer.class, mapping.getAttributeClassification());
        
        mapping = (DirectToFieldMapping) descriptor.getMappingForAttributeName("value");
        assertNotNull(mapping);
        assertEquals("DYNAMIC_SIMPLE.VALUE", mapping.getFieldName());
        assertEquals(String.class, mapping.getAttributeClassification());
    }

    @Test
    public void persistSimpleTypeInstances() {
        ClassDescriptor descriptor = getDescriptor("SimpleType");

        Map newEntity = example.persistDynamicInstances(getEMF(), descriptor);

        assertNotNull(newEntity);

        EntityManager em = getEMF().createEntityManager();
        int count = ((Number) em.createQuery("SELECT COUNT(s) FROM SimpleType s").getSingleResult()).intValue();
        em.close();
        assertEquals(1, count);
    }

    @Test
    public void querySimpleTypeInstances() {
        ClassDescriptor descriptor = getDescriptor("SimpleType");

        List<Map> entities = example.queryDynamicInstances(getEMF(), descriptor);

        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertEquals(1, entities.get(0).get("id"));
        assertEquals("value-1", entities.get(0).get("value"));
    }

    @Test
    public void updateSimpleTypeInstances() {
        ClassDescriptor descriptor = getDescriptor("SimpleType");

        Map entity = example.updateDyanmicInstances(getEMF(), descriptor);

        assertNotNull(entity);
        assertEquals(1, entity.get("id"));
        assertEquals("value-1+", entity.get("value"));
    }

    @Test
    public void deleteSimpleTypeInstances() {
        ClassDescriptor descriptor = getDescriptor("SimpleType");

        example.deleteDynamicInstances(getEMF(), descriptor);

        EntityManager em = getEMF().createEntityManager();
        int count = ((Number) em.createQuery("SELECT COUNT(s) FROM SimpleType s").getSingleResult()).intValue();
        em.close();
        assertEquals(0, count);
    }

    @Test
    public void removeSimpleType() {
        ClassDescriptor descriptor = getDescriptor("SimpleType");

        example.removeDynamicType(getEMF(), descriptor);

        descriptor = JpaHelper.getServerSession(getEMF()).getClassDescriptorForAlias("SimpleType");
        assertNull(descriptor);
        assertEquals(0, JpaHelper.getServerSession(getEMF()).getDescriptors().size());
    }

}
