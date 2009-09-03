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
package test.nestedfetchgroup;

import static junit.framework.Assert.assertNotNull;

import javax.persistence.EntityManager;

import model.Employee;

import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.NestedFetchGroup;
import org.junit.Before;
import org.junit.Test;

import example.Queries;

public class NestedDefaultFetchGroupTests extends BaseFetchGroupTests {

    @Test
    public void simpleReadEmployee() {
        EntityManager em = getEntityManager();

        Employee emp = new Queries().minEmployeeWithAddressAndPhones(em);

        assertNotNull(emp);
        // assertEquals(1, getQuerySQLTracker(em).getTotalSQLSELECTCalls());

    }

    @Before
    public void config() throws Exception {
        assertConfig(getEMF(), "Employee", null);
        assertConfig(getEMF(), "Address", null);
        assertConfig(getEMF(), "PhoneNumber", null);

        // Set Default FetchGroup on Employee for its names
        NestedFetchGroup empFG = new NestedFetchGroup("Employee.names");
        empFG.addAttribute("firstName");
        empFG.addAttribute("lastName");
        FetchGroup empAddrFG = new FetchGroup();
        empAddrFG.addAttribute("country");
        // empFG.addGroup("address", empAddrFG);
        FetchGroup empPhoneFG = new FetchGroup();
        empAddrFG.addAttribute("number");
        empFG.addGroup("phoneNumbers", empPhoneFG);

        getDescriptor("Employee").getFetchGroupManager().setDefaultFetchGroup(empFG);

        // Set Default FetchGroup on Address for its city
        FetchGroup addrFG = new FetchGroup("Address.city");
        addrFG.addAttribute("city");
        getDescriptor("Address").getFetchGroupManager().setDefaultFetchGroup(addrFG);

        assertConfig(getEMF(), "Employee", empFG);
        assertConfig(getEMF(), "Address", addrFG);
        assertConfig(getEMF(), "PhoneNumber", null);

        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

}
