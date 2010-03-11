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
 *     dclarke - Bug 273057: NestedFetchGroup Example
 ******************************************************************************/
package test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static test.FetchGroupAssert.assertFetched;
import static test.FetchGroupAssert.assertFetchedAttribute;
import static test.FetchGroupAssert.assertNoFetchGroup;
import static test.FetchGroupAssert.assertNotFetchedAttribute;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.indirection.IndirectList;
import org.eclipse.persistence.internal.helper.SerializationHelper;
import org.eclipse.persistence.internal.queries.DetachedFetchItem;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.queries.EntityFetchGroup;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroup.FetchItem;
import org.junit.Test;

import example.Queries;

/**
 * Simple tests to verify the functionality of {@link FetchGroup} when the
 * entities are detached through serialization.
 * 
 * @author dclarke
 * @since EclipseLink 2.1
 */
public class SimpleSerializeFetchGroupTests extends BaseFetchGroupTests {

    @Test
    public void verifyWriteReplaceOnFetchGroup() throws Exception {
        FetchGroup<?> fg = new FetchGroup();
        fg.addAttribute("basic");
        fg.addAttribute("a.b");

        assertTrue(fg.getClass() == FetchGroup.class);

        FetchGroup<?> serFG = serialize(fg);

        assertNotNull(serFG);
        assertTrue(serFG.getClass() == EntityFetchGroup.class);
        assertTrue(serFG.hasFetchItems());

        FetchItem basicFI = serFG.getFetchItem("basic");

        assertNotNull(basicFI);
        assertTrue(basicFI instanceof DetachedFetchItem);

        FetchItem aFI = serFG.getFetchItem("a");

        assertNotNull(aFI);
        assertTrue(aFI instanceof DetachedFetchItem);
        assertNotNull(aFI.getFetchGroup());
        assertTrue(aFI.getFetchGroup() instanceof EntityFetchGroup<?>);
        EntityFetchGroup<?> aEFG = (EntityFetchGroup<?>) aFI.getFetchGroup();
        assertNull(aEFG.getParent());
        assertTrue(aEFG.hasFetchItems());

        FetchItem bFI = aEFG.getFetchItem("b");

        assertNotNull(bFI);
        assertTrue(bFI instanceof DetachedFetchItem);
        assertNull(bFI.getFetchGroup());
    }

    @Test
    public void findMinimalFetchGroup() throws Exception {
        EntityManager em = getEntityManager();
        int minId = Queries.minimumEmployeeId(em);

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        Map<String, Object> properties = new HashMap<String, Object>();

        FetchGroup fg = new FetchGroup();
        fg.addAttribute("id");
        fg.addAttribute("version");

        properties.put(QueryHints.FETCH_GROUP, fg);

        Employee emp = em.find(Employee.class, minId, properties);

        assertNotNull(emp);
        assertFetched(getEMF(), emp, fg);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertFetchedAttribute(getEMF(), emp, "id");
        assertFetchedAttribute(getEMF(), emp, "version");
        assertNotFetchedAttribute(getEMF(), emp, "firstName");
        assertNotFetchedAttribute(getEMF(), emp, "lastName");
        assertNotFetchedAttribute(getEMF(), emp, "gender");
        assertNotFetchedAttribute(getEMF(), emp, "salary");
        assertNotFetchedAttribute(getEMF(), emp, "startTime");
        assertNotFetchedAttribute(getEMF(), emp, "endTime");
        assertNotFetchedAttribute(getEMF(), emp, "period");
        assertNotFetchedAttribute(getEMF(), emp, "address");
        assertNotFetchedAttribute(getEMF(), emp, "manager");
        assertNotFetchedAttribute(getEMF(), emp, "phoneNumbers");
        assertNotFetchedAttribute(getEMF(), emp, "projects");

        assertTrue(getFetchGroup(emp).getClass() == EntityFetchGroup.class);
        Employee serEmp = serialize(emp);

        assertNotNull(serEmp);
        assertFetchedAttribute(getEMF(), serEmp, "id");
        assertFetchedAttribute(getEMF(), serEmp, "version");
        assertNotFetchedAttribute(getEMF(), serEmp, "firstName");
        assertNotFetchedAttribute(getEMF(), serEmp, "lastName");
        assertNotFetchedAttribute(getEMF(), serEmp, "gender");
        assertNotFetchedAttribute(getEMF(), serEmp, "salary");
        assertNotFetchedAttribute(getEMF(), serEmp, "startTime");
        assertNotFetchedAttribute(getEMF(), serEmp, "endTime");
        assertNotFetchedAttribute(getEMF(), serEmp, "period");
        assertNotFetchedAttribute(getEMF(), serEmp, "address");
        assertNotFetchedAttribute(getEMF(), serEmp, "manager");
        assertNotFetchedAttribute(getEMF(), serEmp, "phoneNumbers");
        assertNotFetchedAttribute(getEMF(), serEmp, "projects");

        assertTrue(getFetchGroup(serEmp) instanceof EntityFetchGroup<?>);

        serEmp.setFirstName("Doug");
        assertFetchedAttribute(getEMF(), serEmp, "firstName");
    }

    @Test
    public void findEmptyFetchGroup_setUnfetchedSalary() throws Exception {
        EntityManager em = getEntityManager();
        int minId = Queries.minimumEmployeeId(em);

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        Map<String, Object> properties = new HashMap<String, Object>();
        FetchGroup emptyFG = new FetchGroup();
        properties.put(QueryHints.FETCH_GROUP, emptyFG);

        Employee emp = em.find(Employee.class, minId, properties);

        assertNotNull(emp);
        assertFetched(getEMF(), emp, emptyFG);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // Check Basics
        assertFetchedAttribute(getEMF(), emp, "id");
        assertFetchedAttribute(getEMF(), emp, "version");
        assertNotFetchedAttribute(getEMF(), emp, "firstName");
        assertNotFetchedAttribute(getEMF(), emp, "lastName");
        assertNotFetchedAttribute(getEMF(), emp, "gender");
        assertNotFetchedAttribute(getEMF(), emp, "salary");
        assertNotFetchedAttribute(getEMF(), emp, "startTime");
        assertNotFetchedAttribute(getEMF(), emp, "endTime");
        if (emp.getPeriod() != null) {
            assertFetchedAttribute(getEMF(), emp.getPeriod(), "startDate");
            assertFetchedAttribute(getEMF(), emp.getPeriod(), "endDate");
        }
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // Check Relationships
        assertNotFetchedAttribute(getEMF(), emp, "address");
        assertNotFetchedAttribute(getEMF(), emp, "manager");
        assertNotFetchedAttribute(getEMF(), emp, "phoneNumbers");
        assertNotFetchedAttribute(getEMF(), emp, "projects");

        emp.setSalary(1);

        assertFetchedAttribute(getEMF(), emp, "salary");

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        emp.getAddress();

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp.getAddress());

        emp.getPhoneNumbers().size();

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
        }
    }

    /**
     * Verify that attributes added to detached EntityFetchGroup are added using
     * DetachedFetchItem
     */
    @Test
    public void verifyAddAttributeInDetachedEntityFetchGroup() {
        EntityFetchGroup<Employee> detFG = new EntityFetchGroup<Employee>(null);

        detFG.addAttribute("basic");
        detFG.addAttribute("a.b");

        assertNull(detFG.getParent());
        assertEquals(2, detFG.getFetchItems().size());

        FetchItem basicItem = detFG.getFetchItem("basic");
        assertNotNull(basicItem);
        assertEquals("basic", basicItem.getAttributeName());
        assertTrue(basicItem instanceof DetachedFetchItem);
        assertNull(basicItem.getFetchGroup());
        assertSame(detFG, basicItem.getParentFetchGroup());
        assertFalse(basicItem.useDefaultFetchGroup());

        FetchItem aItem = detFG.getFetchItem("a");
        assertNotNull(aItem);
        assertEquals("a", aItem.getAttributeName());
        assertTrue(aItem instanceof DetachedFetchItem);
        assertNotNull(aItem.getFetchGroup());
        assertSame(detFG, aItem.getParentFetchGroup());
        assertFalse(aItem.useDefaultFetchGroup());
        assertTrue(aItem.getFetchGroup() instanceof EntityFetchGroup<?>);

        EntityFetchGroup<?> aFG = (EntityFetchGroup<?>) aItem.getFetchGroup();

        assertEquals(1, aFG.getFetchItems().size());

        FetchItem bItem = aFG.getFetchItem("b");
        assertNotNull(bItem);
        assertEquals("b", bItem.getAttributeName());
        assertTrue(bItem instanceof DetachedFetchItem);
        assertNull(bItem.getFetchGroup());
        assertSame(aFG, bItem.getParentFetchGroup());
        assertFalse(bItem.useDefaultFetchGroup());
    }

    @Test
    public void singleResultEmptyFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        FetchGroup emptyFG = new FetchGroup();
        query.setHint(QueryHints.FETCH_GROUP, emptyFG);

        Employee emp = (Employee) query.getSingleResult();

        assertNotNull(emp);
        assertFetched(getEMF(), emp, emptyFG);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // Check Basics
        assertFetchedAttribute(getEMF(), emp, "id");
        assertFetchedAttribute(getEMF(), emp, "version");
        assertNotFetchedAttribute(getEMF(), emp, "firstName");
        assertNotFetchedAttribute(getEMF(), emp, "lastName");
        assertNotFetchedAttribute(getEMF(), emp, "gender");
        assertNotFetchedAttribute(getEMF(), emp, "salary");
        assertNotFetchedAttribute(getEMF(), emp, "startTime");
        assertNotFetchedAttribute(getEMF(), emp, "endTime");
        if (emp.getPeriod() != null) {
            assertFetchedAttribute(getEMF(), emp.getPeriod(), "startDate");
            assertFetchedAttribute(getEMF(), emp.getPeriod(), "endDate");
        }

        // Check Relationships
        assertNotFetchedAttribute(getEMF(), emp, "address");
        assertNotFetchedAttribute(getEMF(), emp, "manager");
        assertNotFetchedAttribute(getEMF(), emp, "phoneNumbers");
        assertNotFetchedAttribute(getEMF(), emp, "projects");

        emp.getSalary();

        assertFetchedAttribute(getEMF(), emp, "salary");

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        emp.getAddress();

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp.getAddress());

        emp.getPhoneNumbers().size();

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
        }
    }

    /**
     * 
     */
    @Test
    public void resultListEmptyFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        FetchGroup emptyFG = new FetchGroup();
        query.setHint(QueryHints.FETCH_GROUP, emptyFG);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        assertEquals(1, emps.size());

        Employee emp = emps.get(0);

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(getEMF(), emp, emptyFG);

        // Check Basics
        assertFetchedAttribute(getEMF(), emp, "id");
        assertFetchedAttribute(getEMF(), emp, "version");
        assertNotFetchedAttribute(getEMF(), emp, "firstName");
        assertNotFetchedAttribute(getEMF(), emp, "lastName");
        assertNotFetchedAttribute(getEMF(), emp, "gender");
        assertNotFetchedAttribute(getEMF(), emp, "salary");
        assertNotFetchedAttribute(getEMF(), emp, "startTime");
        assertNotFetchedAttribute(getEMF(), emp, "endTime");
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        if (emp.getPeriod() != null) {
            assertFetchedAttribute(getEMF(), emp.getPeriod(), "startDate");
            assertFetchedAttribute(getEMF(), emp.getPeriod(), "endDate");
        }

        // Check Relationships
        assertNotFetchedAttribute(getEMF(), emp, "address");
        assertNotFetchedAttribute(getEMF(), emp, "manager");
        assertNotFetchedAttribute(getEMF(), emp, "phoneNumbers");
        assertNotFetchedAttribute(getEMF(), emp, "projects");

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getSalary();

        assertFetchedAttribute(getEMF(), emp, "salary");
        assertNoFetchGroup(getEMF(), emp);
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNoFetchGroup(getEMF(), emp.getAddress());

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
        }
        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    /**
     * 
     */
    @Test
    public void resultListPeriodFetchGroup() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", Queries.minimumEmployeeId(em));
        FetchGroup fg = new FetchGroup();
        fg.addAttribute("period");
        query.setHint(QueryHints.FETCH_GROUP, fg);

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        assertEquals(1, emps.size());

        Employee emp = emps.get(0);

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(getEMF(), emp, fg);

        // Check Basics
        assertFetchedAttribute(getEMF(), emp, "id");
        assertFetchedAttribute(getEMF(), emp, "version");
        assertNotFetchedAttribute(getEMF(), emp, "firstName");
        assertNotFetchedAttribute(getEMF(), emp, "lastName");
        assertNotFetchedAttribute(getEMF(), emp, "gender");
        assertNotFetchedAttribute(getEMF(), emp, "salary");
        assertNotFetchedAttribute(getEMF(), emp, "startTime");
        assertNotFetchedAttribute(getEMF(), emp, "endTime");
        if (emp.getPeriod() != null) {
            assertFetchedAttribute(getEMF(), emp.getPeriod(), "startDate");
            assertFetchedAttribute(getEMF(), emp.getPeriod(), "endDate");
        }

        // Check Relationships
        assertNotFetchedAttribute(getEMF(), emp, "address");
        assertNotFetchedAttribute(getEMF(), emp, "manager");
        assertNotFetchedAttribute(getEMF(), emp, "phoneNumbers");
        assertNotFetchedAttribute(getEMF(), emp, "projects");

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getSalary();

        assertFetchedAttribute(getEMF(), emp, "salary");
        assertNoFetchGroup(getEMF(), emp);

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertNoFetchGroup(getEMF(), emp.getAddress());

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
        }
        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
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

        assertFetched(getEMF(), emp, managerFG);
        assertFetchedAttribute(getEMF(), emp, "manager");
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getManager();
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        if (emp.getManager() != null) {
            assertFetchedAttribute(getEMF(), emp, "manager");
        }

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getLastName();

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
            phone.getAreaCode();
        }

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void managerFetchGroupWithJoinFetch() throws Exception {
        EntityManager em = getEntityManager();

        int minId = Queries.minimumEmployeeId(em);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // Use q query since find will only use default fetch group
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.id = :ID");
        query.setParameter("ID", minId);
        FetchGroup managerFG = new FetchGroup();
        managerFG.addAttribute("manager");

        query.setHint(QueryHints.FETCH_GROUP, managerFG);
        query.setHint(QueryHints.LEFT_FETCH, "e.manager");

        assertNotNull(JpaHelper.getReadAllQuery(query).getFetchGroup());
        assertSame(managerFG, JpaHelper.getReadAllQuery(query).getFetchGroup());

        Employee emp = (Employee) query.getSingleResult();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(getEMF(), emp, managerFG);
        assertFetchedAttribute(getEMF(), emp, "manager");

        emp.getManager();
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getLastName();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
            phone.getAreaCode();
        }

        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
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
        assertFetched(getEMF(), emp, namesFG);

        emp.getId();
        emp.getFirstName();
        emp.getLastName();
        emp.getVersion();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(getEMF(), emp, namesFG);

        emp.getGender();
        emp.getSalary();

        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp);

        for (PhoneNumber phone : emp.getPhoneNumbers()) {
            assertNoFetchGroup(getEMF(), phone);
            phone.getAreaCode();
        }
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        emp.getManager();

        assertEquals(5, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNoFetchGroup(getEMF(), emp.getManager());
    }

    @Test
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

    @Test
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

    @Test
    public void verifyUnfetchedAttributes() throws Exception {
        EntityManager em = getEntityManager();

        TypedQuery<Employee> q = em.createQuery("SELECT e FROM Employee e WHERE e.id IN (SELECT MIN(p.id) FROM PhoneNumber p)", Employee.class);
        FetchGroup<Employee> fg = new FetchGroup<Employee>("Employee.empty");
        q.setHint(QueryHints.FETCH_GROUP, fg);
        Employee emp = q.getSingleResult();

        assertNotNull(emp);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        // This check using the mapping returns a default (empty) IndirectList
        OneToManyMapping phoneMapping = (OneToManyMapping) getDescriptor(emp).getMappingForAttributeName("phoneNumbers");
        IndirectList phones = (IndirectList) phoneMapping.getAttributeValueFromObject(emp);
        assertNotNull(phones);
        assertTrue(phones.isInstantiated());
        assertEquals(0, phones.size());
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        IndirectList phonesIL = (IndirectList) emp.getPhoneNumbers();
        assertFalse(phonesIL.isInstantiated());
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertTrue(emp.getPhoneNumbers().size() > 0);
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void verifyFetchedRelationshipAttributes() throws Exception {
        EntityManager em = getEntityManager();

        FetchGroup<Employee> fg = new FetchGroup<Employee>("Employee.relationships");
        fg.addAttribute("address");
        fg.addAttribute("phoneNumbers");
        fg.addAttribute("manager");
        fg.addAttribute("projects");

        Map<String, Object> hints = new HashMap<String, Object>();
        hints.put(QueryHints.FETCH_GROUP, fg);

        Employee emp = Queries.minimumEmployee(em, hints);

        assertNotNull(emp);

    }

    private <T> T serialize(Serializable entity) throws IOException, ClassNotFoundException {
        byte[] bytes = SerializationHelper.serialize(entity);
        return (T) SerializationHelper.deserialize(bytes);
    }

    /**
     * Verify the state of all descriptors where no FetchGroup have been
     * configured.
     */
    @Override
    protected EntityManager getEntityManager() {
        assertConfig(getEMF(), "Employee", null, 0);
        assertConfig(getEMF(), "Address", null, 0);
        assertConfig(getEMF(), "PhoneNumber", null, 0);
        assertConfig(getEMF(), "Project", null, 0);
        assertConfig(getEMF(), "SmallProject", null, 0);
        assertConfig(getEMF(), "LargeProject", null, 0);

        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
        return super.getEntityManager();
    }

}
