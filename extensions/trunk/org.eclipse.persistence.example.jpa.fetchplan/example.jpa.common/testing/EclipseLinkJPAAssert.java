/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
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
package testing;

import javax.persistence.EntityManagerFactory;

import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.descriptors.InstanceVariableAttributeAccessor;
import org.eclipse.persistence.internal.descriptors.MethodAttributeAccessor;
import org.eclipse.persistence.internal.descriptors.PersistenceEntity;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.AttributeAccessor;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.queries.FetchGroupTracker;

/**
 * Testing utility to assert various EclipseLink configurations in the
 * descriptors and mappings.
 * 
 * @author dclarke
 * @since EclipseLink 2.0
 */
public abstract class EclipseLinkJPAAssert {

    public static ClassDescriptor assertEntity(EntityManagerFactory emf, String entityTypeName) {
        ClassDescriptor descriptor = JpaHelper.getServerSession(emf).getDescriptorForAlias(entityTypeName);

        Assert.assertNotNull("No ClassDescriptor found for: " + entityTypeName, descriptor);
        return descriptor;
    }

    public static ClassDescriptor assertEntity(EntityManagerFactory emf, Object entity) {
        ClassDescriptor descriptor = JpaHelper.getServerSession(emf).getDescriptor(entity);

        Assert.assertNotNull("No ClassDescriptor found for: " + entity, descriptor);
        return descriptor;
    }

    public static void assertWoven(ClassDescriptor descriptor) {
        Assert.assertNotNull("Null descriptor provided", descriptor);
        Assert.assertTrue("Entity type not woven: " + descriptor, PersistenceEntity.class.isAssignableFrom(descriptor.getJavaClass()));
    }

    public static void assertWoven(EntityManagerFactory emf, String entityTypeName) {
        assertWoven(assertEntity(emf, entityTypeName));
    }

    public static void assertNotWoven(ClassDescriptor descriptor) {
        Assert.assertFalse(PersistenceEntity.class.isAssignableFrom(descriptor.getJavaClass()));
    }

    public static void assertNotWoven(EntityManagerFactory emf, String entityTypeName) {
        assertNotWoven(assertEntity(emf, entityTypeName));
    }

    public static DatabaseMapping assertMapping(ClassDescriptor descriptor, String attributeName) {
        DatabaseMapping mapping = descriptor.getMappingForAttributeName(attributeName);

        Assert.assertNotNull("No mapping found on " + descriptor + " for attribute named: " + attributeName, mapping);
        return mapping;
    }

    public static DatabaseMapping assertMapping(EntityManagerFactory emf, String entityTypeName, String attributeName) {
        return assertMapping(assertEntity(emf, entityTypeName), attributeName);
    }

    public static void assertLazy(ClassDescriptor descriptor, String attributeName) {
        DatabaseMapping mapping = assertMapping(descriptor, attributeName);

        if (mapping.isForeignReferenceMapping()) {
            Assert.assertTrue("FRMapping not lazy: " + mapping, ((ForeignReferenceMapping) mapping).usesIndirection());
        } else if (descriptor.hasFetchGroupManager() && descriptor.getFetchGroupManager().getDefaultFetchGroup() != null){
            Assert.assertTrue("Basic Mapping not lazy: " + mapping, mapping.isLazy());
        } else {
            Assert.fail("Mapping is not lazy: " + mapping);
        }
    }

    public static void assertLazy(EntityManagerFactory emf, String entityTypeName, String attributeName) {
        assertLazy(assertEntity(emf, entityTypeName), attributeName);
    }

    public static void assertNotLazy(ClassDescriptor descriptor, String attributeName) {
        DatabaseMapping mapping = assertMapping(descriptor, attributeName);

        Assert.assertFalse(mapping.isLazy());
    }

    public static void assertNotLazy(EntityManagerFactory emf, String entityTypeName, String attributeName) {
        assertNotLazy(assertEntity(emf, entityTypeName), attributeName);
    }

    public static ForeignReferenceMapping assertRelationship(ClassDescriptor descriptor, String attributeName) {
        DatabaseMapping mapping = assertMapping(descriptor, attributeName);

        Assert.assertTrue(mapping.isForeignReferenceMapping());

        return (ForeignReferenceMapping) mapping;
    }

    public static ForeignReferenceMapping assertRelationship(EntityManagerFactory emf, String entityTypeName, String attributeName) {
        return assertRelationship(assertEntity(emf, entityTypeName), attributeName);
    }

    public static void assertPrivateOwned(ClassDescriptor descriptor, String attributeName) {
        ForeignReferenceMapping mapping = assertRelationship(descriptor, attributeName);
        Assert.assertTrue(mapping.isPrivateOwned());
    }

    public static void assertPrivateOwned(EntityManagerFactory emf, String entityTypeName, String attributeName) {
        assertPrivateOwned(assertEntity(emf, entityTypeName), attributeName);
    }

    /**
     * Verify that the named attribute is loaded. This will return false for
     * lazy mappings that are not loaded into the provided entity instance.
     */
    public static void assertLoaded(EntityManagerFactory emf, Object entity, String attribute) {
        ClassDescriptor descriptor = assertEntity(emf, entity);
        DatabaseMapping mapping = assertMapping(descriptor, attribute);

        if (mapping.isDirectToFieldMapping() && entity instanceof FetchGroupTracker) {
            Assert.assertTrue("DirectToFieldMapping for '" + attribute + "' is not loaded", ((FetchGroupTracker) entity)._persistence_isAttributeFetched(attribute));
        } else {

            Object value = mapping.getAttributeValueFromObject(entity);
            if (value instanceof IndirectContainer) {
                Assert.assertTrue("IndirectContainer for '" + attribute + "' is not loaded", ((IndirectContainer) value).isInstantiated());
            }
            if (value instanceof ValueHolderInterface) {
                Assert.assertTrue("ValueHolderInterface for '" + attribute + "' is not loaded", ((ValueHolderInterface) value).isInstantiated());
            }
        }
    }

    /**
     * Verify that the named attribute is loaded. This will return false for
     * lazy mappings that are not loaded into the provided entity instance.
     */
    public static void assertNotLoaded(EntityManagerFactory emf, Object entity, String attribute) {
        ClassDescriptor descriptor = assertEntity(emf, entity);
        DatabaseMapping mapping = assertMapping(descriptor, attribute);

        if (mapping.isDirectToFieldMapping() && entity instanceof FetchGroupTracker) {
            Assert.assertFalse("DirectToFieldMapping for '" + attribute + "' is loaded", ((FetchGroupTracker) entity)._persistence_isAttributeFetched(attribute));
        } else {

            AttributeAccessor accessor = mapping.getAttributeAccessor();

            // Avoid calling _persistence_get<attribute-name>_vh methods
            if (accessor.isMethodAttributeAccessor() && ((MethodAttributeAccessor) accessor).getGetMethodName().startsWith("_persistence_get")) {
                accessor = new InstanceVariableAttributeAccessor();
                accessor.setAttributeName("_persistence_" + mapping.getAttributeName() + "_vh");
                accessor.initializeAttributes(mapping.getDescriptor().getJavaClass());
            }

            Object value = accessor.getAttributeValueFromObject(entity);
            if (value instanceof IndirectContainer) {
                Assert.assertFalse("IndirectContainer for '" + attribute + "' is loaded", ((IndirectContainer) value).isInstantiated());
            }
            if (value instanceof ValueHolderInterface) {
                Assert.assertFalse("ValueHolderInterface for '" + attribute + "' is loaded", ((ValueHolderInterface) value).isInstantiated());
            }
        }
    }
}
