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
*     bdoughan - Jan 27/2009 - 1.1 - Initial implementation
******************************************************************************/
package org.eclipse.persistence.sdo.helper.jaxb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.namespace.QName;

import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.exceptions.SDOException;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.indirection.WeavedAttributeValueHolderInterface;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.oxm.MappingNodeValue;
import org.eclipse.persistence.internal.oxm.TreeObjectBuilder;
import org.eclipse.persistence.internal.oxm.XPathFragment;
import org.eclipse.persistence.internal.oxm.XPathNode;
import org.eclipse.persistence.internal.queries.ContainerPolicy;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.mappings.ContainerMapping;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.oxm.mappings.XMLCompositeCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLCompositeObjectMapping;
import org.eclipse.persistence.oxm.mappings.XMLDirectMapping;
import org.eclipse.persistence.oxm.mappings.XMLObjectReferenceMapping;
import org.eclipse.persistence.sdo.SDODataObject;
import org.eclipse.persistence.sdo.SDOProperty;
import org.eclipse.persistence.sdo.SDOType;
import org.eclipse.persistence.sdo.ValueStore;
import org.eclipse.persistence.sdo.helper.ListWrapper;

import commonj.sdo.DataObject;
import commonj.sdo.Property;

/**
 * The JAXBValueStore enables a DataObject to access data from a POJO.
 * The link between an SDO property and a POJO property is through their
 * XML representation.  For the POJO property this corresponds to its
 * JAXB mapping.
 */
public class JAXBValueStore implements ValueStore {

    private JAXBHelperContext jaxbHelperContext;
    private Object entity;
    private XMLDescriptor descriptor;
    private SDODataObject dataObject;
    private Map<Property, ListWrapper> listWrappers;
    private Map<Property, ValueHolderInterface> valueHolders;
    private Field[] valueHolderFields;
    private Method[] valueHolderInitMethods;
    
    private static String VALUEHOLDER_ATTRIBUTE_PREFIX = "_persistence_";
    private static String VALUEHOLDER_ATTRIBUTE_SUFFIX = "_vh";
    private static String VALUEHOLDER_INIT_METHOD_NAME = "initialize_";

    public JAXBValueStore(JAXBHelperContext aJAXBHelperContext, SDOType sdoType) {
        this.jaxbHelperContext = aJAXBHelperContext;
        QName xsdQName = sdoType.getXsdType();
        if(null == xsdQName) {
            xsdQName = sdoType.getQName();
        }
        listWrappers = new WeakHashMap<Property, ListWrapper>();
        valueHolders = new WeakHashMap<Property, ValueHolderInterface>();
        XPathFragment xPathFragment = new XPathFragment(xsdQName.getLocalPart());
        xPathFragment.setNamespaceURI(xsdQName.getNamespaceURI());
        JAXBContext jaxbContext = (JAXBContext) jaxbHelperContext.getJAXBContext();
        this.descriptor = jaxbContext.getXMLContext().getDescriptorByGlobalType(xPathFragment);
        if (null == this.descriptor) {
            this.descriptor = jaxbContext.getXMLContext().getDescriptor(xsdQName);
            if (null == this.descriptor) {
                throw SDOException.sdoJaxbNoDescriptorForType(sdoType.getQName(), xsdQName);
            }
        }
        this.entity = descriptor.getInstantiationPolicy().buildNewInstance();
    }

    public JAXBValueStore(JAXBHelperContext aJAXBHelperContext, Object anEntity) {
        this.jaxbHelperContext = aJAXBHelperContext;
        this.listWrappers = new WeakHashMap<Property, ListWrapper>();
        valueHolders = new WeakHashMap<Property, ValueHolderInterface>();
        JAXBContext jaxbContext = (JAXBContext) jaxbHelperContext.getJAXBContext();
        this.descriptor = (XMLDescriptor) jaxbContext.getXMLContext().getSession(anEntity).getDescriptor(anEntity);
        this.entity = anEntity;
    }

    /**
     * Return the DataObject associated with this value store.
     */
    SDODataObject getDataObject() {
        return dataObject;
    }

    /**
     * Return the POJO associated with this value store. 
     */
    Object getEntity() {
        return entity;
    }

    /**
     * Return the XMLDescriptor associated with this value store.
     * This is the XMLDescriptor for the associated POJO. 
     */
    XMLDescriptor getEntityDescriptor() {
        return descriptor;
    }

    /**
     * Return the JAXBHelperContext.  This is the JAXBHelperContext
     * used to create the DataObject.  
     */
    JAXBHelperContext getJAXBHelperContext() {
        return jaxbHelperContext;
    }

    /**
     * Initialize the value store with its associated DataObject.
     */
    public void initialize(DataObject aDataObject) {
        this.dataObject = (SDODataObject) aDataObject;
        initValueHolderFields((SDOType)aDataObject.getType(), this.entity.getClass());
    }

    /**
     * Get the value from the wrapped POJO, wrapping in DataObjects as
     * necessary.
     */
    public Object getDeclaredProperty(int propertyIndex) {
        SDOProperty declaredProperty = (SDOProperty) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        System.out.println("BLAHBLAHBLAH");
        DatabaseMapping mapping = this.getJAXBMappingForProperty(declaredProperty);
        if(this.valueHolderFields[propertyIndex] != null) {
        	triggerValueHolderIfRequired(this.entity, declaredProperty, mapping);
        }
        Object value = mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
        if (null == value || declaredProperty.getType().isDataType()) {
            if (declaredProperty.isMany()) {
                return new JAXBListWrapper(this, declaredProperty);
            } else {
                return value;
            }
        } else if (declaredProperty.isMany()) {
            ListWrapper listWrapper = listWrappers.get(declaredProperty);
            if (null != listWrapper) {
                return listWrapper;
            }
            listWrapper = new JAXBListWrapper(this, declaredProperty);
            listWrappers.put(declaredProperty, listWrapper);
            return listWrapper;
        } else {
            if(declaredProperty.isContainment()) {
                return jaxbHelperContext.wrap(value, declaredProperty, dataObject);
            } else {
                return jaxbHelperContext.wrap(value);
            }
        }
    }

    /**
     * Set the value on the underlying POJO, unwrapping values as necessary.
     */
    public void setDeclaredProperty(int propertyIndex, Object value) {
        SDOProperty declaredProperty = (SDOProperty) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        DatabaseMapping mapping = this.getJAXBMappingForProperty(declaredProperty);
        Object newValue = value;
        if(this.valueHolderFields[propertyIndex] != null) {
            triggerValueHolderIfRequired(this.entity, declaredProperty, mapping);
        }
        Object oldValue = mapping.getAttributeAccessor().getAttributeValueFromObject(entity);

        if (declaredProperty.getType().isDataType()) {
            if (!declaredProperty.isMany()) {
                AbstractSession session = ((JAXBContext) jaxbHelperContext.getJAXBContext()).getXMLContext().getSession(entity);
                XMLDirectMapping directMapping = (XMLDirectMapping) mapping;
                if (directMapping.hasConverter()) {
                    newValue = directMapping.getConverter().convertDataValueToObjectValue(newValue, session);
                } else {
                    DatabaseField field = mapping.getField();
                    newValue = session.getDatasourcePlatform().getConversionManager().convertObject(newValue, descriptor.getObjectBuilder().getFieldClassification(field));
                }
            }
            mapping.setAttributeValueInObject(entity, newValue);
        } else if (declaredProperty.isMany()) {
            // Get a ListWrapper and set it's current elements
            ListWrapper listWrapper = (ListWrapper) getDeclaredProperty(propertyIndex);
            listWrapper.setCurrentElements((List) newValue);
        } else {
            // OLD VALUE
            if (mapping.isAbstractCompositeObjectMapping()) {
                XMLCompositeObjectMapping compositeMapping = (XMLCompositeObjectMapping) mapping;
                if (oldValue != null && compositeMapping.getContainerAccessor() != null) {
                    if(oldValue instanceof ChangeTracker) {
                        PropertyChangeListener listener = ((ChangeTracker) oldValue)._persistence_getPropertyChangeListener();
                        if(null != listener) {
                           Object oldValueOldContainer = compositeMapping.getContainerAccessor().getAttributeValueFromObject(oldValue);
                           listener.propertyChange(new PropertyChangeEvent(oldValue, compositeMapping.getContainerAttributeName(), null, oldValueOldContainer));
                        }
                    }
                    compositeMapping.getContainerAccessor().setAttributeValueInObject(oldValue, null);
                }
            }

            // NEW VALUE
            newValue = jaxbHelperContext.unwrap((DataObject) value);
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, newValue);
            if(valueHolderFields[propertyIndex] != null) {
            	this.setValueInValueHolder(declaredProperty, newValue);
            }
            if (mapping.isAbstractCompositeObjectMapping()) {
                XMLCompositeObjectMapping compositeMapping = (XMLCompositeObjectMapping) mapping;
                if (value != null && compositeMapping.getContainerAccessor() != null) {
                    if(newValue instanceof ChangeTracker) {
                        PropertyChangeListener listener = ((ChangeTracker) newValue)._persistence_getPropertyChangeListener();
                        if(null != listener) {
                            Object newValueOldContainer = compositeMapping.getContainerAccessor().getAttributeValueFromObject(newValue);
                            listener.propertyChange(new PropertyChangeEvent(newValue, compositeMapping.getContainerAttributeName(), entity, newValueOldContainer));
                        }
                    }
                    compositeMapping.getContainerAccessor().setAttributeValueInObject(newValue, entity);
                }
            }
        }

        // If the target entity is using attribute change tracking then we need
        // to force the property event to be fired
        if (entity instanceof ChangeTracker) {
            PropertyChangeListener listener = ((ChangeTracker) entity)._persistence_getPropertyChangeListener();
            if (listener != null) {
                listener.propertyChange(new PropertyChangeEvent(entity, mapping.getAttributeName(), newValue, oldValue));
            }
        }
    }

    /**
     * For isMany=false properties return true if not null. For collection properties
     * return true if the collection is not empty.
     */
    public boolean isSetDeclaredProperty(int propertyIndex) {
        SDOProperty declaredProperty = (SDOProperty) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        DatabaseMapping mapping = this.getJAXBMappingForProperty(declaredProperty);
        if (declaredProperty.isMany()) {
            Collection collection = (Collection) mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
            if (null == collection) {
                return false;
            }
            return !collection.isEmpty();
        } else {
        	if(this.valueHolderFields[propertyIndex] != null) {
        		//trigger the valueholder
        		this.triggerValueHolderIfRequired(entity, declaredProperty, mapping);
        	}
            return null != mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
        }
    }

    /**
     * For isMany=false properties set the value to null. For isMany=true set
     * the value to an empty container of the appropriate type.
     */
    public void unsetDeclaredProperty(int propertyIndex) {
        SDOProperty declaredProperty = (SDOProperty) dataObject.getType().getDeclaredProperties().get(propertyIndex);
        DatabaseMapping mapping = this.getJAXBMappingForProperty(declaredProperty);
        if (declaredProperty.isMany()) {
            ContainerMapping containerMapping = (ContainerMapping) mapping;
            ContainerPolicy containerPolicy = containerMapping.getContainerPolicy();

            // OLD VALUE
            if (mapping.isAbstractCompositeCollectionMapping()) {
                XMLCompositeCollectionMapping compositeMapping = (XMLCompositeCollectionMapping) mapping;
                if (compositeMapping.getContainerAccessor() != null) {
                    
                    Object oldContainer = mapping.getAttributeValueFromObject(entity);
                    if (oldContainer != null) {
                        AbstractSession session = ((JAXBContext) jaxbHelperContext.getJAXBContext()).getXMLContext().getSession(entity);
                        Object iterator = containerPolicy.iteratorFor(oldContainer);
                        while (containerPolicy.hasNext(iterator)) {
                            Object oldValue = containerPolicy.next(iterator, session);
                            compositeMapping.getContainerAccessor().setAttributeValueInObject(oldValue, null);
                        }
                    }
                }
            }

            // NEW VALUE
            Object container = containerPolicy.containerInstance();
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, container);
        } else {
            // OLD VALUE
            if(this.valueHolderFields[propertyIndex] != null) {
            	triggerValueHolderIfRequired(this.entity, declaredProperty, mapping);
            }
            Object oldValue = mapping.getAttributeAccessor().getAttributeValueFromObject(entity);
            if (mapping.isAbstractCompositeObjectMapping()) {
                XMLCompositeObjectMapping compositeMapping = (XMLCompositeObjectMapping) mapping;
                if (compositeMapping.getContainerAccessor() != null) {
                    if (oldValue != null) {
                        if (oldValue instanceof ChangeTracker) {
                            PropertyChangeListener listener = ((ChangeTracker) oldValue)._persistence_getPropertyChangeListener();
                            if (listener != null) {
                                Object oldValueOldContainer = compositeMapping.getContainerAccessor().getAttributeValueFromObject(oldValue); 
                                listener.propertyChange(new PropertyChangeEvent(oldValue, compositeMapping.getContainerAttributeName(), null, oldValueOldContainer));
                            }
                        }
                        compositeMapping.getContainerAccessor().setAttributeValueInObject(oldValue, null);
                    }
                }
            }

            // NEW VALUE
            if (entity instanceof ChangeTracker) {
                PropertyChangeListener listener = ((ChangeTracker) entity)._persistence_getPropertyChangeListener();
                if (listener != null) {
                    listener.propertyChange(new PropertyChangeEvent(entity, mapping.getAttributeName(), null, oldValue));
                }
            }
            mapping.getAttributeAccessor().setAttributeValueInObject(entity, null);
            setValueInValueHolder(declaredProperty, null);
        }
    }

    public Object getOpenContentProperty(Property property) {
        throw new UnsupportedOperationException();
    }

    public void setOpenContentProperty(Property property, Object value) {
        throw new UnsupportedOperationException();
    }

    public boolean isSetOpenContentProperty(Property property) {
        throw new UnsupportedOperationException();
    }

    public void unsetOpenContentProperty(Property property) {
        throw new UnsupportedOperationException();
    }

    public void setManyProperty(Property property, Object value) {
        DatabaseMapping mapping = this.getJAXBMappingForProperty((SDOProperty) property);
        ContainerMapping containerMapping = (ContainerMapping) mapping;
        ContainerPolicy containerPolicy = containerMapping.getContainerPolicy();
        AbstractSession session = ((JAXBContext) jaxbHelperContext.getJAXBContext()).getXMLContext().getSession(entity);
        Collection collection = (Collection) value;
        if (!property.getType().isDataType()) {
            collection = getJAXBHelperContext().unwrap(collection);
        }

        Iterator collectionIterator = collection.iterator();
        Object container = containerMapping.getContainerPolicy().containerInstance();
        while (collectionIterator.hasNext()) {
            Object collectionValue = collectionIterator.next();
            containerPolicy.addInto(collectionValue, container, session);
        }
        mapping.setAttributeValueInObject(entity, container);
    }

    public ValueStore copy() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return the JAXB mapping for the SDO property.  They are matched
     * on their XML schema representation. 
     */
    DatabaseMapping getJAXBMappingForProperty(SDOProperty sdoProperty) {
        DatabaseMapping sdoMapping = sdoProperty.getXmlMapping();
        XMLField field;
        if (sdoMapping instanceof XMLObjectReferenceMapping) {
            XMLObjectReferenceMapping referenceMapping = (XMLObjectReferenceMapping) sdoMapping;
            field = (XMLField) referenceMapping.getFields().get(0);
        } else {
            field = (XMLField) sdoMapping.getField();
        }
        TreeObjectBuilder treeObjectBuilder = (TreeObjectBuilder) descriptor.getObjectBuilder();
        XPathNode xPathNode = treeObjectBuilder.getRootXPathNode();
        XPathFragment xPathFragment = field.getXPathFragment();
        while (xPathNode != null && xPathFragment != null) {
            if (xPathFragment.isAttribute()) {
                if (sdoProperty.isMany() && !sdoProperty.isContainment() && !sdoProperty.getType().isDataType()) {
                    xPathFragment = null;
                    break;
                }
                Map attributeChildrenMap = xPathNode.getAttributeChildrenMap();
                if (null == attributeChildrenMap) {
                    xPathNode = null;
                } else {
                    xPathNode = (XPathNode) xPathNode.getAttributeChildrenMap().get(xPathFragment);
                }
            } else {
                Map nonAttributeChildrenMap = xPathNode.getNonAttributeChildrenMap();
                if (null == nonAttributeChildrenMap) {
                    xPathNode = null;
                } else {
                    xPathNode = (XPathNode) xPathNode.getNonAttributeChildrenMap().get(xPathFragment);
                }
            }
            xPathFragment = xPathFragment.getNextFragment();
            if (xPathFragment != null && xPathFragment.nameIsText()) {
                if (sdoProperty.isMany() && !sdoProperty.isContainment()) {
                    xPathFragment = null;
                    break;
                }
            }
        }
        if (null == xPathFragment && xPathNode != null) {
            if (xPathNode.getNodeValue().isMappingNodeValue()) {
                MappingNodeValue mappingNodeValue = (MappingNodeValue) xPathNode.getNodeValue();
                return mappingNodeValue.getMapping();
            }
        }
        throw SDOException.sdoJaxbNoMappingForProperty(sdoProperty.getName(), field.getXPath());
    }
    
    private void triggerValueHolderIfRequired(Object entity, SDOProperty property, DatabaseMapping mapping) {
    	String propertyName = property.getName();
    	String vhAttribute = VALUEHOLDER_ATTRIBUTE_PREFIX + propertyName + VALUEHOLDER_ATTRIBUTE_SUFFIX;
    	WeavedAttributeValueHolderInterface vh = (WeavedAttributeValueHolderInterface)this.valueHolders.get(property);
    	if(vh == null) {
    		try { 
    			Field field = PrivilegedAccessHelper.getDeclaredField(entity.getClass(), vhAttribute, true);
    			vh = (WeavedAttributeValueHolderInterface)PrivilegedAccessHelper.getValueFromField(field, entity);
    			this.valueHolders.put(property, vh);
    		} catch(Exception ex) {
    			//If the valueholder field doesn't exist on this object, then don't worry about
    			//tiggering it.
    		}
    		//trigger the vh
    	}
    	if(vh != null && !vh.isInstantiated()) {
    		Object value = vh.getValue();
    		mapping.setAttributeValueInObject(entity, value);
    	}
    		
    }
    
    private void setValueInValueHolder(SDOProperty property, Object value) {
    	//See if there's a valueholder for this property:
    	WeavedAttributeValueHolderInterface vh = (WeavedAttributeValueHolderInterface)this.valueHolders.get(property);
    	if(vh != null) {
    		vh.setValue(value);
    		vh.setIsCoordinatedWithProperty(true);
    	} else {
    			Field field = this.valueHolderFields[property.getIndexInType()];
    			Method initMethod = this.valueHolderInitMethods[property.getIndexInType()];
    			try {
    				if(initMethod != null) {
    					PrivilegedAccessHelper.invokeMethod(initMethod, entity);
    				}
    				vh = (WeavedAttributeValueHolderInterface)PrivilegedAccessHelper.getValueFromField(field, entity);
    			} catch(Exception ex) {
    				//unable to invoke method and/or try to get VH from object
    				//throw exception
    			}
    			if(vh != null) {
    				vh.setValue(value);
    				vh.setIsCoordinatedWithProperty(true);
    				this.valueHolders.put(property, vh);
    			}
    	}
    	
    }
    
    private void initValueHolderFields(SDOType type, Class entityType) {
    	List properties = type.getDeclaredProperties();
    	this.valueHolderFields = new Field[properties.size()];
    	this.valueHolderInitMethods = new Method[properties.size()];

    	for(int i = 0; i < properties.size(); i++) {
    		SDOProperty next = (SDOProperty)properties.get(i);
            DatabaseMapping mapping = this.getJAXBMappingForProperty(next);
    		if(next.getType().isDataType() || next.isMany() || mapping.getAttributeAccessor().isMethodAttributeAccessor()) {
    			valueHolderFields[next.getIndexInType()] = null;
    			valueHolderInitMethods[next.getIndexInType()] = null;
    		} else {
    			//check for the value holder field
        		String vhAttributeName = VALUEHOLDER_ATTRIBUTE_PREFIX + next.getName() + VALUEHOLDER_ATTRIBUTE_SUFFIX;
        		String initMethodName = VALUEHOLDER_ATTRIBUTE_PREFIX + VALUEHOLDER_INIT_METHOD_NAME + next.getName() + VALUEHOLDER_ATTRIBUTE_SUFFIX;
        		Method initMethod = null;
        		Field field = null;
        		try {
        			field = PrivilegedAccessHelper.getDeclaredField(entity.getClass(), vhAttributeName, true);
        			initMethod = PrivilegedAccessHelper.getDeclaredMethod(entity.getClass(), initMethodName, new Class[]{});
        		} catch(Exception ex) {
        			//if we don't find an associated vh field, then weaving wasn't turned on.
        		}
        		valueHolderFields[next.getIndexInType()] = field;
        		valueHolderInitMethods[next.getIndexInType()] = initMethod;
    		}
    	}
    }

}