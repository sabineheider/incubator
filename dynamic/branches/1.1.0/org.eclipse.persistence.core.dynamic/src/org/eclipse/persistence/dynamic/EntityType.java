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

import java.util.List;

import org.eclipse.persistence.descriptors.ClassDescriptor;

/**
 * An EntityType provides a metadata facade into the EclipseLink
 * object-relational metadata (descriptors & mappings) with specific knowledge
 * of the entity types being dynamic.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public interface EntityType {

    /**
     * Return the entity type's name. This is the short name of the class or the
     * {@link ClassDescriptor#getAlias()}
     * 
     * @return
     */
    public String getName();

    /**
     * @return Fully qualified name of mapped class.
     */
    public String getClassName();

    public EntityType getParentType();

    public int getNumberOfProperties();

    public List<String> getPropertiesNames();

    public boolean containsProperty(String propertyName);

    public int getPropertyIndex(String propertyName);

    public Class<?> getJavaClass();

    public DynamicEntity newInstance();

    public Class<?> getPropertyType(int propertyIndex);

    public Class<?> getPropertyType(String propertyName);

    public ClassDescriptor getDescriptor();

    /**
     * Property name used to store the EntityTypeImpl on each descriptor in its
     * {@link ClassDescriptor#properties}. The EntityType instance is generally
     * populated by the {@link EntityTypeBuilder} and should only be done when
     * properly initialized.
     */
    public static final String DESCRIPTOR_PROPERTY = "ENTITY_TYPE";

}
