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
package org.eclipse.persistence.testing.models.dynamic.composite;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.eclipse.persistence.annotations.*;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.helper.*;
import org.eclipse.persistence.internal.sessions.*;
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.sessions.server.ClientSession;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.eclipse.persistence.tools.schemaframework.TableDefinition;

/**
 * 
 * 
 * @author dclarke
 * 
 */
@Entity
@Table(name = "COMP_TYPE")
@Cache(type = CacheType.FULL, size = 100)
@Customizer(TypeDefinitionListener.class)
public class TypeDefinition {
	/** Entity name (Descriptor alias) for dynamic type */
	@Id
	private String name;

	@Column(name = "CLASS_NAME")
	private String className;

	@Column(name = "TABLE_NAME")
	private String tableName;

	@OneToMany(mappedBy = "type", cascade = CascadeType.ALL)
	private List<FieldDefinition> fields;

	@Transient
	private EntityTypeImpl entityType;

	public TypeDefinition() {
		this.fields = new ArrayList<FieldDefinition>();
	}

	public TypeDefinition(String className, String tableName) {
		this();
		this.className = className;
		this.tableName = tableName;

		int index = className.lastIndexOf(".");
		if (index >= 0) {
			this.name = className.substring(index + 1);
		} else {
			this.name = className;
		}
	}

	public String getClassName() {
		return this.className;
	}

	public String getName() {
		return name;
	}

	public List<FieldDefinition> getFields() {
		return fields;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	/**
	 * 
	 * @return
	 */
	public DynamicEntity newInstance() {
		return getEntityType().newInstance();
	}

	/**
	 * Initialize the EntityType ...
	 * 
	 * @param entityType
	 */
	protected void initialize(EntityType entityType) {
		this.entityType = (EntityTypeImpl) entityType;
		for (FieldDefinition fieldDef : getFields()) {
			fieldDef.initialize((EntityTypeImpl) getEntityType());
		}
	}

	public FieldDefinition addField(String name, String columnName, Class javaType, boolean primaryKey) {
		FieldDefinition field = new FieldDefinition(name, columnName, javaType, primaryKey);
		getFields().add(field);
		field.setType(this);

		// field.initialize((EntityTypeImpl) getEntityType());
		return field;
	}

	/**
	 * Create a new EntityType based on this TypeDefinition and its fields. This
	 * method is called from the
	 * 
	 * @see TypeDefinitionListener#postMergeUnitOfWorkChangeSet(org.eclipse.persistence.sessions.SessionEvent)
	 * @param session
	 * @return
	 */
	protected EntityType createEntityType(AbstractSession session) {
		if (session.getClassDescriptorForAlias(getName()) != null) {
			throw new IllegalStateException("Cannot create TypeDefinition for existing descriptor alias: " + getName());
		}

		if (session.isUnitOfWork()) {
			session = ((UnitOfWorkImpl) session).getParent();
		}
		if (session.isClientSession()) {
			session = ((ClientSession) session).getParent();
		}

		Class dynamicClass = DynamicClassLoader.getLoader(session).createDynamicClass(getClassName());

		this.entityType = new EntityTypeImpl(dynamicClass, getClassName(), getTableName());
		for (FieldDefinition fieldDef : getFields()) {
			fieldDef.createProperty(this.entityType);
		}

		session.getProject().addDescriptor(this.entityType.getDescriptor(), (DatabaseSessionImpl) session);

		return this.entityType;
	}

	protected void createOnDatabase(AbstractSession session) {
		TableDefinition tableDef = new TableDefinition();
		tableDef.setName(getTableName());
		for (FieldDefinition fieldDef : getFields()) {
			org.eclipse.persistence.tools.schemaframework.FieldDefinition dbFieldDef = new org.eclipse.persistence.tools.schemaframework.FieldDefinition();
			dbFieldDef.setName(fieldDef.getColumnName());

			DatabaseField dbField = fieldDef.getProperty().getMapping().getField();

			if (dbField.getColumnDefinition() != null && dbField.getColumnDefinition().length() > 0) {
				dbFieldDef.setTypeDefinition(dbField.getColumnDefinition());
			} else {
				Class fieldType = dbField.getType();

				// Check if the user field is a String and only then allow the
				// length specified
				// in the @Column annotation to be set on the field.
				if ((fieldType != null)) {
					if (fieldType.equals(ClassConstants.STRING) || fieldType.equals(ClassConstants.APCHAR) || fieldType.equals(ClassConstants.ACHAR)) {
						// The field size is defaulted to "255" or use the user
						// supplied length
						dbFieldDef.setSize(dbField.getLength());
					} else {
						if (dbField.getPrecision() > 0) {
							dbFieldDef.setSize(dbField.getPrecision());
							dbFieldDef.setSubSize(dbField.getScale());
						}
					}
				}

				if ((fieldType == null) || (!fieldType.isPrimitive() && (session.getPlatform().getFieldTypeDefinition(fieldType) == null))) {
					// TODO: log a warning for inaccessiable type or not
					// convertable
					// type.
					AbstractSessionLog.getLog().log(SessionLog.FINEST, "field_type_set_to_java_lang_string", dbField.getQualifiedName(), fieldType);

					// set the default type (lang.String) to all un-resolved
					// java
					// type, like null, Number, util.Date, NChar/NType, Calendar
					// sql.Blob/Clob, Object, or unknown type). Please refer to
					// bug
					// 4352820.
					dbFieldDef.setType(ClassConstants.STRING);
				} else {
					// need to convert the primitive type if applied.
					dbFieldDef.setType(ConversionManager.getObjectClass(fieldType));
				}

				dbFieldDef.setShouldAllowNull(dbField.isNullable());
				dbFieldDef.setUnique(dbField.isUnique());
			}

			dbFieldDef.setIsPrimaryKey(fieldDef.isPrimaryKey());
			tableDef.addField(dbFieldDef);
		}

		if (session.isUnitOfWork()) {
			session = ((UnitOfWorkImpl) session).getParent();
		}
		if (session.isClientSession()) {
			session = ((ClientSession) session).getParent();
		}

		new SchemaManager((DatabaseSessionImpl) session).createObject(tableDef);
	}

	public String toString() {
		return "TypeDefinition(" + getName() + ")";
	}
}
