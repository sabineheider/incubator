package org.eclipse.persistence.jpa.rs.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.URLArchive;
import org.eclipse.persistence.jpa.rs.PersistenceFactory;

public class InMemoryArchive extends URLArchive {

    private String persistencexml;
    private InputStream stream = null;
    
    public InMemoryArchive(String persistencexml){
        super(null, null);
        String persistenceFactoryResource = InMemoryArchive.class.getName().replace('.', '/') + ".class";
        URL myURL = PersistenceFactory.class.getClassLoader().getResource(persistenceFactoryResource);
        try{
            myURL = PersistenceUnitProcessor.computePURootURL(myURL, persistenceFactoryResource);
        } catch (URISyntaxException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }  
        this.persistencexml = persistencexml;
        this.rootURL = myURL;
    }

    @Override
    public InputStream getDescriptorStream() throws IOException {
        
        stream = new ByteArrayInputStream(persistencexml.getBytes());
        return stream;
    }

    @Override
    public void close() {
        super.close();
        try{
            stream.close();
        } catch (IOException e){};

    }

}
