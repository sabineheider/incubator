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
 *     dclarke - Bug TBD: Initial Implementation
 ******************************************************************************/
package test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static test.FetchGroupAssert.assertFetchedAttribute;
import static test.FetchGroupAssert.assertNotFetchedAttribute;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import model.Employee;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;

import testing.EclipseLinkJPATest;
import example.Queries;

/**
 * Simple set of tests that verify the {@link FetchGroup} API. Need to verify
 * that the nesting and default behaves as expected.
 * 
 * @author dclarke
 * @since EclipseLink 2.1
 */
@PersistenceContext(unitName = "employee")
public abstract class BaseFetchGroupTests extends EclipseLinkJPATest {

    public void managerFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        // Use q query since find will only use default fetch group
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));

        FetchGroup managerFG = new FetchGroup();
        managerFG.addAttribute("manager");
        query.setHint(QueryHints.FETCH_GROUP, managerFG);

        assertNotNull(JpaHelper.getReadAllQuery(query).getFetchGroup());
        assertSame(managerFG, JpaHelper.getReadAllQuery(query).getFetchGroup());

        Employee emp = (Employee) query.getSingleResult();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetchedAttribute(getEMF(), emp, "id");
        assertNotFetchedAttribute(getEMF(), emp, "firstName");
        assertFetchedAttribute(getEMF(), emp, "version");
        assertFetchedAttribute(getEMF(), emp, "manager");
        assertFetchedAttribute(getEMF(), emp, "address");
        assertFetchedAttribute(getEMF(), emp, "phoneNumbers");
        assertFetchedAttribute(getEMF(), emp, "projects");

        emp.getManager();
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getLastName();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    public void employeeNamesFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        int minId = Queries.minimumEmployeeId(em);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // Use q query since find will only use default fetch group
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", minId);

        FetchGroup namesFG = new FetchGroup();
        namesFG.addAttribute("firstName");
        namesFG.addAttribute("lastName");
        query.setHint(QueryHints.FETCH_GROUP, namesFG);

        assertNotNull(JpaHelper.getReadAllQuery(query).getFetchGroup());
        assertSame(namesFG, JpaHelper.getReadAllQuery(query).getFetchGroup());

        Employee emp = (Employee) query.getSingleResult();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetchedAttribute(getEMF(), emp, "id");
        assertFetchedAttribute(getEMF(), emp, "firstName");
        assertFetchedAttribute(getEMF(), emp, "lastName");
        assertFetchedAttribute(getEMF(), emp, "gender");
        assertFetchedAttribute(getEMF(), emp, "salary");
        assertFetchedAttribute(getEMF(), emp, "version");
        assertFetchedAttribute(getEMF(), emp, "manager");
        assertFetchedAttribute(getEMF(), emp, "address");
        assertFetchedAttribute(getEMF(), emp, "phoneNumbers");
        assertFetchedAttribute(getEMF(), emp, "projects");

        emp.getId();
        emp.getFirstName();
        emp.getLastName();
        emp.getVersion();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getGender();
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetchedAttribute(getEMF(), emp, "gender");
        assertFetchedAttribute(getEMF(), emp, "salary");

        emp.getSalary();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getManager();

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetchedAttribute(getEMF(), emp, "manager");
    }

    public void joinFetchEmployeeAddressWithDynamicFetchGroup() {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address");

        FetchGroup fetchGroup = new FetchGroup("names");
        fetchGroup.addAttribute("firstName");
        fetchGroup.addAttribute("lastName");
        query.setHint(QueryHints.FETCH_GROUP, fetchGroup);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
    }

    public void joinFetchEmployeeAddressPhoneWithDynamicFetchGroup() {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address WHERE e.id IN (SELECT p.id FROM PhoneNumber p)");

        FetchGroup fetchGroup = new FetchGroup("names");
        fetchGroup.addAttribute("firstName");
        fetchGroup.addAttribute("lastName");
        query.setHint(QueryHints.FETCH_GROUP, fetchGroup);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
    }

    protected Employee findMinimumEmployee(EntityManager em) {
        List<Employee> emps = em.createQuery("SELECT e FROM Employee e WHERE e.id in (SELECT MIN(ee.id) FROM Employee ee)").getResultList();

        assertNotNull("Null returned for min employee query", emps);
        assertEquals("No results returned for Mmin employee query", 1, emps.size());

        return emps.get(0);
    }

    public FetchGroup<?> assertHasFetchGroup(Object entity) {
        assertNotNull("Entity is null", entity);
        assertTrue("Entity does not implement FetchGroupTracker", entity instanceof FetchGroupTracker);
        assertNotNull("Entity does not have FetchGroup", ((FetchGroupTracker) entity)._persistence_getFetchGroup());

        return ((FetchGroupTracker) entity)._persistence_getFetchGroup();
    }

    protected FetchGroup getFetchGroup(Object object) {
        assertNotNull("Cannot get a FetchGroup from null", object);

        if (object instanceof Query) {
            return getFetchGroup((Query) object);
        }
        if (object instanceof ObjectLevelReadQuery) {
            return getFetchGroup((ObjectLevelReadQuery) object);
        }
        assertTrue("Entity " + object + " does not implement FetchGroupTracker", object instanceof FetchGroupTracker);

        FetchGroupTracker tracker = (FetchGroupTracker) object;
        return tracker._persistence_getFetchGroup();
    }

    protected FetchGroup getFetchGroup(Query query) {
        return getFetchGroup(JpaHelper.getReadAllQuery(query));
    }

    protected FetchGroup getFetchGroup(ObjectLevelReadQuery readQuery) {
        if (readQuery.hasFetchGroup()) {
            return readQuery.getFetchGroup();
        }
        if (readQuery.shouldUseDefaultFetchGroup() && !readQuery.isPrepared()) {
            ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(readQuery.getReferenceClass());
            if (desc.hasFetchGroupManager()) {
                return desc.getFetchGroupManager().getDefaultFetchGroup();
            }
        }
        return null;
    }

    public void assertConfig(EntityManagerFactory emf, String entityName, FetchGroup defaultFetchGroup, Integer numNamedFetchGroups) {
        ClassDescriptor descriptor = JpaHelper.getServerSession(emf).getClassDescriptorForAlias(entityName);
        assertNotNull("Not descriptor found for: " + entityName, descriptor);

        assertTrue("FetchGroupTracker not implemented by: " + entityName, FetchGroupTracker.class.isAssignableFrom(descriptor.getJavaClass()));

        if (defaultFetchGroup == null) {
            assertNull("Default FetchGroup not null: " + entityName, descriptor.getFetchGroupManager().getDefaultFetchGroup());
        } else {
            assertEquals("Default FetchGroup does not match", defaultFetchGroup, descriptor.getFetchGroupManager().getDefaultFetchGroup());
            if (descriptor.getDescriptorQueryManager().hasReadObjectQuery() && !descriptor.getDescriptorQueryManager().getReadObjectQuery().shouldUseDefaultFetchGroup()) {
                assertEquals("DescriptorQueryManager.readObjectQuery.fetchGroup does not match", defaultFetchGroup, descriptor.getDescriptorQueryManager().getReadObjectQuery().getFetchGroup());
            }
            if (descriptor.getDescriptorQueryManager().hasReadAllQuery() && !descriptor.getDescriptorQueryManager().getReadAllQuery().shouldUseDefaultFetchGroup()) {
                assertEquals("DescriptorQueryManager.readAllQuery.fetchGroup does not match", defaultFetchGroup, descriptor.getDescriptorQueryManager().getReadAllQuery().getFetchGroup());
            }
        }

        if (numNamedFetchGroups != null) {
            assertEquals("Incorrect number of Named FetchGroups: " + entityName, numNamedFetchGroups.intValue(), descriptor.getFetchGroupManager().getFetchGroups().size());
        }
    }

}
