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

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

import org.eclipse.persistence.internal.libraries.asm.*;
import org.eclipse.persistence.internal.libraries.asm.Type;
import org.eclipse.persistence.sessions.Session;

/**
 * This custom class loader is used to generate dynamic entity types for
 * persistent types that are not found on its wrapper class-loader. This
 * functionality enables dynamic persistence without having concrete classes.
 * <p>
 * One of the goals of this style of implementation is that it is minimally
 * intrusive into EclipseLink and allows an application to replace a dynamic
 * entity with a static one simply by having the class on the parent loader's
 * class path.
 * 
 * @author Doug Clarke
 * @since EclipseLink 1.1
 */
public class DynamicClassLoader extends ClassLoader {
	private ClassLoader delegateLoader;
	private Class parentClass;
	private Map<String, Class> dynamicEntityClasses = new HashMap<String, Class>();
    private static final String INIT = "<init>";
    private static final String WRITE_REPLACE = "writeReplace";

	public DynamicClassLoader(ClassLoader delegate, Class parentClass) {
		this.parentClass = parentClass;
		if (delegate == null) {
			this.delegateLoader = Thread.currentThread().getContextClassLoader();
		} else {
			this.delegateLoader = delegate;
		}
	}

	public ClassLoader getDelegateLoader() {
		return this.delegateLoader;
	}

	public Class getParentsClass() {
		return this.parentClass;
	}

	@Override
	public URL getResource(String name) {
		return getDelegateLoader().getResource(name);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return getDelegateLoader().getResourceAsStream(name);
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return getDelegateLoader().getResources(name);
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class javaClass = null;

		try {
			javaClass = getDelegateLoader().loadClass(name);
		} catch (ClassNotFoundException cnfe) {
			javaClass = getDynamicEntityClass(name);
			if (javaClass == null) {
				throw cnfe;
			}
		}
		return javaClass;
	}

	public Class<?> loadDynamicClass(String name) throws ClassNotFoundException {
		Class javaClass = getDynamicEntityClass(name);

		if (javaClass == null) {
			javaClass = createDynamicClass(name);
		}

		return javaClass;
	}

	private Map<String, Class> getDynamicEntityClasses() {
		return this.dynamicEntityClasses;
	}

	public Class getDynamicEntityClass(String className) {
		return getDynamicEntityClasses().get(className);
	}

	/**
	 * Create a dynamic subclass if one does not already exist and register the
	 * created class for subsequent use.
	 */
	public Class createDynamicClass(String className) {
		Class javaClass = getDynamicEntityClass(className);

		if (javaClass != null) {
			return javaClass;
		}

		if (className == null) {
			return null;
		}
		byte[] bytes = buildClassBytes(getParentsClass(), className);
		javaClass = defineClass(className, bytes, 0, bytes.length);

		getDynamicEntityClasses().put(className, javaClass);

		return javaClass;
	}

	public byte[] buildClassBytes(Class parentClass, String className) {
		if (parentClass == null || parentClass.isPrimitive() || parentClass.isArray() || parentClass.isEnum() || parentClass.isInterface()) {
			throw new IllegalArgumentException("DynamicEntityClassWriter can not create " + "subclass for class: " + parentClass);
		}

		if (Modifier.isFinal(parentClass.getModifiers())) {
			throw new IllegalArgumentException("DynamicEntityClassWriter can not create " + "subclass for final class: " + parentClass);
		}

		ClassWriter cw = new ClassWriter(true);
		cw.visit(V1_2, ACC_PUBLIC + ACC_SUPER, className.replace('.', '/'), Type.getType(parentClass).getInternalName(), null, null);
		addConstructors(cw, parentClass);
		addWriteReplace(cw, parentClass);
		cw.visitEnd();
		return cw.toByteArray();
	}

	private void addConstructors(ClassWriter cw, Class parentClass) {
		Constructor[] constructors = parentClass.getDeclaredConstructors();

		for (int index = 0; index < constructors.length; index++) {
			if (Modifier.isPublic(constructors[index].getModifiers()) || Modifier.isProtected(constructors[index].getModifiers())) {
				addConstructor(cw, constructors[index]);
			}
		}
	}

	private void addConstructor(ClassWriter cw, Constructor constructor) {
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

	private void addWriteReplace(ClassWriter cw, Class parentClass) {
		boolean parentHasWriteReplace = false;

		try {
			parentClass.getDeclaredMethod(WRITE_REPLACE, new Class[0]);
			parentHasWriteReplace = true;
		} catch (NoSuchMethodException e) {
			parentHasWriteReplace = false;
		}

		if (Serializable.class.isAssignableFrom(parentClass) && parentHasWriteReplace) {
			Method method;
			try {
				method = parentClass.getDeclaredMethod(WRITE_REPLACE, new Class[0]);
			} catch (NoSuchMethodException e) {
				return;
			}
			
			String methodDesc = Type.getMethodDescriptor(method);
			String[] exceptionsDesc = new String[] {Type.getType(ObjectStreamException.class).getInternalName()};
			
			CodeVisitor mv = cw.visitMethod(ACC_PROTECTED, method.getName(), methodDesc, exceptionsDesc, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(parentClass), method.getName(), methodDesc);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(0, 0);
		}
	}

	/**
	 * Retrieve the dynamic class loader out of the session. If the loader be
	 * used is not a DynamicClassLoader then create a new one wrapping the
	 * current one.
	 * 
	 * @param session
	 * @param parentClass
	 */
	public static DynamicClassLoader getLoader(Session session, Class parentClass) {
		ClassLoader platformLoader = session.getPlatform().getConversionManager().getLoader();

		if (platformLoader.getClass() != DynamicClassLoader.class) {
			platformLoader = new DynamicClassLoader(platformLoader, parentClass);
			session.getPlatform().getConversionManager().setLoader(platformLoader);
		}
		return (DynamicClassLoader) platformLoader;
	}
	
	
}
