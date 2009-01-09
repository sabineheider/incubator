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

import javax.persistence.*;

import org.eclipse.persistence.annotations.*;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.dynamic.EntityProperty;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;

@Entity
@Table(name = "COMP_FIELD")
@TypeConverter(name = "class", dataType = String.class, objectType = Class.class)
@Cache(type = CacheType.FULL, size = 1000)
public class FieldDefinition {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private int id;

	private String name;

	@Convert("class")
	@Column(name = "JAVA_TYPE")
	private Class javaType;

	@ManyToOne
	@JoinColumn(name = "TYPE_ID")
	private TypeDefinition type;

	@Column(name = "COL_NAME")
	private String columnName;

	@Column(name = "PK")
	private boolean primaryKey;

	@Transient
	private EntityProperty property;

	private FieldDefinition() {
		this.primaryKey = false;
	}

	protected FieldDefinition(String name, String columnName, Class javaType, boolean primaryKey) {
		this();
		this.name = name;
		this.columnName = columnName;
		this.javaType = javaType;
		this.primaryKey = primaryKey;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class getJavaType() {
		return javaType;
	}

	public void setJavaType(Class javaType) {
		this.javaType = javaType;
	}

	public TypeDefinition getType() {
		return type;
	}

	/**
	 * @param type
	 */
	protected void setType(TypeDefinition type) {
		this.type = type;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public EntityProperty getProperty() {
		return this.property;
	}

	public boolean isPrimaryKey() {
		return this.primaryKey;
	}

	protected EntityProperty initialize(EntityTypeImpl entityType) {
		if (this.property == null) {
			this.property = entityType.getProperty(getName());
		}

		return property;
	}

	protected EntityProperty createProperty(EntityTypeImpl entityType) {
		if (this.property == null) {
			this.property = entityType.addProperty(getName(), getColumnName(), getJavaType(), isPrimaryKey());
		}

		return property;
	}
}
