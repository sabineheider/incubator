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

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
import org.eclipse.persistence.queries.*;
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
    public static EntityType getType(Session session, String typeName) {
        ClassDescriptor cd = session.getClassDescriptorForAlias(typeName);

        if (cd == null) {
            return null;
        }
        return getType(cd);
    }

    public static EntityType getType(ClassDescriptor descriptor) {
        return (EntityType) descriptor.getProperty(EntityType.DESCRIPTOR_PROPERTY);
    }

    /**
     * Provide access to the entity's type.
     * 
     * @param entity
     * @return
     * @throws ClassCastException
     *             if entity is not an instance of {@link DynamicEntityImpl}
     */
    public static EntityType getType(DynamicEntity entity) throws ClassCastException {
        return ((DynamicEntityImpl) entity).getType();
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

            ClassDescriptor descriptor = type.getDescriptor();

            session.getProject().getOrderedDescriptors().remove(descriptor);
            session.getProject().getDescriptors().remove(type.getJavaClass());
        }
    }

    /**
     * Helper method to simplify creating a native ReadAllQuery using the entity
     * type name (descriptor alias)
     */
    public static ReadAllQuery newReadAllQuery(Session session, String typeName) {
        EntityType type = getType(session, typeName);

        if (type == null) {
            throw new IllegalArgumentException("DynamicHelper.createQuery: Dynamic type not found: " + typeName);
        }

        return new ReadAllQuery(type.getJavaClass());
    }

    /**
     * Helper method to simplify creating a native ReadObjectQuery using the
     * entity type name (descriptor alias)
     */
    public static ReadObjectQuery newReadObjectQuery(Session session, String typeName) {
        EntityType type = getType(session, typeName);

        if (type == null) {
            throw new IllegalArgumentException("DynamicHelper.createQuery: Dynamic type not found: " + typeName);
        }

        return new ReadObjectQuery(type.getJavaClass());
    }

    /**
     * Helper method to simplify creating a native ReportQuery using the entity
     * type name (descriptor alias)
     */
    public static ReportQuery newReportQuery(Session session, String typeName, ExpressionBuilder builder) {
        EntityType type = getType(session, typeName);

        if (type == null) {
            throw new IllegalArgumentException("DynamicHelper.createQuery: Dynamic type not found: " + typeName);
        }

        return new ReportQuery(type.getJavaClass(), builder);
    }

    /**
     * {@link SessionCustomizer} which configures all descriptors as dynamic
     * entity types.
     */
    public static class SessionCustomizer implements org.eclipse.persistence.config.SessionCustomizer {

        public void customize(Session session) throws Exception {
            DynamicClassLoader dcl = DynamicClassLoader.lookup(session);

            for (Iterator<?> i = session.getProject().getDescriptors().values().iterator(); i.hasNext();) {
                new EntityTypeBuilder(dcl, (ClassDescriptor) i.next(), null);
            }
        }
    }

}
