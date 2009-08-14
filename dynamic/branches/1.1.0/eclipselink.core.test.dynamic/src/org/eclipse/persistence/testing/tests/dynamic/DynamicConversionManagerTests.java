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

import static junit.framework.Assert.*;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;

import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.helper.*;
import org.junit.Test;

public class DynamicConversionManagerTests {

    @Test
    public void testDynamicEntity() throws Exception {
        DynamicConversionManager dcm = new DynamicConversionManager(ConversionManager.getDefaultManager());

        Class dynamicClass = dcm.getDynamicClassLoader().createDynamicClass("test.MyClass");

        assertNotNull(dynamicClass);
        assertSame(dynamicClass, dcm.getDynamicClassLoader().createDynamicClass("test.MyClass"));
        assertEquals("test.MyClass", dynamicClass.getName());

        InstantiationException instEx = null;
        try {
            dynamicClass.newInstance();
        } catch (InstantiationException ie) {
            instEx = ie;
        }
        assertNotNull("InstantiationException not thrown as expected for default constructor", instEx);

        Constructor[] constructors = dynamicClass.getConstructors();
        assertEquals(1, constructors.length);
        assertEquals(1, constructors[0].getParameterTypes().length);
        assertEquals(EntityTypeImpl.class, constructors[0].getParameterTypes()[0]);

        // TODO: Figure out why this fails
        // Constructor<DynamicEntity> constructor =
        // dynamicClass.getDeclaredConstructor(new Class[]
        // {DynamicEntityImpl.class});
        // assertNotNull(constructor);
    }

    @Test
    public void testDefaultConstructor() throws Exception {
        DynamicConversionManager dcm = new DynamicConversionManager(ConversionManager.getDefaultManager());

        Class dynamicClass = dcm.getDynamicClassLoader().createDynamicClass("test.MyClass", DefaultConstructor.class);

        assertNotNull(dynamicClass);
        assertSame(dynamicClass, dcm.getDynamicClassLoader().createDynamicClass("test.MyClass", DefaultConstructor.class));

        DefaultConstructor entity = (DefaultConstructor) dynamicClass.newInstance();

        assertNotNull(entity);
    }

    @Test
    public void testStringConstructor() throws Exception {
        DynamicConversionManager dcm = new DynamicConversionManager(ConversionManager.getDefaultManager());

        Class dynamicClass = dcm.getDynamicClassLoader().createDynamicClass("test.MyClass", StringConstructor.class);

        assertNotNull(dynamicClass);
        assertSame(dynamicClass, dcm.getDynamicClassLoader().createDynamicClass("test.MyClass", StringConstructor.class));

        InstantiationException instEx = null;
        try {
            dynamicClass.newInstance();
        } catch (InstantiationException ie) {
            instEx = ie;
        }
        assertNotNull("InstantiationException not thrown as expected for default constructor", instEx);

        Constructor[] constructors = dynamicClass.getConstructors();
        assertEquals(1, constructors.length);
        assertEquals(1, constructors[0].getParameterTypes().length);
        assertEquals(String.class, constructors[0].getParameterTypes()[0]);

        // TODO: Figure out why this fails
        // Constructor<DynamicEntity> constructor =
        // dynamicClass.getDeclaredConstructor(new Class[]
        // {DynamicEntityImpl.class});
        // assertNotNull(constructor);
    }

    /**
     * Verify that the {@link DynamicClassLoader} will overwrite a writeReplace
     * method if it exists on the base class
     * 
     * @throws Exception
     */
    @Test
    public void testWriteReplace() throws Exception {
        DynamicConversionManager dcm = new DynamicConversionManager(ConversionManager.getDefaultManager());

        Class dynamicClass = dcm.getDynamicClassLoader().createDynamicClass("test.MyClass", WriteReplace.class);

        assertNotNull(dynamicClass);
        assertSame(dynamicClass, dcm.getDynamicClassLoader().createDynamicClass("test.MyClass", WriteReplace.class));

        WriteReplace entity = (WriteReplace) dynamicClass.newInstance();

        assertNotNull(entity);

        byte[] entityBytes = SerializationHelper.serialize(entity);
        byte[] stringBytes = SerializationHelper.serialize(entity.getClass().getName());

        assertEquals(stringBytes.length, entityBytes.length);
        for (int index = 0; index < stringBytes.length; index++) {
            assertEquals(stringBytes[index], entityBytes[index]);
        }

        Object deserializedValue = SerializationHelper.deserialize(entityBytes);

        assertNotNull(deserializedValue);
        assertEquals(String.class, deserializedValue.getClass());
        assertEquals(dynamicClass.getName(), deserializedValue);
    }

    public static class DefaultConstructor {
    }

    public static class StringConstructor {
        public StringConstructor(String arg) {

        }
    }

    public static class WriteReplace implements Serializable {
        protected Object writeReplace() throws ObjectStreamException {
            return getClass().getName();
        }
    }
}
