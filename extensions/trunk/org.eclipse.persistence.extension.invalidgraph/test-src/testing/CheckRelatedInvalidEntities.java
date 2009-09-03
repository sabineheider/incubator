package testing;

import static junit.framework.Assert.*;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.*;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.extension.listeners.RefreshInvalidGraphListener;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.Test;

import example.Queries;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class CheckRelatedInvalidEntities extends EclipseLinkJPATest {

    @Test
    public void invalidAddress_find() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        Address address = employee.getAddress();

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));

        session.getIdentityMapAccessor().invalidateObject(address);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertFalse(session.getIdentityMapAccessor().isValid(address));

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        em.find(Employee.class, employee.getId());

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));
        assertEquals(numSelect + 1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void invalidAddress_refreshEmp_find() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        Address address = employee.getAddress();

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));

        session.getIdentityMapAccessor().invalidateObject(address);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertFalse(session.getIdentityMapAccessor().isValid(address));

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        ReadObjectQuery query = new ReadObjectQuery(employee);
        query.refreshIdentityMapResult();
        query.setProperty(RefreshInvalidGraphListener.DO_NOT_VERIFY_PROPERTY, true);
        query.setIsExecutionClone(true);
        session.executeQuery(query);
        assertEquals(numSelect + 1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.find(Employee.class, employee.getId());

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));
        assertEquals(numSelect + 2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void invalidAddress_removeEmp_find() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        Address address = employee.getAddress();

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));

        session.getIdentityMapAccessor().invalidateObject(address);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertFalse(session.getIdentityMapAccessor().isValid(address));

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        session.getIdentityMapAccessor().removeFromIdentityMap(employee);

        em.clear();
        employee = em.find(Employee.class, employee.getId());
        assertEquals(numSelect + 1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        address = employee.getAddress();
        assertEquals(numSelect + 2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));
    }

    @Test
    public void invalidAddress_jpql_all_resultList() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        Address address = employee.getAddress();

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));

        session.getIdentityMapAccessor().invalidateObject(address);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertFalse(session.getIdentityMapAccessor().isValid(address));

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        em.createQuery("SELECT e FROM Employee e WHERE e.id = " + employee.getId()).getResultList();

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));
        assertEquals(numSelect + 2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void invalidAddress_jpql_id_singeResult() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        Address address = employee.getAddress();

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));

        session.getIdentityMapAccessor().invalidateObject(address);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertFalse(session.getIdentityMapAccessor().isValid(address));

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        em.createQuery("SELECT e FROM Employee e WHERE e.id = " + employee.getId()).getSingleResult();

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));
        assertEquals(numSelect + 2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void invalidAddress_jpql_id_resultList() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        Address address = employee.getAddress();

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));

        session.getIdentityMapAccessor().invalidateObject(address);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertFalse(session.getIdentityMapAccessor().isValid(address));

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        em.createQuery("SELECT e FROM Employee e WHERE e.id = " + employee.getId()).getResultList();

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));
        assertEquals(numSelect + 2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void invalidAddress_refresh() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        Address address = employee.getAddress();

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));

        session.getIdentityMapAccessor().invalidateObject(address);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertFalse(session.getIdentityMapAccessor().isValid(address));

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        em.refresh(employee);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));
        assertEquals(numSelect + 2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void invalidAddress_refreshInTX() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        em.getTransaction().begin();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        Address address = employee.getAddress();

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));

        session.getIdentityMapAccessor().invalidateObject(address);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertFalse(session.getIdentityMapAccessor().isValid(address));

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        em.refresh(employee);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(address));
        assertEquals(numSelect + 2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.getTransaction().rollback();
    }

    @Test
    public void invalidPhone_find() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        PhoneNumber phone = employee.getPhoneNumbers().get(0);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(phone));

        session.getIdentityMapAccessor().invalidateObject(phone);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertFalse(session.getIdentityMapAccessor().isValid(phone));

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        em.find(Employee.class, employee.getId());

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(phone));
        assertEquals(numSelect + 1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void invalidPhone_refreshEmp_find() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        PhoneNumber phone = employee.getPhoneNumbers().get(0);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(phone));

        session.getIdentityMapAccessor().invalidateObject(phone);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertFalse(session.getIdentityMapAccessor().isValid(phone));

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        ReadObjectQuery query = new ReadObjectQuery(employee);
        query.refreshIdentityMapResult();
        query.setProperty(RefreshInvalidGraphListener.DO_NOT_VERIFY_PROPERTY, true);
        query.setIsExecutionClone(true);
        session.executeQuery(query);
        assertEquals(numSelect + 1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.find(Employee.class, employee.getId());
        assertEquals(numSelect + 2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(phone));
    }

    @Test
    public void invalidPhone_removeEmp_find() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        PhoneNumber phone = employee.getPhoneNumbers().get(0);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(phone));

        session.getIdentityMapAccessor().invalidateObject(phone);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertFalse(session.getIdentityMapAccessor().isValid(phone));

        int numSelect = getQuerySQLTracker(em).getTotalSQLSELECTCalls();

        session.getIdentityMapAccessor().removeFromIdentityMap(employee);

        em.clear();
        employee = em.find(Employee.class, employee.getId());
        assertEquals(numSelect + 1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        PhoneNumber refreshedPhone = employee.getPhoneNumbers().get(0);
        assertEquals(phone.getType(), refreshedPhone.getType());
        assertEquals(numSelect + 2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(phone));
    }

    @Test
    public void invalidManagerAddress() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        Employee employee = new Queries().minEmployeeWithAddressAndPhones(em);
        Employee manager = employee.getManager();
        Address address = manager.getAddress();

        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(manager));
        assertTrue(session.getIdentityMapAccessor().isValid(address));

        session.getIdentityMapAccessor().invalidateObject(address);

        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(manager));
        assertFalse(session.getIdentityMapAccessor().isValid(address));

        session.getIdentityMapAccessor().removeFromIdentityMap(employee);

        em.clear();
        employee = em.find(Employee.class, employee.getId());
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        manager = employee.getManager();
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertTrue(session.getIdentityMapAccessor().isValid(employee));
        assertTrue(session.getIdentityMapAccessor().isValid(manager));
        assertTrue(session.getIdentityMapAccessor().isValid(address));
    }

    @Test
    public void invalidAddresses_find() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        List<Employee> employees = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address").setMaxResults(2).getResultList();
        Employee emp1 = employees.get(0);
        Address address1 = emp1.getAddress();
        Employee emp2 = employees.get(1);
        Address address2 = emp2.getAddress();

        assertTrue(session.getIdentityMapAccessor().isValid(emp1));
        assertTrue(session.getIdentityMapAccessor().isValid(address1));
        assertTrue(session.getIdentityMapAccessor().isValid(emp2));
        assertTrue(session.getIdentityMapAccessor().isValid(address2));

        session.getIdentityMapAccessor().invalidateObject(address1);
        session.getIdentityMapAccessor().invalidateObject(address2);

        assertTrue(session.getIdentityMapAccessor().isValid(emp1));
        assertFalse(session.getIdentityMapAccessor().isValid(address1));
        assertTrue(session.getIdentityMapAccessor().isValid(emp2));
        assertFalse(session.getIdentityMapAccessor().isValid(address2));
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.find(Employee.class, emp1.getId());

        assertTrue(session.getIdentityMapAccessor().isValid(emp1));
        assertTrue(session.getIdentityMapAccessor().isValid(address1));
        assertTrue(session.getIdentityMapAccessor().isValid(emp2));
        assertFalse(session.getIdentityMapAccessor().isValid(address2));
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.find(Employee.class, emp2.getId());

        assertTrue(session.getIdentityMapAccessor().isValid(emp1));
        assertTrue(session.getIdentityMapAccessor().isValid(address1));
        assertTrue(session.getIdentityMapAccessor().isValid(emp2));
        assertTrue(session.getIdentityMapAccessor().isValid(address2));
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Test
    public void invalidAddresses_getResultList_IN() {
        EntityManager em = getEntityManager();
        Server session = JpaHelper.getEntityManager(em).getServerSession();

        List<Employee> employees = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address").setMaxResults(2).getResultList();
        Employee emp1 = employees.get(0);
        Address address1 = emp1.getAddress();
        Employee emp2 = employees.get(1);
        Address address2 = emp2.getAddress();

        assertTrue(session.getIdentityMapAccessor().isValid(emp1));
        assertTrue(session.getIdentityMapAccessor().isValid(address1));
        assertTrue(session.getIdentityMapAccessor().isValid(emp2));
        assertTrue(session.getIdentityMapAccessor().isValid(address2));

        session.getIdentityMapAccessor().invalidateObject(address1);
        session.getIdentityMapAccessor().invalidateObject(address2);

        assertTrue(session.getIdentityMapAccessor().isValid(emp1));
        assertFalse(session.getIdentityMapAccessor().isValid(address1));
        assertTrue(session.getIdentityMapAccessor().isValid(emp2));
        assertFalse(session.getIdentityMapAccessor().isValid(address2));
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.createQuery("SELECT e FROM Employee e WHERE e.id IN(" + emp1.getId() + ", " + emp2.getId() + ")").getResultList();

        assertTrue(session.getIdentityMapAccessor().isValid(emp1));
        assertTrue(session.getIdentityMapAccessor().isValid(address1));
        assertTrue(session.getIdentityMapAccessor().isValid(emp2));
        assertTrue(session.getIdentityMapAccessor().isValid(address2));
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        em.find(Employee.class, emp2.getId());
        em.find(Employee.class, emp2.getId());

        assertTrue(session.getIdentityMapAccessor().isValid(emp1));
        assertTrue(session.getIdentityMapAccessor().isValid(address1));
        assertTrue(session.getIdentityMapAccessor().isValid(emp2));
        assertTrue(session.getIdentityMapAccessor().isValid(address2));
        assertEquals(3, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Override
    protected Map<?,?> getEMFProperties() {
        Map<String,String> properties = super.getEMFProperties();
        properties.put(PersistenceUnitProperties.SESSION_EVENT_LISTENER_CLASS, RefreshInvalidGraphListener.class.getName());
        // properties.put(PersistenceUnitProperties.LOGGING_LEVEL,
        // SessionLog.FINEST_LABEL);
        return properties;
    }

}
