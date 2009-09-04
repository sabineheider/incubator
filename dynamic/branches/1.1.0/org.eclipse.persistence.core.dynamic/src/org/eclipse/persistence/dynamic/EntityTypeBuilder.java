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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.descriptors.changetracking.AttributeChangeTrackingPolicy;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.DynamicClassWriter;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.dynamic.EntityTypeInstantiationPolicy;
import org.eclipse.persistence.internal.dynamic.ValuesAccessor;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;

/**
 * The EntityTypeBuilder is a utility class for creating dynamic types and their
 * mappings. It targets the creation of native-ORM mappings. This builder works
 * as a factory for the type that it wraps. A new builder is required for each
 * typed being constructed.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class EntityTypeBuilder {

    /**
     * The type being built.
     */
    protected EntityTypeImpl entityType;

    public EntityTypeBuilder(DatabaseSession session, String className, EntityType parentType, String... tableNames) {
        this(DynamicClassLoader.lookup(session), className, parentType, tableNames);
    }

    public EntityTypeBuilder(DynamicClassLoader dcl, String className, EntityType parentType, String... tableNames) {
        addDynamicClasses(dcl, className, parentType);

        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(dcl.creatDynamicClass(className));
        this.entityType = new EntityTypeImpl(descriptor, parentType);

        configure(tableNames);
    }

    // TODO: What if parent type is not properly registered for creation?
    public EntityTypeBuilder(Class<?> dynamicClass, EntityType parentType, String... tableNames) {
        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(dynamicClass);
        this.entityType = new EntityTypeImpl(descriptor, parentType);

        configure(tableNames);
    }

    public EntityTypeBuilder(DatabaseSession session, ClassDescriptor descriptor, EntityType parentType) {
        this(DynamicClassLoader.lookup(session), descriptor, parentType);
    }

    public EntityTypeBuilder(DynamicClassLoader dcl, ClassDescriptor descriptor, EntityType parentType) {
        this.entityType = new EntityTypeImpl(descriptor, parentType);

        if (descriptor.getJavaClass() == null) {
            addDynamicClasses(dcl, descriptor.getJavaClassName(), parentType);
        }

        configure(descriptor);
    }

    /**
     * 
     * @param dcl
     * @param className
     * @param parentType
     */
    protected void addDynamicClasses(DynamicClassLoader dcl, String className, EntityType parentType) {
        DynamicClassWriter writer = null;

        if (parentType != null && parentType.getJavaClass() == null) {
            if (dcl.getClassWriter(parentType.getClassName()) == null) {
                dcl.addClass(parentType.getClassName());
                writer = new DynamicClassWriter(dcl, parentType.getClassName());
            } else {
                writer = new DynamicClassWriter(parentType.getJavaClass());
            }
        }

        // Add new entity class
        dcl.addClass(className, writer);
    }

    /**
     * 
     * @param tableNames
     */
    protected void configure(String... tableNames) {
        ClassDescriptor descriptor = getType().getDescriptor();

        // Configure Table names
        if (tableNames == null || tableNames.length < 1) {
            descriptor.descriptorIsAggregate();
        } else {
            for (int index = 0; index < tableNames.length; index++) {
                descriptor.addTableName(tableNames[index]);
            }
        }

        configure(descriptor);
    }

    /**
     * Initialize an existing descriptor for dynamic usage.
     */
    protected void configure(ClassDescriptor descriptor) {
        descriptor.setObjectChangePolicy(new AttributeChangeTrackingPolicy());
        descriptor.setInstantiationPolicy(new EntityTypeInstantiationPolicy((EntityTypeImpl) getType()));

        for (int index = 0; index < descriptor.getMappings().size(); index++) {
            addMapping((DatabaseMapping) descriptor.getMappings().get(index));
        }

        descriptor.setProperty(EntityTypeImpl.DESCRIPTOR_PROPERTY, entityType);
    }

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
        if (mapping.isForeignReferenceMapping()) {
            ForeignReferenceMapping frMapping = (ForeignReferenceMapping) mapping;
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
    public void setPrimaryKeyFields(String... pkFieldNames) {
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
    public OneToOneMapping addOneToOneMapping(String name, EntityType refType, String... fkFieldNames) {
        if (fkFieldNames == null || refType.getDescriptor().getPrimaryKeyFields().size() != fkFieldNames.length) {
            throw new IllegalArgumentException("Invalid FK field names: " + fkFieldNames + " for target: " + refType);
        }

        OneToOneMapping mapping = new OneToOneMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceClass(refType.getJavaClass());

        for (int index = 0; index < fkFieldNames.length; index++) {
            String targetField = refType.getDescriptor().getPrimaryKeyFields().get(index).getName();
            mapping.addForeignKeyFieldName(fkFieldNames[index], targetField);
        }

        return (OneToOneMapping) addMapping(mapping);
    }

    public OneToManyMapping addOneToManyMapping(String name, EntityType refType, String... fkFieldNames) {
        if (fkFieldNames == null || getType().getDescriptor().getPrimaryKeyFields().size() != fkFieldNames.length) {
            throw new IllegalArgumentException("Invalid FK field names: " + fkFieldNames + " for target: " + refType);
        }

        OneToManyMapping mapping = new OneToManyMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceClass(refType.getJavaClass());

        for (int index = 0; index < fkFieldNames.length; index++) {
            String targetField = getType().getDescriptor().getPrimaryKeyFields().get(index).getName();
            mapping.addTargetForeignKeyFieldName(fkFieldNames[index], targetField);
        }

        mapping.useTransparentList();

        return (OneToManyMapping) addMapping(mapping);
    }

    public DirectCollectionMapping addDirectCollectionMapping(String name, String targetTable, String valueColumn, Class<?> valueType, String... fkFieldNames) {
        if (fkFieldNames == null || getType().getDescriptor().getPrimaryKeyFields().size() != fkFieldNames.length) {
            throw new IllegalArgumentException("Invalid FK field names: " + fkFieldNames + " for target: ");// TODO
        }

        DirectCollectionMapping mapping = new DirectCollectionMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceTableName(targetTable);
        mapping.setDirectFieldName(valueColumn);
        mapping.setDirectFieldClassification(valueType);

        for (int index = 0; index < fkFieldNames.length; index++) {
            String targetField = getType().getDescriptor().getPrimaryKeyFields().get(index).getName();
            mapping.addReferenceKeyFieldName(fkFieldNames[index], targetField);
        }

        mapping.useTransparentList();

        return (DirectCollectionMapping) addMapping(mapping);
    }

    public AggregateObjectMapping addAggregateObjectMapping(String name, EntityType refType, boolean allowsNull) {
        AggregateObjectMapping mapping = new AggregateObjectMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceClass(refType.getJavaClass());
        mapping.setIsNullAllowed(allowsNull);

        return (AggregateObjectMapping) addMapping(mapping);
    }

    public void addManyToManyMapping(String name, EntityType refType, String relationshipTableName) {
        ManyToManyMapping mapping = new ManyToManyMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceClass(refType.getJavaClass());
        mapping.setRelationTableName(relationshipTableName);

        for (DatabaseField sourcePK : getType().getDescriptor().getPrimaryKeyFields()) {
            mapping.addSourceRelationKeyFieldName(sourcePK.getName(), sourcePK.getQualifiedName());
        }
        for (DatabaseField targetPK : refType.getDescriptor().getPrimaryKeyFields()) {
            String relField = targetPK.getName();
            if (mapping.getSourceRelationKeyFieldNames().contains(relField)) {
                relField = refType.getName() + "_" + relField;
            }
            mapping.addTargetRelationKeyFieldName(relField, targetPK.getQualifiedName());
        }
        mapping.useTransparentList();

        addMapping(mapping);
    }

    /**
     * Add the mapping to the types' descriptor. This is where the
     * {@link ValuesAccessor} is created and the position of the mapping in the
     * descriptor is captured to use as its index.
     */
    protected DatabaseMapping addMapping(DatabaseMapping mapping) {
        ClassDescriptor descriptor = getType().getDescriptor();

        if (!descriptor.getMappings().contains(mapping)) {
            descriptor.addMapping(mapping);
        }

        int index = descriptor.getMappings().indexOf(mapping);

        // Need to account for inherited mappings. When initialized a child
        // descriptor has all of its parent mappings added ahead of its
        // mappings. This adds the necessary offset.
        if (getType().getParentType() != null) {
            EntityType current = getType();
            while (current.getParentType() != null) {
                index += current.getParentType().getDescriptor().getMappings().size();
                current = current.getParentType();
            }
        }

        mapping.setAttributeAccessor(new ValuesAccessor(mapping, index));

        if (requiresInitialization(mapping)) {
            // TODO: Remove impl dependency
            ((EntityTypeImpl) getType()).getMappingsRequiringInitialization().add(mapping);
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

    /**
     * TODO
     * 
     * @param session
     * @param createMissingTables
     * @param generateFKConstraints
     * @param types
     */
    public static void addToSession(DatabaseSession session, boolean createMissingTables, boolean generateFKConstraints, EntityType... types) {
        Collection<ClassDescriptor> descriptors = new ArrayList<ClassDescriptor>(types.length);

        for (int index = 0; index < types.length; index++) {
            descriptors.add(types[index].getDescriptor());

            if (!types[index].getDescriptor().requiresInitialization()) {
                types[index].getDescriptor().getInstantiationPolicy().initialize((AbstractSession) session);
            }
        }

        session.addDescriptors(descriptors);

        if (createMissingTables) {
            new DynamicSchemaManager(session).createTables(generateFKConstraints, types);
        }
    }

}
