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

import static org.eclipse.persistence.internal.dynamic.DynamicClassLoader.DEFAULT_DYNAMIC_PARENT;

import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;

/**
 * TODO
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class RelationalMappingFactory {

    protected EntityTypeImpl entityType;

    public RelationalMappingFactory(DatabaseSession session, String className, String... tableNames) {
        this(((DynamicClassLoader) DynamicConversionManager.lookup(session).getLoader()).createDynamicClass(className), tableNames);
    }

    public RelationalMappingFactory(Class dynamicClass, String... tableNames) {
        verifyDynamicType(dynamicClass);

        if (tableNames == null || tableNames.length < 1) {
            this.entityType = new EntityTypeImpl(dynamicClass, null);
            this.entityType.getDescriptor().descriptorIsAggregate();
        } else {
            this.entityType = new EntityTypeImpl(dynamicClass, tableNames[0]);
            for (int index = 1; index < tableNames.length; index++) {
                this.entityType.getDescriptor().addTableName(tableNames[index]);
            }
        }

        this.entityType.getDescriptor().setProperty(EntityTypeImpl.DESCRIPTOR_PROPERTY, entityType);
    }

    private void verifyDynamicType(Class dynamicClass) {
        if (dynamicClass == null || !DEFAULT_DYNAMIC_PARENT.isAssignableFrom(dynamicClass)) {
            throw new IllegalArgumentException("Dynamic Class must be subclass of: " + DEFAULT_DYNAMIC_PARENT);
        }
    }

    public EntityType getType() {
        return getTypeImpl();
    }

    protected EntityTypeImpl getTypeImpl() {
        return this.entityType;
    }

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
    public DirectToFieldMapping addDirectMapping(String name, Class javaType, String fieldName, boolean isPrimaryKey) {
        DirectToFieldMapping mapping = new DirectToFieldMapping();
        mapping.setAttributeName(name);
        mapping.setFieldName(fieldName);
        mapping.setAttributeClassification(javaType);

        String[] pkFields = isPrimaryKey ? new String[] { fieldName } : null;

        return (DirectToFieldMapping) getTypeImpl().addMapping(mapping, pkFields);
    }

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
     */
    public OneToOneMapping addOneToOneMapping(String name, EntityType refType, String fkFieldName, String targetField) {
        OneToOneMapping mapping = new OneToOneMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceClass(refType.getJavaClass());
        mapping.addForeignKeyFieldName(fkFieldName, targetField);

        return (OneToOneMapping) getTypeImpl().addMapping(mapping);
    }

    public OneToManyMapping addOneToManyMapping(String name, EntityType refType, String fkFieldName, String targetField) {
        OneToManyMapping mapping = new OneToManyMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceClass(refType.getJavaClass());
        mapping.addTargetForeignKeyFieldName(fkFieldName, targetField);
        mapping.useTransparentList();

        return (OneToManyMapping) getTypeImpl().addMapping(mapping);
    }

    public AggregateObjectMapping addAggregateObjectMapping(String name, EntityType refType, boolean allowsNull) {
        AggregateObjectMapping mapping = new AggregateObjectMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceClass(refType.getJavaClass());
        mapping.setIsNullAllowed(allowsNull);

        return (AggregateObjectMapping) getTypeImpl().addMapping(mapping);
    }

    public void addToSession(DatabaseSession session, boolean createMissingTables) {
        session.addDescriptor(getTypeImpl().getDescriptor());

        if (createMissingTables) {
            new DynamicSchemaManager(session).createTables(getTypeImpl());
        }
    }
}
