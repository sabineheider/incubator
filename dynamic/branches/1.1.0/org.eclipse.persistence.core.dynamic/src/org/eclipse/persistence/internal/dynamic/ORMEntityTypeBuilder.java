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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.descriptors.changetracking.AttributeChangeTrackingPolicy;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.AggregateObjectMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
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
public class ORMEntityTypeBuilder implements EntityTypeBuilder {

    protected EntityTypeImpl entityType;

    public ORMEntityTypeBuilder(DatabaseSession session, String className, EntityType parentType, String... tableNames) {
        this(createClass(session, className, parentType), parentType, tableNames);
    }

    public ORMEntityTypeBuilder(Class<?> dynamicClass, EntityType parentType, String... tableNames) {
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

        this.entityType = new EntityTypeImpl(descriptor, parentType);
        configure(descriptor);
    }

    public ORMEntityTypeBuilder(ClassDescriptor descriptor, EntityType parentType) {
        this.entityType = new EntityTypeImpl(descriptor, parentType);
        configure(descriptor);
    }

    public static Class<?> createClass(DatabaseSession session, String className, EntityType parentType) {
        DynamicConversionManager dcm = DynamicConversionManager.lookup(session);

        if (parentType == null) {
            return dcm.getDynamicClassLoader().createDynamicClass(className);
        } else {
            return dcm.getDynamicClassLoader().createDynamicClass(className, parentType.getJavaClass());
        }
    }

    /**
     * Initialize an existing descriptor for dynamic usage.
     * 
     * @param descriptor
     */
    protected void configure(ClassDescriptor descriptor) {
        descriptor.setObjectChangePolicy(new AttributeChangeTrackingPolicy());
        descriptor.setInstantiationPolicy(new EntityTypeInstantiationPolicy((EntityTypeImpl) getType()));

        for (int index = 0; index < descriptor.getMappings().size(); index++) {
            addMapping((DatabaseMapping) descriptor.getMappings().get(index));
        }

        descriptor.setProperty(EntityTypeImpl.DESCRIPTOR_PROPERTY, entityType);
    }

    private void verifyDynamicType(Class<?> dynamicClass) {
        // TODO - make dependency more flexible
        if (dynamicClass == null || !EntityTypeImpl.class.isAssignableFrom(dynamicClass)) {
            throw new IllegalArgumentException("Dynamic Class must be subclass of: EntityTypeImpl");
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
    protected DatabaseMapping addMapping(DatabaseMapping mapping) {
        ClassDescriptor descriptor = getType().getDescriptor();

        if (!descriptor.getMappings().contains(mapping)) {
            descriptor.addMapping(mapping);
        }

        int index = descriptor.getMappings().indexOf(mapping);
        // Need to account for inherited mappings
        if (getType().getParentType() != null) {
            EntityType current = getType();
            while (current.getParentType() != null) {
                index += current.getParentType().getMappings().size();
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

    public void addToSession(DatabaseSession session, boolean createMissingTables) {
        addToSession(session, createMissingTables, true);
    }

    public void addToSession(DatabaseSession session, boolean createMissingTables, boolean generateFKConstraints) {
        session.addDescriptor(getType().getDescriptor());

        if (createMissingTables) {
            new DynamicSchemaManager(session).createTables(getType());
        }
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
