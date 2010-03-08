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
 *     dclarke - Bug 288307: FetchPlan Example
 ******************************************************************************/
package org.eclipse.persistence.extension.fetchplan;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.sessions.Session;

/**
 * Helper class to simplify the usage of the {@link FetchPlan} extension with an
 * EclipseLink JPA context. The API on FetchPlan makes use of native EclipseLink
 * session types while these methods handle the unwrapping of the provided
 * EntityManager as needed.
 * 
 * @author dclarke
 * @since EclipseLink 2.1
 */
public class JpaFetchPlanHelper {

    /**
     * @see FetchPlan#fetch(Object,
     *      org.eclipse.persistence.internal.sessions.AbstractSession)
     */
    public static void fetch(EntityManager em, FetchPlan fetchPlan, Object entity) {
        fetchPlan.fetch(entity, JpaHelper.getEntityManager(em).getServerSession());
    }

    /**
     * @see FetchPlan#fetch(Object,
     *      org.eclipse.persistence.internal.sessions.AbstractSession)
     */
    public static void fetch(EntityManager em, FetchPlan fetchPlan, Collection<?> entities) {
        fetchPlan.fetch(entities, JpaHelper.getEntityManager(em).getServerSession());
    }

    /**
     * @see FetchPlan#fetch(Object,
     *      org.eclipse.persistence.internal.sessions.AbstractSession)
     */
    public static void fetch(EntityManager em, FetchPlan fetchPlan, Collection<Object[]> results, int resultIndex) {
        fetchPlan.fetch(results, resultIndex, JpaHelper.getEntityManager(em).getServerSession());
    }

    /**
     * @see FetchPlan#copy(Object,
     *      org.eclipse.persistence.internal.sessions.AbstractSession, boolean)
     */
    public static <T> T copy(EntityManager em, FetchPlan fetchPlan, T entity) {
        return fetchPlan.copy(entity, JpaHelper.getEntityManager(em).getServerSession());
    }

    /**
     * @see FetchPlan#merge(Object, org.eclipse.persistence.sessions.UnitOfWork)
     */
    public static <T> T merge(EntityManager em, FetchPlan fetchPlan, T entity) {
        return fetchPlan.merge(entity, JpaHelper.getEntityManager(em).getUnitOfWork());
    }

    /**
     * Add all of the attributes from the default FetchGroup to the FetchPlan.
     * The default FetchGroup contains all of the non-lazy attributes assuming
     * at least one basic (direct) mapped attribute is lazy.
     */
    public static void addDefaultFetchGroupAttributes(EntityManager em, FetchPlan fetchPlan) {
        EntityManagerImpl emImpl = (EntityManagerImpl) JpaHelper.getEntityManager(em);

        if (emImpl == null) {
            throw new IllegalArgumentException("JpaFetchPlanHelper.addDefaultFetchGroupAttributes: could not unwrap EntityManager: " + em); 
        }
        addDefaultFetchGroupAttributes(emImpl.getEntityManagerFactory(), fetchPlan);
    }

    /**
     * Add all of the attributes from the default FetchGroup to the FetchPlan.
     * The default FetchGroup contains all of the non-lazy attributes assuming
     * at least one basic (direct) mapped attribute is lazy.
     */
    public static void addDefaultFetchGroupAttributes(EntityManagerFactory emf, FetchPlan fetchPlan) {
        Session session = JpaHelper.getServerSession(emf);
        ClassDescriptor descriptor = session.getClassDescriptor(fetchPlan.getEntityClass());

        if (descriptor != null && descriptor.hasFetchGroupManager()) {
            FetchGroup fg = descriptor.getFetchGroupManager().getDefaultFetchGroup();
            
            if (fg != null) {
                fetchPlan.addAttributes(fg);
                return;
            } 
        }
        throw new IllegalArgumentException("JpaFetchPlanHelper.addDefaultFetchGroupAttributes: No descriptor or default FetchGroup on: " + fetchPlan.getEntityClass()); 
    }

    /**
     * Add all of the attributes from the named FetchGroup to the FetchPlan.
     */
    public static void addNamedFetchGroupAttributes(EntityManager em, String fetchGroupName, FetchPlan fetchPlan) {
        EntityManagerImpl emImpl = (EntityManagerImpl) JpaHelper.getEntityManager(em);

        if (emImpl == null) {
            throw new IllegalArgumentException("JpaFetchPlanHelper.addNamedFetchGroupAttributes: could not unwrap EntityManager: " + em); 
        }
        addNamedFetchGroupAttributes(emImpl.getEntityManagerFactory(), fetchGroupName, fetchPlan);
    }

    /**
     * Add all of the attributes from the named FetchGroup to the FetchPlan.
     */
    public static void addNamedFetchGroupAttributes(EntityManagerFactory emf, String fetchGroupName, FetchPlan fetchPlan) {
        Session session = JpaHelper.getServerSession(emf);
        ClassDescriptor descriptor = session.getClassDescriptor(fetchPlan.getEntityClass());

        if (descriptor != null && descriptor.hasFetchGroupManager()) {
            FetchGroup fg = descriptor.getFetchGroupManager().getFetchGroup(fetchGroupName);
            if (fg != null) {
                fetchPlan.addAttributes(fg);
                return;
            }
        }
        throw new IllegalArgumentException("JpaFetchPlanHelper.addNamedFetchGroupAttributes: No descriptor or default FetchGroup on: " + fetchPlan.getEntityClass()); 
    }
}
