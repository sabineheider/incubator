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

import org.eclipse.persistence.mappings.DatabaseMapping;

/**
 * This abstract class is used to represent an entity which typically is not
 * realized in Java code. In combination with the DynamicClassLoader ASM is used
 * to generate subclasses that will work within EclipseLink's framework. Since
 * no concrete fields or methods exist on this class the mappings used must be
 * customized to use a custom AttributeAccessor (
 * {@link EntityPropertyImpl.DynamicAttributeAccessor}).
 * <p>
 * <b>Type/Property Meta-model</b>: This dynamic entity approach also includes a
 * meta-model facade to simplify access to the types and property information so
 * that clients can more easily understand the model. Each {@link EntityType}
 * wraps the underlying EclipseLink relational-descriptor and the
 * {@link EntityProperty} wraps each mapping. The client application can use
 * these types and properties to facilitate generic access to the entity
 * instances and are required for creating new instances as well as for
 * accessing the Java class needed for JPA and EclipseLink native API calls.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public interface DynamicEntity {

    public EntityType getType();

    public Object get(String propertyName);

    public <T> T get(String propertyName, Class<T> type);

    public Object get(int propertyIndex);

    public <T> T get(int propertyIndex, Class<T> type);

    public Object get(DatabaseMapping mapping);

    public <T> T get(DatabaseMapping mapping, Class<T> type);

    public DynamicEntity set(int propertyIndex, Object value);

    public DynamicEntity set(String propertyName, Object value);

    public DynamicEntity set(DatabaseMapping property, Object value);

    public Object add(String propertyName, Object value);

    public Object remove(String propertyName, Object value);

    public Object get(String mapPropertyName, Object key);

    public Object put(String mapPropertyName, Object key, Object value);

    public boolean isSet(String propertyName);

    public boolean isSet(int propertyIndex);

    public boolean isSet(DatabaseMapping mapping);

}
