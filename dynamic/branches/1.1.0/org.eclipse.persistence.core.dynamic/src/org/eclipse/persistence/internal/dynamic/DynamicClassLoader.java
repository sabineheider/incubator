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
package org.eclipse.persistence.internal.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.sessions.Session;

import com.sun.xml.internal.ws.org.objectweb.asm.ClassWriter;

/**
 * This custom ClassLoader provides support for dynamically generating classes
 * within an EclipseLink application using byte codes created using a
 * {@link DynamicClassWriter}. A DynamicClassLoader requires a parent or
 * delegate class-loader which is provided to the constructor. This delegate
 * class loader handles the lookup and storage of all created classes.
 * 
 * @author dclarke, mnorman
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class DynamicClassLoader extends ClassLoader {

    /**
     * Map of {@link ClassWriter} used to dynamically create a class in the
     * {@link #findClass(String)} call. The application must register classes
     * using addClass or createDynameClass prior to the
     * {@link #findClass(String)} being invoked.
     */
    private volatile Map<String, DynamicClassWriter> classWriters = new HashMap<String, DynamicClassWriter>();

    /**
     * Default writer to use if one is not specified.
     */
    public DynamicClassWriter defaultWriter = new DynamicClassWriter();

    /**
     * 
     * @param delegate
     */
    public DynamicClassLoader(ClassLoader delegate) {
        super(delegate);
    }

    public DynamicClassLoader(ClassLoader delegate, DynamicClassWriter writer) {
        this(delegate);
        this.defaultWriter = writer;
    }

    public DynamicClassWriter getDefaultWriter() {
        return this.defaultWriter;
    }

    protected Map<String, DynamicClassWriter> getClassWriters() {
        return this.classWriters;
    }

    public boolean hasClassWriter(String className) {
        return getClassWriter(className) != null;
    }

    public DynamicClassWriter getClassWriter(String className) {
        synchronized (getClassWriters()) {
            return getClassWriters().get(className);
        }
    }

    /**
     * Add a class to be dynamically written.
     * 
     * @param className
     */
    public void addClass(String className) {
        synchronized (getClassWriters()) {
            getClassWriters().put(className, getDefaultWriter());
        }
    }

    public void addClass(String className, Class<?> parentClass) {
        synchronized (getClassWriters()) {
            getClassWriters().put(className, new DynamicClassWriter(parentClass));
        }
    }

    public void addClass(String className, DynamicClassWriter writer) {
        synchronized (getClassWriters()) {
            getClassWriters().put(className, writer == null ? getDefaultWriter() : writer);
        }
    }

    public Class<?> creatDynamicClass(String className, DynamicClassWriter writer) {
        if (!hasClassWriter(className)) {
            addClass(className, writer);
        }

        try {
            Class<?> dynamicClass = loadClass(className);

            // The findClass call-back will remove the writer. This block is
            // only used if the class already existed and that operation did not
            // occur.
            if (hasClassWriter(className)) {
                synchronized (getClassWriters()) {
                    getClassWriters().remove(className);
                }
            }
            return dynamicClass;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("DyanmicClassLoader could not create class: " + className);
        }
    }

    /**
     * Create a new dynamic entity type for the specified name assuming the use
     * of the default writer and its default parent class.
     * 
     * @param className
     * @return
     */
    public Class<?> creatDynamicClass(String className) {
        return creatDynamicClass(className, getDefaultWriter());
    }

    /**
     * Create a new dynamic entity type for the specified name with the
     * specified parent class.
     * 
     * @param className
     * @param parentClass
     * @return
     */
    public Class<?> creatDynamicClass(String className, Class<?> parentClass) {
        return creatDynamicClass(className, new DynamicClassWriter(parentClass));
    }

    /**
     * Create a new dynamic class if a ClassWriter is registered for the
     * provided className. This code is single threaded to ensure only one class
     * is created for a given name and that the ClassWriter is removed
     * afterwards.
     */
    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        synchronized (getClassWriters()) {
            DynamicClassWriter writer = getClassWriter(className);
            if (writer != null) {
                getClassWriters().remove(className);

                try {
                    byte[] bytes = writer.writeClass(className);
                    return defineClass(className, bytes, 0, bytes.length);
                } catch (ClassFormatError cfe) {
                    throw new ClassNotFoundException(className, cfe);
                }
            }
        }

        return super.findClass(className);
    }

    /**
     * Lookup the DynamicConversionManager for the given session. If the
     * existing ConversionManager is not an instance of DynamicConversionManager
     * then create a new one and replace the existing one.
     * 
     * @param session
     * @return
     */
    public static DynamicClassLoader lookup(Session session) {
        ConversionManager cm = null;

        if (session == null) {
            cm = ConversionManager.getDefaultManager();
        } else {
            cm = session.getPlatform().getConversionManager();
        }

        if (cm.getLoader() instanceof DynamicClassLoader) {
            return (DynamicClassLoader) cm.getLoader();
        }

        DynamicClassLoader dcl = new DynamicClassLoader(cm.getLoader());
        cm.setLoader(dcl);

        if (session == null) {
            ConversionManager.setDefaultLoader(dcl);
        }

        return dcl;
    }

}