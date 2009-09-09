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
package org.eclipse.persistence.testing.tests.dynamic.orm.projectxml;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.exceptions.XMLMarshalException;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.Project;
import org.junit.Test;

/**
 * Test cases verifying invalid args passed to
 * {@link EntityTypeBuilder#loadDynamicProject(String, DatabaseLogin)}
 */
public class InvalidConfig {

    @Test
    public void nullResourceNullLogin() throws Exception {
        try {
            EntityTypeBuilder.loadDynamicProject(null, null);
        } catch (NullPointerException e) {
            return;
        }
        fail("NullPointerException expected");
    }

    @Test
    public void emptyResourcePath() throws Exception {
        try {
            EntityTypeBuilder.loadDynamicProject("", null);
        } catch (XMLMarshalException e) {
            return;
        }
        fail("XMLMarshalException expected");
    }

    @Test
    public void unknownResourcePathNullLogin() throws Exception {
        Project project = EntityTypeBuilder.loadDynamicProject("/foo/bar.xml", null);

        assertNull("Null project expected", project);
    }

    @Test
    public void unknownResourcePath() throws Exception {
        Project project = EntityTypeBuilder.loadDynamicProject("/foo/bar.xml", new DatabaseLogin());

        assertNull("Null project expected", project);
    }
}