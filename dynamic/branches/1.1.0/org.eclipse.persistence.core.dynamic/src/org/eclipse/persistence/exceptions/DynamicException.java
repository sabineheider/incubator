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
package org.eclipse.persistence.exceptions;

import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.exceptions.EclipseLinkException;
import org.eclipse.persistence.internal.dynamic.*;
import org.eclipse.persistence.mappings.DatabaseMapping;

/**
 * Custom exception type that provides information about failure cases
 * encountered when using a GenericEntity with TopLink. Any failures that are
 * not specific to GenericEntity use will still involve the standard TopLink
 * exceptions.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class DynamicException extends EclipseLinkException {
    public static final String ILLEGAL_MODIFY_SHARED = "Illegal attempt to modify shared cache instance on: ";

    public DynamicException(String message) {
        super(message);
    }

    public DynamicException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Exception throw when attempting to access a dynamic property by name
     * which does not have an associated mapping. Make sure the property name
     * exists in {@link EntityType#getPropertiesNames()}
     * 
     * @see EntityTypeImpl#getMapping(String)
     */
    public static DynamicException invalidPropertyName(EntityType type, String propertyName) {
        return new DynamicException("Invalid DynamicEntity[" + type + "] property name: " + propertyName);
    }

    /**
     * Exception throw when attempting to access a dynamic property by index
     * which does not have an associated mapping. Make sure the index used is
     * less then {@link EntityType#getNumberOfProperties()}.
     * 
     * @see EntityTypeImpl#getMapping(int)
     */
    public static DynamicException invalidPropertyIndex(EntityType type, int propertyIndex) {
        return new DynamicException("Invalid DynamicEntity[" + type + "] property index: " + propertyIndex);
    }

    /**
     * 
     * @see DynamicEntityImpl#getCollection(DatabaseMapping)
     * @see DynamicEntityImpl#getMap
     */
    public static DynamicException invalidPropertyType(DatabaseMapping mapping, ClassCastException cce) {
        // TODO: Review readability of exception message
        return new DynamicException("DynamicEntity:: Cannot return: " + mapping + ": " + cce.getMessage(), cce);
    }

    /**
     * A {@link DynamicClassWriter} was attempted to be instantiated with a null
     * loader or invalid parentClassName. The parentClassName must not be null
     * or an empty string.
     */
    public static DynamicException illegalDynamicClassWriter(DynamicClassLoader loader, String parentClassName) {
        return new DynamicException("Illegal DynamicClassWriter(" + loader + ", " + parentClassName + ")");
    }

    /**
     * TODO
     * 
     * @param entity
     * @return
     */
    public static DynamicException entityHasNullType(DynamicEntityImpl entity) {
        return new DynamicException("DynamicEntity has null type: " + entity);
    }
}
