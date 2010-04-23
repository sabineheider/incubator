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
 *     dclarke - Bug 288307: Fetch Plan Extension Incubator
 ******************************************************************************/
package org.eclipse.persistence.extension.fetchplan;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEventManager;
import org.eclipse.persistence.descriptors.VersionLockingPolicy;
import org.eclipse.persistence.exceptions.OptimisticLockException;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.descriptors.ObjectBuilder;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.MergeManager;
import org.eclipse.persistence.mappings.DatabaseMapping;

/**
 * Custom MergeManager to support sparse merge based on a FetchPlan.
 * 
 * @see FetchPlan
 * 
 * @author dclarke
 * @since EclipseLink 1.2
 */
public class FetchPlanMergeManager extends MergeManager {

    /**
     * Map of entities to FetchPlan used to do sparse merge on entities in the
     * graph described by a FetchPlan. When started only the root entity being
     * merged is set in the map and then as each level of the graph being merged
     * is traversed the additional objects are added before calling merge on the
     * mapping so that when called back into
     * {@link #mergeIntoObject(Object, boolean, Object, boolean, boolean, ClassDescriptor)}
     * method the appropriate plan can be looked up and used.
     */
    private Map<Object, FetchPlan> fetchPlans = new HashMap<Object, FetchPlan>();

    public FetchPlanMergeManager(AbstractSession session) {
        super(session);
        setMergePolicy(CLONE_INTO_WORKING_COPY);
    }

    protected Map<Object, FetchPlan> getFetchPlans() {
        return this.fetchPlans;
    }

    @Override
    protected Object mergeChangesOfCloneIntoWorkingCopy(Object rmiClone) {
        Object registeredObject = registerObjectForMergeCloneIntoWorkingCopy(rmiClone);

        if (registeredObject == rmiClone && !shouldForceCascade()) {
            // need to find better better fix. prevents merging into itself.
            return rmiClone;
        }

        ClassDescriptor descriptor = this.session.getDescriptor(rmiClone);
        try {
            ObjectBuilder builder = descriptor.getObjectBuilder();

            if (registeredObject != rmiClone && descriptor.usesVersionLocking() && !mergedNewObjects.containsKey(registeredObject)) {
                VersionLockingPolicy policy = (VersionLockingPolicy) descriptor.getOptimisticLockingPolicy();
                if (policy.isStoredInObject()) {
                    Object currentValue = builder.extractValueFromObjectForField(registeredObject, policy.getWriteLockField(), session);

                    if (policy.isNewerVersion(currentValue, rmiClone, session.keyFromObject(rmiClone), session)) {
                        throw OptimisticLockException.objectChangedSinceLastMerge(rmiClone);
                    }
                }
            }

            // Toggle change tracking during the merge.
            descriptor.getObjectChangePolicy().dissableEventProcessing(registeredObject);

            boolean cascadeOnly = false;
            if (registeredObject == rmiClone || mergedNewObjects.containsKey(registeredObject)) {
                // GF#1139 Cascade merge operations to relationship mappings
                // even if already registered
                cascadeOnly = true;
            }

            // Merge into the clone from the original and use the clone as
            // backup as anything different should be merged.
            mergeIntoObject(registeredObject, false, rmiClone, cascadeOnly, false, descriptor);
        } finally {
            descriptor.getObjectChangePolicy().enableEventProcessing(registeredObject);
        }

        return registeredObject;
    }

    /**
     * This code is copied from
     * {@link ObjectBuilder#mergeIntoObject(Object, boolean, Object, MergeManager, boolean, boolean)}
     * to enable {@link FetchPlan} specific merging. The method in ObjectBuilder
     * merges all relationships based on mappings and cascade configuration
     * where as this does it by FetchPlan's items.
     */
    public void mergeIntoObject(Object target, boolean isUnInitialized, Object source, boolean cascadeOnly, boolean isTargetCloneOfOriginal, ClassDescriptor descriptor) {
        FetchPlan plan = getFetchPlans().get(source);

        if (plan == null) {
            descriptor.getObjectBuilder().mergeIntoObject(target, isUnInitialized, source, this);
            return;
        }

        for (FetchItem item : plan.getItems().values()) {
            DatabaseMapping mapping = item.getMapping(getSession());

            if (mapping.getReferenceDescriptor() != null) {
                Object sourceValue = mapping.getAttributeValueFromObject(source);

                if (sourceValue instanceof IndirectContainer && ((IndirectContainer) sourceValue).isInstantiated()) {
                    sourceValue = ((IndirectContainer) sourceValue).getValueHolder();
                }
                if (sourceValue instanceof ValueHolderInterface && ((ValueHolderInterface) sourceValue).isInstantiated()) {
                    sourceValue = ((ValueHolderInterface) sourceValue).getValue();
                }

                // Configure FetchPlan usage for related objects
                if (sourceValue != null && !getFetchPlans().containsKey(sourceValue)) {
                    FetchPlan fp = item.getFetchPlan();

                    if (fp == null && mapping.getReferenceDescriptor() != null) {
                        fp = FetchPlan.defaultFetchPlan(mapping);
                    }
                    if (sourceValue instanceof Collection<?>) {
                        for (Object obj : (Collection<?>) sourceValue) {
                            getFetchPlans().put(obj, fp);
                        }
                    } else {
                        getFetchPlans().put(sourceValue, fp);
                    }
                }
            }

            mapping.mergeIntoObject(target, isUnInitialized, source, this);
        }

        // PERF: Avoid events if no listeners.
        if (descriptor.getEventManager().hasAnyEventListeners()) {
            org.eclipse.persistence.descriptors.DescriptorEvent event = new org.eclipse.persistence.descriptors.DescriptorEvent(target);
            event.setSession(getSession());
            event.setOriginalObject(source);
            event.setEventCode(DescriptorEventManager.PostMergeEvent);
            descriptor.getEventManager().executeEvent(event);
        }
    }

}
