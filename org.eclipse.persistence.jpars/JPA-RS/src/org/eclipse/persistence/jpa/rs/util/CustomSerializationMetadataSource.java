/*******************************************************************************
 * Copyright (c) 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *      tware
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs.util;

import java.util.Map;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jaxb.metadata.MetadataSource;
import org.eclipse.persistence.jaxb.xmlmodel.JavaType;
import org.eclipse.persistence.jaxb.xmlmodel.ObjectFactory;
import org.eclipse.persistence.jaxb.xmlmodel.XmlBindings;
import org.eclipse.persistence.jaxb.xmlmodel.XmlElement;
import org.eclipse.persistence.jaxb.xmlmodel.JavaType.JavaAttributes;
import org.eclipse.persistence.jaxb.xmlmodel.XmlBindings.JavaTypes;
import org.eclipse.persistence.sessions.server.Server;

/**
 * This class is used to pass a list of objects of varying types over JSON or XML between client and server
 * 
 * It builds a class that wraps an XML Choice List that potentially contains any class in the project for the 
 * session passed in.
 * 
 * The JPA RS service will deserialize this class and then make use of the contents of the list.
 * 
 * This is a workaround for the fact that our current JAXB and JSON support does not support serializing/deserializing 
 * a list that is not wrapped in a mapped object.
 * 
 * @author tware
 *
 */
public class CustomSerializationMetadataSource implements MetadataSource {
    private XmlBindings xmlBindings;

    public CustomSerializationMetadataSource(String persistenceUnitName, Server session, String packageName) {
        ObjectFactory objectFactory = new ObjectFactory();
        xmlBindings = new XmlBindings();
        xmlBindings.setPackageName(packageName);

        JavaTypes javaTypes = new JavaTypes();
        xmlBindings.setJavaTypes(javaTypes);
  
        addSerializationTypes(persistenceUnitName, session, objectFactory, javaTypes, packageName);
    }
    
    private void addSerializationTypes(String persistenceUnitName, Server session, ObjectFactory objectFactory, JavaTypes javaTypes, String packageName){

        for (ClassDescriptor ormDescriptor : session.getProject().getOrderedDescriptors()) {
        
            JavaType serializationType = new JavaType();
            serializationType.setName(ormDescriptor.getAlias() + "ListWrapper");
            serializationType.setJavaAttributes(new JavaAttributes());
        
            XmlElement xmlElement = new XmlElement(); 
            xmlElement.setJavaAttribute("list");
        
            xmlElement.setContainerType("java.util.List");

            xmlElement.setType(ormDescriptor.getJavaClassName());

            serializationType.getJavaAttributes().getJavaAttribute().add(objectFactory.createXmlElement(xmlElement));
            
            serializationType.getJavaAttributes().getJavaAttribute().add(DynamicXMLMetadataSource.createSelfProperty(packageName + "." + ormDescriptor.getAlias() + "ListWrapper", objectFactory));
            
            org.eclipse.persistence.jaxb.xmlmodel.XmlRootElement root = new org.eclipse.persistence.jaxb.xmlmodel.XmlRootElement();
            root.setName(ormDescriptor.getAlias() + "ListWrapper");

            serializationType.setXmlRootElement(root);
            javaTypes.getJavaType().add(serializationType);
        }        
    }
    
    @Override
    public XmlBindings getXmlBindings(Map<String, ?> properties, ClassLoader classLoader) {
        return this.xmlBindings;
    }
}
