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
 *      bdoughan - initial implementation 
 *      tware - initial unmarshall method
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.internal.queries.EntityFetchGroup;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.rs.PersistenceContext;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.queries.FetchGroup;

/**
 * Used with JAXB to convert from a URL representing an relationship to an object
 * It provides functionality at marshall and unmarshall time
 * 
 * At marshall time, the target of a relationship will be marshalled as a URL that could be
 * used to find the object through a REST service
 * 
 * At unmarsall time, the URL will be deconstructed and used to find the object in JPA.
 * 
 * @author tware
 *
 */
public class LinkAdapter extends XmlAdapter<String, Object> {

    private String baseURI = null;
    protected PersistenceContext context;

    public LinkAdapter() {
    }

    public LinkAdapter(String baseURI, PersistenceContext context) {
        this.baseURI = baseURI;
        this.context = context;
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    // TODO Composite keys
    public Object unmarshal(String v) throws Exception {
        if (v.equals("")){
            return null;
        }
        String fixedString = v.replace("\\/", "/");
        int lastSlash = fixedString.lastIndexOf('/');
        String entityType = fixedString.substring((baseURI + context.getName() + "/entity/" ).length(), lastSlash);
        String entityId = fixedString.substring(lastSlash + 1);
        ClassDescriptor descriptor = context.getDescriptor(entityType);
        DatabaseMapping idMapping = getIdMapping(descriptor);
        String idField = idMapping.getAttributeName();;
        Class idType = idMapping.getAttributeClassification();;
        Iterator<DatabaseMapping> i = descriptor.getMappings().iterator();

        while (i.hasNext()){
            DatabaseMapping mapping = i.next();
            if (mapping.isPrimaryKeyMapping()){
                idField = mapping.getAttributeName();
                idType = mapping.getAttributeClassification();
                break;
            }
        }
        Object id = ConversionManager.getDefaultManager().convertObject(entityId, idType);
        
        return constructObjectForId(entityType, idField, id);
    }

    protected Object constructObjectForId(String entityType, String entityIdField, Object id){
        FetchGroup fetchGroup = new FetchGroup();
        fetchGroup.addAttribute(entityIdField);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(QueryHints.FETCH_GROUP, fetchGroup);
        properties.put(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheOnly);
        DynamicEntityImpl entity = (DynamicEntityImpl)context.find(null, entityType, id, properties);
        if (entity == null){
            entity = (DynamicEntityImpl)context.newEntity(entityType);
            entity.set(entityIdField, id);
            EntityFetchGroup group = new EntityFetchGroup(entityIdField);
            entity._persistence_setFetchGroup(group);
            entity._persistence_setId(id);
            entity._persistence_setSession(JpaHelper.getDatabaseSession(context.getEmf()));
        }
        return entity;
    }
    
    @Override
    public String marshal(Object v) throws Exception {
        if (null == v) {
            return null;
        }
        DynamicEntityImpl de = (DynamicEntityImpl) v;
        DatabaseMapping idMapping = getIdMapping(context.getDescriptor(de.getType().getName()));
        if (idMapping == null){
            return "";
        }
        Object id = de.get(idMapping.getAttributeName());
        String href = baseURI + context.getName() + "/entity/"  + v.getClass().getSimpleName() + "/"
                + id;
        return href;
    }
    
    protected DatabaseMapping getIdMapping(ClassDescriptor descriptor){
        Iterator<DatabaseMapping> i = descriptor.getMappings().iterator();
        while (i.hasNext()){
            DatabaseMapping mapping = i.next();
            if (mapping.isPrimaryKeyMapping()){
                return mapping;
            }
        }
        return null;
    }

}