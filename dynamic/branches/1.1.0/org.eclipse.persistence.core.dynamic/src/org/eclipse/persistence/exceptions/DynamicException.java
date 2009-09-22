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
 *     dclarke - Dynamic Persistence INCUBATION - Enhancement 200045
 *     			 http://wiki.eclipse.org/EclipseLink/Development/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.exceptions;

//EclipseLink imports
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.DynamicClassWriter;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
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

    protected DynamicException(String message) {
        super(message);
    }

    protected DynamicException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * A request to get a persistent value from a DynamicEntity was made
     * providing a propertyName that does not correspond to any mappings in the
     * underlying descriptor.
     * 
     * @see DynamicEntityImpl#get(String)
     * */
    public static DynamicException invalidPropertyName(EntityType type, String propertyName) {
        return new DynamicException("Invalid DynamicEntity[" + type + "] property name: " + propertyName);
    }

    /**
     * A request to get a persistent value from a DynamicEntity was made
     * providing a propertyName that does exist but the provided return type
     * failed when casting. The generic type specified on the get method must be
     * supported by the underlying value stored in the dynamic entity.
     * 
     * @see DynamicEntityImpl#get(String)
     */
    public static DynamicException invalidPropertyType(DatabaseMapping mapping, ClassCastException cce) {
        return new DynamicException("DynamicEntity:: Cannot return: " + mapping + ": " + cce.getMessage(), cce);
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
     * A {@link DynamicClassWriter} was attempted to be instantiated with a null
     * loader or invalid parentClassName. The parentClassName must not be null
     * or an empty string.
     */
    public static DynamicException illegalDynamicClassWriter(DynamicClassLoader loader, String parentClassName) {
        return new DynamicException("Illegal DynamicClassWriter(" + loader + ", " + parentClassName + ")");
    }

    /**
     * A {@link DynamicEntity} could not be found
     */
    public static DynamicException entityNotFoundException(String message) {
        return new DynamicException("DynamicEntity not found: " + message);
    }
    
    /**
     * The {@link DynamicEntityImpl} has a null type indicating an illegal state
     * of the entity.
     * 
     * @see DynamicEntityImpl#getType()
     */
    /*
     * This should not happen in the current implementation but may be supported
     * when detachment through serialization is added.
     */
    public static DynamicException entityHasNullType(DynamicEntityImpl entity) {
        return new DynamicException("DynamicEntity has null type: " + entity);
    }

    /**
     * A null or empty string was provided as the parent class for a dynamic
     * class being registered for creation.
     * 
     * @see DynamicClassWriter(String)
     */
    public static DynamicException illegalParentClassName(String parentClassName) {
        return new DynamicException("Illegal parent class name for dynamic type: " + parentClassName);
    }

    /**
     * A call to {@link DynamicClassLoader#addClass(String, DynamicClassWriter)}
     * or
     * {@link DynamicClassLoader#creatDynamicClass(String, DynamicClassWriter)}
     * was invoked with a className that already had a
     * {@link DynamicClassWriter} that is not compatible with the provided
     * writer.
     */
    public static DynamicException incompatibleDuplicateWriters(String className, DynamicClassWriter existingWriter, DynamicClassWriter writer) {
        return new DynamicException("Duplicate addClass request with incompatible writer: " + className + " - existing: " + existingWriter + " - new: " + writer);
    }
}
