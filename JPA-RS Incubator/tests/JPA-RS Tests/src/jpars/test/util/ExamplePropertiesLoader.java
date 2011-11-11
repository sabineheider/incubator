/******************************************************************************
 * ORACLE CONFIDENTIAL
 * Copyright (c) 2011 Oracle. All rights reserved.
 *
 * Contributors:
 * 		 - 
 ******************************************************************************/
package jpars.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Helper class that will load persistence unit overrides from a properties file
 * in both the current running folder and the current user's home folder. The
 * goal is to enable developers and users of the example to customize its
 * behaviour without having to modify the source of the example.
 */
public class ExamplePropertiesLoader {

    public static final String DEFAULT_FILENAME = "jpars.properties";

    /**
     * 
     * @param properties
     */
    public static void loadProperties(Map<String, Object> properties) {
        loadProperties(properties, DEFAULT_FILENAME);
    }

    /**
     * 
     * @param properties
     */
    public static void loadProperties(Map<String, Object> properties, String filename) {
        loadProperties(properties, new File(filename));

        String home = System.getProperty("user.home");
        loadProperties(properties, new File(home + System.getProperty("file.separator") + filename));

        for (Object key : System.getProperties().keySet()) {
            String keyName = (String) key;

            if (keyName.startsWith("javax.persistence") || keyName.startsWith("eclipselink")) {
                String value = System.getProperty(keyName);
                properties.put(keyName, value);
            }
        }
    }

    /**
     * 
     * @param properties
     * @param filePath
     */
    public static void loadProperties(Map<String, Object> properties, File file) {
        try {
            if (file.exists()) {
                Properties exampleProps = new Properties();
                InputStream in = new FileInputStream(file);
                exampleProps.load(in);
                in.close();

                for (Map.Entry<Object, Object> entry : exampleProps.entrySet()) {
                    properties.put((String) entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
