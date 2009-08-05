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
 * 		dclarke - initial JPA Employee example using XML (bug 217884)
 ******************************************************************************/
package testing;

import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

/**
 * Utility class to create the database schema scripts to drop and create.
 * 
 * This class can be used in an ANT target as:
 * 
 * 	<target name="geneateDDL">
 *		<java classname="testing.CreateDatabaseScripts">
 *			<classpath refid="example.classpath" />
 *			<arg value="employee" />
 *		</java>
 *	</target>
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class CreateDatabaseScripts {
	
	public static final String PU_NAME = "employee";

	@Test
	public void createSchemaScripts() {
		createSchemaScripts(PU_NAME);
	}
	
	public void createSchemaScripts(String puName) {
		Map properties = new HashMap();
		properties.put(DDL_GENERATION, DROP_AND_CREATE);
		properties.put(DDL_GENERATION_MODE, DDL_SQL_SCRIPT_GENERATION);

		EntityManagerFactory emf = null;
		
		try {
			emf = Persistence.createEntityManagerFactory(puName, properties);
		} finally {
			if (emf != null && emf.isOpen()) {
				emf.close();
			}
		}
	}

	public static void main(String[] args) {
		String puName = PU_NAME;
		
		if (args.length > 0 && args[0].length() > 0) {
			puName = args[0];
		}
		
		new CreateDatabaseScripts().createSchemaScripts(puName);
	}

}
