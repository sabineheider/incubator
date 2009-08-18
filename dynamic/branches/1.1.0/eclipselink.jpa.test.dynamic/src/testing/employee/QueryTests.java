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

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.junit.Test;

import testing.util.EclipseLinkJPATest;
import example.employee.EntityTypeFactory;
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

    public Queries getExamples() {
        return this.examples;
    }

    /**
     * Simple example using dynamic JP QL to retrieve all Employee instances
     * sorted by lastName and firstName.
     */
    @Test
    public void readAllEmployees_JPQL() {
        EntityManager em = getEntityManager();

        List<DynamicEntity> emps = getExamples().readAllEmployeesUsingJPQL(em);

        new Sample(getEMF()).assertSame(emps);
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

    @Test
    public void largeStackOR() {
        ClassDescriptor descriptor = JpaHelper.getServerSession(getEMF()).getDescriptorForAlias("Employee");

        ReadAllQuery query = new ReadAllQuery();
        query.setReferenceClass(descriptor.getJavaClass());
        Expression expressionRoot = query.getExpressionBuilder();

        Expression expression = null;

        for (int i = 0; i < 2002; i++) {
            Expression filter = expressionRoot.get("firstName").equal("" + i);
            expression = filter.or(expression);
        }

        query.setSelectionCriteria(expression);

        List<DynamicEntity> results = JpaHelper.createQuery(query, getEntityManager()).getResultList();

        assertNotNull(results);

    }

    @Override
    protected EntityManagerFactory createEMF(String unitName, Map properties) {
        EntityManagerFactory emf = super.createEMF(unitName, properties);

        EntityTypeFactory.createTypes(emf, "example.model.employee", true);

        EntityManager em = emf.createEntityManager();
        Sample samples = new Sample(emf);

        samples.resetDatabase(em);
        em.clear();

        em.getTransaction().begin();
        samples.persistAll(em);
        em.getTransaction().commit();

        em.close();

        return emf;
    }
}
