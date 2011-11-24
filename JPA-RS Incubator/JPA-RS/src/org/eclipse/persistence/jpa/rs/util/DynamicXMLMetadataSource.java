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
 *      dclarke/tware - initial 
 *      tware
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs.util;

import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jaxb.metadata.MetadataSource;
import org.eclipse.persistence.jaxb.xmlmodel.JavaType;
import org.eclipse.persistence.jaxb.xmlmodel.JavaType.JavaAttributes;
import org.eclipse.persistence.jaxb.xmlmodel.ObjectFactory;
import org.eclipse.persistence.jaxb.xmlmodel.XmlBindings;
import org.eclipse.persistence.jaxb.xmlmodel.XmlBindings.JavaTypes;
import org.eclipse.persistence.jaxb.xmlmodel.XmlElement;
import org.eclipse.persistence.jpa.rs.PersistenceFactory;
import org.eclipse.persistence.mappings.CollectionMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ObjectReferenceMapping;
import org.eclipse.persistence.sessions.server.Server;

/**
 * {@link MetadataSource} used in the creation of dynamic JAXB contexts for applications.
 * 
 * @see PersistenceFactory#createJAXBContext(Server)
 * @author dclarke
 * @since EclipseLink 2.4.0
 */
public class DynamicXMLMetadataSource implements MetadataSource {

    private XmlBindings xmlBindings;

    public DynamicXMLMetadataSource(String persistenceUnitName, Server session, String packageName) {
        ObjectFactory objectFactory = new ObjectFactory();
        xmlBindings = new XmlBindings();
        xmlBindings.setPackageName(packageName);

        JavaTypes javaTypes = new JavaTypes();
        xmlBindings.setJavaTypes(javaTypes);
  
        for (ClassDescriptor ormDescriptor : session.getProject().getOrderedDescriptors()) {
            javaTypes.getJavaType().add(createJAXBType(ormDescriptor, objectFactory));
        }
    }

    private JavaType createJAXBType(ClassDescriptor classDescriptor, ObjectFactory objectFactory) {
        JavaType javaType = new JavaType();
        javaType.setName(classDescriptor.getAlias());
        javaType.setJavaAttributes(new JavaAttributes());
        for (DatabaseMapping ormMapping : classDescriptor.getMappings()) {
            javaType.getJavaAttributes().getJavaAttribute().add(createJAXBProperty(ormMapping, objectFactory));
        }
        // Make them all root elements for now
        javaType.setXmlRootElement(new org.eclipse.persistence.jaxb.xmlmodel.XmlRootElement());

        return javaType;
    }
    
    private JAXBElement<XmlElement> createJAXBProperty(DatabaseMapping mapping, ObjectFactory objectFactory) {
        XmlElement xmlElement = new XmlElement();
        xmlElement.setJavaAttribute(mapping.getAttributeName());
        if (mapping.isObjectReferenceMapping()){
            xmlElement.setType(((ObjectReferenceMapping)mapping).getReferenceClassName());
        } else if (mapping.isCollectionMapping()){
            xmlElement.setType(((CollectionMapping)mapping).getReferenceClassName());
        } else {
            xmlElement.setType(mapping.getAttributeClassification().getName());
        }
        return objectFactory.createXmlElement(xmlElement);
    }

    @Override
    public XmlBindings getXmlBindings(Map<String, ?> properties, ClassLoader classLoader) {
        return this.xmlBindings;
    }
    
}
