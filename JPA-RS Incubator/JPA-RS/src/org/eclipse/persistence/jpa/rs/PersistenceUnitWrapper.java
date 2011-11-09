/******************************************************************************
 * ORACLE CONFIDENTIAL
 * Copyright (c) 2011 Oracle. All rights reserved.
 *
 * Contributors:
 * 		 - 
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs;

import static org.eclipse.persistence.jaxb.JAXBContext.MEDIA_TYPE;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPADynamicHelper;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.server.Server;

/**
 * A running application within the environment based on an
 * {@link ApplicationDefinition}.
 * 
 * @author douglas.clarke
 * @since Avatar POC - September 2011
 */
public class PersistenceUnitWrapper {

    private EntityManagerFactory emf;

    private JAXBContext context;


    protected PersistenceUnitWrapper(String persistenceUnitName) {
        this.emf = Persistence.createEntityManagerFactory(persistenceUnitName);
        try {
            this.context = createJaxbContext(persistenceUnitName, emf);
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private JAXBContext createJaxbContext(String persistenceUnitName, EntityManagerFactory emf) throws JAXBException {
        String oxmLocation = (String) emf.getProperties().get("eclipselink.jpa-rs.oxm");
        Server serverSession = JpaHelper.getServerSession(emf);
        
        if (oxmLocation == null) {
            return PersistenceFactory.createDynamicJAXBContext(persistenceUnitName,serverSession);
        }
        return PersistenceFactory.createJAXBContext(persistenceUnitName, serverSession, oxmLocation);
    }

    public String getName() {
        return JpaHelper.getServerSession(getEmf()).getName();
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

    public DynamicEntity merge(String tenantId, DynamicEntity entity) {
        EntityManager em = getEmf().createEntityManager();

        try {
            em.getTransaction().begin();
            DynamicEntity mergedEntity = em.merge(entity);
            em.getTransaction().commit();
            return mergedEntity;
        } finally {
            em.close();
        }
    }

    /**
     * TODO
     */
    public DynamicEntity newEntity(String type) {
        return newEntity(null, type);
    }

    public DynamicEntity newEntity(String tenantId, String type) {
        JPADynamicHelper helper = new JPADynamicHelper(getEmf());
        return helper.newDynamicEntity(type);
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

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    /**
     * Stop the current application instance
     */
    protected void stop() {
        PersistenceFactory.closeDynamicEntityManagerFactory(getEmf());
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

}
