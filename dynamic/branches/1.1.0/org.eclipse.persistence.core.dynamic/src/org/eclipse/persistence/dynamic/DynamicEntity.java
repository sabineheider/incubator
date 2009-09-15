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

import org.eclipse.persistence.exceptions.DynamicException;

/**
 * DynamicEntity is the public interface for dealing with dynamic persistent
 * objects. Application code can using dynamic entities can access the
 * persistent state using property names with correspond to the attribute names
 * defined in the underlying descritpor's mappings.
 * <p>
 * In order to understand what attributes are available the {@link EntityType}
 * can be accessed.
 * <p>
 * For Collection and Map operations request the attribute providing its
 * Collection/Map type and then manipulate the resulting container.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public interface DynamicEntity {

    /**
     * Return the persistence value for the given property as the specified type
     * (if provided). In the case of relationships this call will cause lazy
     * load relationships to be instantiated.
     * 
     * @param <T>
     *            generic type of the attribute. If the value cannot be found or
     *            it cannot be cast to the specific type a
     *            {@link DynamicException} will be thrown.
     * @param property
     *            the name of the mapped attribute.
     * @throws DynamicException
     * @return persistent value or relationship container of the specified type
     */
    public <T> T get(String property) throws DynamicException;

    /**
     * TODO
     * 
     * @param property
     * @param value
     * @return
     * @throws DynamicException
     */
    public DynamicEntity set(String property, Object value) throws DynamicException;

    /**
     * TODO
     * 
     * @param property
     * @return
     * @throws DynamicException
     */
    public boolean isSet(String property) throws DynamicException;

    /**
     * TODO
     * 
     * @return
     * @throws DynamicException
     */
    public EntityType getType() throws DynamicException;
}
