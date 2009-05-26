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

import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.descriptors.changetracking.AttributeChangeTrackingPolicy;
import org.eclipse.persistence.dynamic.DynamicEntityException;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl.ValuesAccessor;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.tools.schemaframework.TableDefinition;

/**
 * An EntityType provides a metadata facade into the EclipseLink
 * object-relational metadata (descriptors & mappings) with specific knowledge
 * of the entity types being dynamic.
 * 
 * @author Doug Clarke
 * @since EclipseLink 1.0
 */
public class EntityTypeImpl implements EntityType {
    /**
     * Property name used to store the singleton EntityType on each descriptor.
     */
    private static final String DESCRIPTOR_PROPERTY = "ENTITY_TYPE";
    /**
     * Method name on EntityType used to by the descriptor's instantiation
     * policy. The EntityType instance functions as the factory object in the
     * policy.
     */
    private static String FACTORY_METHOD = "newInstance";

    private ClassDescriptor descriptor;

    /**
     * These properties require initialization when a new instance is created.
     * This includes properties that are primitives as well as relationships
     * requiring indirection ValueHolders or collections.
     */
    private List<DatabaseMapping> mappingsRequiringInitialization;

    /**
     * Creation of an EntityTypeImpl for an existing Descriptor with mappings.
     * 
     * @param descriptor
     */
    public EntityTypeImpl(ClassDescriptor descriptor) {
        if (descriptor.isAggregateDescriptor()) {
            throw DynamicEntityException.featureNotSupported("AggregateObjectMapping - " + descriptor.getAlias());
        }
        if (descriptor.hasInheritance()) {
            throw DynamicEntityException.featureNotSupported("Inheritance - " + descriptor.getAlias());
        }

        this.descriptor = descriptor;
        buildProperties();

        descriptor.setObjectChangePolicy(new AttributeChangeTrackingPolicy());
        descriptor.getInstantiationPolicy().useFactoryInstantiationPolicy(this, FACTORY_METHOD);
    }

    /**
     * 
     * @param className
     * @param tableName
     */
    public EntityTypeImpl(Class dynamicClass, String tableName) {
        this.descriptor = new RelationalDescriptor();

        getDescriptor().setJavaClass(dynamicClass);
        getDescriptor().setTableName(tableName);
        getDescriptor().setObjectChangePolicy(new AttributeChangeTrackingPolicy());
        getDescriptor().getInstantiationPolicy().useFactoryInstantiationPolicy(this, FACTORY_METHOD);

        this.mappingsRequiringInitialization = new ArrayList<DatabaseMapping>();
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

    public String getName() {
        return getDescriptor().getAlias();
    }

    public int getNumberOfProperties() {
        return getMappings().size();
    }

    protected List<DatabaseMapping> getMappingsRequiringInitialization() {
        return this.mappingsRequiringInitialization;
    }

    /**
     * Build properties for the mappings on the descriptor.
     */
    private void buildProperties() {
        int numProperties = getDescriptor().getMappings().size();
        this.mappingsRequiringInitialization = new ArrayList<DatabaseMapping>();

        for (int index = 0; index < numProperties; index++) {
            DatabaseMapping mapping = (DatabaseMapping) getDescriptor().getMappings().get(index);
            buildProperty(mapping, index);
        }
    }

    private void buildProperty(DatabaseMapping mapping, int index) {
        mapping.setAttributeAccessor(new ValuesAccessor(mapping, index));

        if (requiresInitialization(mapping)) {
            getMappingsRequiringInitialization().add(mapping);
        }
    }

    private boolean requiresInitialization(DatabaseMapping mapping) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * 
     * @return new DynamicEntity with initialized attributes
     */
    public DynamicEntityImpl newInstance() {
        DynamicEntityImpl entity = buildNewInstance(this);

        for (DatabaseMapping mapping : getMappingsRequiringInitialization()) {
            initializeValue(mapping, entity);
        }

        return entity;
    }

    private void initializeValue(DatabaseMapping mapping, DynamicEntityImpl entity) {
        // TODO Auto-generated method stub

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

    protected DynamicEntityImpl buildNewInstance(EntityTypeImpl type) throws DescriptorException {
        try {
            return (DynamicEntityImpl) PrivilegedAccessHelper.invokeConstructor(this.getTypeConstructor(), new Object[] { type });
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
    }

    public boolean isInitialized() {
        return getDescriptor().isFullyInitialized();
    }

    /**
     * Add the dynamically created EntityType to the provided session. If the
     * session is already initialized then the descriptor will also be
     * initialized.
     * 
     * @param session
     */
    public void initialize(DatabaseSession session) {
        DynamicClassLoader loader = DynamicClassLoader.getLoader(session, DynamicEntityImpl.class);
        Class javaClass = loader.createDynamicClass(getDescriptor().getJavaClassName());
        
        getDescriptor().setJavaClass(javaClass);
        session.addDescriptor(getDescriptor());
    }

    public String toString() {
        return "DynamicEntity(" + getName() + ")";
    }

    /**
	 * 
	 */
    public static class ConfigCustomizer implements DescriptorCustomizer {

        public void customize(ClassDescriptor descriptor) throws Exception {
            EntityTypeImpl.getType(descriptor);
        }

    }

    public boolean containsProperty(String propertyName) {
        // TODO Auto-generated method stub
        return false;
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

    public Set<String> getPropertiesNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getPropertyIndex(String propertyName) {
        return getMappings().indexOf(getMapping(propertyName));
    }

    public DirectToFieldMapping addDirectMapping(String name, Class javaType, String fieldName) {
        DirectToFieldMapping mapping = (DirectToFieldMapping) getDescriptor().addDirectMapping(name, fieldName);
        mapping.setAttributeClassification(javaType);
        mapping.setAttributeAccessor(new ValuesAccessor(mapping, getDescriptor().getMappings().indexOf(mapping)));
        return mapping;
    }

    public OneToOneMapping addOneToOneMapping(String name, Class refType, String fieldName) {
        OneToOneMapping mapping = new OneToOneMapping();
        mapping.setAttributeName(name);
        mapping.setReferenceClass(refType); // TODO
        mapping.addForeignKeyFieldName(fieldName, "?");
        descriptor.addMapping(mapping);
        mapping.setAttributeAccessor(new ValuesAccessor(mapping, getDescriptor().getMappings().indexOf(mapping)));

        return mapping;
    }

    public TableDefinition getTableDefinition() {
        TableDefinition tableDef = new TableDefinition();

        tableDef.setName(getDescriptor().getTableName());
        tableDef.addPrimaryKeyField("ID", Integer.class);

        return tableDef;
    }

}
