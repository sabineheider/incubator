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
* mmacivor - Feb 06/2009 - Initial implementation
******************************************************************************/
package org.eclipse.persistence.internal.oxm.accessor;

import org.eclipse.persistence.indirection.ValueHolder;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.indirection.WeavedAttributeValueHolderInterface;
import org.eclipse.persistence.mappings.AttributeAccessor;
/**
 * INTERNAL:
 * A custom AttriuteAccessor to be used when the same object is mapped in both
 * OXM and ORM. This will bridge the gap between the two for attributes that use
 * ValueHolders. Specifically for JPA weaving.
 * @author matt.macivor
 *
 */
public class ValueHolderAttributeAccessor extends AttributeAccessor {
    private AttributeAccessor ormAccessor;
    private AttributeAccessor oxmAccessor;

    public ValueHolderAttributeAccessor(AttributeAccessor ormAccessor, AttributeAccessor oxmAccessor) {
        this.ormAccessor = ormAccessor;
        this.oxmAccessor = oxmAccessor;
    }

    public Object getAttributeValueFromObject(Object object) {
        ValueHolderInterface vh = (ValueHolderInterface)ormAccessor.getAttributeValueFromObject(object);
        if(vh != null && !vh.isInstantiated()) {
            Object value = vh.getValue();
            oxmAccessor.setAttributeValueInObject(object, value);
            if(vh instanceof WeavedAttributeValueHolderInterface) {
                ((WeavedAttributeValueHolderInterface)vh).setIsCoordinatedWithProperty(true);
            }
        }
        return oxmAccessor.getAttributeValueFromObject(object);
    }
	
    public void setAttributeValueInObject(Object object, Object value) {
        ValueHolderInterface vh = (ValueHolderInterface)ormAccessor.getAttributeValueFromObject(object);
        if(vh == null) {
            vh = new ValueHolder();
            ((ValueHolder)vh).setIsNewlyWeavedValueHolder(true);
        }
        vh.setValue(value);
        ormAccessor.setAttributeValueInObject(object, vh);
        oxmAccessor.setAttributeValueInObject(object, value);
    }
}
