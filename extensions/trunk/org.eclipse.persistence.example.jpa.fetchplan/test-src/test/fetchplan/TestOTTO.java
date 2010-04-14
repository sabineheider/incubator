package test.fetchplan;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.internal.helper.SerializationHelper;
import org.junit.Assert;
import org.junit.Test;

import testing.EclipseLinkJPATest;

@PersistenceContext(unitName = "employee")
public class TestOTTO extends EclipseLinkJPATest {

    /**
     * This test shows that in some cases (or always?) the copy method
     * instantiates empty collections (wrong) instead of setting them to null.
     * If I copy an entity with a list attribute, which is not in fetchplan and
     * not instantiated, the copied entity has a empty list. If I want to merge
     * this object later without any changes, all entries of the list attribute
     * will be deleted.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testShowEmptyCollections() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        query.setHint(QueryHints.BATCH, "e.address");

        FetchPlan fetchPlan = new FetchPlan(Employee.class);
        fetchPlan.addAttribute("address");

        List<Employee> emps = query.getResultList();

        List<Employee> detachedEmps = JpaFetchPlanHelper.copy(em, fetchPlan, emps);

        for (Employee detachedEmp : detachedEmps) {
            assertNull(detachedEmp.getPhoneNumbers());
            assertNull(detachedEmp.getProjects());
            assertNull(detachedEmp.getManagedEmployees());
            assertNull(detachedEmp.getManager());
        }

        em.getTransaction().begin();

        // Since the detached emps are partial based on a FetchPLan they must be
        // merged using JpaFetchPlanHelper
        Employee attEmp = JpaFetchPlanHelper.merge(em, fetchPlan, detachedEmps.get(0));

        em.flush();

        if (attEmp.getPhoneNumbers().isEmpty())
            em.getTransaction().rollback();

    }

    /**
     * This test throws an InstantiationException for a reason i want to
     * understand.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testThrowsInstantiationException() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e where e.managedEmployees is not empty").getResultList();

        FetchPlan fetchplan = new FetchPlan("f", Employee.class, true);
        fetchplan.addAttribute("managedEmployees");
        
        List<Employee> detachedEmps = JpaFetchPlanHelper.copy(em, fetchplan, emps);
        
        assertNotNull(detachedEmps);
    }

    /**
     * If testThrowsInstantiationException is fixed this test should show that
     * the to-many relations of managedEmployees are loaded though they are not
     * configured in the FetchPlan. If You do this with a big model, You run
     * into memory problems.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testShowOutOfMemoryInCopy() throws Exception {
        EntityManager em = getEntityManager();

        List<Employee> emps = em.createQuery("SELECT e FROM Employee e where e.managedEmployees is not empty").getResultList();

        FetchPlan fetchplan = new FetchPlan(Employee.class);
        fetchplan.addAttribute("managedEmployees").setFetchPlan(new FetchPlan(Employee.class));

        List<Employee> detachedEmps = JpaFetchPlanHelper.copy(em, fetchplan, emps);

        for (Employee emp : detachedEmps) {
            //logMappedValues(emp, "");
            assertNull(emp.getAddress());
            assertNull(emp.getPhoneNumbers());
            assertNull(emp.getManager());
            assertNull(emp.getPeriod());
            assertNull(emp.getProjects());
            Assert.assertNotNull(emp.getManagedEmployees());

            //logMappedValues(emp, "");

            for (Employee managedEmp : emp.getManagedEmployees()) {
                assertNull(managedEmp.getAddress());
                assertNull(managedEmp.getPhoneNumbers());
                assertNull(managedEmp.getManager());
                assertNull(managedEmp.getPeriod());
                assertNull(managedEmp.getProjects());

                // If the managedEmp is part of the original result then it will
                // have a list of managed employees. Otherwise it will be null.
                if (detachedEmps.contains(managedEmp)) {
                    assertNotNull(managedEmp.getManagedEmployees());
                } else {
                    assertNull(managedEmp.getManagedEmployees());
                }
            }

        }
    }

    /**
     * What we do: - read Employee with Fetchplan NOT containing phoneNumbers =>
     * test fails because the detached instance contains attributes not included
     * in Fetchplan (=> should throw a "lazy" exception which would lead to
     * successful test).
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAddOnRelationNotDetached() throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        // Find
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");
        FetchPlan fetchplan = new FetchPlan(Employee.class);
        fetchplan.addAttribute("address");

        List<Employee> emps = query.getResultList();

        JpaFetchPlanHelper.fetch(em, fetchplan, emps);

        Employee empToDetach = emps.get(0);
        Employee detEmp = (Employee) JpaFetchPlanHelper.copy(em, fetchplan, empToDetach);

        // End of Transaction
        em.getTransaction().commit();

        // Client edits data
        try {
            detEmp.addPhoneNumber(new PhoneNumber("office", "+49", "1234567"));
            fail("phoneNumbers should be null");
        } catch (Exception ignore) {
        }

    }

    /**
     * same as before but here we - read Employee with Fetchplan containing
     * phoneNumbers - try to detach this Employee WITHOUT phoneNumbers but
     * unfortunately the detached Employee still contains phoneNumber => test
     * fails because the copy does not "unload" relations which are not
     * configured in Fetchplan but are loaded in the source of the copy.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAddOnRelationNotDetached2() throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        // Find
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        FetchPlan fetchplan = new FetchPlan(Employee.class);
        fetchplan.addAttribute("address");
        fetchplan.addAttribute("phoneNumbers");

        JpaFetchPlanHelper.fetch(em, fetchplan, emps);

        Employee empToDetach = emps.get(0);

        // define smaller fetchplan
        FetchPlan fetchplan2 = new FetchPlan(Employee.class);
        fetchplan.addAttribute("address");

        // Detach with smaller Fetchplan
        Employee detEmp = (Employee) JpaFetchPlanHelper.copy(em, fetchplan2, empToDetach);

        // End of Transaction
        em.getTransaction().commit();

        // Client edits data
        try {
            detEmp.addPhoneNumber(new PhoneNumber("office", "+49", "1234567"));
            fail("phoneNumbers should be null");
        } catch (Exception ignore) {
        }
    }

    /**
     * This test shows that reading, detaching, changing and re-attaching
     * relations contained in Fetchplan works.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testFindDetachChangeAttachCompleteProcessWithBiggerFetchplan() throws Exception {
        int cntSQL = 0;
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        // Find
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        query.setHint(QueryHints.BATCH, "e.address");
        query.setHint(QueryHints.BATCH, "e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        FetchPlan fetchplan = new FetchPlan(Employee.class);
        fetchplan.addAttribute("address");
        fetchplan.addAttribute("phoneNumbers");

        JpaFetchPlanHelper.fetch(em, fetchplan, emps);

        Employee empToDetach = emps.get(0);
        int cntPhones = empToDetach.getPhoneNumbers().size();

        // if fetched with large fetchplan, for fetchplan entries is no lazy
        // load recommended
        cntSQL = getQuerySQLTracker(em).getTotalSQLCalls();
        empToDetach.getPhoneNumbers().get(0).getNumber();
        assertEquals(cntSQL, getQuerySQLTracker(em).getTotalSQLCalls());

        // Detach with "bigger" FetchPlan containing FG
        FetchPlan fetchplan2 = new FetchPlan(Employee.class);
        fetchplan2.addAttribute("address");
        fetchplan2.addAttribute("phoneNumbers");
        fetchplan2.addAttribute("firstName");
        Employee detEmp = (Employee) JpaFetchPlanHelper.copy(em, fetchplan2, empToDetach);

        // End of Transaction
        em.getTransaction().commit();

        // Client edits data, is allowed to access on phonenumbers
        detEmp.addPhoneNumber(new PhoneNumber("office", "+49", "1234567"));
        detEmp.setFirstName(detEmp.getFirstName() + "-x");

        // Begin of second transaction
        em.getTransaction().begin();

        // Attach with larger fetchplan
        Employee attEmp = (Employee) JpaFetchPlanHelper.merge(em, fetchplan2, detEmp);

        // check, if new Phonenumber is added
        assertEquals(cntPhones + 1, attEmp.getPhoneNumbers().size());
        // check, if detached changes are merged
        assertTrue(attEmp.getFirstName().endsWith("-x"));

        em.flush();
        em.getTransaction().rollback();
    }

    /**
     * Even after serialization, entity object contains relations which should
     * not be loaded.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testFetchWithBiggerFetchplan() throws Exception {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        // Find
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        List<Employee> emps = query.getResultList();

        FetchPlan fetchplan = new FetchPlan(Employee.class, false);
        fetchplan.addAttribute("firstName");
        fetchplan.addAttribute("phoneNumbers");

        JpaFetchPlanHelper.fetch(em, fetchplan, emps);

        Employee empToDetach = emps.get(0);
        // just test the fetch, not the detach; serialize in order to avoid lazy
        // loading
        Employee detEmp = (Employee) SerializationHelper.clone(empToDetach);
        detEmp.getPhoneNumbers().size();
        // check address - not in Fetchplan
        try {
            detEmp.getAddress();
            fail("address should be null");
        } catch (Exception ignore) {
        }

        // Detach with "bigger" Fetchplan
        FetchPlan fetchplan2 = new FetchPlan(Employee.class, false);
        fetchplan.addAttribute("firstName");
        fetchplan2.addAttribute("address");
        fetchplan2.addAttribute("phoneNumbers");
        // just test the fetch, not the detach
        JpaFetchPlanHelper.fetch(em, fetchplan, emps);
        empToDetach = emps.get(0);
        // just test the fetch, not the detach; serialize in order to avoid lazy
        // loading
        detEmp = (Employee) SerializationHelper.clone(empToDetach);

        // now loaded
        try {
            detEmp.getAddress();
        } catch (ValidationException ve) {
            assertEquals("Expected ValidationException::" + ValidationException.INSTANTIATING_VALUEHOLDER_WITH_NULL_SESSION, ValidationException.INSTANTIATING_VALUEHOLDER_WITH_NULL_SESSION, ve.getErrorCode());
            return;
        } finally {
            em.getTransaction().rollback();
        }
        fail("serialize employee getAddress should thrown ValidationException");
    }
}
