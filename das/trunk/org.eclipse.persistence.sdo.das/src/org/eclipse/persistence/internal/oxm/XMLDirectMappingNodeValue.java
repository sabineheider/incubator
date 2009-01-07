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
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/
package org.eclipse.persistence.internal.oxm;

import javax.xml.namespace.QName;
import org.eclipse.persistence.internal.oxm.record.MarshalContext;
import org.eclipse.persistence.internal.oxm.record.ObjectMarshalContext;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.oxm.mappings.XMLDirectMapping;
import org.eclipse.persistence.oxm.record.MarshalRecord;
import org.eclipse.persistence.oxm.record.UnmarshalRecord;
import org.eclipse.persistence.sessions.Session;

/**
 * INTERNAL:
 * <p><b>Purpose</b>: This is how the XML Direct Mapping is handled when used
 * with the TreeObjectBuilder.</p>
 */
public class XMLDirectMappingNodeValue extends XMLSimpleMappingNodeValue implements NullCapableValue {
    private XMLDirectMapping xmlDirectMapping;

    public XMLDirectMappingNodeValue(XMLDirectMapping xmlDirectMapping) {
        super();
        this.xmlDirectMapping = xmlDirectMapping;
    }

    public void setXPathNode(XPathNode xPathNode) {
        super.setXPathNode(xPathNode);
        xmlDirectMapping.getNullPolicy().xPathNode(xPathNode, this);
    }

    public boolean isOwningNode(XPathFragment xPathFragment) {
        return xPathFragment.isAttribute() || xPathFragment.nameIsText();
    }

    public boolean marshal(XPathFragment xPathFragment, MarshalRecord marshalRecord, Object object, AbstractSession session, NamespaceResolver namespaceResolver) {
        return marshal(xPathFragment, marshalRecord, object, session, namespaceResolver, ObjectMarshalContext.getInstance());
    }

    public boolean marshal(XPathFragment xPathFragment, MarshalRecord marshalRecord, Object object, AbstractSession session, NamespaceResolver namespaceResolver, MarshalContext marshalContext) {
        if (xmlDirectMapping.isReadOnly()) {
            return false;
        }
        Object objectValue = marshalContext.getAttributeValue(object, xmlDirectMapping);
        Object fieldValue = xmlDirectMapping.getFieldValue(objectValue, session, marshalRecord);
        // Check for a null value 
        if (null == fieldValue) {
            // Perform marshal operations based on the null policy
            return xmlDirectMapping.getNullPolicy().directMarshal(xPathFragment, marshalRecord, object, session, namespaceResolver);
        } else {
            QName schemaType = getSchemaType((XMLField) xmlDirectMapping.getField(), fieldValue, session);
            String stringValue = getValueToWrite(schemaType, fieldValue, (XMLConversionManager) session.getDatasourcePlatform().getConversionManager());
            XPathFragment groupingFragment = marshalRecord.openStartGroupingElements(namespaceResolver);
            if (xPathFragment.isAttribute()) {
                marshalRecord.attribute(xPathFragment, namespaceResolver, stringValue);
                marshalRecord.closeStartGroupingElements(groupingFragment);
            } else {
                marshalRecord.closeStartGroupingElements(groupingFragment);
                if (xmlDirectMapping.isCDATA()) {
                    marshalRecord.cdata(stringValue);
                } else {
                    marshalRecord.characters(stringValue);
                }
            }
            return true;
        }
    }

    public void attribute(UnmarshalRecord unmarshalRecord, String namespaceURI, String localName, String value) {
        unmarshalRecord.removeNullCapableValue(this);
        XMLField xmlField = (XMLField) xmlDirectMapping.getField();
        Object realValue = xmlField.convertValueBasedOnSchemaType(value, (XMLConversionManager) unmarshalRecord.getSession().getDatasourcePlatform().getConversionManager());
        // Perform operations on the object based on the null policy
        Object convertedValue = xmlDirectMapping.getAttributeValue(realValue, unmarshalRecord.getSession(), unmarshalRecord);
        xmlDirectMapping.setAttributeValueInObject(unmarshalRecord.getCurrentObject(), convertedValue);
    }

    public void endElement(XPathFragment xPathFragment, UnmarshalRecord unmarshalRecord) {
        unmarshalRecord.removeNullCapableValue(this);
        XMLField xmlField = (XMLField) xmlDirectMapping.getField();
        if (!xmlField.getLastXPathFragment().nameIsText()) {
            return;
        }
        Object value = unmarshalRecord.getStringBuffer().toString();
        if (value.equals(EMPTY_STRING)) {
            value = null;
        }
        unmarshalRecord.resetStringBuffer();

        XMLConversionManager xmlConversionManager = (XMLConversionManager) unmarshalRecord.getSession().getDatasourcePlatform().getConversionManager();
        if (unmarshalRecord.getTypeQName() != null) {
            Class typeClass = xmlField.getJavaClass(unmarshalRecord.getTypeQName());
            value = xmlConversionManager.convertObject(value, typeClass, unmarshalRecord.getTypeQName());
        } else {
            value = xmlField.convertValueBasedOnSchemaType(value, xmlConversionManager);
        }

        Object convertedValue = xmlDirectMapping.getAttributeValue(value, unmarshalRecord.getSession(), unmarshalRecord);
        unmarshalRecord.setAttributeValue(convertedValue, xmlDirectMapping);
    }

    public void setNullValue(Object object, Session session) {
        Object value = xmlDirectMapping.getAttributeValue(null, session);
        xmlDirectMapping.setAttributeValueInObject(object, value);
    }

    public boolean isNullCapableValue() {
        return xmlDirectMapping.getNullPolicy().getIsSetPerformedForAbsentNode();
    }

    public XMLDirectMapping getMapping() {
        return xmlDirectMapping;
    }

}
