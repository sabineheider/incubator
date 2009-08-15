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
package org.eclipse.persistence.internal.dynamic;

import static org.eclipse.persistence.internal.dynamic.DynamicClassLoader.DEFAULT_DYNAMIC_PARENT;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.descriptors.changetracking.AttributeChangeTrackingPolicy;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.dynamic.EntityTypeFactory;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.mappings.structures.ReferenceMapping;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;

/**
 * TODO
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class RelationalEntityTypeFactory implements EntityTypeFactory {

    protected EntityTypeImpl entityType;

    public RelationalEntityTypeFactory(DatabaseSession session, String className, String... tableNames) {
        this((DynamicConversionManager.lookup(session).getDynamicClassLoader()).createDynamicClass(className), tableNames);
    }

    public RelationalEntityTypeFactory(Class<?> dynamicClass, String... tableNames) {
        verifyDynamicType(dynamicClass);

        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(dynamicClass);
        
        if (tableNames == null || tableNames.length < 1) {
            descriptor.descriptorIsAggregate();
        } else {
            for (int index = 0; index < tableNames.length; index++) {
                descriptor.addTableName(tableNames[index]);
            }
        }
        
        this.entityType = new EntityTypeImpl(descriptor);
        configure(descriptor);
    }
    
    public RelationalEntityTypeFactory(ClassDescriptor descriptor) {
        this.entityType = new EntityTypeImpl(descriptor);
        configure(descriptor);
    }

    /**
     * Initialize an existing descriptor for dynamic usage.
     * 
     * @param descriptor
     */
    protected void configure(ClassDescriptor descriptor) {
        descriptor.setObjectChangePolicy(new AttributeChangeTrackingPolicy());
        descriptor.getInstantiationPolicy().useFactoryInstantiationPolicy(getType(), "newInstance");

        for (int index = 0; index < descriptor.getMappings().size(); index++) {
            addMapping((DatabaseMapping) descriptor.getMappings().get(index));
        }

        descriptor.setProperty(EntityTypeImpl.DESCRIPTOR_PROPERTY, entityType);
    }

    private void verifyDynamicType(Class<?> dynamicClass) {
        if (dynamicClass == null || !DEFAULT_DYNAMIC_PARENT.isAssignableFrom(dynamicClass)) {
            throw new IllegalArgumentException("Dynamic Class must be subclass of: " + DEFAULT_DYNAMIC_PARENT);
        }
    }

    /**
     * 
     * @return
     */
    public EntityType getType() {
        return this.entityType;
    }

    /**
     * Test if a mapping requires initialization when a new instance is created.
     * This is true for:
     * <ul>
     * <li>primitives
     * <li>collection mappings
     * <li>basic indirection references
     * </ul>
     * 
     * @see #newInstance() for creation and initialization
     */
    private boolean requiresInitialization(DatabaseMapping mapping) {
        if (mapping.isDirectToFieldMapping() && mapping.getAttributeClassification() != null && mapping.getAttributeClassification().isPrimitive()) {
            return true;
        }
        if (mapping.isReferenceMapping()) {
            ReferenceMapping frMapping = (ReferenceMapping) mapping;
            return frMapping.usesIndirection() || frMapping.isCollectionMapping();
        }
        if (mapping.isAggregateMapping()) {
            return !((AggregateObjectMapping) mapping).isNullAllowed();
        }
        return false;
    }

    /**
     * Set the PK field names on the underlying descriptor ensuring no duplicate
     * names are added.
     * 
     * @param pkFieldNames
     *            qualified or unqualified field names
     */
    public void addPrimaryKeyFields(String... pkFieldNames) {
        if (pkFieldNames != null && pkFieldNames.length > 0) {
            for (int index = 0; index < pkFieldNames.length; index++) {
                DatabaseField pkField = new DatabaseField(pkFieldNames[index]);

                if (!getType().getDescriptor().getPrimaryKeyFields().contains(pkField)) {
                    getType().getDescriptor().addPrimaryKeyFieldName(pkFieldNames[index]);
                }
            }
        }
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
    public DirectToFieldMapping addDirectMapping(String name, Class<?> javaType, String fieldName) {
        DirectToFieldMapping mapping = new DirectToFieldMapping();
        mapping.setAttributeName(name);
        mapping.setFieldName(fieldName);
        mapping.setAttributeClassification(javaType);

        return (DirectToFieldMapping) addMapping(mapping);
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
    public OneToOneMapping addOneToOneMapping(String name, EntityType refType, String ... fkFieldNames) {
        if (fkFieldNames == null || refType.getDescriptor().getPrimaryKeyFields().size() != fkFieldNames.length) {
            throw new IllegalArgumentException("Invalid FK field names: " + fkFieldNames + " for target: " + refType);
        }
        
        OneToOneMapping mapping = new OneToOneMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceClass(refType.getJavaClass());
        
        for (int index = 0 ; index < fkFieldNames.length; index++) {
            String targetField = refType.getDescriptor().getPrimaryKeyFields().get(index).getName();
            mapping.addForeignKeyFieldName(fkFieldNames[index], targetField);
        }

        return (OneToOneMapping) addMapping(mapping);
    }

    public OneToManyMapping addOneToManyMapping(String name, EntityType refType, String fkFieldName, String targetField) {
        OneToManyMapping mapping = new OneToManyMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceClass(refType.getJavaClass());
        mapping.addTargetForeignKeyFieldName(fkFieldName, targetField);
        mapping.useTransparentList();

        return (OneToManyMapping) addMapping(mapping);
    }

    public AggregateObjectMapping addAggregateObjectMapping(String name, EntityType refType, boolean allowsNull) {
        AggregateObjectMapping mapping = new AggregateObjectMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceClass(refType.getJavaClass());
        mapping.setIsNullAllowed(allowsNull);

        return (AggregateObjectMapping) addMapping(mapping);
    }

    /**
     * 
     */
    private DatabaseMapping addMapping(DatabaseMapping mapping) {
        ClassDescriptor descriptor = getType().getDescriptor();

        if (!descriptor.getMappings().contains(mapping)) {
            descriptor.addMapping(mapping);
        }

        mapping.setAttributeAccessor(new ValuesAccessor(mapping, descriptor.getMappings().indexOf(mapping)));

        if (requiresInitialization(mapping)) {
            // TODO: Remove impl dependency
            ((EntityTypeImpl)getType()).getMappingsRequiringInitialization().add(mapping);
        }

        return mapping;
    }

    public void configureSequencing(String numberName, String numberFieldName) {
        getType().getDescriptor().setSequenceNumberName(numberName);
        getType().getDescriptor().setSequenceNumberFieldName(numberFieldName);
    }

    public void configureSequencing(Sequence sequence, String numberName, String numberFieldName) {
        configureSequencing(numberName, numberFieldName);
        getType().getDescriptor().setSequence(sequence);
    }

    public void addToSession(DatabaseSession session, boolean createMissingTables) {
        session.addDescriptor(getType().getDescriptor());

        if (createMissingTables) {
            new DynamicSchemaManager(session).createTables(getType());
        }
    }

}
