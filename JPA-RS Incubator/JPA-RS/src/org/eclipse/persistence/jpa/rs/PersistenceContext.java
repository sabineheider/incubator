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
 * 		dclarke/tware - initial 
 *      tware
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs;

import static org.eclipse.persistence.jaxb.JAXBContext.MEDIA_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.ws.rs.core.MediaType;
import org.eclipse.persistence.jaxb.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;
import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.eclipse.persistence.jpa.Archive;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.eclipse.persistence.jpa.dynamic.JPADynamicHelper;
import org.eclipse.persistence.jpa.rs.util.CustomSerializationMetadataSource;
import org.eclipse.persistence.jpa.rs.util.DynamicXMLMetadataSource;
import org.eclipse.persistence.jpa.rs.util.InMemoryArchive;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.sessions.server.ServerSession;

import org.eclipse.persistence.jpa.rs.util.ChangeListener;
import org.eclipse.persistence.jpa.rs.qcn.JPARSChangeNotificationListener;
import org.eclipse.persistence.jpa.rs.PersistenceFactory;



/**
 * A wrapper around the JPA and JAXB artifacts used to persist an application
 * 
 * @author douglas.clarke, tom.ware
 */
public class PersistenceContext {
    
    private static final String PACKAGE_ROOT = "jpars.app.";
    private static final String MODEL_PACKAGE = ".model";
    
    private String name = null;
    
    private EntityManagerFactory emf;
    
    private JAXBContext context = null;

    public PersistenceContext(Archive archive, Map<String, ?> properties, ClassLoader classLoader){
        super();
        List<SEPersistenceUnitInfo> persistenceUnits = PersistenceUnitProcessor.getPersistenceUnits(archive, classLoader);
        SEPersistenceUnitInfo persistenceUnitInfo = persistenceUnits.get(0);
        
        this.name = persistenceUnitInfo.getPersistenceUnitName();
        

        EntityManagerFactoryImpl emf = createDynamicEMF(persistenceUnitInfo, properties);
        this.emf = emf;
       
        try{
            JAXBContext jaxbContext = createJAXBContext(persistenceUnitInfo.getPersistenceUnitName(), emf.getServerSession());
           this.context = jaxbContext;
        } catch (Exception e){
            e.printStackTrace();
            emf.close();
            throw new RuntimeException("JAXB Creation Exception", e);
        }
    }
    
    protected EntityManagerFactoryImpl createDynamicEMF(PersistenceUnitInfo info, Map<String, ?> properties){
        PersistenceProvider provider = new PersistenceProvider();
        EntityManagerFactory emf = provider.createContainerEntityManagerFactory(info, properties);
        PersistenceContext.subscribeToEventNotification(emf);
        return (EntityManagerFactoryImpl)emf;
    }
    
    /**
     * @param session
     * @return
     */
    protected JAXBContext createJAXBContext(String persistenceUnitName, Server session) throws JAXBException, IOException {
        JAXBContext jaxbContext = (JAXBContext) session.getProperty(JAXBContext.class.getName());
        if (jaxbContext != null) {
            return jaxbContext;
        }
        String oxmLocation = (String) emf.getProperties().get("eclipselink.jpa-rs.oxm");
        
        String packageName = PACKAGE_ROOT + persistenceUnitName + MODEL_PACKAGE;
        Map<String, Object> properties = new HashMap<String, Object>(1);
        Object metadataLocation = null;
        if (oxmLocation != null){
            metadataLocation = new InMemoryArchive((new URL(oxmLocation)).openStream());
        } else {
            metadataLocation = new DynamicXMLMetadataSource(persistenceUnitName, session, packageName);
        }
        List<Object> metadataLocations = new ArrayList<Object>();
        metadataLocations.add(metadataLocation);
        metadataLocations.add(new CustomSerializationMetadataSource(persistenceUnitName, session, packageName));
        properties.put(JAXBContextFactory.ECLIPSELINK_OXM_XML_KEY, metadataLocations);

        ClassLoader cl = session.getPlatform().getConversionManager().getLoader();
        jaxbContext = DynamicJAXBContextFactory.createContextFromOXM(cl, properties);

        session.setProperty(JAXBContext.class.getName(), jaxbContext);

        return jaxbContext;
    }
    
    public String getName() {
        return name;
    }

    public EntityManagerFactory getEmf() {
        return emf;
    }

    public JAXBContext getJAXBContext() {
        return context;
    }

    public void create(String tenantId, DynamicEntity entity) {
        EntityManager em = getEmf().createEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public DynamicEntity merge(String type, String tenantId, DynamicEntity entity) {
        EntityManager em = getEmf().createEntityManager();
        DynamicEntity mergedEntity = null;
        try {
            em.getTransaction().begin();
            
            if (isList(type)){
                mergedEntity = newEntity(type);
                List<Object> returnValues = new ArrayList<Object>();
                mergedEntity.set("serializedData", returnValues);
                @SuppressWarnings("unchecked")
                List<Object> values = (List<Object>)entity.get("serializedData");
                for (Object value: values){
                    Object merged = em.merge(value);
                    returnValues.add(merged);
                }
            } else {
                mergedEntity = em.merge(entity);
            }
            em.getTransaction().commit();
            return mergedEntity;
        } finally {
            em.close();
        }
    }

    public boolean isList(String type){
        Server session = JpaHelper.getServerSession(getEmf());
        ClassDescriptor descriptor = session.getDescriptorForAlias(type);
        if (descriptor == null && getDescriptor(type) != null){
            return true;
        }
        return false;
    }
    
    /**
     * TODO
     */
    public DynamicEntity newEntity(String type) {
        return newEntity(null, type);
    }

    public DynamicEntity newEntity(String tenantId, String type) {
        JPADynamicHelper helper = new JPADynamicHelper(getEmf());
        DynamicEntity entity = null;
        try{
            entity = helper.newDynamicEntity(type);
        } catch (IllegalArgumentException e){
            ClassDescriptor descriptor = getDescriptor(type);
            if (descriptor != null){
                DynamicType jaxbType = (DynamicType) descriptor.getProperty(DynamicType.DESCRIPTOR_PROPERTY);     
                if (jaxbType != null){
                    return jaxbType.newDynamicEntity();
                }
            }
            throw e;
        }
        return entity;
    }

    /**
     * TODO
     */
    public void save(String tenantId, DynamicEntity entity) {
        EntityManager em = getEmf().createEntityManager();

        try {
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    /**
     * TODO
     */
    public void delete(String tenantId, String type, Object id) {
        EntityManager em = getEmf().createEntityManager();

        try {
            em.getTransaction().begin();
            DynamicEntity entity = (DynamicEntity)em.find(getClass(type), id);
            em.remove(entity);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    /**
     * Stop the current application instance
     */
    protected void stop() {
        emf.close();
        this.emf = null;
        this.context = null;
    }

    public DynamicEntity find(String entityName, Object id) {
        return find(null, entityName, id);
    }

    public DynamicEntity find(String tenantId, String entityName, Object id) {
        EntityManager em = getEmf().createEntityManager();

        try {
            return (DynamicEntity) em.find(getClass(entityName), id);
        } finally {
            em.close();
        }
    }

    public String toString() {
        return "Application(" + getName() + ")::" + System.identityHashCode(this);
    }

    public ClassDescriptor getDescriptor(String entityName){
        Server session = JpaHelper.getServerSession(getEmf());
        ClassDescriptor descriptor = session.getDescriptorForAlias(entityName);
        if (descriptor == null){
            for (Object ajaxBSession:((JAXBContext)getJAXBContext()).getXMLContext().getSessions() ){
                descriptor = ((Session)ajaxBSession).getClassDescriptorForAlias(entityName);
                if (descriptor != null){
                    break;
                }
            }
        }
        return descriptor;
    }
    
    public Class<?> getClass(String entityName) {
        return getDescriptor(entityName).getJavaClass();
    }

    public Object query(String name, Map<?, ?> parameters) {
        return query(name, parameters, false);
    }
    
    @SuppressWarnings("rawtypes")
    public Object query(String name, Map<?, ?> parameters, boolean returnSingleResult) {
        EntityManager em = getEmf().createEntityManager();
        try{
            Query query = em.createNamedQuery(name);
            DatabaseQuery dbQuery = ((EJBQueryImpl<?>)query).getDatabaseQuery();
            if (parameters != null){
                Iterator i=parameters.keySet().iterator();
                while (i.hasNext()){
                    String key = (String)i.next();
                    Class parameterClass = null;
                    int index = dbQuery.getArguments().indexOf(key);
                    if (index >= 0){
                        parameterClass = dbQuery.getArgumentTypes().get(index);
                    }
                    Object parameter = parameters.get(key);
                    if (parameterClass != null){
                        parameter = ConversionManager.getDefaultManager().convertObject(parameter, parameterClass);
                    }
                    query.setParameter(key, parameter);
                }
            }
            if (returnSingleResult){
                return query.getSingleResult();
            } else {
                return query.getResultList();
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            em.close();
        }
        return null;
    }
    
    public DynamicEntity unmarshalEntity(String entityName, String tenantId, String acceptedMedia, InputStream in) throws JAXBException {
        Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
        if (acceptedMedia == null || acceptedMedia.indexOf(MediaType.APPLICATION_JSON) < 0) {
            unmarshaller.setProperty(MEDIA_TYPE, MediaType.APPLICATION_JSON);
            // TODO - handle type
        } else {
            unmarshaller.setProperty(MEDIA_TYPE, MediaType.APPLICATION_XML);
        }
        JAXBElement<?> element = unmarshaller.unmarshal(new StreamSource(in), getClass(entityName));
		return (DynamicEntity) element.getValue();
    }

    protected EntityManager createEntityManager(String tenantId) {
        return getEmf().createEntityManager();
    }
    
    /**
     * TODO
     */
    public void addListener(ChangeListener listener) {
        JPARSChangeNotificationListener changeListener = (JPARSChangeNotificationListener) JpaHelper.getDatabaseSession(getEmf()).getProperty(PersistenceFactory.CHANGE_NOTIFICATION_LISTENER);
        if (changeListener == null) {
            throw new RuntimeException("Change Listener not registered properly");
        }
        changeListener.addChangeListener(listener);
    }

    /**
     * TODO
     */
    public void remove(ChangeListener listener) {
        JPARSChangeNotificationListener changeListener = (JPARSChangeNotificationListener) JpaHelper.getDatabaseSession(getEmf()).getProperty(PersistenceFactory.CHANGE_NOTIFICATION_LISTENER);
        if (changeListener != null) {
            changeListener.removeChangeListener(listener);
        }
    }
    
    public static JPARSChangeNotificationListener subscribeToEventNotification(EntityManagerFactory emf) {
        ServerSession session = (ServerSession) JpaHelper.getServerSession(emf);
        Iterator<ClassDescriptor> i = session.getDescriptors().values().iterator();
        JPARSChangeNotificationListener listener = new JPARSChangeNotificationListener();
        session.setDatabaseEventListener(listener);
        while (i.hasNext()) {
            ClassDescriptor descriptor = i.next();
            listener.initialize(descriptor, session);
        }
        listener.register(session);
        session.setProperty(PersistenceFactory.CHANGE_NOTIFICATION_LISTENER, listener);
        return listener;
    }

}
