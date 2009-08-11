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
package org.eclipse.persistence.internal.helper;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.internal.dynamic.DynamicClassWriter;
import org.eclipse.persistence.internal.dynamic.DynamicEntityImpl;
import org.eclipse.persistence.sessions.Session;

/**
 * Custom {@link ConversionManager} used in EclipseLink session where Dynamic
 * Persistence is required.
 * <p>
 * NOTE: When this code is migrated into the incubator this code will become
 * part of the core {@link ConversionManager} and this custom subclass will no
 * longer be needed.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class DynamicConversionManager extends ConversionManager {

    /**
     * Lookup the DynamicConversionManager for the given session. If the
     * existing ConversionManager is not an instance of DynamicConversionManager
     * then create a new one and replace the existing one.
     * 
     * @param session
     * @return
     */
    public static DynamicConversionManager getDynamicConversionManager(Session session) {
        ConversionManager cm = null;

        if (session == null) {
            cm = ConversionManager.getDefaultManager();
        } else {
            cm = session.getPlatform().getConversionManager();
        }

        if (cm == null || cm.getClass() != DynamicConversionManager.class) {
            cm = new DynamicConversionManager(cm);

            if (session == null) {
                ConversionManager.setDefaultManager(cm);
            } else {
                session.getPlatform().setConversionManager(cm);
            }
        }

        return (DynamicConversionManager) cm;
    }

    public DynamicConversionManager(ConversionManager cm) {
        this.defaultNullValues = cm.getDefaultNullValues();
        this.loader = new DynamicClassLoader(cm.getLoader());
        this.dataTypesConvertedFromAClass = cm.dataTypesConvertedFromAClass;
        this.dataTypesConvertedToAClass = cm.dataTypesConvertedToAClass;
    }

    protected DynamicClassLoader getDynamicClassLoader() {
        return (DynamicClassLoader) getLoader();
    }

    private static Class DEFAULT_DYNAMIC_PARENT_CLASS = DynamicEntityImpl.class;

    public Class createDynamicClass(String className) {
        return createDynamicClass(className, DEFAULT_DYNAMIC_PARENT_CLASS);
    }

    public Class createDynamicClass(String className, Class baseClass) {
        return createDynamicClass(className, baseClass, new DynamicClassWriter());
    }

    public Class createDynamicClass(String className, Class baseClass, DynamicClassWriter classWriter) {
        return getDynamicClassLoader().createDynamicClass(className, baseClass, classWriter);
    }

    /**
     * 
     */
    private class DynamicClassLoader extends ClassLoader {

        private ClassLoader delegateLoader;

        private Map<String, Class> dynamicEntityClasses = new HashMap<String, Class>();

        private DynamicClassLoader(ClassLoader delegate) {
            this.delegateLoader = delegate;
        }

        private Map<String, Class> getDynamicEntityClasses() {
            return this.dynamicEntityClasses;
        }

        protected Class getDynamicEntityClass(String className) {
            return getDynamicEntityClasses().get(className);
        }

        /**
         * Create a dynamic subclass if one does not already exist and register
         * the created class for subsequent use.
         */
        public Class<DynamicEntity> createDynamicClass(String className, Class baseClass, DynamicClassWriter writer) {
            Class javaClass = getDynamicEntityClass(className);

            if (javaClass != null) {
                return javaClass;
            }

            if (className == null) {
                return null;
            }

            synchronized (getDynamicEntityClasses()) {
                javaClass = getDynamicEntityClass(className);

                if (javaClass == null) {
                    byte[] bytes = writer.writeClass(baseClass, className);
                    javaClass = super.defineClass(className, bytes, 0, bytes.length);
                    getDynamicEntityClasses().put(className, javaClass);
                }
            }

            return javaClass;
        }

        @Override
        public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
            Class dynamicClass = getDynamicEntityClass(name);
            if (dynamicClass != null) {
                return dynamicClass;
            }
            return this.delegateLoader.loadClass(name);
        }

    };

}
