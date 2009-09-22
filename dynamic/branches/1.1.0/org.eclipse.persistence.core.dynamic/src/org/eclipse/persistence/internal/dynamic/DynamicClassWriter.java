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
 *     dclarke - Dynamic Persistence INCUBATION - Enhancement 200045
 *     			 http://wiki.eclipse.org/EclipseLink/Development/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.internal.dynamic;

//javase imports
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

//EclipseLink imports
import org.eclipse.persistence.exceptions.DynamicException;
import org.eclipse.persistence.internal.helper.Helper;
import org.eclipse.persistence.internal.libraries.asm.ClassWriter;
import org.eclipse.persistence.internal.libraries.asm.CodeVisitor;
import org.eclipse.persistence.internal.libraries.asm.Type;
import static org.eclipse.persistence.internal.libraries.asm.Constants.ACC_PROTECTED;
import static org.eclipse.persistence.internal.libraries.asm.Constants.ACC_PUBLIC;
import static org.eclipse.persistence.internal.libraries.asm.Constants.ACC_SUPER;
import static org.eclipse.persistence.internal.libraries.asm.Constants.ALOAD;
import static org.eclipse.persistence.internal.libraries.asm.Constants.ARETURN;
import static org.eclipse.persistence.internal.libraries.asm.Constants.INVOKESPECIAL;
import static org.eclipse.persistence.internal.libraries.asm.Constants.RETURN;
import static org.eclipse.persistence.internal.libraries.asm.Constants.V1_5;

/**
 * Write the byte codes of a dynamic entity class. The class writer will create
 * the byte codes for a dynamic class that subclasses any provided class
 * replicating its constructors and writeReplace method (if one exists).
 * <p>
 * The intent is to provide a common writer for dynamic JPA entities but also
 * allow for subclasses of this to be used in more complex writing situations
 * such as SDO and DBWS.
 * <p>
 * Instances of this class and any subclasses are maintained within the
 * {@link DynamicClassLoader#getClassWriters()} and
 * {@link DynamicClassLoader#defaultWriter} for the life of the class loader so
 * it is important that no unnecessary state be maintained that may effect
 * memory usage.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class DynamicClassWriter {

    protected Class<?> parentClass;

    /**
     * Name of parent class. This is used only when the parent class is not
     * known at the time the dynamic class writer is registered. This is
     * generally only required when loading from an XML mapping file where the
     * order of class access is not known.
     */
    protected String parentClassName;

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
     * @see DynamicException#illegalDynamicClassWriter(DynamicClassLoader,
     *      String)
     */
    public DynamicClassWriter(String parentClassName) {
        if (parentClassName == null || parentClassName.isEmpty()) {
            throw DynamicException.illegalParentClassName(parentClassName);
        }
        this.parentClassName = parentClassName;
    }

    public Class<?> getParentClass() {
        return this.parentClass;
    }

    public String getParentClassName() {
        return this.parentClassName;
    }

    public byte[] writeClass(DynamicClassLoader loader, String className) throws ClassNotFoundException {
        if (this.parentClass == null && this.parentClassName != null) {
            this.parentClass = loader.loadClass(this.parentClassName);
        }

        Class<?> parent = getParentClass();

        if (parent == null || parent.isPrimitive() || parent.isArray() || parent.isEnum() || parent.isInterface() || Modifier.isFinal(parent.getModifiers())) {
            throw new IllegalArgumentException("Invalid parent class: " + parent);
        }

        ClassWriter cw = new ClassWriter(true);
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, className.replace('.', '/'), Type.getType(parent).getInternalName(), getInterfaces(), null);

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
    protected void addConstructors(ClassWriter cw) {
        Constructor<?>[] constructors = getParentClass().getDeclaredConstructors();

        for (int index = 0; index < constructors.length; index++) {
            if (Modifier.isPublic(constructors[index].getModifiers()) || Modifier.isProtected(constructors[index].getModifiers())) {
                addConstructor(cw, constructors[index]);
            }
        }
    }

    private static final String INIT = "<init>";

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

    private static final String WRITE_REPLACE = "writeReplace";

    /**
     * Add a writeReplace method if one is found in the parentClass. The created
     * writeReplace method will call the parent class version. This is provided
     * to support {@link Serializable} which requires that the writeReplace
     * method exist as a method on the {@link Serializable} class and not
     * provided through inheritance.
     */
    protected void addWriteReplace(ClassWriter cw) {
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
     * 
     * @param loader
     */
    protected void addMethods(ClassWriter cw) {
    }

    /**
     * Create a copy of this {@link DynamicClassWriter} but with a different
     * parent class.
     * 
     * @see DynamicClassLoader#addClass(String, Class)
     */
    protected DynamicClassWriter createCopy(Class<?> parentClass) {
        return new DynamicClassWriter(parentClass);
    }

    /**
     * Verify that the provided writer is compatible with the current writer.
     * Returning true means that the bytes that would be created using this
     * writer are identical with what would come from the provided writer.
     * <p>
     * Used in {@link DynamicClassLoader#addClass(String, DynamicClassWriter)}
     * to verify if a duplicate request of the same className can proceed and
     * return the same class that may already exist.
     */
    protected boolean isCompatible(DynamicClassWriter writer) {
        if (writer == null) {
            return false;
        }
        // Ensure writers are the exact same class. If subclasses do not alter
        // the bytes created then they must override this method and not return
        // false on this check.
        if (getClass() != writer.getClass()) {
            return false;
        }
        if (getParentClass() == null) {
            return getParentClassName() != null && getParentClassName().equals(writer.getParentClassName());
        }
        return getParentClass() == writer.getParentClass();
    }

    @Override
    public String toString() {
        return Helper.getShortClassName(getClass()) + "(" + getParentClass() == null ? getParentClassName() : getParentClass().getName() + ")";
    }
}
