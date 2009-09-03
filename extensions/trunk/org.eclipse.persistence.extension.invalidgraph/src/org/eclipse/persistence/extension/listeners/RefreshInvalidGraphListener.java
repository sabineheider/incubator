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
 *     dclarke - Bug 288363: Extensions Incubator - RefreshInvalidGraphListener
 *               http://wiki.eclipse.org/EclipseLink/Development/Incubator/Extensions/RefreshInvalidGraphListener
 ******************************************************************************/
package org.eclipse.persistence.extension.listeners;

import java.lang.reflect.Modifier;
import java.util.*;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.extension.EntityCollector;
import org.eclipse.persistence.extension.MappingCollector;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.sessions.*;

/**
 * This session event listener provides a work-around for accessing persistent
 * object graphs from the EclipseLink cache where related objects may be invalid
 * in the cache. EclipseLink's cache invalidation will only refresh the invalid
 * objects if they are directly queried for. This can cause a problem if an
 * application queries for a related object that is valid and then navigates
 * through relationships to the invalid one. Using this event listener the
 * application will now post-process query results to ensure all invalid objects
 * in the graph are refreshed.
 * 
 * @author dclarke
 * @since EclipseLink 1.1.2
 */
public class RefreshInvalidGraphListener extends SessionEventAdapter {

    /**
     * Flag used on queries to indicate that this listener should not process
     * them verifying their resulting object graphs. This is used internally on
     * the {@link #refresh(Session, Object)} queries to ensure these internally
     * generated queries do not cause additional verification processing. This
     * flag can also be set on application queries that should not have their
     * results verified.
     */
    public static final String DO_NOT_VERIFY_PROPERTY = "VerifyRelatedObjects-DoNotVerify";

    /** Cache of relationship mappings by persistent type. */
    private MappingCollector mappingsFilter;

    /**
     * Shared session (database/server) this listener was assigned to.
     * Initialized in {@link #postLogin(SessionEvent)}
     */
    private AbstractSession sharedSession;

    public MappingCollector getMappingsFilter() {
        return this.mappingsFilter;
    }

    public AbstractSession getSharedSession() {
        return this.sharedSession;
    }

    /**
     * This event is called after the execution of all queries. It will verify
     * the related entities for all object queries (ReadObjectQuery and
     * ReadAllQuery) which do not have the {@link #DO_NOT_VERIFY_PROPERTY}
     * property set.
     */
    @Override
    public void postExecuteQuery(SessionEvent event) {
        if (!event.getQuery().isObjectLevelReadQuery() || event.getQuery().getProperties().containsKey(DO_NOT_VERIFY_PROPERTY)) {
            return;
        }

        getSharedSession().getSessionLog().log(SessionLog.FINEST, "VerifyRelatedObjects.postExecuteQuery::" + event.getQuery());
        EntityCollector collector = new EntityCollector(getSharedSession(), getMappingsFilter(), new InvalidCacheCriteria(), event.getResult());
        refresh(collector.getInvalidEntities());
    }

    /**
     * Perform the refresh for all invalid entities. A refresh using IN is used
     * for cases where a class has multiple instances and it uses a single part
     * primary key. Otherwise the entities are looped through one at a time
     * doing separate refresh queries.
     */
    @SuppressWarnings("unchecked")
    private void refresh(Map<Class<?>, Collection<Object>> entitiesByClass) {
        for (Class<?> entityClass : entitiesByClass.keySet()) {
            Collection<Object> entities = entitiesByClass.get(entityClass);
            ClassDescriptor descriptor = getSharedSession().getClassDescriptor(entityClass);

            if (entities.size() > 1 && descriptor.getPrimaryKeyFields().size() == 1) {
                ReadAllQuery query = new ReadAllQuery(entityClass);
                query.refreshIdentityMapResult();
                query.setIsExecutionClone(true);
                query.setProperty(DO_NOT_VERIFY_PROPERTY, true);

                Object[] pks = new Object[entities.size()];
                int index = 0;
                for (Object entity : entities) {
                    List<Object> entityPK = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(entity, (AbstractSession) getSharedSession());
                    pks[index++] = entityPK.get(0);
                }
                query.setSelectionCriteria(query.getExpressionBuilder().get(descriptor.getObjectBuilder().getPrimaryKeyMappings().get(0).getAttributeName()).in(pks));
                getSharedSession().getSessionLog().log(SessionLog.FINEST, "VerifyRelatedObjects.refresh::" + entityClass + " query: " + query);
                getSharedSession().executeQuery(query);
            } else {
                for (Object entity : entities) {
                    ReadObjectQuery query = new ReadObjectQuery(entity);
                    query.refreshIdentityMapResult();
                    query.setIsExecutionClone(true);
                    query.setProperty(DO_NOT_VERIFY_PROPERTY, true);
                    getSharedSession().getSessionLog().log(SessionLog.FINEST, "VerifyRelatedObjects.refresh::" + entity + " query: " + query);
                    getSharedSession().executeQuery(query);
                }
            }
        }
    }

    /**
     * This implementation of
     * {@link SessionEventListener#postLogin(SessionEvent)} is used to
     * initialize the cache of relationship mappings once during startup versus
     * lazily (and potentially with threading issues) during use of this event
     * listener's {@link #verifyGraph(Session, Object, HashSet)}
     */
    @Override
    public void postLogin(SessionEvent event) {
        this.sharedSession = (AbstractSession) event.getSession();
        this.mappingsFilter = new MappingCollector(getSharedSession(), new RelationshipMappingCriteria());
    }

    /**
     * Criteria for collecting the relationship mappings that will be checked.
     */
    private class RelationshipMappingCriteria implements MappingCollector.Criteria {

        public boolean collect(ClassDescriptor descriptor) {
            return !descriptor.isDescriptorForInterface() && !Modifier.isAbstract(descriptor.getJavaClass().getModifiers());
        }

        public boolean collect(DatabaseMapping mapping) {
            return mapping.getReferenceDescriptor() != null && !mapping.getReferenceDescriptor().shouldBeReadOnly();
        }
    }

    /**
     * Criteria used to identify the entities that are invalid and need to be
     * refreshed.
     */
    private class InvalidCacheCriteria implements EntityCollector.Criteria {

        @Override
        public boolean collect(Session session, DatabaseMapping mapping, Object entity) {
            return entity != null && (mapping != null && !mapping.isAggregateMapping() && !session.getIdentityMapAccessor().isValid(entity));
        }
    }
}
