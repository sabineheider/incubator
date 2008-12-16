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
 *    bdoughan - JPA DAS INCUBATOR - Enhancement 258057
 *               http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.sdo.helper.jaxb;

import org.eclipse.persistence.sdo.SDODataObject;
import org.eclipse.persistence.sdo.SDOType;
import org.eclipse.persistence.sdo.helper.delegates.SDODataFactoryDelegate;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;

/**
 * This implementation of commonj.sdo.helper.DataFactory is responsible for 
 * ensuring that newly created DataObjects are assigned a JAXB aware value store. 
 */
public class JAXBDataFactory extends SDODataFactoryDelegate {

    public JAXBDataFactory(HelperContext helperContext) {
        super(helperContext);
    }
 
    public JAXBHelperContext getHelperContext() {
        return (JAXBHelperContext) super.getHelperContext();
    }

    public DataObject create(Type type) {
        SDODataObject dataObject = (SDODataObject) super.create(type);
        JAXBValueStore jpaValueStore = new JAXBValueStore(getHelperContext(),((SDOType)type).getQName());
        jpaValueStore.initialize(dataObject);
        dataObject._setCurrentValueStore(jpaValueStore);
        return dataObject;
    }

}