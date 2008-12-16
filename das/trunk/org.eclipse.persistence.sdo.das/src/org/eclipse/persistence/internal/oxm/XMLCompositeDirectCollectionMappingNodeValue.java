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

import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import org.eclipse.persistence.internal.oxm.record.MarshalContext;
import org.eclipse.persistence.internal.oxm.record.ObjectMarshalContext;
import org.eclipse.persistence.internal.queries.ContainerPolicy;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLConstants;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.oxm.mappings.XMLCompositeDirectCollectionMapping;
import org.eclipse.persistence.oxm.mappings.converters.XMLConverter;
import org.eclipse.persistence.oxm.record.MarshalRecord;
import org.eclipse.persistence.oxm.record.UnmarshalRecord;
import org.xml.sax.Attributes;

/**
 * INTERNAL:
 * <p><b>Purpose</b>: This is how the XML Composite Direct Collection Mapping is 
 * handled when used with the TreeObjectBuilder.</p>
 */

public class XMLCompositeDirectCollectionMappingNodeValue extends XMLSimpleMappingNodeValue implements ContainerValue, MappingNodeValue {
    private static final String SPACE = " ";
    private XMLCompositeDirectCollectionMapping xmlCompositeDirectCollectionMapping;

    public XMLCompositeDirectCollectionMappingNodeValue(XMLCompositeDirectCollectionMapping xmlCompositeDirectCollectionMapping) {
        super();
        this.xmlCompositeDirectCollectionMapping = xmlCompositeDirectCollectionMapping;
    }

    public boolean isOwningNode(XPathFragment xPathFragment) {
        if (xmlCompositeDirectCollectionMapping.usesSingleNode()) {
            return xPathFragment.nameIsText() || xPathFragment.isAttribute();
        } else {
            XPathFragment nextFragment = xPathFragment.getNextFragment();
            return (nextFragment != null) && (nextFragment.nameIsText() || nextFragment.isAttribute());
        }
    }

    /**
     * Override the method in XPathNode such that the marshaller can be set on the
     * marshalRecord - this is required for XMLConverter usage.
     */
    public boolean marshal(XPathFragment xPathFragment, MarshalRecord marshalRecord, Object object, AbstractSession session, NamespaceResolver namespaceResolver) {
        if (xmlCompositeDirectCollectionMapping.isReadOnly()) {
            return false;
        }

        ContainerPolicy cp = getContainerPolicy();
        Object collection = xmlCompositeDirectCollectionMapping.getAttributeAccessor().getAttributeValueFromObject(object);
        if (null == collection) {
            return false;
        }
        Object iterator = cp.iteratorFor(collection);
        if (cp.hasNext(iterator)) {
            XPathFragment groupingFragment = marshalRecord.openStartGroupingElements(namespaceResolver);
            marshalRecord.closeStartGroupingElements(groupingFragment);
        } else {
            return false;
        }
        Object objectValue;
        String stringValue = "";
        String newValue;
        QName schemaType;
        if (xmlCompositeDirectCollectionMapping.usesSingleNode()) {
            while (cp.hasNext(iterator)) {
                objectValue = cp.next(iterator, session);
                if (xmlCompositeDirectCollectionMapping.hasValueConverter()) {
                    if (xmlCompositeDirectCollectionMapping.getValueConverter() instanceof XMLConverter) {
                        objectValue = ((XMLConverter) xmlCompositeDirectCollectionMapping.getValueConverter()).convertObjectValueToDataValue(objectValue, session, marshalRecord.getMarshaller());
                    } else {
                        objectValue = xmlCompositeDirectCollectionMapping.getValueConverter().convertObjectValueToDataValue(objectValue, session);
                    }
                }
                schemaType = getSchemaType((XMLField) xmlCompositeDirectCollectionMapping.getField(), objectValue, session);
                newValue = getValueToWrite(schemaType, objectValue, (XMLConversionManager) session.getDatasourcePlatform().getConversionManager());
                if (null != newValue) {
                    stringValue += newValue;
                    if (cp.hasNext(iterator)) {
                        stringValue += SPACE;
                    }
                }
            }
            XPathFragment groupingFragment = marshalRecord.openStartGroupingElements(namespaceResolver);
            if (xPathFragment.isAttribute()) {
                marshalRecord.attribute(xPathFragment, namespaceResolver, stringValue);
                marshalRecord.closeStartGroupingElements(groupingFragment);
            } else {
                marshalRecord.closeStartGroupingElements(groupingFragment);
                if (xmlCompositeDirectCollectionMapping.isCDATA()) {
                    marshalRecord.cdata(stringValue);
                } else {
                    marshalRecord.characters(stringValue);
                }
            }
        } else {
            while (cp.hasNext(iterator)) {
                objectValue = cp.next(iterator, session);
                marshalSingleValue(xPathFragment, marshalRecord, object, objectValue, session, namespaceResolver, ObjectMarshalContext.getInstance());
            }
        }
        return true;
    }

    public void attribute(UnmarshalRecord unmarshalRecord, String namespaceURI, String localName, String value) {
        Object collection = unmarshalRecord.getContainerInstance(this);
        if (xmlCompositeDirectCollectionMapping.usesSingleNode()) {
            StringTokenizer stringTokenizer = new StringTokenizer(value);
            while (stringTokenizer.hasMoreTokens()) {
                addUnmarshalValue(unmarshalRecord, stringTokenizer.nextToken(), collection);
            }
        } else {
            addUnmarshalValue(unmarshalRecord, value, collection);
        }
    }

    public boolean startElement(XPathFragment xPathFragment, UnmarshalRecord unmarshalRecord, Attributes atts) {
        XMLField xmlField = (XMLField) xmlCompositeDirectCollectionMapping.getField();
        if (xmlField.getLastXPathFragment().nameIsText()) {
            String type = atts.getValue(XMLConstants.SCHEMA_INSTANCE_URL, XMLConstants.SCHEMA_TYPE_ATTRIBUTE);
            if (null != type) {
                String namespaceURI = null;
                int colonIndex = type.indexOf(COLON);
                if (colonIndex > -1) {
                    String prefix = type.substring(0, colonIndex);
                    namespaceURI = unmarshalRecord.resolveNamespacePrefix(prefix);
                    type = type.substring(colonIndex + 1);
                }
                unmarshalRecord.setTypeQName(new QName(namespaceURI, type));
            }
        } else if (xmlField.getLastXPathFragment().isAttribute()) {
            if (!this.xmlCompositeDirectCollectionMapping.usesSingleNode()) {
                String namespaceURI = xmlField.getLastXPathFragment().getNamespaceURI();
                if (namespaceURI == null) {
                    namespaceURI = EMPTY_STRING;
                }
                String value = atts.getValue(namespaceURI, xmlField.getLastXPathFragment().getLocalName());
                Object collection = unmarshalRecord.getContainerInstance(this);
                addUnmarshalValue(unmarshalRecord, value, collection);
            }
        }
        return true;
    }

    public void endElement(XPathFragment xPathFragment, UnmarshalRecord unmarshalRecord) {
        Object value = unmarshalRecord.getStringBuffer().toString();
        Object collection = unmarshalRecord.getContainerInstance(this);
        unmarshalRecord.resetStringBuffer();

        if (xmlCompositeDirectCollectionMapping.usesSingleNode()) {
            StringTokenizer stringTokenizer = new StringTokenizer((String) value);
            while (stringTokenizer.hasMoreTokens()) {
                addUnmarshalValue(unmarshalRecord, stringTokenizer.nextToken(), collection);
            }
        } else {
            addUnmarshalValue(unmarshalRecord, value, collection);
        }
    }

    public void endElement(XPathFragment xPathFragment, UnmarshalRecord unmarshalRecord, Object collection) {
        Object value = unmarshalRecord.getStringBuffer().toString();
        unmarshalRecord.resetStringBuffer();

        if (xmlCompositeDirectCollectionMapping.usesSingleNode()) {
            StringTokenizer stringTokenizer = new StringTokenizer((String) value);
            while (stringTokenizer.hasMoreTokens()) {
                addUnmarshalValue(unmarshalRecord, stringTokenizer.nextToken(), collection);
            }
        } else {
            addUnmarshalValue(unmarshalRecord, value, collection);
        }
    }

    private void addUnmarshalValue(UnmarshalRecord unmarshalRecord, Object value, Object collection) {
        if ((null == value) || EMPTY_STRING.equals(value)) {
            return;
        }
        XMLField xmlField = (XMLField) xmlCompositeDirectCollectionMapping.getField();

        XMLConversionManager xmlConversionManager = (XMLConversionManager) unmarshalRecord.getSession().getDatasourcePlatform().getConversionManager();
        if (unmarshalRecord.getTypeQName() != null) {
            Class typeClass = xmlField.getJavaClass(unmarshalRecord.getTypeQName());
            value = xmlConversionManager.convertObject(value, typeClass, unmarshalRecord.getTypeQName());
        } else {
            value = xmlField.convertValueBasedOnSchemaType(value, xmlConversionManager);
        }

        if (xmlCompositeDirectCollectionMapping.hasValueConverter()) {
            if (xmlCompositeDirectCollectionMapping.getValueConverter() instanceof XMLConverter) {
                value = ((XMLConverter) xmlCompositeDirectCollectionMapping.getValueConverter()).convertDataValueToObjectValue(value, unmarshalRecord.getSession(), unmarshalRecord.getUnmarshaller());
            } else {
                value = xmlCompositeDirectCollectionMapping.getValueConverter().convertDataValueToObjectValue(value, unmarshalRecord.getSession());
            }
        }
        unmarshalRecord.addAttributeValue(this, value, collection);
    }

    public Object getContainerInstance() {
        return getContainerPolicy().containerInstance();
    }

    public void setContainerInstance(Object object, Object containerInstance) {
        xmlCompositeDirectCollectionMapping.setAttributeValueInObject(object, containerInstance);
    }

    public ContainerPolicy getContainerPolicy() {
        return xmlCompositeDirectCollectionMapping.getContainerPolicy();
    }

    public boolean isContainerValue() {
        return true;
    }

    public void marshalSingleValue(XPathFragment xPathFragment, MarshalRecord marshalRecord, Object object, Object value, AbstractSession session, NamespaceResolver namespaceResolver, MarshalContext marshalContext) {
        if (xmlCompositeDirectCollectionMapping.hasValueConverter()) {
            if (xmlCompositeDirectCollectionMapping.getValueConverter() instanceof XMLConverter) {
                value = ((XMLConverter) xmlCompositeDirectCollectionMapping.getValueConverter()).convertObjectValueToDataValue(value, session, marshalRecord.getMarshaller());
            } else {
                value = xmlCompositeDirectCollectionMapping.getValueConverter().convertObjectValueToDataValue(value, session);
            }
        }
        XMLField xmlField = (XMLField) xmlCompositeDirectCollectionMapping.getField();
        QName schemaType = getSchemaType(xmlField, value, session);
        String stringValue = getValueToWrite(schemaType, value, (XMLConversionManager) session.getDatasourcePlatform().getConversionManager());
        if (null != stringValue) {
            marshalRecord.openStartElement(xPathFragment, namespaceResolver);
            XPathFragment nextFragment = xPathFragment.getNextFragment();
            if (nextFragment.isAttribute()) {
                marshalRecord.attribute(nextFragment, namespaceResolver, stringValue);
                marshalRecord.closeStartElement();
            } else {
                if (xmlField.isTypedTextField()) {
                    String typeQName = namespaceResolver.resolveNamespaceURI(XMLConstants.SCHEMA_INSTANCE_URL) + ":type";
                    String schemaTypePrefix = namespaceResolver.resolveNamespaceURI(schemaType.getNamespaceURI());
                    marshalRecord.attribute(XMLConstants.SCHEMA_INSTANCE_URL, schemaType.getLocalPart(), typeQName, schemaTypePrefix + ':' + schemaType.getLocalPart());
                }
                marshalRecord.closeStartElement();
                if (xmlCompositeDirectCollectionMapping.isCDATA()) {
                    marshalRecord.cdata(stringValue);
                } else {
                    marshalRecord.characters(stringValue);
                }
            }
            marshalRecord.endElement(xPathFragment, namespaceResolver);
        }

    }

    public XMLCompositeDirectCollectionMapping getMapping() {
        return xmlCompositeDirectCollectionMapping;
    }

}