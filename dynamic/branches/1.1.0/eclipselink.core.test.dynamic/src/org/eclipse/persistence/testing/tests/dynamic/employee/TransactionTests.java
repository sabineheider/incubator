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
 * 		dclarke - initial JPA Employee example using XML (bug 217884)
 ******************************************************************************/
package org.eclipse.persistence.testing.tests.dynamic.employee;

import static junit.framework.Assert.*;

import javax.persistence.PersistenceContext;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.testing.models.dynamic.employee.*;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.junit.Test;

@PersistenceContext(unitName = "empty")
public class TransactionTests extends EclipseLinkORMTest {

    Transactions transactions = new Transactions();
    Queries queries = new Queries();
    private static Sample samplePopulation;

    @Test
    public void pessimisticLocking() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        transactions.pessimisticLocking(helper, getSession());
    }

    @Test
    public void createUsingPersist() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        DynamicEntity emp = transactions.createUsingPersist(helper, session);

        assertNotNull(emp);
        assertTrue(emp.<Integer> get("id") > 0);

        UnitOfWork uow = session.acquireUnitOfWork();
        DynamicEntity empWC = (DynamicEntity) uow.readObject(emp);
        // Delete assuming private owned relationships;
        uow.deleteObject(empWC);
        uow.commit();

        getSample().verifyCounts(session);
    }

    @Test
    public void createUsingMerge() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        DynamicEntity emp = transactions.createUsingMerge(helper, session);

        assertNotNull(emp);
        assertTrue(emp.<Integer> get("id") > 0);

        UnitOfWork uow = session.acquireUnitOfWork();
        DynamicEntity empWC = (DynamicEntity) uow.readObject(emp);
        // Delete assuming private owned relationships;
        uow.deleteObject(empWC);
        uow.commit();

        getSample().verifyCounts(session);
    }

    @Test
    public void mergeDetached_UOW() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        // get shared copy
        DynamicEntity emp = this.queries.minimumEmployee(helper, session);
        assertNotNull(emp);

        // Create detached copy using separate UOW
        UnitOfWork detachUOW = session.acquireUnitOfWork();
        DynamicEntity detachedEmp = (DynamicEntity) detachUOW.readObject(emp);
        detachUOW.release();

        detachedEmp.set("salary", emp.<Integer> get("salary") + 1);

        getQuerySQLTracker(session).reset();
        assertEquals(0, getQuerySQLTracker(session).getTotalSQLUPDATECalls());

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.deepMergeClone(detachedEmp);
        uow.writeChanges();
        assertEquals(1, getQuerySQLTracker(session).getTotalSQLUPDATECalls());

        uow.release();
    }

    @Test
    public void mergeDetached_Copy() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        // get shared copy
        DynamicEntity emp = this.queries.minimumEmployee(helper, session);
        assertNotNull(emp);

        // Create detached copy using session.copyObject
        ObjectCopyingPolicy policy = new ObjectCopyingPolicy();
        policy.setShouldResetPrimaryKey(false);
        policy.setDepth(2);
        DynamicEntity detachedEmp = (DynamicEntity) session.copyObject(emp, policy);

        detachedEmp.set("salary", emp.<Integer> get("salary") + 1);

        getQuerySQLTracker(session).reset();
        assertEquals(0, getQuerySQLTracker(session).getTotalSQLUPDATECalls());

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.deepMergeClone(detachedEmp);
        uow.writeChanges();
        assertEquals(1, getQuerySQLTracker(session).getTotalSQLUPDATECalls());

        uow.release();
    }

    public Sample getSample() {
        if (samplePopulation == null) {
            samplePopulation = new Sample(getSharedSession());
        }
        return samplePopulation;
    }

    @Override
    protected DatabaseSession createSharedSession() {
        DatabaseSession shared = super.createSharedSession();

        EmployeeDynamicMappings.createTypes(shared, "example.model.employee", true);

        DynamicSchemaManager dsm = new DynamicSchemaManager(shared);
        dsm.replaceDefaultTables(true, true);

        getSample().persistAll(shared);

        return shared;
    }

}
