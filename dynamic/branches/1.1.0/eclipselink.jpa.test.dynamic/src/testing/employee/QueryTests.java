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
package testing.employee;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.junit.Test;

import testing.util.EclipseLinkJPATest;
import example.employee.EmployeeDynamicMappings;
import example.employee.Queries;
import example.employee.Sample;

/**
 * Simple query examples for the XML mapped Employee domain model.
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
@PersistenceContext(unitName = "empty")
public class QueryTests extends EclipseLinkJPATest {

    private Queries examples = new Queries();

    private Sample samples;
    
    public Queries getExamples() {
        return this.examples;
    }
    
    public Sample getSamples() {
        if (this.samples == null) {
            this.samples = new Sample(getEMF());
        }
        return this.samples;
    }

    /**
     * Simple example using dynamic JP QL to retrieve all Employee instances
     * sorted by lastName and firstName.
     */
    @Test
    public void readAllEmployees_JPQL() {
        EntityManager em = getEntityManager();

        List<DynamicEntity> emps = getExamples().readAllEmployeesUsingJPQL(em);

        getSamples().assertSame(emps);
    }

    @Test
    public void joinFetchJPQL() {
        List<DynamicEntity> emps = getExamples().joinFetchJPQL(getEntityManager());
        assertNotNull(emps);
    }

    @Test
    public void joinFetchHint() {
        List<DynamicEntity> emps = getExamples().joinFetchHint(getEntityManager());
        assertNotNull(emps);
    }

    @Test
    public void minEmployeeId() {
        getExamples();
        int minId = Queries.minimumEmployeeId(getEntityManager());

        assertTrue(minId > 0);
    }

    @Test
    public void testGenderIn() throws Exception {
        List<DynamicEntity> emps = getExamples().findEmployeesUsingGenderIn(getEntityManager());

        assertNotNull(emps);
    }

    @Test
    public void testReadAllExressions() throws Exception {
        List<DynamicEntity> emps = getExamples().findUsingNativeReadAllQuery(getEntityManager());

        assertNotNull(emps);
    }

    @Override
    protected EntityManagerFactory createEMF(String unitName, Map properties) {
        EntityManagerFactory emf = super.createEMF(unitName, properties);

        EmployeeDynamicMappings.createTypes(emf, "example.model.employee", true);

        Server session = JpaHelper.getServerSession(emf);

        DynamicSchemaManager dsm = new DynamicSchemaManager(session);
        dsm.replaceDefaultTables(false, true);
        
        this.samples = new Sample(emf);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        getSamples().persistAll(em);
        em.getTransaction().commit();
        em.close();

        return emf;
    }
}
