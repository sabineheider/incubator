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
 *     dclarke - Dynamic Persistence INCUBATION - Enhancement 200045
 *               http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.jpa.dynamic;

import org.eclipse.persistence.dynamic.RelationalMappingFactory;
import org.eclipse.persistence.internal.jpa.CMP3Policy;
import org.eclipse.persistence.sessions.DatabaseSession;

public class JPAMappingFactory extends RelationalMappingFactory {

    public JPAMappingFactory(DatabaseSession session, String className, String... tableNames) {
        super(session, className, tableNames);
    }

    public JPAMappingFactory(Class dynamicClass, String[] tableNames) {
        super(dynamicClass, tableNames);
        
        this.entityType.getDescriptor().setCMPPolicy(new CMP3Policy());
    }

}
