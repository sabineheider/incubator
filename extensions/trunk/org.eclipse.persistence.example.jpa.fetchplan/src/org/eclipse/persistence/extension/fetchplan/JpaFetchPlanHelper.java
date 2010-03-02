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

import org.eclipse.persistence.jpa.JpaHelper;

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
}
