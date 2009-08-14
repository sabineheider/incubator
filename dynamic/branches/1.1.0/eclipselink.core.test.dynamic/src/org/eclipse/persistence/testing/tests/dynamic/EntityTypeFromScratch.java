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
 *     			 http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.testing.tests.dynamic;

import static junit.framework.Assert.assertEquals;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.RelationalMappingFactory;
import org.eclipse.persistence.internal.dynamic.*;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.Test;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class EntityTypeFromScratch {

    @Test
    public void entityTypeFromDescriptor() throws Exception {
        EntityTypeImpl entityType = buildMyEntityType();

        assertEquals(MyEntity.class, entityType.getJavaClass());

        DatabaseSession session = new Project(buildDatabaseLogin()).createDatabaseSession();
        session.getSessionLog().setLevel(SessionLog.FINE);
        session.login();

        session.addDescriptor(entityType.getDescriptor());
        new SchemaManager(session).replaceDefaultTables();

        DynamicEntity entity = entityType.newInstance();
        entity.set("id", 1);
        entity.set("name", "Name");

        session.insertObject(entity);

        session.logout();

    }

    private EntityTypeImpl buildMyEntityType() {
        RelationalMappingFactory factory = new RelationalMappingFactory(MyEntity.class, "MY_ENTITY");
        factory.addDirectMapping("id", int.class, "ID", true);
        factory.addDirectMapping("name", String.class, "NAME", false);

        return (EntityTypeImpl) factory.getType();
    }

    /**
     * Return
     */
    private DatabaseLogin buildDatabaseLogin() {
        DatabaseLogin login = new DatabaseLogin();

        login.useOracleThinJDBCDriver();
        login.setDatabaseURL("localhost:1521:ORCL");
        login.setUserName("scott");
        login.setPassword("tiger");

        // TODO - override with values from system properties

        return login;
    }

    /**
     * Simple concrete subclass of DynamicEntityImpl to test the functionality
     * of EntityType independently of the {@link DynamicClassLoader}
     * functionality which typically generates subclasses.
     */
    public static class MyEntity extends DynamicEntityImpl {

        protected MyEntity(EntityTypeImpl type) {
            super(type);
        }

    }
}
