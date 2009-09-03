package testing;

import static junit.framework.Assert.*;

import java.util.List;

import javax.persistence.*;

import junit.framework.Assert;
import model.*;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.indirection.BatchValueHolder;
import org.eclipse.persistence.internal.weaving.PersistenceWeaved;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.Test;

@PersistenceContext(unitName = "employee")
public class RWUOWInBatchValueHolder extends EclipseLinkJPATest {

    @Test
    public void newEmpPersistRefreshBatch() throws Exception {
        Server session = JpaHelper.getServerSession(getEMF());
        session.getIdentityMapAccessor().initializeAllIdentityMaps();

        EntityManager em = getEntityManager();

        em.getTransaction().begin();
        JpaHelper.getEntityManager(em).getUnitOfWork().beginEarlyTransaction();

        Employee newEmp = new Employee();
        newEmp.setFirstName("test");
        newEmp.setLastName("test");
        newEmp.setGender(Gender.Male);
        Address newAddr = new Address();
        newEmp.setAddress(newAddr);

        em.persist(newEmp);
        em.flush();

        Query q = em.createQuery("SELECT e FROM Employee e WHERE e.id = " + newEmp.getId());
        q.setHint(QueryHints.REFRESH, "true");
        q.setHint(QueryHints.BATCH, "e.address");
        q.setHint(QueryHints.BATCH, "e.phoneNumbers");
        q.getResultList();

        em.getTransaction().commit();
        em.clear();

        verify(em, session, "newEmpPersistRefreshBatch");
    }

    @Test
    public void queryOutsideTX() throws Exception {
        Server session = JpaHelper.getServerSession(getEMF());
        session.getIdentityMapAccessor().initializeAllIdentityMaps();

        EntityManager em = getEntityManager();

        verify(em, session, "queryOutsideTX");
    }

    @Test
    public void queryInsideTX() throws Exception {
        Server session = JpaHelper.getServerSession(getEMF());
        session.getIdentityMapAccessor().initializeAllIdentityMaps();

        EntityManager em = getEntityManager();

        em.getTransaction().begin();
        verify(em, session, "queryInsideTX");
        em.getTransaction().rollback();
    }

    @Test
    public void queryInsideDBTX() throws Exception {
        Server session = JpaHelper.getServerSession(getEMF());
        session.getIdentityMapAccessor().initializeAllIdentityMaps();

        EntityManager em = getEntityManager();

        em.getTransaction().begin();
        JpaHelper.getEntityManager(em).getUnitOfWork().beginEarlyTransaction();
        Query query = em.createQuery("SELECT e FROM Employee e ORDER BY e.id");
        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");
        query.getResultList();
        em.getTransaction().commit();
        
        verify(em, session, "queryInsideDBTX");
    }

    @SuppressWarnings("unchecked")
    private void verify(EntityManager em, Server session, String testCaseName) {
        // Verify Configuration
        assertWoven("Employee");
        assertLazy("Employee", "address");
        assertLazy("Employee", "phoneNumbers");

        // em.getTransaction().begin();

        Query query = em.createQuery("SELECT e FROM Employee e ORDER BY e.id");
        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        assertNotNull(emps);
        assertTrue(emps.size() > 0);

        OneToOneMapping addressMapping = getMapping("Employee", "address", OneToOneMapping.class);
        OneToManyMapping phonesMapping = getMapping("Employee", "phoneNumbers", OneToManyMapping.class);

        for (Employee emp : emps) {
            Employee sharedEmp = (Employee) session.getIdentityMapAccessor().getFromIdentityMap(emp);
            assertNotNull("Shared instance not found in cache: " + emp, sharedEmp);
            assertNotSame(emp, sharedEmp);

            System.out.println(testCaseName + "::verifying: Employee(" + sharedEmp.getId() + ") @ " + System.identityHashCode(sharedEmp));

            ValueHolderInterface addressHolder = (ValueHolderInterface) addressMapping.getAttributeValueFromObject(sharedEmp);
            assertNotNull("No address holder found Employee id:" + sharedEmp.getId(), addressHolder);
            assertTrue("address holder not batch: " + addressHolder, addressHolder instanceof BatchValueHolder);
            BatchValueHolder addressBVH = (BatchValueHolder) addressHolder;
            assertFalse(addressBVH.isInstantiated());
            assertSame(session, addressBVH.getSession());
            Session querySession = addressBVH.getQuery().getSession();
            if (querySession != null) {
                assertSame(session, querySession);
                assertFalse(querySession.isUnitOfWork());
            } else {
                System.out.println("Employee(" + sharedEmp.getId() + ") has null session in address.BVH.query");
            }
            addressBVH.getValue();
            assertTrue(addressBVH.isInstantiated());
            assertNull(addressBVH.getSession());
            assertNull(addressBVH.getQuery());

            IndirectContainer phonesContainer = (IndirectContainer) phonesMapping.getAttributeValueFromObject(sharedEmp);
            ValueHolderInterface phonesHolder = phonesContainer.getValueHolder();
            assertNotNull("No phones holder found Employee id:" + sharedEmp.getId(), phonesHolder);
            assertTrue("phones holder not batch: " + phonesHolder, phonesHolder instanceof BatchValueHolder);
            BatchValueHolder phonesBVH = (BatchValueHolder) phonesHolder;
            assertFalse(phonesBVH.isInstantiated());
            assertSame(session, phonesBVH.getSession());
            querySession = phonesBVH.getQuery().getSession();
            if (querySession != null) {
                assertSame(session, querySession);
                assertFalse(querySession.isUnitOfWork());
            } else {
                System.out.println("Employee(" + sharedEmp.getId() + ") has null session in phoneNumbers.BVH.query");
            }
            phonesBVH.getValue();
            assertTrue(phonesBVH.isInstantiated());
            assertNull(phonesBVH.getSession());
            assertNull(phonesBVH.getQuery());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getMapping(String entityAlias, String attribute, Class<T> class1) {
        return (T) JpaHelper.getServerSession(getEMF()).getDescriptorForAlias(entityAlias).getMappingForAttributeName(attribute);
    }

    private void assertLazy(String entityAlias, String attribute) {
        Assert.assertTrue(getMapping(entityAlias, attribute, DatabaseMapping.class).isLazy());
    }

    private void assertWoven(String entityAlias) {
        Assert.assertTrue(PersistenceWeaved.class.isAssignableFrom(JpaHelper.getServerSession(getEMF()).getDescriptorForAlias(entityAlias).getJavaClass()));
    }

}
