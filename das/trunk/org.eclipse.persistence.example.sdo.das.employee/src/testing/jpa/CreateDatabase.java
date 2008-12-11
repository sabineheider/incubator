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
 *     dclarke - JPA DAS INCUBATOR - Enhancement 258057
 *     			 http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing.jpa;

import static org.eclipse.persistence.config.PersistenceUnitProperties.DDL_GENERATION;
import static org.eclipse.persistence.config.PersistenceUnitProperties.DROP_AND_CREATE;

import java.util.Map;

import javax.persistence.EntityManager;


import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.Test;

/**
 * Utility class to create the database schema and populate it for the Employee
 * JPA example using XML configuration. This
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class CreateDatabase extends EclipseLinkJPATest {
	
	@Test
	public void createDatabase() {
		EntityManager em = getEntityManager();

		new SchemaManager(JpaHelper.getEntityManager(em).getServerSession()).replaceSequences();

		em.getTransaction().begin();

		SamplePopulation.population.persistAll(em);

		em.getTransaction().commit();
	}

	@Override
	protected Map getEMFProperties() {
		Map properties = super.getEMFProperties();
		properties.put(DDL_GENERATION, DROP_AND_CREATE);
		return properties;
	}
}
