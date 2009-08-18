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

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.jpa.CMP3Policy;
import org.eclipse.persistence.sessions.DatabaseSession;

public class JPAEntityTypeBuilder extends EntityTypeBuilder {

    public JPAEntityTypeBuilder(DatabaseSession session, String className, EntityType parentType, String... tableNames) {
        super(session, className, parentType, tableNames);
    }

    public JPAEntityTypeBuilder(Class dynamicClass, EntityType parentType,String[] tableNames) {
        super(dynamicClass, parentType, tableNames);

        this.entityType.getDescriptor().setCMPPolicy(new CMP3Policy());
    }
    
    public JPAEntityTypeBuilder(DynamicClassLoader dcl,ClassDescriptor descriptor, EntityType parentType) {
        super(dcl, descriptor, parentType);
    }

    /**
     * Initialize an existing descriptor for dynamic usage.
     * 
     * @param descriptor
     */
    protected void configure(ClassDescriptor descriptor) {
        super.configure(descriptor);

        if (descriptor.getCMPPolicy() == null) {
            descriptor.setCMPPolicy(new DynamicIdentityPolicy());
        }

    }

}
