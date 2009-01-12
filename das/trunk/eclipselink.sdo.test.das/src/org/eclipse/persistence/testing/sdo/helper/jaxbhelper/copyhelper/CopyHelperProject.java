package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.copyhelper;

import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.mappings.XMLCompositeObjectMapping;
import org.eclipse.persistence.oxm.mappings.XMLDirectMapping;
import org.eclipse.persistence.oxm.mappings.XMLCollectionReferenceMapping;
import org.eclipse.persistence.oxm.mappings.XMLObjectReferenceMapping;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.oxm.schema.XMLSchemaClassPathReference;
import org.eclipse.persistence.platform.xml.XMLSchemaReference;

public class CopyHelperProject extends Project {

    public CopyHelperProject() {
        super();
        this.addDescriptor(getRootDescriptor());
        this.addDescriptor(getChild1Descriptor());
        this.addDescriptor(getChild2Descriptor());
    }
    
    private XMLDescriptor getChild1Descriptor() {
        XMLDescriptor xmlDescriptor = new XMLDescriptor();
        xmlDescriptor.setJavaClass(Child1.class);
        xmlDescriptor.addPrimaryKeyFieldName("@id");
        
        NamespaceResolver namespaceResolver = new NamespaceResolver();
        namespaceResolver.put("tns", "urn:copy");
        xmlDescriptor.setNamespaceResolver(namespaceResolver);
        
        XMLSchemaClassPathReference schemaReference = new XMLSchemaClassPathReference();
        schemaReference.setSchemaContext("/tns:child1");
        schemaReference.setType(XMLSchemaReference.COMPLEX_TYPE);
        xmlDescriptor.setSchemaReference(schemaReference);

        XMLDirectMapping idMapping = new XMLDirectMapping();
        idMapping.setAttributeName("id");
        idMapping.setXPath("@id");
        xmlDescriptor.addMapping(idMapping);

        XMLObjectReferenceMapping child2Mapping = new XMLObjectReferenceMapping();
        child2Mapping.setReferenceClass(Child2.class);
        child2Mapping.setAttributeName("child2");
        child2Mapping.addSourceToTargetKeyFieldAssociation("tns:child2/text()", "@id");
        xmlDescriptor.addMapping(child2Mapping);
        
        XMLCollectionReferenceMapping child2CollectionMapping = new XMLCollectionReferenceMapping();
        child2CollectionMapping.setReferenceClass(Child2.class);
        child2CollectionMapping.setAttributeName("child2Collection");
        child2CollectionMapping.addSourceToTargetKeyFieldAssociation("tns:child2collection/text()", "@id");
        xmlDescriptor.addMapping(child2CollectionMapping);
        
        return xmlDescriptor;
    }
    
    private XMLDescriptor getChild2Descriptor() {
        XMLDescriptor xmlDescriptor = new XMLDescriptor();
        xmlDescriptor.setJavaClass(Child2.class);
        xmlDescriptor.setDefaultRootElement("tns:child2");
        xmlDescriptor.addPrimaryKeyFieldName("@id");
        
        XMLSchemaClassPathReference schemaReference = new XMLSchemaClassPathReference();
        schemaReference.setSchemaContext("/tns:child2");
        schemaReference.setType(XMLSchemaReference.ELEMENT);
        xmlDescriptor.setSchemaReference(schemaReference);

        NamespaceResolver namespaceResolver = new NamespaceResolver();
        namespaceResolver.put("tns", "urn:copy");
        xmlDescriptor.setNamespaceResolver(namespaceResolver);
        
        XMLDirectMapping idMapping = new XMLDirectMapping();
        idMapping.setAttributeName("id");
        idMapping.setXPath("@id");
        xmlDescriptor.addMapping(idMapping);

        XMLObjectReferenceMapping child1Mapping = new XMLObjectReferenceMapping();
        child1Mapping.setReferenceClass(Child1.class);
        child1Mapping.setAttributeName("child1");
        child1Mapping.addSourceToTargetKeyFieldAssociation("tns:child1/text()", "@id");
        xmlDescriptor.addMapping(child1Mapping);
        
        return xmlDescriptor;
    }
    
    private XMLDescriptor getRootDescriptor() {
        XMLDescriptor xmlDescriptor = new XMLDescriptor();
        xmlDescriptor.setJavaClass(Root.class);
        
        XMLSchemaClassPathReference schemaReference = new XMLSchemaClassPathReference();
        schemaReference.setSchemaContext("/tns:root");
        schemaReference.setType(XMLSchemaReference.COMPLEX_TYPE);
        xmlDescriptor.setSchemaReference(schemaReference);
        
        NamespaceResolver namespaceResolver = new NamespaceResolver();
        namespaceResolver.put("tns", "urn:copy");
        xmlDescriptor.setNamespaceResolver(namespaceResolver);
        
        XMLCompositeObjectMapping child1Mapping = new XMLCompositeObjectMapping();
        child1Mapping.setAttributeName("child1");
        child1Mapping.setXPath("tns:child1");
        xmlDescriptor.addMapping(child1Mapping);

        XMLCompositeObjectMapping child2Mapping = new XMLCompositeObjectMapping();
        child2Mapping.setAttributeName("child2");
        child2Mapping.setXPath("tns:child2");
        xmlDescriptor.addMapping(child2Mapping);
        
        return xmlDescriptor;
    }
    
}
