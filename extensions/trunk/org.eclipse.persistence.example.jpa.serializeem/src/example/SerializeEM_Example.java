package example;

import java.sql.Time;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.helper.SerializationHelper;
import org.eclipse.persistence.internal.jpa.EntityManagerHandle;

import junit.framework.Assert;

import model.Address;
import model.Employee;
import model.Gender;
import model.PhoneNumber;

public class SerializeEM_Example {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(PersistenceUnitProperties.SESSION_NAME, "employee");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("employee", properties);
        EntityManager em = emf.createEntityManager();

        Employee emp = new Queries().minEmployeeWithAddressAndPhones(em);
        emp.setSalary(emp.getSalary() + 1);
        emp.setEndTime(new Time(System.currentTimeMillis()));
        emp.getPeriod().setEndDate(Calendar.getInstance());
        Employee newEmp = new Employee();
        newEmp.setId(666666666);
        newEmp.setFirstName("Doug");
        newEmp.setLastName("Clarke");
        newEmp.setGender(Gender.Male);

        em.persist(newEmp);

        Assert.assertNotNull(em.find(Employee.class, 666666666));

        EntityManagerHandle emHandle = new EntityManagerHandle(em);

        EntityManagerHandle emHandle2 = (EntityManagerHandle) SerializationHelper.deserialize(SerializationHelper.serialize(emHandle));

        Assert.assertNotSame(emHandle, emHandle2);

        EntityManager em2 = emHandle2.getEntityManager();

        Employee emp2 = em2.find(Employee.class, emp.getId());

        Assert.assertNotSame(emp, emp2);
        Assert.assertNotNull(em2.find(Employee.class, 666666666));

        em2.getTransaction().begin();

        emp2.getAddress().setPostalCode(emp2.getAddress().getPostalCode().equals("AAA111") ? "111AAA" : "AAA111");
        emp2.getPhoneNumbers().size();

        // Employee newEmp2 = em2.find(Employee.class, 666666666);
        // newEmp2.setSalary(123456);

        em2.flush();
        em2.getTransaction().rollback();

        em.close();
        em2.close();
        emf.close();
    }

}
