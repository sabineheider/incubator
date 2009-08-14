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

import org.eclipse.persistence.dynamic.DynamicEntity;

/**
 * 
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class DynamicClassLoader extends ClassLoader {
    
    public static final Class DEFAULT_DYNAMIC_PARENT = DynamicEntityImpl.class;

    private boolean createDynamicClasses = true;

    private Map<String, Class> dynamicEntityClasses = new HashMap<String, Class>();

    private Class defaultParentClass = DEFAULT_DYNAMIC_PARENT;

    private DynamicClassWriter defaultWriter = new DynamicClassWriter();

    public DynamicClassLoader(ClassLoader delegate) {
        super(delegate);
    }

    public boolean isCreateDynamicClasses() {
        return createDynamicClasses;
    }

    public void setCreateDynamicClasses(boolean createDynamicClasses) {
        this.createDynamicClasses = createDynamicClasses;
    }

    private Map<String, Class> getDynamicEntityClasses() {
        return this.dynamicEntityClasses;
    }

    public Class getDynamicEntityClass(String className) {
        return getDynamicEntityClasses().get(className);
    }

    public Class getDefaultParentClass() {
        return defaultParentClass;
    }

    public void setDefaultParentClass(Class defaultParentClass) {
        this.defaultParentClass = defaultParentClass;
    }

    public DynamicClassWriter getDefaultWriter() {
        return defaultWriter;
    }

    public void setDefaultWriter(DynamicClassWriter defaultWriter) {
        this.defaultWriter = defaultWriter;
    }

    public Class<DynamicEntity> createDynamicClass(String name) {
        return createDynamicClass(name, getDefaultParentClass());
    }

    public Class<DynamicEntity> createDynamicClass(String name, Class baseClass) {
        return createDynamicClass(name, baseClass, getDefaultWriter());
    }

    /**
     * Create a dynamic subclass if one does not already exist and register the
     * created class for subsequent use.
     */
    public Class<DynamicEntity> createDynamicClass(String name, Class baseClass, DynamicClassWriter writer) {
        Class javaClass = getDynamicEntityClass(name);

        if (javaClass != null) {
            return javaClass;
        }

        if (name == null) {
            return null;
        }

        synchronized (getDynamicEntityClasses()) {
            javaClass = getDynamicEntityClass(name);

            if (javaClass == null) {
                byte[] bytes = writer.writeClass(baseClass, name);
                javaClass = super.defineClass(name, bytes, 0, bytes.length);
                getDynamicEntityClasses().put(name, javaClass);
            }
        }

        return javaClass;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!isCreateDynamicClasses()) {
            return super.findClass(name);
        } 
        
        return createDynamicClass(name);
    }

}