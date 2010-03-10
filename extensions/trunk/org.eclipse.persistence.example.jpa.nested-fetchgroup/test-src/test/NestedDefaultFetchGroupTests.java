/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     dclarke - Bug 273057: NestedFetchGroup Example
 ******************************************************************************/
package test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static test.FetchGroupAssert.*;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import model.Address;
import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroup;
import org.junit.Before;
import org.junit.Test;

import example.Queries;

/**
 * Test named nested FetchGroup usage.
 * 
 * @author dclarke
 * @since EclipseLink 2.1
 */
public class NestedDefaultFetchGroupTests extends BaseFetchGroupTests {

    @Test
    public void findMinEmployee() {
        EntityManager em = getEntityManager();
        int minId = Queries.minEmployeeIdWithAddressAndPhones(em);
        assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

        Employee emp = em.find(Employee.class, minId);

        assertNotNull(emp);
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        
        emp.getAddress();
        emp.getPhoneNumbers().size();
        
        assertEquals(4, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
        assertFetched(getEMF(), emp, defaultEmployeeFG);
        assertFetchedAttribute(getEMF(), emp, "address");
        assertFetchedAttribute(getEMF(), emp, "phoneNumbers");
        
        // Check Address
        FetchGroup<Address> nestedAddressFG = defaultEmployeeFG.getFetchItem("address").getFetchGroup();
        assertFetched(getEMF(), emp.getAddress(), nestedAddressFG);
        
        // Check phones
        FetchGroup<PhoneNumber> nestedPhoneFG = defaultEmployeeFG.getFetchItem("phoneNumbers").getFetchGroup();
        for (PhoneNumber phone: emp.getPhoneNumbers()) {
            assertFetched(getEMF(), phone, nestedPhoneFG);
        }
    }

    @Test
    public void allAddress() {
        EntityManager em = getEntityManager();
        
        
        List<Address> allAddresses = em.createQuery("SELECT a FROM Address a", Address.class).getResultList();
        
        for (Address address: allAddresses) {
            assertNoFetchGroup(getEMF(), address);
        }
    }

    @Test
    public void allPhone() {
        EntityManager em = getEntityManager();
        
        
        List<PhoneNumber> allPhones = em.createQuery("SELECT p FROM PhoneNumber p", PhoneNumber.class).getResultList();
        
        for (PhoneNumber phone: allPhones) {
            assertFetched(getEMF(), phone, defaultPhoneFG);
        }
    }

    @Test
    public void singleResultMinEmployeeFetchJoinAddress() {
        EntityManager em = getEntityManager();

        TypedQuery<Employee> query = em.createQuery("SELECT e FROM Employee e JOIN FETCH e.address WHERE e.id IN (SELECT MIN(p.id) FROM PhoneNumber p)", Employee.class);
        Employee emp = query.getSingleResult();

        assertNotNull(emp);
        assertEquals(2, getQuerySQLTracker(em).getTotalSQLSELECTCalls());
    }

    @Override
    public EntityManager getEntityManager() {
        EntityManager em = super.getEntityManager();
        
        assertNotNull("Default Employee FetchGroup not available in test case", defaultEmployeeFG);
        assertNotNull("Default PhoneNumber FetchGroup not available in test case", defaultPhoneFG);
        
        assertConfig(getEMF(), "Employee", defaultEmployeeFG, 0);
        assertConfig(getEMF(), "Address", null, 0);
        assertConfig(getEMF(), "PhoneNumber", defaultPhoneFG, 0);
        

        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
        
        return em;
    }

    @Override
    protected Map getEMFProperties() {
        Map properties = super.getEMFProperties();
        properties.put(PersistenceUnitProperties.DESCRIPTOR_CUSTOMIZER_ + "Employee", EmployeeCustomizer.class.getName());
        properties.put(PersistenceUnitProperties.DESCRIPTOR_CUSTOMIZER_ + "PhoneNumber", PhoneCustomizer.class.getName());
        return properties;
    }
    
    public static FetchGroup<Employee> defaultEmployeeFG;

    public static class EmployeeCustomizer implements DescriptorCustomizer {

        public void customize(ClassDescriptor descriptor) throws Exception {
            defaultEmployeeFG = new FetchGroup<Employee>("Employee.default");
            defaultEmployeeFG.addAttribute("firstName");
            defaultEmployeeFG.addAttribute("lastName");
            defaultEmployeeFG.addAttribute("address.country");
            defaultEmployeeFG.addAttribute("phoneNumbers.areaCode");

            descriptor.getFetchGroupManager().setDefaultFetchGroup(defaultEmployeeFG);
        }

    }

    private static FetchGroup<PhoneNumber> defaultPhoneFG;

    public static class PhoneCustomizer implements DescriptorCustomizer {

        public void customize(ClassDescriptor descriptor) throws Exception {
            defaultPhoneFG = new FetchGroup<PhoneNumber>("PhoneNumber.default");
            defaultPhoneFG.addAttribute("number");
            descriptor.getFetchGroupManager().setDefaultFetchGroup(defaultPhoneFG);
        }

    }

}
