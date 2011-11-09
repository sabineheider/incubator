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
package org.eclipse.persistence.jpa.rs.util;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.Map;

import org.eclipse.persistence.jpa.metadata.XMLMetadataSource;
import org.eclipse.persistence.logging.SessionLog;

public class XMLDatabaseMetadataSource extends XMLMetadataSource {
    
    public Reader getEntityMappingsReader(Map<String, Object> properties, ClassLoader classLoader, SessionLog log) {
       
        CharArrayReader reader = new CharArrayReader(null);
        return reader;
    }
    
}
