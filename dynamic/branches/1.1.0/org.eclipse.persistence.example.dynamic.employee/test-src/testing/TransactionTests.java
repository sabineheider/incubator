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
package testing;

import static junit.framework.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.Test;

import example.*;

@PersistenceContext(unitName="employee")
public class TransactionTests extends EclipseLinkJPATest {

	Transactions transactions = new Transactions();
	private static Sample samplePopulation;

	@Test
	public void pessimisticLocking() throws Exception {
		transactions.pessimisticLocking(getEntityManager());
	}

	@Test
	public void updateEmployeeWithCity() throws Exception {
		EntityManager em = getEntityManager();

		transactions.updateEmployeeWithCity(em);
		getSample().resetDatabase(em);
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

        Server session = JpaHelper.getServerSession(getEMF());
        ClassDescriptor descriptor = session.getDescriptorForAlias("Employee");
        
		int minId = Queries.minimumEmployeeId(em);		
        DynamicEntity emp = (DynamicEntity) em.find(descriptor.getJavaClass(), minId);
		assertNotNull(emp);
		
		emp.set("salary", emp.get("salary", Integer.class) + 1);
		
		em.getTransaction().begin();
		
		getQuerySQLTracker(em).reset();
		assertEquals(0, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
		
		
		em.merge(emp);
		
		em.flush();
		assertEquals(2, getQuerySQLTracker(em).getTotalSQLUPDATECalls());
		
	}
	
	
	public Sample getSample() {
	    if (samplePopulation == null) {
	        samplePopulation = new Sample(getEMF());
	    }
	    return samplePopulation;
	}
}
