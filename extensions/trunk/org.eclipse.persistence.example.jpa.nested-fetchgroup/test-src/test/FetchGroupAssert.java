package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.VersionLockingPolicy;
import org.eclipse.persistence.exceptions.QueryException;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.queries.ManagedFetchItem;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.queries.FetchGroup.FetchItem;
import org.eclipse.persistence.sessions.server.Server;

/**
 * Test utility to verify the state of entities after they are loaded, copied,
 * or detached with respect to a defined FetchGroup.
 * 
 * @author dclarke
 * @since EclipseLink 2.1.0
 */
public class FetchGroupAssert {

    /**
     * Verify that a FetchGroup is valid with respect to the mappings of the
     * provided entity class.
     */
    public static boolean isValid(FetchGroup<?> fetchGroup, EntityManagerFactory emf, Class<?> entityClass) {
        assertNotNull(fetchGroup);

        try {
            for (Map.Entry<String, FetchItem> entry : fetchGroup.getFetchItems().entrySet()) {
                ManagedFetchItem itemImpl = (ManagedFetchItem) entry.getValue();
                DatabaseMapping mapping = itemImpl.getMapping(JpaHelper.getServerSession(emf), entityClass);

                if (mapping.isForeignReferenceMapping()) {
                    if (itemImpl.getFetchGroup() != null) {
                        if (!isValid(itemImpl.getFetchGroup(), emf, ((ForeignReferenceMapping) mapping).getReferenceClass())) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        } catch (QueryException qe) {
            return false;
        }
        return true;
    }

    /**
     * Verify that the attribute path specified is loaded in the provided entity
     */
    public static void assertFetchedAttribute(EntityManagerFactory emf, Object entity, String... attribute) {
        assertNotNull("EntityManagerFactory is null", emf);
        assertNotNull("Entity is null", entity);
        Server session = JpaHelper.getServerSession(emf);
        assertNotNull("No Server session found for: " + emf, session);
        ClassDescriptor desc = session.getClassDescriptor(entity);
        assertNotNull("No descriptor found for: " + entity, desc);

        Object value = entity;
        if (attribute.length > 1) {
            String attrName = attribute[1];

            if (desc.hasFetchGroupManager()) {
                assertTrue("Attribute: '" + attrName + "' not fetched on: " + value, desc.getFetchGroupManager().isAttributeFetched(session, value, attrName));
            }
            DatabaseMapping mapping = desc.getMappingForAttributeName(attrName);
            value = mapping.getAttributeValueFromObject(value);

            if (value instanceof IndirectContainer) {
                value = ((IndirectContainer) value).getValueHolder();
            }
            if (value instanceof ValueHolderInterface) {
                ValueHolderInterface vhi = (ValueHolderInterface) value;
                assertTrue("ValueHolder for: '" + attrName + "' not instantiated", vhi.isInstantiated());
                value = vhi.getValue();
            }
            String[] tail = new String[attribute.length - 1];
            System.arraycopy(attribute, 1, tail, 0, attribute.length - 1);
            if (value instanceof Collection<?>) {
                for (Object obj : ((Collection<?>) value)) {
                    assertFetchedAttribute(emf, value, tail);
                }
            } else {
                assertFetchedAttribute(emf, value, tail);
            }
        } else {
            // This is where the actual end attribute in the path is validated.
            if (desc.hasFetchGroupManager()) {
                assertTrue(desc.getFetchGroupManager().isAttributeFetched(session, value, attribute[0]));
            }
        }
    }

    /**
     * Verify that the attribute path is loaded.
     */
    public static void assertNotFetchedAttribute(EntityManagerFactory emf, Object entity, String... attribute) {
        assertNotNull("EntityManagerFactory is null", emf);
        assertNotNull("Entity is null", entity);
        Server session = JpaHelper.getServerSession(emf);
        assertNotNull("No Server session found for: " + emf, session);
        ClassDescriptor desc = session.getClassDescriptor(entity);
        assertNotNull("No descriptor found for: " + entity, desc);

        Object value = entity;
        for (int index = 0; index < attribute.length - 1; index++) {
            String attrName = attribute[index];

            if (desc.hasFetchGroupManager()) {
                assertTrue("Attribute: '" + attrName + "' not fetched on: " + value, desc.getFetchGroupManager().isAttributeFetched(session, value, attrName));
            }
            DatabaseMapping mapping = desc.getMappingForAttributeName(attrName);
            value = mapping.getAttributeValueFromObject(value);

            if (value instanceof IndirectContainer) {
                value = ((IndirectContainer) value).getValueHolder();
            }
            if (value instanceof ValueHolderInterface) {
                ValueHolderInterface vhi = (ValueHolderInterface) value;
                assertTrue("ValueHolder for: '" + attrName + "' not instantiated", vhi.isInstantiated());
                value = vhi.getValue();
            }
        }
    }

    /**
     * Assert that the entity provided has the attributes defined in the
     * FetchGroup loaded.
     * 
     * @param emf
     * @param entity
     * @param fetchGroup
     */
    public static void assertFetched(EntityManagerFactory emf, Object entity, FetchGroup fetchGroup) {
        assertNotNull("Null entity", entity);
        assertNotNull("No FetchGroup provided", fetchGroup);
        if (!(entity instanceof FetchGroupTracker)) {
            System.out.println();
        }
        assertTrue("Entity does not implement FetchGroupTracker: " + entity, entity instanceof FetchGroupTracker);

        FetchGroupTracker tracker = (FetchGroupTracker) entity;
        assertNotNull("FetchGroup on entity is null", tracker._persistence_getFetchGroup());
        // assertEquals("FetchGroup on entity does not equal provided",
        // fetchGroup, tracker._persistence_getFetchGroup());

        Server session = JpaHelper.getServerSession(emf);
        assertNotNull(session);
        ClassDescriptor descriptor = session.getClassDescriptor(entity);
        assertNotNull(descriptor);
        assertTrue("", descriptor.getJavaClass().isAssignableFrom(entity.getClass()));

        for (DatabaseMapping mapping : descriptor.getMappings()) {
            if (descriptor.getObjectBuilder().getPrimaryKeyMappings().contains(mapping)) {
                assertTrue("PrimaryKey mapping not fetched: " + entity, tracker._persistence_isAttributeFetched(mapping.getAttributeName()));
            } else if (descriptor.usesOptimisticLocking() && descriptor.getOptimisticLockingPolicy() instanceof VersionLockingPolicy && ((VersionLockingPolicy) descriptor.getOptimisticLockingPolicy()).getVersionMapping() == mapping) {
                assertTrue("Optimistic version mapping not fetched: " + entity, tracker._persistence_isAttributeFetched(mapping.getAttributeName()));
            } else if (tracker._persistence_getFetchGroup().containsAttribute(mapping.getAttributeName())) {
                assertTrue(tracker._persistence_isAttributeFetched(mapping.getAttributeName()));
                FetchItem attrFI = tracker._persistence_getFetchGroup().getFetchItem(mapping.getAttributeName());
                if (attrFI.getFetchGroup() != null) {
                    Object value = mapping.getAttributeValueFromObject(entity);
                    if (value instanceof IndirectContainer) {
                        assertTrue(((IndirectContainer) value).isInstantiated());
                        Collection<?> values = (Collection<?>) value;
                        for (Object val : values) {
                            assertFetched(emf, val, attrFI.getFetchGroup());
                        }
                        return;
                    }
                    if (value instanceof ValueHolderInterface) {
                        assertTrue(((ValueHolderInterface) value).isInstantiated());
                        value = ((ValueHolderInterface) value).getValue();
                    }
                    if (value != null) {
                        assertFetched(emf, value, attrFI.getFetchGroup());
                    }
                }
            } else { // Should not be fetched
                assertFalse(tracker._persistence_isAttributeFetched(mapping.getAttributeName()));
            }
        }
    }

    public static void assertDefaultFetched(EntityManagerFactory emf, Object entity) {
        assertNotNull("Null entity", entity);

        ClassDescriptor descriptor = JpaHelper.getServerSession(emf).getClassDescriptor(entity);
        assertNotNull("No descriptor found for: " + entity, descriptor);

        assertTrue("No FetchGroupManager on: " + descriptor, descriptor.hasFetchGroupManager());

        FetchGroup<?> defaultFG = descriptor.getFetchGroupManager().getDefaultFetchGroup();

        assertNotNull("No default FetchGroup on: " + descriptor, defaultFG);

        assertFetched(emf, entity, defaultFG);
    }

    public static void assertFetched(EntityManagerFactory emf, Object entity, String fetchGroupName) {
        assertNotNull("Null entity", entity);

        ClassDescriptor descriptor = JpaHelper.getServerSession(emf).getClassDescriptor(entity);
        assertNotNull("No descriptor found for: " + entity, descriptor);

        assertTrue("No FetchGroupManager on: " + descriptor, descriptor.hasFetchGroupManager());

        FetchGroup<?> fg = descriptor.getFetchGroupManager().getFetchGroup(fetchGroupName);

        assertNotNull("No FetchGroup named: " + fetchGroupName, fg);

        assertFetched(emf, entity, fg);
    }

    /**
     * Verify that the provided entity does not have a FetchGroup configured on
     * it.
     */
    public static void assertNoFetchGroup(EntityManagerFactory emf, Object entity) {
        if (entity instanceof FetchGroupTracker) {
            FetchGroupTracker tracker = (FetchGroupTracker) entity;

            assertNull("Entity: " + entity + " has: " + tracker._persistence_getFetchGroup(), tracker._persistence_getFetchGroup());
        }
    }

    public static void assertConfig(EntityManagerFactory emf, String entityName, FetchGroup defaultFetchGroup) throws Exception {
        assertConfig(emf, entityName, defaultFetchGroup, 0);
    }

    public static void assertConfig(EntityManagerFactory emf, String entityName, FetchGroup defaultFetchGroup, int numNamedFetchGroups) throws Exception {
        ClassDescriptor descriptor = JpaHelper.getServerSession(emf).getClassDescriptorForAlias(entityName);
        assertNotNull("Not descriptor found for: " + entityName, descriptor);

        assertTrue("FetchGroupTracker not implemented by: " + entityName, FetchGroupTracker.class.isAssignableFrom(descriptor.getJavaClass()));

        if (defaultFetchGroup == null) {
            assertNull("Default FetchGroup not null: " + entityName, descriptor.getFetchGroupManager().getDefaultFetchGroup());
        } else {
            assertEquals("Default FetchGroup does not match", defaultFetchGroup, descriptor.getFetchGroupManager().getDefaultFetchGroup());
        }

        assertEquals("Incorrect number of Named FetchGroups: " + entityName, numNamedFetchGroups, descriptor.getFetchGroupManager().getFetchGroups().size());
    }

}
