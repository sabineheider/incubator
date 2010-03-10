package test.batchin;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import junit.framework.Assert;
import model.Employee;
import model.Gender;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.extension.query.BatchInConfig;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.internal.indirection.BatchValueHolder;
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
        
        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Assert.assertNotNull(emps);
        Assert.assertFalse(emps.isEmpty());
        
        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToOneMapping addressMapping = (OneToOneMapping) desc.getMappingForAttributeName("address");
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) addressMapping.getAttributeValueFromObject(emp1);
        Assert.assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);
        
        emp1.getAddress();
        
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        for (Employee emp: emps) {
            Assert.assertNotNull(emp.getAddress());
        }
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findAllEmployeesBatchInAddress() {
        EntityManager em = getEntityManager();
        
        Query q = em.createQuery("SELECT e FROM Employee e");
        q.setHint(QueryHints.BATCH, "e.address");
        
        List<Employee> emps = q.getResultList();
        
        BatchInConfig.config(em, emps, "address");
        
        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Assert.assertNotNull(emps);
        Assert.assertFalse(emps.isEmpty());
        
        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToOneMapping addressMapping = (OneToOneMapping) desc.getMappingForAttributeName("address");
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) addressMapping.getAttributeValueFromObject(emp1);
        Assert.assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);
        
        emp1.getAddress();
        
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        for (Employee emp: emps) {
            Assert.assertNotNull(emp.getAddress());
        }
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findMaleEmployeesBatchJoinAddress() {
        EntityManager em = getEntityManager();
        
        Query q = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        q.setParameter("GENDER", Gender.Male);
        q.setHint(QueryHints.BATCH, "e.address");
        
        List<Employee> emps = q.getResultList();
        
        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Assert.assertNotNull(emps);
        Assert.assertFalse(emps.isEmpty());
        
        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToOneMapping addressMapping = (OneToOneMapping) desc.getMappingForAttributeName("address");
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) addressMapping.getAttributeValueFromObject(emp1);
        Assert.assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);
        
        emp1.getAddress();
        
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        for (Employee emp: emps) {
            Assert.assertNotNull(emp.getAddress());
        }
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findMaleEmployeesBatchInAddress() {
        EntityManager em = getEntityManager();
        
        Query q = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        q.setParameter("GENDER", Gender.Male);
        q.setHint(QueryHints.BATCH, "e.address");
        
        List<Employee> emps = q.getResultList();
        
        BatchInConfig.config(em, emps, "address");
        
        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Assert.assertNotNull(emps);
        Assert.assertFalse(emps.isEmpty());
        
        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToOneMapping addressMapping = (OneToOneMapping) desc.getMappingForAttributeName("address");
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) addressMapping.getAttributeValueFromObject(emp1);
        Assert.assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);
        
        emp1.getAddress();
        
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        for (Employee emp: emps) {
            Assert.assertNotNull(emp.getAddress());
        }
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findAllEmployeesBatchJoinPhones() {
        EntityManager em = getEntityManager();
        
        Query q = em.createQuery("SELECT e FROM Employee e");
        q.setHint(QueryHints.BATCH, "e.phoneNumbers");
        
        List<Employee> emps = q.getResultList();
        
        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Assert.assertNotNull(emps);
        Assert.assertFalse(emps.isEmpty());
        
        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToManyMapping phoneMapping = (OneToManyMapping) desc.getMappingForAttributeName("phoneNumbers");
        IndirectContainer phonesContainer = (IndirectContainer) phoneMapping.getAttributeValueFromObject(emp1);
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) phonesContainer.getValueHolder();
        Assert.assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);
        
        emp1.getPhoneNumbers().size();
        
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        for (Employee emp: emps) {
            Assert.assertFalse(emp.getPhoneNumbers().isEmpty());
        }
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }
    
    @Test
    public void findAllEmployeesBatchInPhones() {
        EntityManager em = getEntityManager();
        
        Query q = em.createQuery("SELECT e FROM Employee e");
        q.setHint(QueryHints.BATCH, "e.phoneNumbers");
        
        List<Employee> emps = q.getResultList();
        
        BatchInConfig.config(em, emps, "phoneNumbers");

        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Assert.assertNotNull(emps);
        Assert.assertFalse(emps.isEmpty());
        
        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToManyMapping phoneMapping = (OneToManyMapping) desc.getMappingForAttributeName("phoneNumbers");
        IndirectContainer phonesContainer = (IndirectContainer) phoneMapping.getAttributeValueFromObject(emp1);
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) phonesContainer.getValueHolder();
        Assert.assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);
        
        emp1.getPhoneNumbers().size();
        
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        for (Employee emp: emps) {
            Assert.assertFalse(emp.getPhoneNumbers().isEmpty());
        }
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void findMaleEmployeesBatchJoinPhones() {
        EntityManager em = getEntityManager();
        
        Query q = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        q.setParameter("GENDER", Gender.Male);
        q.setHint(QueryHints.BATCH, "e.phoneNumbers");
        
        List<Employee> emps = q.getResultList();
        
        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Assert.assertNotNull(emps);
        Assert.assertFalse(emps.isEmpty());
        
        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToManyMapping phoneMapping = (OneToManyMapping) desc.getMappingForAttributeName("phoneNumbers");
        IndirectContainer phonesContainer = (IndirectContainer) phoneMapping.getAttributeValueFromObject(emp1);
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) phonesContainer.getValueHolder();
        Assert.assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);
        
        emp1.getPhoneNumbers().size();
        
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        for (Employee emp: emps) {
            Assert.assertFalse(emp.getPhoneNumbers().isEmpty());
        }
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }
    
    @Test
    public void findMaleEmployeesBatchInPhones() {
        EntityManager em = getEntityManager();
        
        Query q = em.createQuery("SELECT e FROM Employee e WHERE e.gender = :GENDER");
        q.setParameter("GENDER", Gender.Male);
        q.setHint(QueryHints.BATCH, "e.phoneNumbers");
        
        List<Employee> emps = q.getResultList();
        
        BatchInConfig.config(em, emps, "phoneNumbers");

        Assert.assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        Assert.assertNotNull(emps);
        Assert.assertFalse(emps.isEmpty());
        
        Employee emp1 = emps.get(0);
        ClassDescriptor desc = JpaHelper.getServerSession(getEMF()).getClassDescriptor(emp1);
        OneToManyMapping phoneMapping = (OneToManyMapping) desc.getMappingForAttributeName("phoneNumbers");
        IndirectContainer phonesContainer = (IndirectContainer) phoneMapping.getAttributeValueFromObject(emp1);
        UnitOfWorkValueHolder uowvh = (UnitOfWorkValueHolder) phonesContainer.getValueHolder();
        Assert.assertTrue(uowvh.getWrappedValueHolder() instanceof BatchValueHolder);
        
        emp1.getPhoneNumbers().size();
        
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        for (Employee emp: emps) {
            Assert.assertFalse(emp.getPhoneNumbers().isEmpty());
        }
        Assert.assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Before
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    @Override
    protected void verifyConfig(EntityManager em) {
        super.verifyConfig(em);
        
        ClassDescriptor employeeDescriptor =  EclipseLinkJPAAssert.assertEntity(getEMF(), "Employee");
        //EclipseLinkJPAAssert.assertWoven(employeeDescriptor);
        EclipseLinkJPAAssert.assertLazy(employeeDescriptor, "address");
        EclipseLinkJPAAssert.assertLazy(employeeDescriptor, "phoneNumbers");

    }    
}
