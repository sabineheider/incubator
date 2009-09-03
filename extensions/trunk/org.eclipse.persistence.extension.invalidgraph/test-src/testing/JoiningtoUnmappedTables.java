package testing;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.junit.Test;

@PersistenceContext(unitName = "employee")
public class JoiningtoUnmappedTables extends EclipseLinkJPATest {

    @Test
    public void test() throws Exception {
        EntityManager em = getEntityManager();

        ReadAllQuery raq = new ReadAllQuery(PhoneNumber.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        
        ExpressionBuilder empEB = new ExpressionBuilder(Employee.class);
        Expression addrExp = empEB.getField("EMPLOYEE.GENDER").equal("M").and(empEB.getField("EMPLOYEE.EMP_ID").equal(eb.getParameter("PHONE.EMP_ID")));
        
        raq.setSelectionCriteria(addrExp);
        
        JpaHelper.createQuery(raq, em).getResultList();
        
        em.close();
    }
}
