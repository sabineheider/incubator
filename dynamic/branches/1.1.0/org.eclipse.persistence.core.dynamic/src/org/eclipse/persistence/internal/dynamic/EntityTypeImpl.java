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
package org.eclipse.persistence.internal.dynamic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.persistence.Persistence;

import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.descriptors.changetracking.AttributeChangeTrackingPolicy;
import org.eclipse.persistence.dynamic.DynamicEntityException;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.indirection.ValueHolder;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.indirection.BasicIndirectionPolicy;
import org.eclipse.persistence.internal.jpa.CMP3Policy;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.mappings.structures.ReferenceMapping;

/**
 * An EntityType provides a metadata facade into the EclipseLink
 * object-relational metadata (descriptors & mappings) with specific knowledge
 * of the entity types being dynamic.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class EntityTypeImpl implements EntityType {

    /**
     * Property name used to store the EntityTypeImpl on each descriptor.
     */
    public static final String DESCRIPTOR_PROPERTY = "ENTITY_TYPE";

    private ClassDescriptor descriptor;

    private List<String> propertyNames = new PropertyNameList();

    /**
     * These properties require initialization when a new instance is created.
     * This includes properties that are primitives as well as relationships
     * requiring indirection ValueHolders or collections.
     */
    private Set<DatabaseMapping> mappingsRequiringInitialization = new HashSet<DatabaseMapping>();

    /**
     * Creation of an EntityTypeImpl for an existing Descriptor with mappings.
     * 
     * @param descriptor
     */
    public EntityTypeImpl(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
        initialize(this.descriptor);
    }

    /**
     * Create an {@link EntityTypeImpl} for a dynamic class mapping to the
     * provided table.
     * 
     * @param dynamicClass
     *            created using the {@link DynamicClassLoader}
     * @param tableName
     *            Qualified table name
     */
    public EntityTypeImpl(Class dynamicClass, String tableName) {
        this.descriptor = new RelationalDescriptor();
        this.descriptor.setJavaClass(dynamicClass);
        if (tableName != null) {
            this.descriptor.setTableName(tableName);
        }
        initialize(this.descriptor);
    }

    /**
     * Initialize a ClassDescriptor for dynamic usage.
     */
    private void initialize(ClassDescriptor descriptor) {
        descriptor.setObjectChangePolicy(new AttributeChangeTrackingPolicy());
        descriptor.getInstantiationPolicy().useFactoryInstantiationPolicy(this, "newInstance");
        if (descriptor.getCMPPolicy() == null) {
            descriptor.setCMPPolicy(new CMP3Policy());
        }

        for (int index = 0; index < getDescriptor().getMappings().size(); index++) {
            DatabaseMapping mapping = (DatabaseMapping) getDescriptor().getMappings().get(index);

            mapping.setAttributeAccessor(new ValuesAccessor(mapping, index));

            if (requiresInitialization(mapping)) {
                getMappingsRequiringInitialization().add(mapping);
            }
        }
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

    public static EntityTypeImpl getType(ClassDescriptor descriptor) {
        EntityTypeImpl type = (EntityTypeImpl) descriptor.getProperty(DESCRIPTOR_PROPERTY);

        if (type == null) {
            synchronized (descriptor) {
                type = (EntityTypeImpl) descriptor.getProperty(DESCRIPTOR_PROPERTY);
                if (type == null) {
                    type = new EntityTypeImpl(descriptor);
                    descriptor.setProperty(DESCRIPTOR_PROPERTY, type);
                }
            }
        }

        return type;
    }

    public static boolean isDynamicType(ClassDescriptor descriptor) {
        return descriptor.getProperties().containsKey(DESCRIPTOR_PROPERTY);
    }

    public ClassDescriptor getDescriptor() {
        return this.descriptor;
    }

    public List<DatabaseMapping> getMappings() {
        return getDescriptor().getMappings();
    }

    /**
     * @see EntityType#getName()
     */
    public String getName() {
        return getDescriptor().getAlias();
    }

    public int getNumberOfProperties() {
        return getMappings().size();
    }

    public Set<DatabaseMapping> getMappingsRequiringInitialization() {
        return this.mappingsRequiringInitialization;
    }

    /**
     * Using privileged reflection create a new instance of the dynamic type
     * passing in this {@link EntityTypeImpl} so the dynamic entity can have a
     * reference to its type. After creation initialize all required attributes.
     * 
     * @see DynamicEntityImpl#DynamicEntityImpl(EntityTypeImpl)
     * @return new DynamicEntity with initialized attributes
     */
    public DynamicEntityImpl newInstance() {
        DynamicEntityImpl entity = null;

        try {
            entity = (DynamicEntityImpl) PrivilegedAccessHelper.invokeConstructor(this.getTypeConstructor(), new Object[] { this });
        } catch (InvocationTargetException exception) {
            throw DescriptorException.targetInvocationWhileConstructorInstantiation(this.getDescriptor(), exception);
        } catch (IllegalAccessException exception) {
            throw DescriptorException.illegalAccessWhileConstructorInstantiation(this.getDescriptor(), exception);
        } catch (InstantiationException exception) {
            throw DescriptorException.instantiationWhileConstructorInstantiation(this.getDescriptor(), exception);
        } catch (NoSuchMethodError exception) {
            // This exception is not documented but gets thrown.
            throw DescriptorException.noSuchMethodWhileConstructorInstantiation(this.getDescriptor(), exception);
        } catch (NullPointerException exception) {
            // Some JVMs will throw a NULL pointer exception here
            throw DescriptorException.nullPointerWhileConstructorInstantiation(this.getDescriptor(), exception);
        }

        for (DatabaseMapping mapping : getMappingsRequiringInitialization()) {
            initializeValue(mapping, entity);
        }

        return entity;
    }

    /**
     * Initialize the default value handling primitives, collections and
     * indirection.
     * 
     * @param mapping
     * @param entity
     */
    private void initializeValue(DatabaseMapping mapping, DynamicEntityImpl entity) {
        Object value = null;

        if (mapping.isDirectToFieldMapping() && mapping.getAttributeClassification().isPrimitive()) {
            Class primClass = mapping.getAttributeClassification();

            if (primClass == ClassConstants.PBOOLEAN) {
                value = false;
            } else if (primClass == ClassConstants.PINT) {
                value = 0;
            } else if (primClass == ClassConstants.PLONG) {
                value = 0L;
            } else if (primClass == ClassConstants.PCHAR) {
                value = Character.MIN_VALUE;
            } else if (primClass == ClassConstants.PDOUBLE) {
                value = 0.0d;
            } else if (primClass == ClassConstants.PFLOAT) {
                value = 0.0f;
            } else if (primClass == ClassConstants.PSHORT) {
                value = Short.MIN_VALUE;
            } else if (primClass == ClassConstants.PBYTE) {
                value = Byte.MIN_VALUE;
            }
        } else if (mapping.isForeignReferenceMapping()) {
            ForeignReferenceMapping refMapping = (ForeignReferenceMapping) mapping;

            if (refMapping.usesIndirection() && refMapping.getIndirectionPolicy() instanceof BasicIndirectionPolicy) {
                value = new ValueHolder(value);
            }
        } else if (mapping.isAggregateObjectMapping()) {
            value = mapping.getReferenceDescriptor().getObjectBuilder().buildNewInstance();
        }

        mapping.setAttributeValueInObject(entity, value);
    }

    private Constructor defaultConstructor = null;

    /**
     * Return the default (zero-argument) constructor for the descriptor class.
     */
    protected Constructor getTypeConstructor() throws DescriptorException {
        // Lazy initialize, because the constructor cannot be serialized
        if (defaultConstructor == null) {
            buildTypeConstructorFor(getDescriptor().getJavaClass());
        }
        return defaultConstructor;
    }

    /**
     * Build and return the default (zero-argument) constructor for the
     * specified class.
     */
    protected void buildTypeConstructorFor(Class javaClass) throws DescriptorException {
        try {
            this.defaultConstructor = PrivilegedAccessHelper.getDeclaredConstructorFor(javaClass, new Class[] { EntityTypeImpl.class }, true);
            this.defaultConstructor.setAccessible(true);
        } catch (NoSuchMethodException exception) {
            throw DescriptorException.noSuchMethodWhileInitializingInstantiationPolicy(javaClass.getName() + ".<Default Constructor>", getDescriptor(), exception);
        }
    }

    public boolean isInitialized() {
        return getDescriptor().isFullyInitialized();
    }

    public boolean containsProperty(String propertyName) {
        return getPropertiesNames().contains(propertyName);
    }

    public Class getJavaClass() {
        return getDescriptor().getJavaClass();
    }

    public DatabaseMapping getMapping(String propertyName) {
        DatabaseMapping mapping = getDescriptor().getMappingForAttributeName(propertyName);

        if (mapping == null) {
            throw DynamicEntityException.invalidPropertyName(this, propertyName);
        }

        return mapping;
    }

    public DatabaseMapping getMapping(int propertyIndex) {
        if (propertyIndex < 0 || propertyIndex >= getMappings().size()) {
            throw DynamicEntityException.invalidPropertyIndex(this, propertyIndex);
        }

        DatabaseMapping mapping = getMappings().get(propertyIndex);

        return mapping;
    }

    /**
     * 
     */
    public DatabaseMapping addMapping(DatabaseMapping mapping, String... pkFields) {
        // TODO: verify that duplicate PK fields are not added
        if (pkFields != null && pkFields.length > 0) {
            for (int index = 0; index < pkFields.length; index++) {
                getDescriptor().addPrimaryKeyFieldName(pkFields[index]);
            }
        }

        if (!getDescriptor().getMappings().contains(mapping)) {
            getDescriptor().addMapping(mapping);
        }

        mapping.setAttributeAccessor(new ValuesAccessor(mapping, getDescriptor().getMappings().indexOf(mapping)));

        if (requiresInitialization(mapping)) {
            getMappingsRequiringInitialization().add(mapping);
        }

        return mapping;
    }

    public List<String> getPropertiesNames() {
        return this.propertyNames;
    }

    public int getPropertyIndex(String propertyName) {
        return getMappings().indexOf(getMapping(propertyName));
    }

    public Class getPropertyType(int propertyIndex) {
        return getMapping(propertyIndex).getAttributeClassification();
    }

    public Class getPropertyType(String propertyName) {
        return getMapping(propertyName).getAttributeClassification();
    }

    /**
     * Helper method to access internal EclipseLink types within this
     * {@link EntityTypeImpl} with a generic interface reducing unnecessary
     * casting in the calling code.
     */
    public <T> T unwrap(Class<T> T) {
        if (ClassDescriptor.class.isAssignableFrom(T)) {
            return (T) getDescriptor();
        }

        throw new IllegalArgumentException("Cannot unwrap " + this + " as: " + T);
    }

    public String toString() {
        return "EntityType(" + getName() + ") - " + getDescriptor();
    }

    /**
     * DescriptorCustomizer implementation provided to simplify configuration of
     * an entity type as dynamic. This method can be invoked directly against a
     * descriptor read from standard metadata or it can be invoked using a
     * {@link PersistenceUnitProperties#DESCRIPTOR_CUSTOMIZER_} in the
     * persistence.xml or properties passed to
     * {@link Persistence#createEntityManagerFactory(String, Map)}
     */
    public static class ConfigCustomizer implements DescriptorCustomizer {

        public void customize(ClassDescriptor descriptor) throws Exception {
            EntityTypeImpl.getType(descriptor);
        }
    }

    /**
     * Simple AbstractList to dynamically provide read-only access to the
     * property names leveraging the descriptor's mappings. This list will allow
     * users to access the properties cleanly through the meta-model approach of
     * asking a type for its properties
     */
    private class PropertyNameList extends AbstractList<String> {

        public String get(int index) {
            return EntityTypeImpl.this.getMapping(index).getAttributeName();
        }

        public int size() {
            return EntityTypeImpl.this.getNumberOfProperties();
        }

    }
}
