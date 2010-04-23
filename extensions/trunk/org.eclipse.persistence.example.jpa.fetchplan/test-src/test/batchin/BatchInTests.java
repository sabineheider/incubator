package test.batchin;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import static org.junit.Assert.*;
import model.Employee;
import model.Gender;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.extension.query.BatchInConfig;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolder;
import org.eclipse.persistence.internal.indirection.BatchValueHolder;
import org.eclipse.persistence.internal.indirection.QueryBasedValueHolder;
import org.eclipse.persistence.internal.indirection.UnitOfWorkValueHolder;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.junit.Before;
import org.junit.Test;

import testing.EclipseLinkJPAAssert;
import testing.EclipseLinkJPATest;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class BatchInTests extends EclipseLinkJPATest {

    @Test
    public void findAllEmployeesBatchJoinAddress() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT e FROM Employee e");
        q.setHint(QueryHints.BATCH, "e.address");

        List<Employee> emps = q.getResultList();

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotNull(emps);
        assertFalse(emps.isEmpty());

        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToOneMapping addressMapping = (OneToOneMapping) desc.getMappingForAttributeName("address");
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) addressMapping.getAttributeValueFromObject(emp1);
        assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);

        emp1.getAddress();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertNotNull(emp.getAddress());
        }
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findAllEmployeesBatchInAddress() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT e FROM Employee e");
        q.setHint(QueryHints.BATCH, "e.address");

        List<Employee> emps = q.getResultList();

        BatchInConfig.config(em, emps, "address");

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotNull(emps);
        assertFalse(emps.isEmpty());

        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToOneMapping addressMapping = (OneToOneMapping) desc.getMappingForAttributeName("address");
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) addressMapping.getAttributeValueFromObject(emp1);
        assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);

        emp1.getAddress();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertNotNull(emp.getAddress());
        }
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findMaleEmployeesBatchJoinAddress() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        q.setParameter("GENDER", Gender.Male);
        q.setHint(QueryHints.BATCH, "e.address");

        List<Employee> emps = q.getResultList();

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotNull(emps);
        assertFalse(emps.isEmpty());

        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToOneMapping addressMapping = (OneToOneMapping) desc.getMappingForAttributeName("address");
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) addressMapping.getAttributeValueFromObject(emp1);
        assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);

        emp1.getAddress();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertNotNull(emp.getAddress());
        }
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findMaleEmployeesBatchInAddress() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        q.setParameter("GENDER", Gender.Male);
        q.setHint(QueryHints.BATCH, "e.address");

        List<Employee> emps = q.getResultList();

        BatchInConfig.config(em, emps, "address");

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotNull(emps);
        assertFalse(emps.isEmpty());

        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToOneMapping addressMapping = (OneToOneMapping) desc.getMappingForAttributeName("address");
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) addressMapping.getAttributeValueFromObject(emp1);
        assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);

        emp1.getAddress();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertNotNull(emp.getAddress());
        }
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findAllEmployeesBatchJoinPhones() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT e FROM Employee e");
        q.setHint(QueryHints.BATCH, "e.phoneNumbers");

        List<Employee> emps = q.getResultList();

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotNull(emps);
        assertFalse(emps.isEmpty());

        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToManyMapping phoneMapping = (OneToManyMapping) desc.getMappingForAttributeName("phoneNumbers");
        IndirectContainer phonesContainer = (IndirectContainer) phoneMapping.getAttributeValueFromObject(emp1);
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) phonesContainer.getValueHolder();
        assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);

        emp1.getPhoneNumbers().size();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertFalse(emp.getPhoneNumbers().isEmpty());
        }
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findAllEmployeesBatchInPhones() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT e FROM Employee e");
        q.setHint(QueryHints.BATCH, "e.phoneNumbers");

        List<Employee> emps = q.getResultList();

        BatchInConfig.config(em, emps, "phoneNumbers");

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotNull(emps);
        assertFalse(emps.isEmpty());

        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToManyMapping phoneMapping = (OneToManyMapping) desc.getMappingForAttributeName("phoneNumbers");
        IndirectContainer phonesContainer = (IndirectContainer) phoneMapping.getAttributeValueFromObject(emp1);
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) phonesContainer.getValueHolder();
        assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);

        emp1.getPhoneNumbers().size();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertFalse(emp.getPhoneNumbers().isEmpty());
        }
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findMaleEmployeesBatchJoinPhones() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        q.setParameter("GENDER", Gender.Male);
        q.setHint(QueryHints.BATCH, "e.phoneNumbers");

        List<Employee> emps = q.getResultList();

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotNull(emps);
        assertFalse(emps.isEmpty());

        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToManyMapping phoneMapping = (OneToManyMapping) desc.getMappingForAttributeName("phoneNumbers");
        IndirectContainer phonesContainer = (IndirectContainer) phoneMapping.getAttributeValueFromObject(emp1);
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) phonesContainer.getValueHolder();
        assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);

        emp1.getPhoneNumbers().size();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertFalse(emp.getPhoneNumbers().isEmpty());
        }
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findMaleEmployeesBatchInPhones() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        q.setParameter("GENDER", Gender.Male);
        q.setHint(QueryHints.BATCH, "e.phoneNumbers");

        List<Employee> emps = q.getResultList();

        BatchInConfig.config(em, emps, "phoneNumbers");

        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertNotNull(emps);
        assertFalse(emps.isEmpty());

        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToManyMapping phoneMapping = (OneToManyMapping) desc.getMappingForAttributeName("phoneNumbers");
        IndirectContainer phonesContainer = (IndirectContainer) phoneMapping.getAttributeValueFromObject(emp1);
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) phonesContainer.getValueHolder();
        assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);

        emp1.getPhoneNumbers().size();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        for (Employee emp : emps) {
            assertFalse(emp.getPhoneNumbers().isEmpty());
        }
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    /**
     * Test that batch reading where entities are already in memory works
     */
    @Test
    public void verifyBatchingOnReadEntities() {
        OneToOneMapping addrMapping = (OneToOneMapping) getDescriptor("Employee").getMappingForAttributeName("address");
        OneToManyMapping phonesMapping = (OneToManyMapping) getDescriptor("Employee").getMappingForAttributeName("phoneNumbers");
        OneToOneMapping managerMapping = (OneToOneMapping) getDescriptor("Employee").getMappingForAttributeName("manager");
        OneToManyMapping managedEmpsMapping = (OneToManyMapping) getDescriptor("Employee").getMappingForAttributeName("managedEmployees");

        EntityManager em = getEntityManager();

        List<Employee> allEmps = em.createQuery("SELECT e FROM Employee e").getResultList();

        // Verify that just a UOW
        for (Employee emp : allEmps) {
            assertQueryBasedValueHolder(addrMapping.getAttributeValueFromObject(emp));
            assertQueryBasedValueHolder(phonesMapping.getAttributeValueFromObject(emp));
            assertQueryBasedValueHolder(managerMapping.getAttributeValueFromObject(emp));
            assertQueryBasedValueHolder(managedEmpsMapping.getAttributeValueFromObject(emp));
        }

        Query query = em.createQuery("SELECT e FROM Employee e");
        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");
        query.setHint(QueryHints.BATCH, "e.manager");
        query.setHint(QueryHints.BATCH, "e.managedEmployees");

        // Currently the Batch ValueHolders are only added when the cahe is clear
        em.clear();
        allEmps = query.getResultList();

        // Verify that just a UOW
        for (Employee emp : allEmps) {
            assertBatchValueHolder(addrMapping.getAttributeValueFromObject(emp));
            assertBatchValueHolder(phonesMapping.getAttributeValueFromObject(emp));
            assertBatchValueHolder(managerMapping.getAttributeValueFromObject(emp));
            assertBatchValueHolder(managedEmpsMapping.getAttributeValueFromObject(emp));
        }
    }

    @Before
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    @Override
    protected void verifyConfig(EntityManager em) {
        super.verifyConfig(em);

        ClassDescriptor employeeDescriptor = EclipseLinkJPAAssert.assertEntity(getEMF(), "Employee");
        EclipseLinkJPAAssert.assertWoven(employeeDescriptor);
        EclipseLinkJPAAssert.assertLazy(employeeDescriptor, "address");
        EclipseLinkJPAAssert.assertLazy(employeeDescriptor, "phoneNumbers");
        EclipseLinkJPAAssert.assertLazy(employeeDescriptor, "manager");
        EclipseLinkJPAAssert.assertLazy(employeeDescriptor, "managedEmployees");
    }

    private void assertQueryBasedValueHolder(Object value) {
        assertNotNull(value);

        Object proxy = value;

        if (proxy instanceof IndirectContainer) {
            proxy = ((IndirectContainer) proxy).getValueHolder();
        }

        if (proxy instanceof UnitOfWorkValueHolder) {
            proxy = ((UnitOfWorkValueHolder) proxy).getWrappedValueHolder();
        }
        if (proxy.getClass() != ValueHolder.class) {
            assertEquals(QueryBasedValueHolder.class, proxy.getClass());
        }
    }

    private void assertBatchValueHolder(Object value) {
        assertNotNull(value);

        Object proxy = value;

        if (proxy instanceof IndirectContainer) {
            proxy = ((IndirectContainer) proxy).getValueHolder();
        }

        if (proxy instanceof UnitOfWorkValueHolder) {
            proxy = ((UnitOfWorkValueHolder) proxy).getWrappedValueHolder();
        }

        if (proxy.getClass() != ValueHolder.class) {
            assertEquals(BatchValueHolder.class, proxy.getClass());
        }
    }
}
