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
 *     			 http://wiki.eclipse.org/EclipseLink/JPA/Dynamic
 *     mnorman
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.dynamic;

//EclipseLink imports
import org.eclipse.persistence.exceptions.DynamicException;

/**
 * <code>DynamicEntity</code> is the public interface for dealing with dynamic persistent objects.
 * <p>
 * The purpose of dynamic persistent objects is to enable (simple) data access when only mapping
 * information is available <br />
 * and no concrete Java model is present (specifically, no <tt>.class</tt> files .)
 * <p>
 * Applications using <code>DynamicEntity</code>'s can access the persistent state using property names
 * which correspond <br />
 * to the mapped attributes in the underlying EclipseLink descriptors.
 * For properties mapped to containers ({@link java.util.Collection Collection},<br />
 * {@link java.util.Map Map}, etc.), the property is retrieved then the resulting container can
 * be manipulated.
 * <pre>
 *     ...
 *     DynamicEntity de = ...; // retrieve from database
 *     Collection&lt;String&gt; myListOfGroups = de.&lt;Collection&lt;String&gt;&gt;get("myListOfGroups");
 *     if (!myListOfGroups.isEmpty()) {
 *        myListOfGroups.add("FabFour");
 *     }
 * </pre>
 * To discover meta-data about a DynamicEntity's properties, see the {@link DynamicHelper} class
 *
 * @author dclarke, mnorman
 * @since EclipseLink 1.2
 */
public interface DynamicEntity {

    /**
     * Return the persistence value for the given property as the specified type.
     * In the case of relationships, this call will populate lazy-loaded relationships
     *
     * @param <T>
     *      generic type of the property (if not provided, assume Object).
     *      If the property cannot be cast to the specific type, a {@link DynamicException}will be thrown.
     * @param
     *      propertyPath the name of a mapped property, or a path-like expression that navigates to it.
     *      If the property cannot be found, a {@link DynamicException} will be thrown.
     * @throws
     *      DynamicException
     * @return
     *      persistent value or relationship container of the specified type
     */
    public <T> T get(String propertyPath) throws DynamicException;

    /**
     * Set the persistence value for the given property to the specified value
     *
     * @param
     *      propertyPath the name of a mapped property, or a path-like expression that navigates to it.
     *      If the property cannot be found, a {@link DynamicException} will be thrown.
     * @param
     *      value the specified object
     * @throws
     *      DynamicException
     * @return
     *      the same DynamicEntity instance
     */
    public DynamicEntity set(String propertyPath, Object value) throws DynamicException;

    /**
     * Discover if a property has a persistent value
     *
     * @param
     *      propertyPath the name of a mapped property, or a path-like expression that navigates to it.
     *      If the property cannot be found, a {@link DynamicException} will be thrown.
     * @return
     *      true if the property has been set
     * @throws
     *      DynamicException
     */
    public boolean isSet(String propertyPath) throws DynamicException;

}
