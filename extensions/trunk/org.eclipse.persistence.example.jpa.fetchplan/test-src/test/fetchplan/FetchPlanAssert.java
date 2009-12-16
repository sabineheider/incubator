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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.persistence.extension.fetchplan.FetchItem;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.queries.FetchGroupTracker;

/**
 * Helper class used by test cases to ensure that the expected attributes and
 * relationships were loaded.
 * 
 * @author dclarke
 * @since EclipseLink 2.0.0
 */
public class FetchPlanAssert {

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

    private static void assertFetched(FetchItem fetchItem, Object result) {
        Assert.assertNotNull("Null FetchItem", fetchItem);
        Assert.assertNotNull("Null Result", result);

        DatabaseMapping[] mappings = fetchItem.getMappings();
        Assert.assertNotNull(mappings);

        Object current = fetchItem.getEntityValue(result);

        for (int index = 0; index < mappings.length; index++) {
            if (current instanceof Collection<?>) {
                current = assertFetched(mappings[index], (Collection<?>) current);
            } else {
                current = assertFetched(mappings[index], current);
            }

            if (current == null) {
                break;
            }
        }
    }

    private static Object assertFetched(DatabaseMapping mapping, Object entity) {
        Assert.assertFalse("Collections not supported", entity instanceof Collection<?>);
        Assert.assertFalse("Maps not  supported", entity instanceof Map<?, ?>);

        if (mapping.isDirectToFieldMapping() && entity instanceof FetchGroupTracker) {
            Assert.assertTrue("FetchPlan did not load: '" + mapping.getAttributeName() + "' on " + entity, ((FetchGroupTracker) entity)._persistence_isAttributeFetched(mapping.getAttributeName()));
            return null;
        }

        Object value = mapping.getAttributeValueFromObject(entity);

        if (value instanceof IndirectContainer) {
            Assert.assertTrue("FetchPlan did not load: '" + mapping.getAttributeName() + "' on " + entity, ((IndirectContainer) value).isInstantiated());
        } else if (value instanceof ValueHolderInterface) {
            Assert.assertTrue("FetchPlan did not load: '" + mapping.getAttributeName() + "' on " + entity, ((ValueHolderInterface) value).isInstantiated());
            value = ((ValueHolderInterface) value).getValue();
        }

        return value;
    }

    private static Collection<Object> assertFetched(DatabaseMapping mapping, Collection<?> entities) {
        Collection<Object> results = new ArrayList<Object>();

        for (Object entity : entities) {
            Object result = assertFetched(mapping, entity);

            if (mapping.isForeignReferenceMapping() && ((ForeignReferenceMapping) mapping).getReferenceDescriptor() != null) {
                if (result instanceof Collection<?>) {
                    results.addAll((Collection<?>) result);
                } else if (result instanceof Map<?, ?>) {
                    // Assume for now that only the values can be entities
                    results.addAll(((Map<?, ?>) result).values());
                } else {
                    results.add(result);
                }
            }
        }
        return results;
    }
}
