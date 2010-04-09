package org.eclipse.persistence.internal.jpa.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class BundleJarUrlArchive extends JarInputStreamURLArchive {

    String descriptorPath;
    
    public BundleJarUrlArchive(URL jarUrl, String descPath) throws IOException {
        super(jarUrl);
        descriptorPath = descPath;
    }
    
    public InputStream getDescriptorStream() {
        InputStream result = null;
        try {
            result = getEntry(descriptorPath);
        } catch (IOException ioEx) {} // result is just null
        return result;
    }

    public URL getDescriptorURL() {
        return null;
    }
    
    public URL getJarUrl() {
        // The JAR url is what the superclass thinks is the root URL
        return super.getRootURL();
    }
    
    public URL getRootURL() {
        // The root url is just the root of the bundle
        try {
            URL rootUrl = new URL(getJarUrl().getProtocol()+ "://" + getJarUrl().getAuthority());
            return rootUrl;
        } catch (IOException ioEx) {
            throw new RuntimeException("Error trying to create Root URL in BundleJarArchive: " + ioEx);
        }
    }
}
