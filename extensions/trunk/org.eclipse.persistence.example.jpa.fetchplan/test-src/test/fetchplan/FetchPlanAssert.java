/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     dclarke - Fetch Plan Extension Incubator
 ******************************************************************************/
package test.fetchplan;

import java.util.Collection;

import junit.framework.Assert;

import org.eclipse.persistence.extension.fetchplan.FetchItem;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.mappings.DatabaseMapping;

/**
 * Helper class used by test cases to ensure that the expected attributes and
 * relationships were loaded.
 * 
 * @author dclarke
 * @since EclipseLink 2.0.0
 */
public class FetchPlanAssert {

    public static void assertFetched(FetchItem fetchItem, Object result) {
        Assert.assertNotNull("Null FetchItem", fetchItem);
        Assert.assertNotNull("Null Result", result);

        DatabaseMapping[] mappings = fetchItem.getMappings();
        Assert.assertNotNull(mappings);

        Object current = result;
        for (int index = 0; index < mappings.length; index++) {
            Object value = mappings[index].getAttributeValueFromObject(current);

            if (value instanceof IndirectContainer) {
                Assert.assertTrue(((IndirectContainer) value).isInstantiated());
            } else if (value instanceof ValueHolderInterface) {
                Assert.assertTrue(((ValueHolderInterface) value).isInstantiated());
            }
            current = value;
        }
    }

    public static void assertFetched(FetchPlan fetchPlan, Object result) {
        Assert.assertNotNull("Null FetchPlan", fetchPlan);
        Assert.assertNotNull("Null Result", result);

        for (FetchItem item : fetchPlan.getItems()) {
            assertFetched(item, result);
        }
    }

    public static void assertFetched(FetchPlan fetchPlan, Collection<?> results) {
        for (Object result : results) {
            assertFetched(fetchPlan, result);
        }
    }
}
