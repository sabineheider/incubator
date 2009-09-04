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
 *      dclarke - Example utility to support serializing EntityManager for use in 
 *                HTTP session state replication
 ******************************************************************************/
package org.eclipse.persistence.internal.jpa;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.persistence.EntityManager;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.descriptors.changetracking.AttributeChangeListener;
import org.eclipse.persistence.internal.identitymaps.CacheKey;
import org.eclipse.persistence.internal.indirection.BasicIndirectionPolicy;
import org.eclipse.persistence.internal.indirection.UnitOfWorkQueryValueHolder;
import org.eclipse.persistence.internal.indirection.UnitOfWorkValueHolder;
import org.eclipse.persistence.internal.sessions.AggregateChangeRecord;
import org.eclipse.persistence.internal.sessions.AggregateCollectionChangeRecord;
import org.eclipse.persistence.internal.sessions.AggregateObjectChangeSet;
import org.eclipse.persistence.internal.sessions.ChangeRecord;
import org.eclipse.persistence.internal.sessions.ObjectChangeSet;
import org.eclipse.persistence.internal.sessions.RepeatableWriteUnitOfWork;
import org.eclipse.persistence.internal.sessions.UnitOfWorkChangeSet;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.sessions.server.Server;

/**
 * The EntityManagerHandle class provides a serializable wrapper for an
 * {@link EntityManager}. The intent is to allow an EntityManager to be
 * serialized between nodes and thus allow a persistence context to be continued
 * in a new application instance for fail-over situations.
 * 
 * @author dclarke
 * @since EclipseLink 1.1.2
 */
public class EntityManagerHandle implements Serializable {

    private static final long serialVersionUID = 1l;

    /**
     * The wrapped EntityManager
     */
    private EntityManagerImpl entityManager;

    /**
     * This map is the state cached of a {@link RepeatableWriteUnitOfWork} when
     * the handle is serialized. It is used to re-initialize a handle. This map
     * is only not null when the handle is not initialzied.
     */
    private Map<Object, Object> uowCloneMapping;

    /**
     * Map of clones to {@link ObjectChangeSet} captured from ...
     */
    private Map<Object, ObjectChangeSet> changeSets;

    private Map<Object, Object> newObjectsCloneToOriginal;

    public EntityManagerHandle(EntityManager em) {
        this.entityManager = (EntityManagerImpl) JpaHelper.getEntityManager(em);
    }

    protected EntityManagerHandle(EntityManager em, Map<Object, Object> cloneMapping, Map<Object, ObjectChangeSet> changetSets, Map<Object, Object> newObjectsCloneToOriginal) {
        this.entityManager = (EntityManagerImpl) JpaHelper.getEntityManager(em);

        this.uowCloneMapping = cloneMapping;
        this.newObjectsCloneToOriginal = newObjectsCloneToOriginal;

        this.changeSets = changetSets;
    }

    protected EntityManagerImpl getEntityManagerImpl() {
        return this.entityManager;
    }

    public boolean isInitialized() {
        return this.uowCloneMapping == null;
    }

    public EntityManager getEntityManager() {
        if (!isInitialized()) {
            initialize();
        }
        return this.entityManager;
    }

    public Map<?, ?> getUowCloneMapping() {
        return uowCloneMapping;
    }

    public Map<Object, ObjectChangeSet> getChangeSets() {
        return this.changeSets;
    }

    /**
     * The initialize operation will fix up the contained
     * {@link RepeatableWriteUnitOfWork} to ensure it and all of its contained
     * objects have their proper session state that is lost when serialized. It
     * is done lazily to ensure it is only done in receivers that require it to
     * continue functioning and not just storage in case of fail-over cases.
     */
    protected synchronized void initialize() {
        RepeatableWriteUnitOfWork uow = (RepeatableWriteUnitOfWork) getEntityManagerImpl().getUnitOfWork();

        if (getUowCloneMapping() != null) {
            for (Object clone : getUowCloneMapping().keySet()) {
                initialize(uow, clone, getUowCloneMapping().get(clone));
            }

            this.newObjectsCloneToOriginal = null;
            this.uowCloneMapping = null;
            this.changeSets = null;
        }
    }

    /**
     * Initialize a clone added to this {@link RepeatableWriteUnitOfWork}. The
     * clone was previously serialized and has thus lost any transient state.
     * This method handles populating this state to work with the new
     * {@link RepeatableWriteUnitOfWork}.
     * 
     * @param uow
     * @param clone
     */
    @SuppressWarnings("unchecked")
    private void initialize(RepeatableWriteUnitOfWork uow, Object clone, Object backupClone) {
        Server session = getEntityManagerImpl().getServerSession();
        ClassDescriptor descriptor = session.getClassDescriptor(clone);
        // need to put clone in identityMap as well.
        if (getChangeSets().containsKey(clone)) {
            ObjectChangeSet ocs = getChangeSets().get(clone);
            if (!ocs.isNew()) {
                uow.getIdentityMapAccessor().putInIdentityMap(clone);
                uow.getCloneMapping().put(clone, backupClone);
            } else {
                uow.registerNewObject(clone);
            }
        } else {
            if (this.newObjectsCloneToOriginal.containsKey(clone)) {
                uow.registerNewObject(clone);
            } else {
                // has no changes.
                uow.getCloneMapping().put(clone, backupClone);
                uow.getIdentityMapAccessor().putInIdentityMap(clone);
            }
        }

        if (ChangeTracker.class.isAssignableFrom(clone.getClass())) {
            initializeChangeTracker((ChangeTracker) clone, uow, descriptor);
        }

        for (DatabaseMapping mapping : descriptor.getMappings()) {
            ValueHolderInterface vhi = getValueHolder(mapping, clone);

            if (vhi != null && !vhi.isInstantiated()) {
                UnitOfWorkValueHolder uowVH = (UnitOfWorkValueHolder) vhi;
                uowVH.setSession(uow);
                Object original = session.readObject(clone);

                ValueHolderInterface wrappedHolder = getValueHolder(mapping, original);
                uowVH = new UnitOfWorkQueryValueHolder(wrappedHolder, clone, (ForeignReferenceMapping) mapping, uowVH.getRow(), uow);
                setValueHolder((ForeignReferenceMapping) mapping, clone, uowVH);

                uowVH = new UnitOfWorkQueryValueHolder(wrappedHolder, backupClone, (ForeignReferenceMapping) mapping, uowVH.getRow(), uow);
                setValueHolder((ForeignReferenceMapping) mapping, backupClone, uowVH);
            }
        }
    }

    /**
     * Setup a change tracker.
     * 
     * TODO: What about aggregates? TODO: Can existing infrastructure for
     * building working copies with change listeners be used?
     * 
     * @param changeTracker
     * @param uow
     * @param descriptor
     */
    @SuppressWarnings("unchecked")
    private void initializeChangeTracker(ChangeTracker changeTracker, RepeatableWriteUnitOfWork uow, ClassDescriptor descriptor) {
        AttributeChangeListener listener = (AttributeChangeListener) changeTracker._persistence_getPropertyChangeListener();

        if (uow.getUnitOfWorkChangeSet() == null) {
            uow.setUnitOfWorkChangeSet(new UnitOfWorkChangeSet(uow));
        }

        if (listener != null) {
            listener.setUnitOfWork(uow);
        } else {
            listener = new AttributeChangeListener(descriptor, uow, changeTracker);
            changeTracker._persistence_setPropertyChangeListener(listener);
        }

        if (getChangeSets().containsKey(changeTracker)) {
            ObjectChangeSet ocs = getChangeSets().get(changeTracker);
            // must create and merge so that we can place the UOW clone in the
            // changeSet for later commit.
            ObjectChangeSet newOCS = new ObjectChangeSet(changeTracker, (UnitOfWorkChangeSet) uow.getUnitOfWorkChangeSet(), ocs.isNew());
            newOCS.setCacheKey(ocs.getCacheKey());

            ocs.getClassType(uow);
            ocs.setUOWChangeSet((UnitOfWorkChangeSet) new UnitOfWorkChangeSet());
            for (ChangeRecord entry : (Vector<ChangeRecord>) ocs.getChanges()) {
                // reset transient mappings in ChangeRecords.
                DatabaseMapping mapping = descriptor.getMappingForAttributeName(entry.getAttribute());
                if (mapping.isAggregateObjectMapping()) {
                    Object aggregate = mapping.getRealAttributeValueFromObject(changeTracker, uow);
                    AggregateObjectChangeSet aocs = new AggregateObjectChangeSet(new Vector(0), aggregate.getClass(), aggregate, (UnitOfWorkChangeSet) uow.getUnitOfWorkChangeSet(), true);
                    ((UnitOfWorkChangeSet) uow.getUnitOfWorkChangeSet()).addObjectChangeSetForIdentity(aocs, aggregate);
                    aocs.mergeObjectChanges((ObjectChangeSet) ((AggregateChangeRecord) entry).getChangedObject(), (UnitOfWorkChangeSet) uow.getUnitOfWorkChangeSet(), (UnitOfWorkChangeSet) ocs.getUOWChangeSet());
                    ((AggregateChangeRecord) entry).setChangedObject(aocs);
                    for (ChangeRecord record : (Vector<ChangeRecord>) aocs.getChanges()) {
                        record.setMapping(mapping.getReferenceDescriptor().getMappingForAttributeName(record.getAttribute()));
                    }

                } else if (mapping.isAggregateCollectionMapping()) {
                    Object attributeValue = mapping.getRealAttributeValueFromObject(changeTracker, uow);
                    Vector<ObjectChangeSet> changes = (Vector<ObjectChangeSet>) ((AggregateCollectionChangeRecord) entry).getChangedValues();
                    Object iterator = mapping.getContainerPolicy().iteratorFor(attributeValue);

                    while (mapping.getContainerPolicy().hasNext(iterator)) {
                        Object aggregate = mapping.getContainerPolicy().next(iterator, uow);
                        CacheKey cacheKey = new CacheKey(mapping.getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(aggregate, uow));
                        int index = changes.indexOf(cacheKey);
                        if (index >= 0) {
                            ObjectChangeSet aggChangeSet = changes.get(index);
                            ObjectChangeSet newAggChangeSet = new ObjectChangeSet(aggregate, (UnitOfWorkChangeSet) uow.getUnitOfWorkChangeSet(), true);
                            ((UnitOfWorkChangeSet) uow.getUnitOfWorkChangeSet()).addObjectChangeSetForIdentity(newAggChangeSet, aggregate);
                            newAggChangeSet.mergeObjectChanges(aggChangeSet, (UnitOfWorkChangeSet) uow.getUnitOfWorkChangeSet(), (UnitOfWorkChangeSet) ocs.getUOWChangeSet());
                            for (ChangeRecord record : (Vector<ChangeRecord>) newAggChangeSet.getChanges()) {
                                record.setMapping(mapping.getReferenceDescriptor().getMappingForAttributeName(record.getAttribute()));
                            }

                        }
                    }
                }
                entry.setMapping(mapping);
            }

            listener.setObjectChangeSet(newOCS);
            ((UnitOfWorkChangeSet) uow.getUnitOfWorkChangeSet()).addObjectChangeSetForIdentity(newOCS, changeTracker);
            newOCS.mergeObjectChanges(ocs, (UnitOfWorkChangeSet) uow.getUnitOfWorkChangeSet(), (UnitOfWorkChangeSet) ocs.getUnitOfWorkClone());
            if (newOCS.hasChanges()) {
                // The listener's changes will not be recorded unless the
                // listener has received a change notification
                // this code skips the change calc step and forces the changes
                // into the current UOW changeset
                ((UnitOfWorkChangeSet) uow.getUnitOfWorkChangeSet()).addObjectChangeSet(newOCS, uow, newOCS.isNew());
                uow.addToChangeTrackedHardList(changeTracker);
            }
        }
    }

    /*
     * Helper method to retrieve the underlying ValueHolderInterface for a
     * relationship. Handle IndirectContainer unwrapping.
     */
    private ValueHolderInterface getValueHolder(DatabaseMapping mapping, Object entity) {
        Object value = null;

        if (mapping.isForeignReferenceMapping() && mapping.isLazy()) {
            value = mapping.getAttributeValueFromObject(entity);

            if (value != null && IndirectContainer.class.isAssignableFrom(value.getClass())) {
                value = ((IndirectContainer) value).getValueHolder();
            }
        }
        if (value != null && !(value instanceof ValueHolderInterface)) {
            return null;
        }
        return (ValueHolderInterface) value;
    }

    /*
     * Helper method to populate a new ValueHolderInterface into an object's
     * mapped relationship.
     */
    private void setValueHolder(ForeignReferenceMapping mapping, Object entity, ValueHolderInterface valueHolder) {
        if (mapping.getIndirectionPolicy() instanceof BasicIndirectionPolicy) {
            mapping.setAttributeValueInObject(entity, valueHolder);
        } else {
            IndirectContainer container = (IndirectContainer) mapping.getAttributeValueFromObject(entity);
            container.setValueHolder(valueHolder);
        }
    }

    /**
     * Replace this object which holds an non-serializable EntityManager with a
     * replacement that contains the essential state of the EntityManager for
     * use in other nodes.
     * 
     * @return
     * @throws ObjectStreamException
     */
    public Object writeReplace() throws ObjectStreamException {
        verifyIsSerializable();

        return new SerializedEntityManager(getEntityManagerImpl());
    }

    /**
     * Verify the EntityManager being wrapped is in a state that it can be
     * serialized. For now we'll only allow EntityManagers not in active
     * transactions.
     */
    /*
     * TODO: Later this may be increased to include active JPA transactions
     * where no physical database transaction has been started. In this case
     * we'll also need to ensure that commit cycle of the UnitOfWork has not
     * been started.
     */
    public void verifyIsSerializable() {
        if (getEntityManagerImpl() == null) {
            throw new IllegalStateException("EntityManagerHandle cannot be serialized as it does not have an EntityManager");
        }

        if (!getEntityManagerImpl().isOpen()) {
            throw new IllegalStateException("EntityManagerHandle cannot be serialized as its EntityManager is closed");
        }

        if (getEntityManagerImpl().checkForTransaction(false) != null) {
            throw new IllegalStateException("EntityManagerHandle cannot be serialized as its EntityManager is in an active RESOURCE_LOCAL transaction");
        }
    }

}
