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

import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.descriptors.*;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.sessions.SessionEvent;

/**
 * Example of how a session event listener can be used to create new types and
 * load them at system startup.
 * 
 * @author dclarke
 * @since EclipseLink 1.1 - November 2008
 */
public class TypeDefinitionListener extends DescriptorEventAdapter implements DescriptorCustomizer {

	/**
	 * After login all TypeDefinitions from the database are loaded into the
	 * cache
	 */
	public void postBuild(SessionEvent event) {
	}

	/**
	 * After a new TypeDefinition is merged into the shared cache it must be
	 * populated with an underlying EntityType and if necessary the
	 * corresponding database table must be created.
	 */
	@Override
	public void postMerge(DescriptorEvent event) {
		TypeDefinition original = (TypeDefinition) event.getOriginalObject();
		TypeDefinition workingCopy = (TypeDefinition) event.getSource();
		EntityType entityType = original.getEntityType();

		if (entityType == null) {
			entityType = original.createEntityType(event.getSession());
			original.createOnDatabase(event.getSession());
		}
		workingCopy.initialize(entityType);
	}

	public void customize(ClassDescriptor descriptor) throws Exception {
		descriptor.getDescriptorEventManager().addListener(this);
	}

}
