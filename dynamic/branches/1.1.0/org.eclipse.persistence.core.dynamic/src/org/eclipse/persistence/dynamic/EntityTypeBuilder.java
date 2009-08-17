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
 *               http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.dynamic;

import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.mappings.AggregateObjectMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;

/**
 * An EntityTypeBuilder is used to construct dynamic descriptors from an
 * existing descriptor or to construct one 'on-the-fly' adding mappings,
 * Sequencing, and inheritance.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public interface EntityTypeBuilder {

    /**
     * Returns the EntityType in its current state. This could mean that the
     * configuration is incomplete.
     */
    public EntityType getType();

    public void setPrimaryKeyFields(String... pkFieldNames);

    /**
     * Allows {@link DirectToFieldMapping} (@Basic) mapping to be added to a
     * dynamic type through API. This method can be used on a new
     * {@link EntityTypeImpl} that has yet to be added to a session and have its
     * descriptor initialized, or it can be called on an active (initialized)
     * descriptor.
     * <p>
     * There is no support currently for having the EclipseLink
     * {@link SchemaManager} generate ALTER TABLE calls so any new columns
     * expected must be added without the help of EclipseLink or use the
     * {@link SchemaManager#replaceObject(org.eclipse.persistence.tools.schemaframework.DatabaseObjectDefinition)}
     * to DROP and CREATE the table. WARNING: This will cause data loss.
     */
    public DirectToFieldMapping addDirectMapping(String name, Class<?> javaType, String fieldName);

    /**
     * Allows {@link OneToOneMapping} (@OneToOne and @ManyToOne) mappings to be
     * added to a dynamic type through API. This method can be used on a new
     * {@link EntityTypeImpl} that has yet to be added to a session and have its
     * descriptor initialized, or it can be called on an active (initialized)
     * descriptor.
     * <p>
     * There is no support currently for having the EclipseLink
     * {@link SchemaManager} generate ALTER TABLE calls so any new columns
     * expected must be added without the help of EclipseLink or use the
     * {@link SchemaManager#replaceObject(org.eclipse.persistence.tools.schemaframework.DatabaseObjectDefinition)}
     * to DROP and CREATE the table. WARNING: This will cause data loss.
     * 
     * @param fkFieldNames
     *            names aligning with the PK fields of the target type
     */
    public OneToOneMapping addOneToOneMapping(String name, EntityType refType, String... fkFieldNames);

    public OneToManyMapping addOneToManyMapping(String name, EntityType refType, String... fkFieldNames);

    public AggregateObjectMapping addAggregateObjectMapping(String name, EntityType refType, boolean allowsNull);

    /**
     * 
     * @param session
     * @param createMissingTables
     */
    public void addToSession(DatabaseSession session, boolean createMissingTables);

    public void addToSession(DatabaseSession session, boolean createMissingTables, boolean generateFKConstraints);

    public void configureSequencing(Sequence sequence, String numberName, String numberFieldName);

    public void configureSequencing(String numberName, String numberFieldName);
}
