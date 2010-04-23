/*******************************************************************************
 * Copyright (c) 1998, 2010 Oracle. All rights reserved.
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
package org.eclipse.persistence.extension.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.indirection.BatchValueHolder;
import org.eclipse.persistence.internal.indirection.UnitOfWorkValueHolder;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReadQuery;
import org.eclipse.persistence.sessions.server.Server;

/**
 * This is an extension to EclipseLink intending to assist customers who need to
 * use BATCH reading with an IN operator instead of joining from the target
 * table back to the source table.
 * <p>
 * Usage Requirements:
 * <ul>
 * <li>Can only be used on the results of a query with batching configured. This
 * also means that the query can only return a single entity type in select.
 * <li>The source of the query must be an entity with a single part primary key
 * <li>The mapping that is to be batched using IN must be a 1:1, M:1 or 1:M with
 * a single FK mapping.
 * <li>The quantity of ids to use with the IN must not exceed the limits of the
 * target database
 * </ul>
 * <p>
 * <b>Usage Example</b>: <code><br/>
 * Query q = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");<br/>
 * q.setParameter("GENDER", Gender.Male);<br/>
 * q.setHint(QueryHints.BATCH, "e.address");<br/>
 * List<Employee> emps = q.getResultList();<br/>
 * //The batch-in configuration is made after the results are retrieved<br/>
 * BatchInConfig.config(em, emps, "address");<br/>
 * </code>
 * 
 * @author dclarke
 * @since EclipseLink 1.2
 */
public class BatchInConfig {

    /**
     * Configure the batching in query results to use IN instead of joining back
     * to the source tables. This method should be called after the results of a
     * query using batch with fetch=LAZY but before the BatchValueHolder on the
     * first result has been instantiated.
     * 
     * @param em
     *            EntityManager the query was run against
     * @param results
     *            A list of query results assumed to all be of the same entity
     *            type.
     * @param attribute
     *            name of attribute to be batched
     */
    public static void config(EntityManager em, List<?> results, String attribute) {
        if (results == null || results.isEmpty()) {
            return;
        }

        Server session = JpaHelper.getEntityManager(em).getServerSession();
        ClassDescriptor descriptor = session.getClassDescriptor(results.get(0));

        if (descriptor == null) {
            throw new IllegalArgumentException("No descriptor found for entity: " + results.get(0));
        }

        DatabaseMapping mapping = descriptor.getMappingForAttributeName(attribute);

        if (mapping == null) {
            throw new IllegalArgumentException("No mapping found on " + descriptor + " with name:" + attribute);
        }
        if (!mapping.isOneToOneMapping() && !mapping.isOneToManyMapping()) {
            throw new IllegalArgumentException("Invalid mapping type found: " + mapping);
        }

        ForeignReferenceMapping frMapping = (ForeignReferenceMapping) mapping;
        if (!frMapping.usesIndirection()) {
            throw new IllegalArgumentException("Invalid mapping type found: " + mapping);
        }

        BatchValueHolder batchVH = getBatchValueHolder(frMapping, results);

        if (batchVH == null) {
            SessionLog log = JpaHelper.getEntityManager(em).getServerSession().getSessionLog();
            log.log(SessionLog.FINE, "BatchInConfig::No BatchValueHolder found for " + attribute);
            return;
        }

        if (mapping.isOneToOneMapping()) {
            configBatchInQuery(session, (OneToOneMapping) mapping, (ReadAllQuery) batchVH.getQuery(), results);
        } else if (mapping.isOneToManyMapping()) {
            configBatchInQuery(session, (OneToManyMapping) mapping, (ReadAllQuery) batchVH.getQuery(), results);
        }
    }

    /**
     * Find the first BatchValueHolder in the results
     */
    private static BatchValueHolder getBatchValueHolder(ForeignReferenceMapping mapping, List<?> results) {
        Object result = results.get(0);

        Object value = mapping.getAttributeValueFromObject(result);
        if (value instanceof IndirectContainer) {
            value = ((IndirectContainer) value).getValueHolder();
        }
        if (value instanceof UnitOfWorkValueHolder) {
            value = ((UnitOfWorkValueHolder) value).getWrappedValueHolder();
        }
        if (value instanceof BatchValueHolder) {
            // This assumes that if the first value holder is batched its for
            // this collection of results and all results have the same batch
            // value holder
            return (BatchValueHolder) value;
        }

        // No BatchValueHolder found? TODO: Build one?
        return null;
    }

    /**
     * Customize the query for a 1:1 or M:1
     */
    private static void configBatchInQuery(Server session, OneToOneMapping mapping, ReadAllQuery raq, List<?> results) {
        if (mapping.getForeignKeyFields().size() != 1) {
            throw new IllegalArgumentException("Cannot configure batch using IN with composite FK: " + mapping);
        }

        Object[] ids = new Object[results.size()];

        for (int index = 0; index < results.size(); index++) {
            ValueHolderInterface vhi = (ValueHolderInterface) mapping.getAttributeValueFromObject(results.get(index));
            if (vhi instanceof UnitOfWorkValueHolder) {
                vhi = ((UnitOfWorkValueHolder) vhi).getWrappedValueHolder();
            }
            BatchValueHolder bvh = (BatchValueHolder) vhi;

            ids[index] = bvh.getRow().get(mapping.getForeignKeyFields().get(0));
        }

        ExpressionBuilder eb = new ExpressionBuilder(mapping.getReferenceClass());
        raq.setExpressionBuilder(eb);
        Expression idExp = eb.getField(mapping.getReferenceDescriptor().getPrimaryKeyFields().get(0)).in(ids);
        raq.setSelectionCriteria(idExp);
    }

    /**
     * Customize the query for a 1:M
     */
    private static void configBatchInQuery(Server session, OneToManyMapping mapping, ReadAllQuery raq, List<?> results) {
        if (mapping.getTargetForeignKeyFields().size() != 1) {
            throw new IllegalArgumentException("Cannot configure batch using IN with composite FK: " + mapping);
        }

        Object[] ids = new Object[results.size()];

        for (int index = 0; index < results.size(); index++) {
            List<?> idValues = mapping.getDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(results.get(index), (AbstractSession) session);
            ids[index] = idValues.get(0);
        }

        ExpressionBuilder eb = new ExpressionBuilder(mapping.getReferenceClass());
        raq.setExpressionBuilder(eb);
        Expression idExp = eb.getField(mapping.getTargetForeignKeyFields().get(0)).in(ids);
        raq.setSelectionCriteria(idExp);
    }

    public static void config(Query query, List<?> results, String attribute) {
        if (results == null || results.isEmpty()) {
            return;
        }

        ReadAllQuery raq = JpaHelper.getReadAllQuery(query);
        ClassDescriptor descriptor = raq.getDescriptor();

        if (descriptor == null) {
            throw new IllegalArgumentException("No descriptor found for query: " + raq);
        }

        DatabaseMapping mapping = descriptor.getMappingForAttributeName(attribute);

        if (mapping == null) {
            throw new IllegalArgumentException("No mapping found on " + descriptor + " with name:" + attribute);
        }
        if (!mapping.isOneToOneMapping() && !mapping.isOneToManyMapping()) {
            throw new IllegalArgumentException("Invalid mapping type found: " + mapping);
        }

        ForeignReferenceMapping frMapping = (ForeignReferenceMapping) mapping;
        if (!frMapping.usesIndirection()) {
            throw new IllegalArgumentException("Invalid mapping type found: " + mapping);
        }

        ReadQuery batchQuery = frMapping.prepareNestedBatchQuery(raq);
        Object value = frMapping.getIndirectionPolicy().valueFromBatchQuery(batchQuery, null, raq);
        
        for (Object result: results) {
            frMapping.setAttributeValueInObject(result, value);
        }
    }
}
