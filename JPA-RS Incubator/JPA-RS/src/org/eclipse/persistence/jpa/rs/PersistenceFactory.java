/*******************************************************************************
 * Copyright (c) 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 		dclarke - TODO
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Singleton;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.jpa.Archive;
import org.eclipse.persistence.jpa.rs.util.InMemoryArchive;

/**
 * Factory for the creation of persistence contexts (JPA and JAXB). These
 * contexts are for the persistence of both the meta-model as well as the
 * dynamic persistence contexts for the hosted applications.
 * 
 * @author douglas.clarke, tom.ware
 */
@Singleton
public class PersistenceFactory {

    public static final String CHANGE_NOTIFICATION_LISTENER = "jpars.change-notification-listener";
    
	private Map<String, PersistenceContext> persistenceContexts = new HashMap<String, PersistenceContext>();

    public PersistenceContext bootstrapPersistenceContext(String name, String persistenceXML, Map<String, ?> originalProperties){
        ByteArrayInputStream stream = new ByteArrayInputStream(persistenceXML.getBytes());
        InMemoryArchive archive = new InMemoryArchive(stream);
        return bootstrapPersistenceContext(name, archive, originalProperties);
    }
    
    public PersistenceContext bootstrapPersistenceContext(String name, URL persistenceXMLURL, Map<String, ?> originalProperties) throws IOException{
        InMemoryArchive archive = new InMemoryArchive(persistenceXMLURL.openStream());
        return bootstrapPersistenceContext(name, archive, originalProperties);
    }
    
    public PersistenceContext bootstrapPersistenceContext(String name, InputStream persistenceXMLStream, Map<String, ?> originalProperties){
        InMemoryArchive archive = new InMemoryArchive(persistenceXMLStream);
        return bootstrapPersistenceContext(name, archive, originalProperties);
    }
    
    public PersistenceContext getPersistenceContext(String name){
    	return persistenceContexts.get(name);
    }
    
    public void closePersistenceContext(String name){
        PersistenceContext context = persistenceContexts.get(name);
        if (context != null){
            context.getEmf().close();
            persistenceContexts.remove(name);
        }
    }
    
    public void close(){
        for (String key: persistenceContexts.keySet()){
            persistenceContexts.get(key).stop();
        }
    }
    
    public PersistenceContext bootstrapPersistenceContext(String name, Archive archive, Map<String, ?> originalProperties){        	
    	DynamicClassLoader dcl = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
        Map<String, Object> properties = createProperties(dcl, originalProperties);
    	PersistenceContext persistenceContext = new PersistenceContext(archive, properties, dcl);

        persistenceContexts.put(name, persistenceContext);
        return persistenceContext;
    }

    protected static Map<String, Object> createProperties(DynamicClassLoader dcl, Map<String, ?> originalProperties) {
        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put(PersistenceUnitProperties.CLASSLOADER, dcl);
        properties.put(PersistenceUnitProperties.WEAVING, "static");
        properties.put("eclipselink.ddl-generation", "create-tables");
        // properties.put("eclipselink.ddl-generation.output-mode", "database");
        properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINEST");

        // For now we'll copy the connection info from admin PU
        for (Map.Entry<String, ?> entry : originalProperties.entrySet()) {
            if (entry.getKey().startsWith("javax") || entry.getKey().startsWith("eclipselink.log") || entry.getKey().startsWith("eclipselink.target-server")) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
        if (!properties.containsKey(PersistenceUnitProperties.JDBC_DRIVER)) {
            properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, "jdbc/avatar");
        }
        return properties;
    }

}
