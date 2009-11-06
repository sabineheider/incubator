package test.fetchplan;
/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 		dclarke - initial JPA Employee example using XML (bug 217884)
 ******************************************************************************/


import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.weaving.PersistenceWeaved;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;

/**
 * 
 * 
 * @author dclarke
 * @since EclipseLink 1.2
 */
public abstract class EclipseLinkJPAAssert {

    /**
     * 
     * @param emf
     * @param entityTypeName
     */
    public static void assertIsWoven(EntityManagerFactory emf, String entityTypeName) {
        assertNotNull("Null EMF passed to EclipseLinkJPAAssert.assertIsWoven", emf);
        assertTrue("Invalid entity type name: " + entityTypeName, entityTypeName != null && entityTypeName.length() > 0);

        Server session = JpaHelper.getServerSession(emf);
        assertNotNull("Unable to retrieve EclipseLink Server session from: " + emf, session);

        ClassDescriptor desc = session.getDescriptorForAlias(entityTypeName);

        assertNotNull("No descriptor found for alias: " + entityTypeName, desc);
        assertTrue("Class not woven: " + desc.getJavaClass(), PersistenceWeaved.class.isAssignableFrom(desc.getJavaClass()));
    }

    public static void assertIsLazy(EntityManagerFactory emf, String entityTypeName, String attributeName) {

    }

    /**
     * Verify that the specified attribute path is loaded.
     * 
     * @param em
     * @param entity
     * @param attributes
     */
    public static void assertLoaded(EntityManager em, Object entity, String... attributes) {

    }
}
