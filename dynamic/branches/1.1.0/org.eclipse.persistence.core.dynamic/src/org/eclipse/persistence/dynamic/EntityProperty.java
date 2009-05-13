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
package org.eclipse.persistence.dynamic;

import org.eclipse.persistence.mappings.DatabaseMapping;

/**
 * <b>Purpose</b>: Metadata model for persistent property of a dynamic entity.
 * <p>
 * The property is used within the EntityType metadata model to represent a
 * persistent property wrapping the underlying TopLink mapping. It can be used
 * by an application to access the structure of the dynamic entity model as well
 * as provide more optimal access to data values within an entity.
 * <p>
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public interface EntityProperty {

	public EntityType getType();

	public String getName();

	public Class getAttributeType();

	public DatabaseMapping getMapping();

	public int getIndex();

	public boolean isPrimaryKey();

	public boolean isLazy();

	public boolean isReference();

}
