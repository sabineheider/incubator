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
package org.eclipse.persistence.testing.tests.dynamic;

import static junit.framework.Assert.*;

import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.exceptions.IntegrityException;
import org.eclipse.persistence.internal.dynamic.*;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.mappings.foundation.AbstractDirectMapping;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.Test;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class EntityTypeFromDescriptor {

	@Test
	public void entityTypeFromDescriptor() throws Exception {
		EntityTypeImpl entityType = new EntityTypeImpl(buildMyEntityDescriptor());

		assertEquals(MyEntity.class, entityType.getJavaClass());

		DatabaseSession session = new Project(buildDatabaseLogin()).createDatabaseSession();
		session.getSessionLog().setLevel(SessionLog.FINE);
		session.login();

		session.addDescriptor(entityType.getDescriptor());
		new SchemaManager(session).replaceDefaultTables();
		
		DynamicEntity entity = entityType.newInstance();
		entity.set("id", 1);
		entity.set("name", "Name");
		
		session.insertObject(entity);

		session.logout();

	}

	/**
	 * Verify that the descriptor for a dynamic type fails without the
	 * additional configuration which is applied to the descriptor during the
	 * EntityType creation.
	 */
	@Test
	public void invalidDescriptorWithoutEntityType() throws Exception {
		RelationalDescriptor descriptor = buildMyEntityDescriptor();

		DatabaseSession session = new Project(buildDatabaseLogin()).createDatabaseSession();
		session.getSessionLog().setLevel(SessionLog.FINE);
		session.addDescriptor(descriptor);

		try {
			session.login();
		} catch (IntegrityException ie) {
			assertEquals(descriptor.getMappings().size() + 1, ie.getIntegrityChecker().getCaughtExceptions().size());
			
			// Verify NoSuchField errors for each mapping
			for (int index = 0 ; index < descriptor.getMappings().size(); index++) {
				DescriptorException ex = (DescriptorException) ie.getIntegrityChecker().getCaughtExceptions().get(index);
				assertEquals(DescriptorException.NO_SUCH_FIELD_WHILE_INITIALIZING_ATTRIBUTES_IN_INSTANCE_VARIABLE_ACCESSOR, ex.getErrorCode());
			}
			DescriptorException de = (DescriptorException) ie.getIntegrityChecker().getCaughtExceptions().lastElement();
			assertEquals(DescriptorException.NO_SUCH_METHOD_WHILE_INITIALIZING_INSTANTIATION_POLICY, de.getErrorCode());
			
			return;
		}
		
		fail("Expected IntegrityException not thrown");
	}

	private RelationalDescriptor buildMyEntityDescriptor() {
		RelationalDescriptor descriptor = new RelationalDescriptor();

		descriptor.setJavaClass(MyEntity.class);
		descriptor.setTableName("MY_ENTITY");
		descriptor.addPrimaryKeyFieldName("ID");

		AbstractDirectMapping mapping = (AbstractDirectMapping) descriptor.addDirectMapping("id", "ID");
		mapping.setAttributeClassification(int.class);
		mapping = (AbstractDirectMapping) descriptor.addDirectMapping("name", "NAME");
		mapping.setAttributeClassification(String.class);

		return descriptor;
	}

	/**
	 * Return
	 */
	private DatabaseLogin buildDatabaseLogin() {
		DatabaseLogin login = new DatabaseLogin();

		login.useOracleThinJDBCDriver();
		login.setDatabaseURL("localhost:1521:ORCL");
		login.setUserName("scott");
		login.setPassword("tiger");

		// TODO - override with values from system properties
		
		return login;
	}

	/**
	 * Simple concrete subclass of DynamicEntityImpl to test the functionality
	 * of EntityType independently of the {@link DynamicClassLoader}
	 * functionality which typically generates subclasses.
	 */
	public static class MyEntity extends DynamicEntityImpl {

		protected MyEntity(EntityTypeImpl type) {
			super(type);
		}

	}
}
