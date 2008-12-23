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
 *     bdoughan - JPA DAS INCUBATOR - Enhancement 258057
 *     			 http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package com.example.customer.eclipselink;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.descriptors.InstanceVariableAttributeAccessor;
import org.eclipse.persistence.oxm.mappings.XMLCompositeCollectionMapping;
import org.eclipse.persistence.oxm.mappings.XMLCompositeObjectMapping;


/**
 * This class contains the after load methods referred to by the descriptors.
 * After load methods are required to specify settings that are not available
 * in the EclipseLink Workbench.
 */
public class DescriptorAfterLoads  {

	public static void amendCustomerDescriptor(ClassDescriptor descriptor) {
	    XMLCompositeCollectionMapping phoneNumbersMapping = (XMLCompositeCollectionMapping) descriptor.getMappingForAttributeName("phoneNumbers");
	    InstanceVariableAttributeAccessor containerAccessor = new InstanceVariableAttributeAccessor();
	    containerAccessor.setAttributeName("customer");
	    phoneNumbersMapping.setContainerAccessor(containerAccessor);
	    
        XMLCompositeObjectMapping billingAddressMapping = (XMLCompositeObjectMapping) descriptor.getMappingForAttributeName("billingAddress");
        InstanceVariableAttributeAccessor containerAccessor3 = new InstanceVariableAttributeAccessor();
        containerAccessor3.setAttributeName("customer");
        billingAddressMapping.setContainerAccessor(containerAccessor3);

        XMLCompositeObjectMapping shippingAddressMapping = (XMLCompositeObjectMapping) descriptor.getMappingForAttributeName("shippingAddress");
        InstanceVariableAttributeAccessor containerAccessor2 = new InstanceVariableAttributeAccessor();
        containerAccessor2.setAttributeName("customer");
        shippingAddressMapping.setContainerAccessor(containerAccessor2);
	    
	}
  
}
