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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryProvider;
import org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl;
import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.rs.util.DynamicXMLMetadataSource;
import org.eclipse.persistence.jpa.rs.util.InMemoryArchive;
import org.eclipse.persistence.jpa.rs.util.XMLDatabaseMetadataSource;
import org.eclipse.persistence.sessions.server.Server;

/**
 * Factory for the creation of persistence contexts (JPA and JAXB). These
 * contexts are for the persistence of both the meta-model as well as the
 * dynamic persistence contexts for the hosted applications.
 * 
 * @author douglas.clarke
 * @since Avatar POC - September 2011
 */
public class PersistenceFactory {

    private static final String PACKAGE_ROOT = "avatar.app.";
    private static final String MODEL_PACKAGE = ".model";

    public static final String CHANGE_NOTIFICATION_LISTENER = "avatar.change-notification-listener";

    public static EntityManagerFactory createDynamicEMF(String persistenceUnitName, Map<String, ?> originalProperties) {
        String sessionName = "AVATAR-" + persistenceUnitName;
        EntityManagerFactoryImpl emfImpl = null;

        DynamicClassLoader dcl = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
        Map<String, Object> properties = createProperties(dcl, sessionName, originalProperties);
        properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_ONLY);
        properties.put(PersistenceUnitProperties.METADATA_SOURCE, XMLDatabaseMetadataSource.class.getName());

        InMemoryArchive archive = new InMemoryArchive(null); // TODO
        List<SEPersistenceUnitInfo> persistenceUnits = PersistenceUnitProcessor.getPersistenceUnits(archive, dcl);
        SEPersistenceUnitInfo persistenceUnitInfo = persistenceUnits.get(0);
        EntityManagerSetupImpl emSetupImpl = null;
        synchronized (EntityManagerFactoryProvider.emSetupImpls) {
            emSetupImpl = EntityManagerFactoryProvider.getEntityManagerSetupImpl(sessionName);
            if (emSetupImpl == null) {
                emSetupImpl = new EntityManagerSetupImpl(sessionName, sessionName);
                emSetupImpl.predeploy(persistenceUnitInfo, properties);
                EntityManagerFactoryProvider.addEntityManagerSetupImpl(sessionName, emSetupImpl);
            }
        }
        emfImpl = new EntityManagerFactoryImpl(emSetupImpl, properties);
        return emfImpl;
    }

    public static void closeDynamicEntityManagerFactory(EntityManagerFactory emf) {
        Server session = JpaHelper.getServerSession(emf);
        emf.close();
        if (session.isConnected()) {
            session.logout();
        }
    }

    private static Map<String, Object> createProperties(DynamicClassLoader dcl, String sessionName, Map<String, ?> originalProperties) {
        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put(PersistenceUnitProperties.CLASSLOADER, dcl);
        properties.put(PersistenceUnitProperties.WEAVING, "static");
        // properties.put("eclipselink.ddl-generation",
        // "drop-and-create-tables");
        // properties.put("eclipselink.ddl-generation.output-mode", "database");
        properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
        properties.put(PersistenceUnitProperties.SESSION_NAME, sessionName);

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

    /**
     * TODO
     * 
     * @param session
     * @return
     */
    public static JAXBContext createDynamicJAXBContext(String persistenceUnitName, Server session) throws JAXBException {
        JAXBContext jaxbContext = (JAXBContext) session.getProperty(JAXBContext.class.getName());
        if (jaxbContext != null) {
            return jaxbContext;
        }

        String packageName = PACKAGE_ROOT + persistenceUnitName + MODEL_PACKAGE;
        Map<String, Object> properties = new HashMap<String, Object>(1);
        properties.put(JAXBContextFactory.ECLIPSELINK_OXM_XML_KEY, new DynamicXMLMetadataSource(session, packageName));

        ClassLoader cl = session.getPlatform().getConversionManager().getLoader();
        jaxbContext = DynamicJAXBContextFactory.createContextFromOXM(cl, properties);
        session.setProperty(JAXBContext.class.getName(), jaxbContext);

        return jaxbContext;
    }

    /**
     * TODO
     * 
     * @param session
     * @return
     */
    public static JAXBContext createJAXBContext(String persistenceUnitName, Server session, String oxmLocation) throws JAXBException {
        JAXBContext jaxbContext = (JAXBContext) session.getProperty(JAXBContext.class.getName());
        if (jaxbContext != null) {
            return jaxbContext;
        }

        String packageName = PACKAGE_ROOT + persistenceUnitName + MODEL_PACKAGE;
        Map<String, Object> properties = new HashMap<String, Object>(1);
        properties.put(JAXBContextFactory.ECLIPSELINK_OXM_XML_KEY, new InMemoryArchive(oxmLocation));

        ClassLoader cl = session.getPlatform().getConversionManager().getLoader();
        jaxbContext = DynamicJAXBContextFactory.createContextFromOXM(cl, properties);
        session.setProperty(JAXBContext.class.getName(), jaxbContext);

        return jaxbContext;
    }

}
