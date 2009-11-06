/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *      dclarke - introduced during 1.2.0 example updates
 ******************************************************************************/
package testing.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class that will load properties 
 * 
 * @author dclarke
 * @since EclipseLink 1.2.0
 */
public class ExamplePropertiesLoader {

	public static void loadProperties(Map<String, Object> properties) {
		loadProperties(properties, "./eclipselink-example.properties");
		loadProperties(properties, "$HOME/eclipselink-example.properties");
	}

	@SuppressWarnings("unchecked")
    public static void loadProperties(Map properties, String filePath) {
		try {
			File examplePropertiesFile = new File(filePath);
			if (examplePropertiesFile.exists()) {
				Properties exampleProps = new Properties();
				InputStream in = new FileInputStream(examplePropertiesFile);
				exampleProps.load(in);
				in.close();
				properties.putAll(exampleProps);
			}
		} catch (Exception e) {
			throw new RuntimeException("ExamplePropertiesLoader failure loading properties: " + filePath, e);
		}

		properties.putAll(System.getProperties());
	}
}
