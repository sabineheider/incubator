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

import static test.FetchGroupAssert.isValid;
import static org.junit.Assert.*;
import javax.persistence.PersistenceContext;

import model.Address;
import model.Employee;

import org.eclipse.persistence.queries.FetchGroup;
import org.junit.Test;

import testing.EclipseLinkJPATest;

/**
 * Simple tests to verify the functionality of single level FetchGroup usage
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
@PersistenceContext(unitName = "employee")
public class FetchGroupValidateTests extends EclipseLinkJPATest {

    @Test
    public void testVerify_EmptyFetchGroup() {
        FetchGroup<Employee> fg = new FetchGroup<Employee>();

        assertTrue(isValid(fg, getEMF(), Employee.class));
        assertTrue(isValid(fg, getEMF(), Address.class));
    }

    @Test
    public void testInvalidBasic() {
        FetchGroup<Employee> fg = new FetchGroup<Employee>();
        fg.addAttribute("invalid");

        assertFalse(isValid(fg, getEMF(), Employee.class));
    }

    @Test
    public void testInvalidRelationship() {
        FetchGroup<Employee> fg = new FetchGroup<Employee>();
        fg.addAttribute("invalid", new FetchGroup<Address>());

        assertFalse(isValid(fg, getEMF(), Employee.class));
    }

    @Test
    public void testInvalidNestedRelationship() {
        FetchGroup<Employee> fg = new FetchGroup<Employee>();
        fg.addAttribute("manager.invalid", new FetchGroup<Address>());

        assertFalse(isValid(fg, getEMF(), Employee.class));
    }
}
