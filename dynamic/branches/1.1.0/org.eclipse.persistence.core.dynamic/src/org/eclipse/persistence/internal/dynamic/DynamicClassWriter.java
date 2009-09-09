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
package org.eclipse.persistence.internal.dynamic;

import static org.eclipse.persistence.internal.libraries.asm.Constants.*;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.*;

import org.eclipse.persistence.dynamic.DynamicEntityException;
import org.eclipse.persistence.internal.libraries.asm.*;
import org.eclipse.persistence.internal.libraries.asm.Type;

/**
 * Write the byte codes of a dynamic entity class. The class writer will create
 * the byte codes for a dynamic class that subclasses any provided class
 * replicating its constructors and writeReplace method (if one exists).
 * <p>
 * The intent is to provide a common writer for dynamic JPA entities but also
 * allow for subclasses of this to be used in more complex writing situations
 * such as SDO and DBWS.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class DynamicClassWriter {

    protected static final String INIT = "<init>";

    protected Class<?> parentClass;

    private String parentClassName;

    private DynamicClassLoader loader;

    private static final String WRITE_REPLACE = "writeReplace";

    public DynamicClassWriter() {
        this(DynamicEntityImpl.class);
    }

    public DynamicClassWriter(Class<?> parentClass) {
        this.parentClass = parentClass;
    }

    /**
     * Create using a loader and class name so that the parent class can be
     * lazily loaded when the writer is used to generate a dynamic class.
     * <p>
     * The loader must not be null and the parentClassName must not be null and
     * not an empty String. The parentClassName will be converted to a class
     * using the provided loader lazily.
     * 
     * @see #getParentClass()
     * @see DynamicEntityException#illegalDynamicClassWriter(DynamicClassLoader, String)
     */
    public DynamicClassWriter(DynamicClassLoader loader, String parentClassName) {
        if (loader == null || parentClassName == null || parentClassName.isEmpty()) {
            throw DynamicEntityException.illegalDynamicClassWriter(loader, parentClassName);
        }
        this.loader = loader;
        this.parentClassName = parentClassName;
    }

    protected DynamicClassLoader getLoader() {
        return this.loader;
    }

    /**
     * 
     * @throws ClassNotFoundException
     *             if the parentClassName could not be converted into a class
     */
    public Class<?> getParentClass() throws ClassNotFoundException {
        if (this.parentClass == null && this.parentClassName != null) {
            this.parentClass = getLoader().loadClass(this.parentClassName);
        }

        return this.parentClass;
    }

    public byte[] writeClass(String className) throws ClassNotFoundException {
        if (getParentClass() == null || getParentClass().isPrimitive() || getParentClass().isArray() || getParentClass().isEnum() || parentClass.isInterface() || Modifier.isFinal(parentClass.getModifiers())) {
            throw new IllegalArgumentException("Invalid parent class: " + getParentClass());
        }

        ClassWriter cw = new ClassWriter(true);
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, className.replace('.', '/'), Type.getType(getParentClass()).getInternalName(), getInterfaces(), null);

        addFields(cw);
        addConstructors(cw);
        addMethods(cw);
        addWriteReplace(cw);

        cw.visitEnd();
        return cw.toByteArray();
    }

    /**
     * Interfaces the dynamic entity class implements. By default this is none
     * but in the case of SDO a concrete interface must be implemented.
     * Subclasses should override this as required.
     * 
     * @return Interfaces implemented by Dynamic class. May be null
     */
    protected String[] getInterfaces() {
        return null;
    }

    /**
     * Adds all constructors calling those available in the parent class.
     * 
     * @see #addConstructor(ClassWriter, Constructor)
     */
    protected void addConstructors(ClassWriter cw) throws ClassNotFoundException {
        Constructor<?>[] constructors = getParentClass().getDeclaredConstructors();

        for (int index = 0; index < constructors.length; index++) {
            if (Modifier.isPublic(constructors[index].getModifiers()) || Modifier.isProtected(constructors[index].getModifiers())) {
                addConstructor(cw, constructors[index]);
            }
        }
    }

    /**
     * Add a new constructor based invoking the provided constructor from the
     * parent class. This method is called by
     * {@link #addConstructors(ClassWriter, Class)} for each constructor
     * available in the parent class.
     */
    protected void addConstructor(ClassWriter cw, Constructor<?> constructor) {
        Type[] types = new Type[constructor.getParameterTypes().length];

        for (int index = 0; index < constructor.getParameterTypes().length; index++) {
            types[index] = Type.getType(constructor.getParameterTypes()[index]);
        }

        String consDesc = Type.getMethodDescriptor(Type.VOID_TYPE, types);
        CodeVisitor mv = cw.visitMethod(ACC_PUBLIC, INIT, consDesc, null, null);
        mv.visitVarInsn(ALOAD, 0);

        for (int param = 1; param <= constructor.getParameterTypes().length; param++) {
            mv.visitVarInsn(ALOAD, param);
        }

        mv.visitMethodInsn(INVOKESPECIAL, Type.getType(constructor.getDeclaringClass()).getInternalName(), INIT, consDesc);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
    }

    /**
     * Add a writeReplace method if one is found in the parentClass. The created
     * writeReplace method will call the parent class version. This is provided
     * to support {@link Serializable} which requires that the writeReplace
     * method exist as a method on the {@link Serializable} class and not
     * provided through inheritance.
     */
    protected void addWriteReplace(ClassWriter cw) throws ClassNotFoundException {
        boolean parentHasWriteReplace = false;

        try {
            getParentClass().getDeclaredMethod(WRITE_REPLACE, new Class[0]);
            parentHasWriteReplace = true;
        } catch (NoSuchMethodException e) {
            parentHasWriteReplace = false;
        }

        if (Serializable.class.isAssignableFrom(getParentClass()) && parentHasWriteReplace) {
            Method method;
            try {
                method = getParentClass().getDeclaredMethod(WRITE_REPLACE, new Class[0]);
            } catch (NoSuchMethodException e) {
                return;
            }

            String methodDesc = Type.getMethodDescriptor(method);
            String[] exceptionsDesc = new String[] { Type.getType(ObjectStreamException.class).getInternalName() };

            CodeVisitor mv = cw.visitMethod(ACC_PROTECTED, method.getName(), methodDesc, exceptionsDesc, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(getParentClass()), method.getName(), methodDesc);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
        }
    }

    /**
     * Provided to allow subclasses to add their own fields.
     */
    protected void addFields(ClassWriter cw) {
    }

    /**
     * Provided to allow subclasses to add their own methods. This must add
     * additional methods needed to implement any interfaces returned from
     * {@link #getInterfaces()}
     */
    protected void addMethods(ClassWriter cw) {
    }
}
