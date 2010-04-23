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
 *     dclarke - Bug 288307: Extensions Incubator - FetchPlan 
 ******************************************************************************/
package test.fetchplan;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import model.Employee;
import model.LargeProject;
import model.PhoneNumber;
import model.Project;
import model.SmallProject;

import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.extension.fetchplan.JpaFetchPlanHelper;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.After;
import org.junit.Test;

import testing.EclipseLinkJPAAssert;
import testing.EclipseLinkJPATest;

/**
 * Tests that verify the {@link JpaFetchPlanHelper} API around the creation of
 * FetchPlan instances based on the mappings and FetchGroups associated with an
 * entity type's descriptor.
 * 
 * @author dclarke
 * @since EclispeLink 1.2
 */
@PersistenceContext(unitName = "employee")
public class DefaultFetchPlanTests extends EclipseLinkJPATest {

    @Test
    public void defaultFetchPlan_Employee() {
        FetchPlan fp = new FetchPlan(Employee.class);

        JpaFetchPlanHelper.addDefaultFetchAttributes(getEMF(), fp);

        assertEquals(9, fp.getFetchItems().size());
        assertTrue(fp.containsAttribute("id"));
        assertTrue(fp.containsAttribute("version"));
        assertTrue(fp.containsAttribute("firstName"));
        assertTrue(fp.containsAttribute("lastName"));
        assertTrue(fp.containsAttribute("gender"));
        assertTrue(fp.containsAttribute("startTime"));
        assertTrue(fp.containsAttribute("endTime"));
        assertTrue(fp.containsAttribute("salary"));
        assertTrue(fp.containsAttribute("period"));
        assertFalse(fp.containsAttribute("address"));
        assertFalse(fp.containsAttribute("phoneNumbers"));
        assertFalse(fp.containsAttribute("projects"));
        assertFalse(fp.containsAttribute("responsibilities"));
    }

    @Test
    public void defaultFetchPlan_SmallProject() {
        FetchPlan fp = new FetchPlan(SmallProject.class);

        JpaFetchPlanHelper.addDefaultFetchAttributes(getEMF(), fp);

        assertEquals(4, fp.getFetchItems().size());
        assertTrue(fp.containsAttribute("id"));
        assertTrue(fp.containsAttribute("name"));
        assertTrue(fp.containsAttribute("description"));
        assertTrue(fp.containsAttribute("version"));
        assertFalse(fp.containsAttribute("teamLeader"));
    }

    @Test
    public void defaultFetchPlan_LargeProject() {
        FetchPlan fp = new FetchPlan(LargeProject.class);

        JpaFetchPlanHelper.addDefaultFetchAttributes(getEMF(), fp);

        assertEquals(6, fp.getFetchItems().size());
        assertTrue(fp.containsAttribute("id"));
        assertTrue(fp.containsAttribute("name"));
        assertTrue(fp.containsAttribute("description"));
        assertTrue(fp.containsAttribute("version"));
        assertFalse(fp.containsAttribute("teamLeader"));
        assertTrue(fp.containsAttribute("budget"));
        assertTrue(fp.containsAttribute("milestone"));
    }

    @Test
    public void defaultFetchPlan_Project() {
        FetchPlan fp = new FetchPlan(Project.class);

        JpaFetchPlanHelper.addDefaultFetchAttributes(getEMF(), fp);

        assertEquals(4, fp.getFetchItems().size());
        assertTrue(fp.containsAttribute("id"));
        assertTrue(fp.containsAttribute("name"));
        assertTrue(fp.containsAttribute("description"));
        assertTrue(fp.containsAttribute("version"));
        assertFalse(fp.containsAttribute("teamLeader"));
    }

    @Test
    public void requireAttributesFetchPlan_PhoneNumber() {
        FetchPlan fp = new FetchPlan(PhoneNumber.class);
        
        fp.initialize(JpaHelper.getServerSession(getEMF()));

        assertEquals(2, fp.getFetchItems().size());
        assertTrue(fp.containsAttribute("type"));
        assertTrue(fp.containsAttribute("owner"));
        assertFalse(fp.containsAttribute("number"));
        assertFalse(fp.containsAttribute("areaCode"));
    }

    @After
    public void clearCache() {
        JpaHelper.getServerSession(getEMF()).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    @Override
    protected void verifyConfig(EntityManager em) {
        super.verifyConfig(em);

        EclipseLinkJPAAssert.assertWoven(getDescriptor("Employee"));
        FetchPlanAssert.verifyEmployeeConfig(getEMF());
    }

}
