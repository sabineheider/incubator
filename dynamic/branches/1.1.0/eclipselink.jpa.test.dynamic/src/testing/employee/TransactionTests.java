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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.junit.Test;

import testing.util.EclipseLinkJPATest;
import example.employee.EmployeeDynamicMappings;
import example.employee.Queries;
import example.employee.Sample;
import example.employee.Transactions;

@PersistenceContext(unitName = "empty")
public class TransactionTests extends EclipseLinkJPATest {

    Transactions transactions = new Transactions();
    private static Sample samplePopulation;

    @Test
    public void pessimisticLocking() throws Exception {
        transactions.pessimisticLocking(getEntityManager());
    }

    @Test
    public void createUsingPersist() throws Exception {
        EntityManager em = getEntityManager();

        DynamicEntity emp = transactions.createUsingPersist(em);

        assertNotNull(emp);
        assertTrue(emp.get("id", Integer.class) > 0);

        em.getTransaction().begin();
        em.createQuery("DELETE from PhoneNumber p WHERE p.owner.id = " + emp.get("id")).executeUpdate();
        em.createQuery("DELETE from Employee e WHERE e.id = " + emp.get("id")).executeUpdate();
        em.createQuery("DELETE from Address a WHERE a.id = " + emp.get("address", DynamicEntity.class).get("id", Integer.class)).executeUpdate();
        em.getTransaction().commit();

        getSample().verifyCounts(em);
    }

    @Test
    public void createUsingMerge() throws Exception {
        EntityManager em = getEntityManager();

        DynamicEntity emp = transactions.createUsingMerge(em);

        assertNotNull(emp);
        assertTrue(emp.get("id", Integer.class) > 0);

        em.getTransaction().begin();
        em.createQuery("DELETE from PhoneNumber p WHERE p.owner.id = " + emp.get("id")).executeUpdate();
        em.createQuery("DELETE from Employee e WHERE e.id = " + emp.get("id")).executeUpdate();
        em.createQuery("DELETE from Address a WHERE a.id = " + emp.get("address", DynamicEntity.class).get("id", Integer.class)).executeUpdate();
        em.getTransaction().commit();

        getSample().verifyCounts(em);
    }

    @Test
    public void mergeDetached() throws Exception {
        EntityManager em = getEntityManager();

        EntityType empType = DynamicHelper.getType(JpaHelper.getServerSession(getEMF()), "Employee");

        int minId = Queries.minimumEmployeeId(em);
        DynamicEntity emp = (DynamicEntity) em.find(empType.getJavaClass(), minId);
        assertNotNull(emp);

        em.clear();

        emp.set("salary", emp.get("salary", Integer.class) + 1);

        em.getTransaction().begin();

        getQuerySQLTracker(em).reset();
        assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());

        em.merge(emp);

        em.flush();
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLUPDATECalls());

    }

    public Sample getSample() {
        if (samplePopulation == null) {
            samplePopulation = new Sample(getEMF());
        }
        return samplePopulation;
    }

    @Override
    protected EntityManagerFactory createEMF(String unitName, Map properties) {
        EntityManagerFactory emf = super.createEMF(unitName, properties);

        EmployeeDynamicMappings.createTypes(emf, "example.model.employee", true);

        Server session = JpaHelper.getServerSession(emf);

        DynamicSchemaManager dsm = new DynamicSchemaManager(session);
        dsm.replaceDefaultTables(true, true);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        new Sample(emf).persistAll(em);
        em.getTransaction().commit();
        em.close();

        return emf;
    }

}
