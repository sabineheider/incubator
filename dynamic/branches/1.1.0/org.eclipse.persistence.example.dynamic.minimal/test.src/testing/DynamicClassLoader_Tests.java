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
 *     dclarke - SimpleDynamicMap Example - Bug 277731
 *               http://wiki.eclipse.org/EclipseLink/Examples/JPA/Dynamic/SimpleDynamicMap
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.junit.Test;

import example.DynamicMapEntity;

public class DynamicClassLoader_Tests {

    private DynamicClassLoader classLoader = new DynamicClassLoader(null, DynamicMapEntity.class);

    @Test
    public void validateConstructor() {
        assertNotNull(this.classLoader.getDelegateLoader());
        assertSame(Thread.currentThread().getContextClassLoader(), this.classLoader.getDelegateLoader());
    }

    @Test
    public void validate_createDynamicType() throws ClassNotFoundException {
        Class dynamicType = this.classLoader.createDynamicClass("model.SimpleType");

        assertNotNull(dynamicType);
        assertTrue(DynamicMapEntity.class.isAssignableFrom(dynamicType));

        Class dynamicType2 = this.classLoader.loadClass("model.SimpleType");

        assertNotNull(dynamicType2);
        assertSame(dynamicType, dynamicType2);
    }

    /**
     * Validate that the load of an unknown class throws a CNFE
     */
    @Test
    public void validate_loadClass() {
        try {
            this.classLoader.loadClass("model.Blah");
        } catch (ClassNotFoundException cnfe) {
            assertEquals("model.Blah", cnfe.getMessage());
            return;
        }

        fail("ClassNotFoundException should have been thrown");
    }
}
