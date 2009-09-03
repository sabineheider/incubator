package test.fetchplan;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import junit.framework.Assert;
import model.Employee;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchPlan;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.junit.Test;

import testing.EclipseLinkJPATest;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class BasicFetchPlanTests extends EclipseLinkJPATest {

    @Test
    public void employeeAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        // fetchPlan.addItem("address");
        // fetchPlan.addItem("phoneNumbers");
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");
        // fetchPlan.addItem("responsibilities");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void managerAddressPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void responsibilities() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.responsibilities");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void responsibilitiesBatch() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.gender IS NOT NULL");
        query.setHint(QueryHints.FETCH, "e.responsibilities");
        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.responsibilities");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void employeeAddress_ReturnBoth() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, e.address FROM Employee e WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void readAllEmployee() throws Exception {
        EntityManager em = getEntityManager();

        ReadAllQuery raq = new ReadAllQuery(Employee.class);

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(raq);
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");
        
        Query query = JpaHelper.createQuery(raq, em);
        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }
}
