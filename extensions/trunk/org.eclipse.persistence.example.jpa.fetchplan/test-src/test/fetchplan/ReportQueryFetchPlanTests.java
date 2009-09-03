package test.fetchplan;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import junit.framework.Assert;
import model.Employee;

import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchPlan;
import org.junit.Test;

import testing.EclipseLinkJPATest;

@SuppressWarnings("unchecked")
@PersistenceContext(unitName = "employee")
public class ReportQueryFetchPlanTests extends EclipseLinkJPATest {

    @Test
    public void employeeAddress() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, a FROM Employee e, Address a WHERE e.gender IS NOT NULL");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.manager.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void employeeManager() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, e.manager.address FROM Employee e");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }

    @Test
    public void employeeCountPhones() throws Exception {
        EntityManager em = getEntityManager();

        Query query = em.createQuery("SELECT e, COUNT(e.phoneNumbers) FROM Employee e");

        FetchPlan fetchPlan = FetchPlan.getFetchPlan(JpaHelper.getReadAllQuery(query));
        fetchPlan.addFetchItem("e.manager.address");
        fetchPlan.addFetchItem("e.phoneNumbers");

        List<Employee> emps = query.getResultList();

        Assert.assertNotNull(emps);
    }
}
