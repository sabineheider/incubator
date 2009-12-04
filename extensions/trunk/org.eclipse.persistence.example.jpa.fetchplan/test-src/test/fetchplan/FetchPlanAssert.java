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
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.persistence.extension.fetchplan.FetchItem;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.queries.FetchGroupTracker;

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

        Object current =  fetchItem.getEntityValue(result);
        
        for (int index = 0; index < mappings.length; index++) {
            Assert.assertFalse("Collections not yet supported", current instanceof Collection<?>);
            Assert.assertFalse("Maps not yet supported", current instanceof Map<?, ?>);

            Object value = mappings[index].getAttributeValueFromObject(current);

            if (value instanceof IndirectContainer) {
                Assert.assertTrue(((IndirectContainer) value).isInstantiated());
            } else if (value instanceof ValueHolderInterface) {
                Assert.assertTrue(((ValueHolderInterface) value).isInstantiated());
                value = ((ValueHolderInterface) value).getValue();
            }
            current = value;
            
            if (current == null) {
                break;
            }
            
            // TODO" If the value is a collection/map then we need to iterate over each of them
            // asserting its children are loaded.
        }
    }
    
    public static Object assertLoaded(DatabaseMapping mapping, Object entity) {
        Assert.assertEquals("Incorrect class", mapping.getDescriptor().getJavaClass(), entity.getClass());
        
        if (entity instanceof Collection<?>) {
            for (Object o : ((Collection<?>) entity)) {
                assertLoaded(mapping, o);
            }
            return null;
        }

        Object value = mapping.getAttributeValueFromObject(entity);

        if (mapping.isDatabaseMapping()) {
            if (entity instanceof FetchGroupTracker) {
                Assert.assertTrue("Attribute not fetched: " + mapping.getAttributeName(), ((FetchGroupTracker) entity)._persistence_isAttributeFetched(mapping.getAttributeName()));
            }
        } else if (value instanceof IndirectContainer) {
            Assert.assertTrue(((IndirectContainer) value).isInstantiated());
        } else if (value instanceof ValueHolderInterface) {
            Assert.assertTrue(((ValueHolderInterface) value).isInstantiated());
            value = ((ValueHolderInterface) value).getValue();
        }
        return value;
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
