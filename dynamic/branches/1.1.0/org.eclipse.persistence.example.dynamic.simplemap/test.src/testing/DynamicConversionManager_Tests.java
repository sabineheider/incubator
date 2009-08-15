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
 *     dclarke - SimpleDynamicMap Example - Bug 277731
 *               http://wiki.eclipse.org/EclipseLink/Examples/JPA/Dynamic/SimpleDynamicMap
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing;

import static junit.framework.Assert.*;

import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.junit.Test;

import example.DynamicMapEntity;

public class DynamicConversionManager_Tests {

    private DynamicConversionManager dcm = new DynamicConversionManager(ConversionManager.getDefaultManager());

    @Test
    public void validate_createDynamicType() throws ClassNotFoundException {
        Class dynamicType = this.dcm.getDynamicClassLoader().createDynamicClass("model.SimpleType", DynamicMapEntity.class);

        assertNotNull(dynamicType);
        assertTrue(DynamicMapEntity.class.isAssignableFrom(dynamicType));

        Class dynamicType2 = this.dcm.getDynamicClassLoader().loadClass("model.SimpleType");

        assertNotNull(dynamicType2);
        assertSame(dynamicType, dynamicType2);

        Class dynamicType3 = Class.forName("model.SimpleType", true, this.dcm.getDynamicClassLoader());

        assertNotNull(dynamicType3);
        assertSame(dynamicType, dynamicType3);

        Class dynamicType4 = this.dcm.convertClassNameToClass("model.Simple");

        assertNotNull(dynamicType4);
        // TODO assertSame(dynamicType, dynamicType4);
}

}
