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

    private String persistencexml = null;
    private URL persistenceXMLURL = null;
    private InputStream stream = null;
    
    private InMemoryArchive(){
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
        this.rootURL = myURL;
    }
    
    public InMemoryArchive(String persistencexml){
    	this();
        this.persistencexml = persistencexml;
    }
    
    public InMemoryArchive(URL persistencexmlurl){
    	this();
        this.persistenceXMLURL = persistencexmlurl;
    }
    
    public InMemoryArchive(InputStream stream){
        this();
        this.stream = stream;
    }

    @Override
    public InputStream getDescriptorStream() throws IOException {
        if (stream == null){
            if (persistencexml == null){
            	stream = persistenceXMLURL.openStream();
            } else {
            	stream = new ByteArrayInputStream(persistencexml.getBytes());
            }
        }
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
