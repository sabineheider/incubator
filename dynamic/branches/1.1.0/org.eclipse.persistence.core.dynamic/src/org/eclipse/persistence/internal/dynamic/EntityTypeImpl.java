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
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.sessions.DatabaseSession;

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
	 * Map of properties keyed by name.
	 */
	private Map<String, EntityPropertyImpl> propertiesMap;
	/**
	 * List of properties. This is the same properties as the propertiesMap but
	 * is used to match the order of the mappings and aligns with the values[]
	 * in the DynamicEntity instances. This allows the values to be accessed by
	 * index as well as by property name/instance.
	 */
	private ArrayList<EntityPropertyImpl> properties;
	/**
	 * These properties require initialization when a new instance is created.
	 * This includes properties that are primitives as well as relationships
	 * requiring indirection ValueHolders or collections.
	 */
	private List<EntityPropertyImpl> propertiesRequiringInitialization;

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

		this.properties = new ArrayList<EntityPropertyImpl>();
		this.propertiesMap = new HashMap<String, EntityPropertyImpl>();
		this.propertiesRequiringInitialization = new ArrayList<EntityPropertyImpl>();
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

	protected Map<String, EntityPropertyImpl> getPropertiesMap() {
		return this.propertiesMap;
	}

	protected List<EntityPropertyImpl> getProperties() {
		return this.properties;
	}

	public Iterator<EntityPropertyImpl> getPropertiesIterator() {
		return getProperties().iterator();
	}

	public String getName() {
		return getDescriptor().getAlias();
	}

	public int getPropertiesSize() {
		return getProperties().size();
	}

	public Set<String> getPropertiesNames() {
		return getPropertiesMap().keySet();
	}

	public boolean containsProperty(String propertyName) {
		return getPropertiesMap().containsKey(propertyName);
	}

	public EntityPropertyImpl getProperty(String propertyName) {
		EntityPropertyImpl prop = getPropertiesMap().get(propertyName);

		if (prop == null) {
			throw DynamicEntityException.invalidPropertyName(this, propertyName);
		}
		return prop;
	}

	public EntityPropertyImpl getProperty(int propertyIndex) {
		if (propertyIndex < 0 || propertyIndex > getProperties().size()) {
			throw DynamicEntityException.invalidPropertyIndex(this, propertyIndex);
		}
		return getProperties().get(propertyIndex);
	}

	public int getPropertyIndex(String propertyName) {
		return getProperty(propertyName).getIndex();
	}

	public Class getJavaClass() {
		return getDescriptor().getJavaClass();
	}

	public List<EntityPropertyImpl> getPropertiesRequiringInitialization() {
		if (this.propertiesRequiringInitialization == null) {
			this.propertiesRequiringInitialization = new ArrayList<EntityPropertyImpl>();
		}
		return this.propertiesRequiringInitialization;
	}

	/**
	 * Build properties for the mappings on the descriptor.
	 */
	private void buildProperties() {
		int numProperties = getDescriptor().getMappings().size();
		this.properties = new ArrayList<EntityPropertyImpl>(numProperties);
		this.propertiesMap = new HashMap<String, EntityPropertyImpl>(numProperties);
		this.propertiesRequiringInitialization = new ArrayList<EntityPropertyImpl>();

		for (int index = 0; index < numProperties; index++) {
			DatabaseMapping mapping = (DatabaseMapping) getDescriptor().getMappings().get(index);
			buildProperty(mapping, index);
		}
	}

	private EntityPropertyImpl buildProperty(DatabaseMapping mapping, int index) {
		EntityPropertyImpl property = null;

		if (mapping.isForeignReferenceMapping()) {
			ForeignReferenceMapping frMapping = (ForeignReferenceMapping) mapping;

			if (frMapping.isCollectionMapping()) {
				property = new EntityCollectionPropertyImpl(this, (CollectionMapping) mapping);
			}
			property = new EntityReferencePropertyImpl(this, (OneToOneMapping) mapping);
		} else {
			property = new EntityPropertyImpl(this, mapping);
		}

		this.properties.add(property);
		this.propertiesMap.put(property.getName(), property);

		if (property.requiresInitialization()) {
			getPropertiesRequiringInitialization().add(property);
		}

		return property;
	}

	/**
	 * Add a new Property with underlying direct mapping to the EntityType.
	 * 
	 * @param name
	 * @param columnName
	 * @return
	 */
	public EntityProperty addProperty(String name, String columnName, Class attributeType, boolean primaryKey) {
		if (isInitialized()) {
			// TODO: Proper validation exception stating that an
			// ENtityType/Descriptor cannot be modified after it is initialized.
			throw new IllegalStateException("Cannot add property (mappings) EntityType (Descriptor) initialized.");
		}
		EntityPropertyImpl property = new EntityPropertyImpl(this, name, columnName, attributeType, primaryKey);

		this.properties.add(property);
		this.propertiesMap.put(property.getName(), property);

		if (property.requiresInitialization()) {
			getPropertiesRequiringInitialization().add(property);
		}

		return property;
	}

	/**
	 * 
	 * @return new DynamicEntity with initialized attributes
	 */
	public DynamicEntityImpl newInstance() {
		DynamicEntityImpl entity = buildNewInstance(this);

		for (EntityPropertyImpl property : getPropertiesRequiringInitialization()) {
			property.initializeValue(entity);
		}

		return entity;
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
		Class javaClass = DynamicClassLoader.getLoader(session).createDynamicClass(getDescriptor().getJavaClassName());
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
}
