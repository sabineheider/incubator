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
 *     dclarke - Dynamic Persistence INCUBATION - Enhancement 200045
 *     			 http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.dynamic;

import java.util.Iterator;
import java.util.Map;

import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.dynamic.RelationalEntityTypeFactory;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;

/**
 * A DynamicHelper provides some utility methods to simplify application
 * development with dynamic types. Since the application does not have static
 * references to the dynamic types it must use entity names. This helper
 * provides simplified access to methods that would typically require the static
 * classes.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class DynamicHelper {

    /**
     * Lookup the dynamic type for an alias. This is required to get the type
     * for factory creation but can also be used to provide the application with
     * access to the meta model (type and properties) allowing for dynamic use
     * as well as optimized data value retrieval from an entity.
     */
    public static EntityType getType(DatabaseSession session, String typeName) {
        if (session == null) {
            throw new IllegalArgumentException("No session provided");
        }

        EntityType type = null;
        try {
            ClassDescriptor cd = session.getClassDescriptorForAlias(typeName);
            type = getType(cd);
        } catch (NullPointerException e) { // Workaround for bug ???
            throw DynamicEntityException.invalidTypeName(typeName);
        }
        if (type == null) {
            throw DynamicEntityException.invalidTypeName(typeName);
        }

        return type;
    }

    public static EntityType getType(ClassDescriptor descriptor) {
        EntityType type = (EntityType) descriptor.getProperty(EntityType.DESCRIPTOR_PROPERTY);

        if (type == null) {
            synchronized (descriptor) {
                type = (EntityType) descriptor.getProperty(EntityType.DESCRIPTOR_PROPERTY);
                if (type == null) {
                    EntityTypeFactory factory = new RelationalEntityTypeFactory(descriptor);
                    type = factory.getType();
                }
            }
        }

        return type;
    }

    public static boolean isDynamicType(ClassDescriptor descriptor) {
        return descriptor.getProperties().containsKey(EntityType.DESCRIPTOR_PROPERTY);
    }

    /**
     * Remove a dynamic type from the system.
     * 
     * This implementation assumes that the dynamic type has no relationships to
     * it and that it is not involved in an inheritance relationship. If there
     * are concurrent processes using this type when it is removed some
     * exceptions may occur.
     * 
     * @param session
     * @param typeName
     */
    public static void removeType(DatabaseSession session, String typeName) {
        EntityType type = getType(session, typeName);

        if (type != null) {
            session.getIdentityMapAccessor().initializeIdentityMap(type.getJavaClass());

            ClassDescriptor descriptor = type.unwrap(ClassDescriptor.class);

            session.getProject().getOrderedDescriptors().remove(descriptor);
            session.getProject().getDescriptors().remove(type.getJavaClass());
        }
    }

    /**
     * {@link SessionCustomizer} which configures all descriptors as dynamic
     * entity types.
     */
    public static class SessionCustomizer implements org.eclipse.persistence.config.SessionCustomizer {

        public void customize(Session session) throws Exception {
            for (Iterator<?> i = session.getProject().getDescriptors().values().iterator(); i.hasNext();) {
                getType((ClassDescriptor) i.next());
            }
        }
    }

    /**
     * DescriptorCustomizer implementation provided to simplify configuration of
     * an entity type as dynamic. This method can be invoked directly against a
     * descriptor read from standard metadata or it can be invoked using a
     * {@link PersistenceUnitProperties#DESCRIPTOR_CUSTOMIZER_} in the
     * persistence.xml or properties passed to
     * {@link Persistence#createEntityManagerFactory(String, Map)}
     */
    public static class DescriptorCustomizer implements org.eclipse.persistence.config.DescriptorCustomizer {

        public void customize(ClassDescriptor descriptor) throws Exception {
            getType(descriptor);
        }
    }

}
