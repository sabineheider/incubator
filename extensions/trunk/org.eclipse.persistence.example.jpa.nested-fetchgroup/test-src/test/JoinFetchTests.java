package test;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import model.Employee;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.junit.Test;

import testing.EclipseLinkJPATest;

@PersistenceContext(unitName="employee")
public class JoinFetchTests extends EclipseLinkJPATest {

    @Test
    public void employeeFetchJoinAddress() {
        EntityManager em = getEntityManager();
        
        TypedQuery<Employee> q = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address", Employee.class);
        
        FetchGroup<Employee> fg = new FetchGroup<Employee>();
        fg.addAttribute("id");
        fg.addAttribute("version");
        
        q.setHint(QueryHints.FETCH_GROUP, fg);
        
        ReadAllQuery raq = JpaHelper.getReadAllQuery(q);
        raq.prepareCall(JpaHelper.getServerSession(getEMF()), new DatabaseRecord());
        
        List<Employee> emps = q.getResultList();
    }
}
