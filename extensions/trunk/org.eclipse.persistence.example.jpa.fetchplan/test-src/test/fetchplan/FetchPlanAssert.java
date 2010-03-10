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

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import junit.framework.Assert;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.extension.fetchplan.FetchItem;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

import testing.EclipseLinkJPAAssert;

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

        for (FetchItem item : fetchPlan.getFetchItems()) {
            assertFetched(item, result);
        }
    }

    public static void assertFetched(FetchPlan fetchPlan, Object[] result, int resultIndex) {
        Assert.assertNotNull("Null FetchPlan", fetchPlan);
        Assert.assertNotNull("Null Result", result);

        for (FetchItem item : fetchPlan.getFetchItems()) {
            assertFetched(item, result[resultIndex]);
        }
    }

    public static void assertFetched(FetchPlan fetchPlan, Collection<?> results) {
        for (Object result : results) {
            if (result != null) {
                assertFetched(fetchPlan, result);
            }
        }
    }

    public static void assertFetched(FetchPlan fetchPlan, Collection<Object[]> results, int resultIndex) {
        for (Object[] result : results) {
            assertFetched(fetchPlan, result, resultIndex);
        }
    }

    private static void assertFetched(FetchItem fetchItem, Object result) {
        Assert.assertNotNull("Null FetchItem", fetchItem);
        Assert.assertNotNull("Null Result", result);

        // Check FetchGroup
        if (result instanceof FetchGroupTracker && ((FetchGroupTracker) result)._persistence_getFetchGroup() != null) {
            Assert.assertTrue(((FetchGroupTracker) result)._persistence_isAttributeFetched(fetchItem.getName()));
            return;
        }

        // Check actual value
        Object value = getMapping(fetchItem, null).getAttributeValueFromObject(result);
        if (value instanceof IndirectContainer) {
            Assert.assertTrue(((IndirectContainer) value).isInstantiated());
        } else if (value instanceof ValueHolderInterface) {
            Assert.assertTrue(((ValueHolderInterface) value).isInstantiated());
        }
    }

    public static void assertNotFetched(FetchPlan fetchPlan, Object result) {
        Assert.assertNotNull("Null FetchPlan", fetchPlan);
        Assert.assertNotNull("Null Result", result);

        for (FetchItem item : fetchPlan.getFetchItems()) {
            assertNotFetched(item, result);
        }
    }

    public static void assertNotFetched(FetchPlan fetchPlan, Collection<?> results) {
        for (Object result : results) {
            assertNotFetched(fetchPlan, result);
        }
    }

    private static void assertNotFetched(FetchItem fetchItem, Object result) {
        Assert.assertNotNull("Null FetchItem", fetchItem);
        Assert.assertNotNull("Null Result", result);

        // Check FetchGroup
        if (result instanceof FetchGroupTracker && ((FetchGroupTracker) result)._persistence_getFetchGroup() != null) {
            Assert.assertFalse(((FetchGroupTracker) result)._persistence_isAttributeFetched(fetchItem.getName()));
            return;
        }

        // Check actual value
        Object value = getMapping(fetchItem, null).getAttributeValueFromObject(result);
        if (value instanceof IndirectContainer) {
            Assert.assertFalse(((IndirectContainer) value).isInstantiated());
        } else if (value instanceof ValueHolderInterface) {
            Assert.assertFalse(((ValueHolderInterface) value).isInstantiated());
        }
    }

    private static Method GET_MAPPING_METHOD = null;

    /**
     * Helper to access the protected getMapping method on FetchItem
     */
    private static DatabaseMapping getMapping(FetchItem item, Session session) {
        try {
            if (GET_MAPPING_METHOD == null) {
                GET_MAPPING_METHOD = PrivilegedAccessHelper.getMethod(FetchItem.class, "getMapping", new Class[] { Session.class }, true);
            }

            return (DatabaseMapping) PrivilegedAccessHelper.invokeMethod(GET_MAPPING_METHOD, item, new Object[] { session });
        } catch (Exception e) {
            throw new RuntimeException("FetchPlanAssert.getMapping failed", e);
        }
    }

    public static void verifyEmployeeConfig(EntityManagerFactory emf) {
        ClassDescriptor employeeDescriptor = EclipseLinkJPAAssert.assertEntity(emf, "Employee");
        assertTrue("Entity does not implement FetchGroupTracker: " + employeeDescriptor, FetchGroupTracker.class.isAssignableFrom(employeeDescriptor.getJavaClass()));
        EclipseLinkJPAAssert.assertLazy(employeeDescriptor, "address");
        EclipseLinkJPAAssert.assertLazy(employeeDescriptor, "phoneNumbers");
        EclipseLinkJPAAssert.assertLazy(employeeDescriptor, "manager");
        EclipseLinkJPAAssert.assertLazy(employeeDescriptor, "managedEmployees");

        ClassDescriptor addressDescriptor = EclipseLinkJPAAssert.assertEntity(emf, "Address");
        assertTrue("Entity does not implement FetchGroupTracker: " + employeeDescriptor, FetchGroupTracker.class.isAssignableFrom(addressDescriptor.getJavaClass()));

        ClassDescriptor phoneDescriptor = EclipseLinkJPAAssert.assertEntity(emf, "PhoneNumber");
        assertTrue("Entity does not implement FetchGroupTracker: " + employeeDescriptor, FetchGroupTracker.class.isAssignableFrom(phoneDescriptor.getJavaClass()));
    }

}
