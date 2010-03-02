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
 *     dclarke - TODO
 ******************************************************************************/
package test.fetchplan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import model.Employee;

import org.eclipse.persistence.extension.fetchplan.FetchItem;
import org.eclipse.persistence.extension.fetchplan.FetchPlan;
import org.eclipse.persistence.jpa.JpaHelper;
import org.junit.After;
import org.junit.Test;

/**
 * Basic set of test verifying that the FetchPlan public API performs as
 * expected. No persistence related validation or usage is performed.
 * 
 * @author dclarke
 * @since EclipseLink 1.2
 */
public class FetchPlanAPITests {

    @Test
    public void verifyConstructor_nullEntityClass() {
        FetchPlan plan = new FetchPlan(null);
        assertNull(plan.getEntityClass());

        try {
            plan.initialize(null);
        } catch (IllegalStateException ise) {
            return;
        }
        fail("IllegalStateException expected on initialize with null entityClass");
    }

    @Test
    public void verifyConstructor_withEntityClass() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertEquals(Employee.class, plan.getEntityClass());
        assertTrue(plan.getFetchItems().isEmpty());

        // With a valid entity class and null session initialize should throw an
        // NPE
        try {
            plan.initialize(null);
        } catch (NullPointerException npe) {
            return;
        }
        fail("NullPointerException expected on initialize with null session");
    }

    @Test
    public void verifyGetFetchItems_add() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertTrue(plan.getFetchItems().isEmpty());

        // With a valid entity class and null session initialize should throw an
        // NPE
        try {
            plan.getFetchItems().add(null);
        } catch (UnsupportedOperationException npe) {
            return;
        }
        fail("UnsupportedOperationException expected on Fetchplan.getFetchItems().add()");
    }

    @Test
    public void verifyAddAttribute_nullString() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertTrue(plan.getFetchItems().isEmpty());

        // With a valid entity class and null session initialize should throw an
        // NPE
        try {
            plan.addAttribute((String) null);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("IllegalArgumentException expected");
    }

    @Test
    public void verifyAddAttribute_nullStringArray() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertTrue(plan.getFetchItems().isEmpty());

        // With a valid entity class and null session initialize should throw an
        // NPE
        try {
            plan.addAttribute((String[]) null);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("IllegalArgumentException expected");
    }

    @Test
    public void verifyAddAttribute_emptyString() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertTrue(plan.getFetchItems().isEmpty());

        // With a valid entity class and null session initialize should throw an
        // NPE
        try {
            plan.addAttribute("");
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("IllegalArgumentException expected");
    }

    @Test
    public void verifyAddAttribute_period() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertTrue(plan.getFetchItems().isEmpty());

        // With a valid entity class and null session initialize should throw an
        // NPE
        try {
            plan.addAttribute(".");
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("IllegalArgumentException expected");
    }

    @Test
    public void verifyAddAttribute_EndWithPeriod() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertTrue(plan.getFetchItems().isEmpty());

        // With a valid entity class and null session initialize should throw an
        // NPE
        try {
            plan.addAttribute("test.");
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("IllegalArgumentException expected");
    }

    @Test
    public void verifyAddAttribute_StartWithPeriod() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertTrue(plan.getFetchItems().isEmpty());

        // With a valid entity class and null session initialize should throw an
        // NPE
        try {
            plan.addAttribute(".test");
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("IllegalArgumentException expected");
    }

    @Test
    public void verifyAddAttribute_StartAndEndWithPeriod() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertTrue(plan.getFetchItems().isEmpty());

        // With a valid entity class and null session initialize should throw an
        // NPE
        try {
            plan.addAttribute(".test.");
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("IllegalArgumentException expected");
    }

    @Test
    public void verifyAddAttribute_space() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertTrue(plan.getFetchItems().isEmpty());

        // With a valid entity class and null session initialize should throw an
        // NPE
        try {
            plan.addAttribute(" ");
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("IllegalArgumentException expected");
    }

    @Test
    public void verifyAddAttribute_basic() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertTrue(plan.getFetchItems().isEmpty());

        plan.addAttribute("attribute");

        assertFalse(plan.getFetchItems().isEmpty());
        assertEquals(1, plan.getFetchItems().size());

        FetchItem item = (FetchItem) plan.getFetchItems().toArray()[0];

        assertEquals("attribute", item.getName());
        assertNull(item.getFetchPlan());
        assertSame(plan, item.getParent());
    }

    @Test
    public void verifyAddAttribute_nested() {
        FetchPlan plan = new FetchPlan(Employee.class);
        assertTrue(plan.getFetchItems().isEmpty());

        plan.addAttribute("attribute");

        assertFalse(plan.getFetchItems().isEmpty());
        assertEquals(1, plan.getFetchItems().size());

        FetchItem item = (FetchItem) plan.getFetchItems().toArray()[0];

        assertEquals("attribute", item.getName());
        assertNull(item.getFetchPlan());
        assertSame(plan, item.getParent());

        plan.addAttribute("attribute.attribute2");

        assertEquals(1, plan.getFetchItems().size());
        assertNotNull(item.getFetchPlan());

        FetchItem item2 = (FetchItem) item.getFetchPlan().getFetchItems().toArray()[0];

        assertEquals("attribute2", item2.getName());
        assertNull(item2.getFetchPlan());
        assertSame(item.getFetchPlan(), item2.getParent());
    }

}
