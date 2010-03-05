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
 *     mkeith - Gemini JPA work (INCUBATION) 
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.osgi;

import java.net.URL;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;
import javax.sql.DataSource;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.jpa.deployment.osgi.BundleProxyClassLoader;
import org.eclipse.persistence.internal.jpa.deployment.osgi.CompositeClassLoader;

import org.eclipse.gemini.jpa.PlainDriverDataSource;
import org.eclipse.gemini.jpa.GeminiUtil;
import org.eclipse.gemini.jpa.PersistenceBundleExtender;
import org.eclipse.gemini.jpa.PersistenceUnitBundleUtil;
import org.eclipse.gemini.jpa.PersistenceServicesUtil;
import org.eclipse.gemini.jpa.PUnitInfo;
import org.eclipse.gemini.jpa.provider.OSGiJpaProvider;


import static org.eclipse.gemini.jpa.GeminiUtil.*;
import static org.osgi.service.jdbc.DataSourceFactory.*;

//TODO Add substitutability of provider

public class EclipseLinkOSGiProvider implements BundleActivator, 
                                                OSGiJpaProvider,
                                                PersistenceProvider {

    /*==================*/
    /* Static constants */
    /*==================*/

    public static final String PROVIDER_CLASS_NAME = "org.eclipse.persistence.jpa.PersistenceProvider";
    public static final String ANCHOR_CLASS_NAME   = "Jpa$Anchor";
    
    public static final int MAX_EVENT_COLLISION_TRIES = 5;
    
    /*================*/
    /* Provider state */
    /*================*/
    
    /** Provider bundle context */
    BundleContext ctx;

    /** Extender code to find and process persistence unit bundles */
    PersistenceBundleExtender extender;

    /** Services utility code */
    PersistenceServicesUtil servicesUtil;
    
    /** Other Bundle-level utility code */
    PersistenceUnitBundleUtil puBundleUtil;

    /** An SPI instance of this provider */
    PersistenceProvider eclipseLinkProvider;
    
    /** Map of p-units we have registered */
    Map<String, PUnitInfo> pUnitsByName;

    /*=====================*/
    /* Activator functions */
    /*=====================*/
    
    public void start(BundleContext context) throws Exception {
        
        debug("EclipseProvider starting...");
        // Initialize our state
        ctx = context;
        pUnitsByName = Collections.synchronizedMap(new HashMap<String, PUnitInfo>());
        extender = new PersistenceBundleExtender(this);
        servicesUtil = new PersistenceServicesUtil(this);
        puBundleUtil = new PersistenceUnitBundleUtil();
        eclipseLinkProvider = new org.eclipse.persistence.jpa.osgi.PersistenceProvider();
        
        // Register as a provider 
        servicesUtil.registerProviderService();

        // Kick the extender to go looking for persistence bundles
        extender.startListening();
        extender.lookForExistingBundles();
        debug("EclipseProvider started");
    }
    
    public void stop(BundleContext context) throws Exception {

        debug("EclipseProvider stopping...");
        // Take the extender offline and unregister ourselves as a provider
        extender.stopListening();
        servicesUtil.unregisterProviderService();
        
        // Unregister all of the persistence units that we have registered
        List<PUnitInfo> pUnits = new ArrayList<PUnitInfo>(); // Need a new copy
        pUnits.addAll(pUnitsByName.values());
        unregisterPersistenceUnits(pUnits);
        pUnitsByName = null;
        
        // Now unassign all of the persistence units that have been assigned to us
        Map<Bundle,List<PUnitInfo>> pUnitInfos = extender.clearAllPUnitInfos();
        for (Map.Entry<Bundle,List<PUnitInfo>> entry : pUnitInfos.entrySet()) {
            unassignPersistenceUnitsInBundle(entry.getKey(), entry.getValue());
        }        
        debug("EclipseProvider stopped");
    }
    
    /*==============================*/
    /* OSGiJpaProvider impl methods */
    /*==============================*/

    // Used to compare against the <provider> element in persistence descriptors
    public String getProviderClassName() { return PROVIDER_CLASS_NAME; }

    // Used to invoke regular JPA createEntityManagerFactory() methods
    public javax.persistence.spi.PersistenceProvider getProviderInstance() { 
        return this;
    }

    // Used when generating the anchor interfaces
    public String getAnchorClassName() { return ANCHOR_CLASS_NAME; }

    public Bundle getBundle() { return ctx.getBundle(); }
    
    public BundleContext getBundleContext() { return ctx; }
    
    /**
     * Assignment happens before resolution. This callback offers the provider a chance to do 
     * anything that must be done before the bundle is resolved.
     * 
     * @param b
     * @param pUnits
     */
    public void assignPersistenceUnitsInBundle(Bundle b, Collection<PUnitInfo> pUnits) {

        debug("EclipseProvider assignPersistenceUnitsInBundle: ", b.getSymbolicName());
        //TODO Check state of bundle in assign call

        // TODO Problem installing fragments in PDE
        // Generate a fragment for the p-units
        // Bundle result = extender.generateAndInstallFragment(b, pUnits);
    }

    /**
     * The persistence bundle is resolved. In this callback the provider 
     * must register the persistence unit services in the registry.
     * 
     * @param pUnits Usually, but not always, all in the same bundle
     */
    public void registerPersistenceUnits(Collection<PUnitInfo> pUnits) {
        
        debug("EclipseProvider registerPersistenceUnits: ", pUnits);
        if (pUnits == null) return;

        for (PUnitInfo info : pUnits) {
            String pUnitName = info.getUnitName();
            int attempts = 0;
            while (pUnitsByName.containsKey(pUnitName) && (attempts < MAX_EVENT_COLLISION_TRIES)) {
                // We hit a race condition due to event ordering 
                // Take a break and give a chance for the unregister to occur
                try { Thread.sleep(1000); } catch (InterruptedException iEx) {}
                attempts++;
            } 
            if (pUnitsByName.containsKey(pUnitName)) {
                // Take matters into our own hands and force the unregister
                debug("EclipseProvider forcing unregistering pUnit: ", info);
                Collection<PUnitInfo> units = new ArrayList<PUnitInfo>();
                units.add(info);
                unregisterPersistenceUnits(units);
            }
            // Keep a local copy of all of the p-units we are registering
            pUnitsByName.put(pUnitName, info); 
            // Do the registering
            servicesUtil.registerEMFServices(info);
        }
    }

    /**
     * In this callback the provider must unregister the persistence unit services 
     * from the registry and clean up any resources.
     * 
     * @param pUnits Usually, but not always, all in the same bundle
     */
    public void unregisterPersistenceUnits(Collection<PUnitInfo> pUnits) {

        debug("EclipseProvider unregisterPersistenceUnits: ", pUnits);
        EntityManagerFactory emf1 = null, 
                             emf2 = null;

        if (pUnits == null) return;
        
        for (PUnitInfo info : pUnits) {
            // TODO re-org tracker to be from provider
            servicesUtil.stopTrackingDataSourceFactory(info);
            emf1 = servicesUtil.unregisterEMFService(info);
            emf2 = servicesUtil.unregisterEMFBuilderService(info);
            if (emf1 != null) {
                emf1.close();
            } else if (emf2 != null) {
                emf2.close();
            }
            // Remove from our local pUnit copy 
            pUnitsByName.remove(info.getUnitName()); 
        }
    }

    public void unassignPersistenceUnitsInBundle(Bundle b, Collection<PUnitInfo> pUnits) {
        
        debug("EclipseProvider unassignPersistenceUnitsInBundle: ", b.getSymbolicName());
        // Make sure we don't have any artifacts associated with the bundle
    }
    
    /*=============================*/
    /* PersistenceProvider methods */
    /*=============================*/
    
    /**
     * Intercept calls to the OSGi EclipseLink JPA provider so we can insert a 
     * classloader property that can be used to find the classes and resources
     */
    
    public EntityManagerFactory createEntityManagerFactory(String emName, Map properties) {
        
        debug("EclipseJPAProvider createEMF invoked for p-unit: ", emName);
        debug("Properties map: ", properties);
        PUnitInfo pUnitInfo = pUnitsByName.get(emName);
        if (pUnitInfo == null)
            fatalError("createEntityManagerFactory() called on provider, but provider has not registered the p-unit " + emName, null);
        Map props = new HashMap();
        props.putAll(properties);
        props.put(PersistenceUnitProperties.CLASSLOADER, compositeLoader(pUnitInfo));
        props.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, acquireDataSource(pUnitInfo, properties));
        props.put(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, descriptorPath(pUnitInfo));
        
        EntityManagerFactory emf = eclipseLinkProvider.createEntityManagerFactory(emName, props);

        // EntityManager em = emf.createEntityManager();
        // Session s = em.unwrap(Session.class);
        // Class c = s.getClassDescriptorForAlias("Account").getJavaClass();
        // debugClassloader(">>> From ELProvider, Account class stored in Descriptor: " + c + " classloader: ", c.getClassLoader());
        return emf;
    }

    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {

        String pUnitName = info.getPersistenceUnitName();
        warning("Container JPA not currently supported for p-unit ", pUnitName);
        
        // Can't hurt to go ahead and try, though...
        PUnitInfo pUnitInfo = pUnitsByName.get(pUnitName);
        if (pUnitInfo == null)
            fatalError("createContainerEntityManagerFactory() called on provider, but provider has not registered the p-unit " + pUnitName, null);
        Map props = new HashMap();
        props.putAll(properties);
        props.put(PersistenceUnitProperties.CLASSLOADER, compositeLoader(pUnitInfo));
        props.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, acquireDataSource(pUnitInfo, properties));
        props.put(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, descriptorPath(pUnitInfo));
        return eclipseLinkProvider.createContainerEntityManagerFactory(info, props);
    }

    public ProviderUtil getProviderUtil() { 
        debug("EclipseJPAProvider getProviderUtil invoked");
        return eclipseLinkProvider.getProviderUtil(); 
    }

    /*================*/
    /* Helper methods */
    /*================*/

    protected ClassLoader compositeLoader(PUnitInfo pUnitInfo) {

        ClassLoader pUnitLoader = new BundleProxyClassLoader(pUnitInfo.getBundle());
        debug("PUnit bundle proxy loader created: ", pUnitLoader);
        ClassLoader providerLoader = new BundleProxyClassLoader(ctx.getBundle());
        debug("Provider bundle proxy loader created: ", providerLoader);
        List<ClassLoader> loaders = new ArrayList<ClassLoader>();
        loaders.add(pUnitLoader);
        loaders.add(providerLoader);
        ClassLoader compositeLoader = new CompositeClassLoader(loaders);
        debug("Composite loader created: ", compositeLoader);
        return compositeLoader;
    }

    protected DataSource acquireDataSource(PUnitInfo pUnitInfo, Map<?,?> properties) {

        ServiceReference[] dsfRefs = null;
        Properties props = getJdbcProperties(pUnitInfo, properties);
        String driverName = (String) props.get(JDBC_DRIVER_CLASS);
        String url = (String) props.get(JDBC_URL);
        
        String filterString = "(" + JDBC_DRIVER_CLASS + "=" + driverName + ")";
        debug("EclipseProvider acquireDataSource - pUnit = ", 
                pUnitInfo.getUnitName(), " filter = ", filterString);
        try {
            dsfRefs = getBundleContext().getServiceReferences(
                                DataSourceFactory.class.getName(), filterString);
        } catch (InvalidSyntaxException isEx) {} // dev time error
        if (dsfRefs == null)
            fatalError("Could not find data source factory in registry: " + driverName, null);

        DataSourceFactory dsf = (DataSourceFactory) getBundleContext().getService(dsfRefs[0]);
        Driver driver = null;
        try {
             driver = dsf.createDriver(props);
        } catch (SQLException sqlEx) {
            fatalError("Could not create data source for " + driverName, sqlEx);
        }
        return new PlainDriverDataSource(driver, props);
    }
    
    protected String descriptorPath(PUnitInfo pUnitInfo) {
        return pUnitInfo.getDescriptorUrl().getPath();
    }
    
    protected Properties getJdbcProperties(PUnitInfo pUnitInfo, Map<?,?> properties) {

        // Try to get the right properties from the ones in the pUnitInfo (XML file)
        // and the ones passed in the Map. This method is probably never going to be right...
        // Assume the Map, passed in at runtime, overrides the XML file
        Properties props = new Properties();
        
        // Get the 4 driver properties, if they exist (driver, url, user, password)
        debug("EclipseProvider - getJDBCProperties - fromMap: ", properties); 
        debug("  fromDescriptor: ", pUnitInfo);

        String driverName = (String)properties.get(GeminiUtil.JPA_JDBC_DRIVER_PROPERTY);
        if (driverName == null)
            driverName = pUnitInfo.getDriverClassName();
        if (driverName != null) 
            props.put(JDBC_DRIVER_CLASS, driverName);

        String url = (String)properties.get(GeminiUtil.JPA_JDBC_URL_PROPERTY);
        if (url == null)
            url = pUnitInfo.getDriverUrl();        
        if (url != null) 
            props.put(JDBC_URL, url);
        
        String user = (String)properties.get(GeminiUtil.JPA_JDBC_USER_PROPERTY);
        if (user == null)
            user = pUnitInfo.getDriverUser();        
        if (user != null) 
            props.put(JDBC_USER, user);

        String pw = (String)properties.get(GeminiUtil.JPA_JDBC_PASSWORD_PROPERTY);
        if (pw == null)
            pw = pUnitInfo.getDriverPassword();        
        if (pw != null) 
            props.put(JDBC_PASSWORD, pw);

        debug("EclipseProvider - getJDBCProperties - returning: ", props);
        return props;
    }
}