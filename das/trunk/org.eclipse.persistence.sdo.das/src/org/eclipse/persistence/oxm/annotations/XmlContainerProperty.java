package org.eclipse.persistence.oxm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates a transient property on the target object of this
 * field that refers back to the owning object. 
 * @author Matt
 *
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XmlContainerProperty {
	//The name of the back pointer attribute on the target class
	String value();
	//The get method to be invoked when accessing the back pointer
	String getMethodName() default "";
	//The set method to be used when setting the back pointer	
	String setMethodName() default "";
}
