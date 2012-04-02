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
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.jpa.CMP3Policy;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.rs.PersistenceContext;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.server.Server;


/**
 * EclipseLink helper class used for converting composite key values passed into
 * JAX-RS calls as query or matrix parameters into a value that can be used in a
 * find.
 * 
 * @author dclarke
 * @since EclipseLink 2.4.0
 */
public class IdHelper {

    public static Object buildId(PersistenceContext app, String entityName, String idString) {
        Server session = JpaHelper.getServerSession(app.getEmf());
        ClassDescriptor descriptor = null;

        descriptor = app.getDescriptor(entityName);
        List<DatabaseMapping> pkMappings = descriptor.getObjectBuilder().getPrimaryKeyMappings();
        List<SortableKey> pkIndices = new ArrayList<SortableKey>();
        int index = 0;
        for (DatabaseMapping mapping: pkMappings){
            pkIndices.add(new SortableKey(mapping, index));
            index++;
        }
        Collections.sort(pkIndices);
        
        // Handle composite key in map
        int[] elementIndex = new int[pkMappings.size()];
        Object[] keyElements = new Object[pkMappings.size()];
       /* for (int index = 0; index < pkMappings.size(); index++) {
            DatabaseMapping mapping = pkMappings.get(index);
            elementIndex[index] = index;
            List<String> idValues = multivaluedMap.get(mapping.getAttributeName());
            if (idValues == null || idValues.isEmpty() || idValues.size() != 1) {
                throw new WebApplicationException(new RuntimeException("Missing or duplicate id values named: " + mapping.getAttributeName()), Status.BAD_REQUEST);
            }
            Object idValue = idValues.get(0);
            idValue = session.getPlatform().getConversionManager().convertObject(idValue, mapping.getAttributeClassification());
            keyElements[index] = idValue;

        }*/
        StringTokenizer tokenizer = new StringTokenizer(idString, "+");
        int tokens = tokenizer.countTokens();
        if (tokens != pkMappings.size()){
            throw new RuntimeException("Failed, incorrect number of keys values");
        }
        index = 0;
        Iterator<SortableKey> iterator = pkIndices.iterator();
        while (tokenizer.hasMoreTokens()){
            SortableKey key = iterator.next();
            Object idValue = session.getPlatform().getConversionManager().convertObject(tokenizer.nextToken(), key.getMapping().getAttributeClassification());
            keyElements[key.getIndex()] = idValue;
            index++;
        }

        if (descriptor.hasCMPPolicy()) {
            CMP3Policy policy = (CMP3Policy) descriptor.getCMPPolicy();
            return policy.createPrimaryKeyInstanceFromPrimaryKeyValues((AbstractSession) session, elementIndex, keyElements);
        }

        if (keyElements.length == 1) {
            return keyElements[0];
        }
        return keyElements;
    }
    
    private static class SortableKey implements Comparable<SortableKey>{
        
        private DatabaseMapping mapping;
        private int index;
        
        public SortableKey(DatabaseMapping mapping, int index){
            this.mapping = mapping;
            this.index = index;
        }
        
        public int compareTo(SortableKey o){
            return mapping.getAttributeName().compareTo(o.getMapping().getAttributeName());
        }
        
        public DatabaseMapping getMapping(){
            return mapping;
        }
        
        public int getIndex(){
            return index;
        }

    }
}
