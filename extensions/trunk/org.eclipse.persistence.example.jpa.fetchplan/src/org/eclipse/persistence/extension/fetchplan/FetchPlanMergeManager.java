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
 *     dclarke - TODO
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
 * TODO
 * 
 * Custom MergeManager to support sparse merge based on a FetchPlan.
 * 
 * @author dclarke
 * @since EclipseLink 1.2
 */
public class FetchPlanMergeManager extends MergeManager {

    private Map<Object, FetchPlan> fetchPlans = new HashMap<Object, FetchPlan>();

    public FetchPlanMergeManager(AbstractSession session, Object rootEntity, FetchPlan plan) {
        super(session);
        getFetchPlans().put(rootEntity, plan);

        setMergePolicy(CLONE_INTO_WORKING_COPY);
    }

    public Map<Object, FetchPlan> getFetchPlans() {
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

    public void mergeIntoObject(Object target, boolean isUnInitialized, Object source, boolean cascadeOnly, boolean isTargetCloneOfOriginal, ClassDescriptor descriptor) {
        FetchPlan plan = getFetchPlans().get(source);

        if (plan == null) {
            descriptor.getObjectBuilder().mergeIntoObject(target, isUnInitialized, source, this);
            return;
        }

        for (FetchItem item : plan.getItems().values()) {
            DatabaseMapping mapping = item.getMapping(getSession());

            if (mapping.getReferenceDescriptor() != null && item.getFetchPlan() != null) {
                Object sourceValue = mapping.getAttributeValueFromObject(source);

                if (sourceValue instanceof IndirectContainer && ((IndirectContainer) sourceValue).isInstantiated()) {
                    sourceValue = ((IndirectContainer) sourceValue).getValueHolder();
                }
                if (sourceValue instanceof ValueHolderInterface && ((ValueHolderInterface) sourceValue).isInstantiated()) {
                    sourceValue = ((ValueHolderInterface) sourceValue).getValue();
                }

                // Configure FetchPlan usage for related objects
                if (sourceValue != null) {
                    if (sourceValue instanceof Collection<?>) {
                        for (Object obj: (Collection<?>) sourceValue) {
                            getFetchPlans().put(obj, item.getFetchPlan());
                        }
                    } else {
                        getFetchPlans().put(sourceValue, item.getFetchPlan());
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
