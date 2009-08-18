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

/**
 * TODO
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class DynamicClassLoader extends ClassLoader {

    private Map<String, DynamicClassWriter> classWriters = new HashMap<String, DynamicClassWriter>();

    public DynamicClassWriter defaultWriter = new DynamicClassWriter();

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
            return loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("DyanmicClassLoader could not create class: " + className);
        }
    }

    public Class<?> creatDynamicClass(String className) {
        return creatDynamicClass(className, getDefaultWriter());
    }

    public Class<?> creatDynamicClass(String className, Class<?> parentClass) {
        return creatDynamicClass(className, new DynamicClassWriter(parentClass));
    }

    /**
     * create a class, but only if we are in class-creation mode
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