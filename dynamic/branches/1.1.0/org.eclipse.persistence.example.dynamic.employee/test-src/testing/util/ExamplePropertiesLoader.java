package testing.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class ExamplePropertiesLoader {

	public static void loadProperties(Map properties) {
		loadProperties(properties, "./eclipselink-example.proerties");
		System.getProperty("");
		loadProperties(properties, "$HOME/eclipselink-example.proerties");
	}

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
			// TODO
		}

		properties.putAll(System.getProperties());
	}
}
