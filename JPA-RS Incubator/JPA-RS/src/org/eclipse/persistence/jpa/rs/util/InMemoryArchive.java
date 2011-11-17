package org.eclipse.persistence.jpa.rs.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.URLArchive;

/**
 * This archive is designed for use with dynamic persistence units
 * it is built with a stream that allows it to read a persistence.xml file and creates a fake base URL
 * based the classpath location of the InMemoryArchiveClass
 * @author tware
 *
 */
public class InMemoryArchive extends URLArchive {

    private InputStream stream = null;
    
    private InMemoryArchive(){
        super(null, null);
        String persistenceFactoryResource = InMemoryArchive.class.getName().replace('.', '/') + ".class";
        URL myURL = InMemoryArchive.class.getClassLoader().getResource(persistenceFactoryResource);
        try{
            myURL = PersistenceUnitProcessor.computePURootURL(myURL, persistenceFactoryResource);
        } catch (URISyntaxException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }  
        this.rootURL = myURL;
    }
    
    public InMemoryArchive(InputStream stream){
        this();
        this.stream = stream;
    }

    @Override
    public InputStream getDescriptorStream() throws IOException {
        return stream;
    }

    @Override
    public void close() {
        super.close();
        try{
            stream.close();
        } catch (IOException e){};
        stream = null;

    }

}
