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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import javax.persistence.PersistenceContext;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.testing.models.dynamic.employee.*;
import org.eclipse.persistence.testing.tests.dynamic.EclipseLinkORMTest;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.Test;

/**
 * Simple query examples for the XML mapped Employee domain model.
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
@PersistenceContext(unitName = "empty")
public class QueryTests extends EclipseLinkORMTest {

    private Queries examples = new Queries();

    private Sample samples;

    public Queries getQueries() {
        return this.examples;
    }

    public Sample getSamples() {
        if (this.samples == null) {
            this.samples = new Sample(getSharedSession());
        }
        return this.samples;
    }

    /**
     * Simple example using dynamic JP QL to retrieve all Employee instances
     * sorted by lastName and firstName.
     */
    @Test
    public void readAllEmployees() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        List<DynamicEntity> emps = getQueries().readAllEmployees(helper, session);

        getSamples().assertSame(emps);
        session.release();
    }

    @Test
    public void readAllEmployeesWithAddress() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        List<DynamicEntity> emps = getQueries().readAllEmployeesWithAddress(helper, session);
        assertNotNull(emps);
    }

    @Test
    public void joinFetchHint() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        List<DynamicEntity> emps = getQueries().readAllEmployeesWithAddress(helper, session);
        assertNotNull(emps);
    }

    @Test
    public void minEmployeeId() {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        int minId = Queries.minimumEmployeeId(helper, session);

        assertTrue(minId > 0);
    }

    @Test
    public void testReadAllExressions() throws Exception {
        DynamicHelper helper = new DynamicHelper(getSharedSession());
        Session session = getSession();

        List<DynamicEntity> emps = getQueries().findUsingNativeReadAllQuery(helper, session);

        assertNotNull(emps);
    }

    @Override
    protected DatabaseSession createSharedSession() {
        DatabaseSession shared = super.createSharedSession();

        if (shared.getDescriptors().isEmpty()) {
            EmployeeDynamicMappings.createTypes(shared, "model.dynamic.employee", false);
        }
        new SchemaManager(shared).replaceDefaultTables();
        this.samples = new Sample(shared);
        this.samples.persistAll(shared);

        return shared;
    }

}