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
 *     dclarke - Bug 244124: Nested FetchGroup Enhancement
 ******************************************************************************/
package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceContext;

import model.Address;
import model.Employee;
import model.PhoneNumber;

import org.eclipse.persistence.internal.jpa.weaving.ClassWeaver;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.junit.Test;

import testing.EclipseLinkJPATest;

/**
 * Test to verify changes made to {@link FetchGroupTracker} and
 * {@link ClassWeaver} are working as expected.
 * 
 * @author dclarke
 * @since EclipseLink 2.1
 */
@PersistenceContext(unitName = "employee")
public class FetchGroupTrackerWeavingTests extends EclipseLinkJPATest {

    String checkAttribute = null;
    String checkForSetAttribute = null;

    @Test
    public void verifyCheckFetchedForSetWithFetchGroup() {
        Employee emp = new Employee();
        TestFetchGroup fg = new TestFetchGroup();
        fg.setOnEntity((FetchGroupTracker) emp);

        assertNull(this.checkAttribute);
        assertNull(this.checkForSetAttribute);

        emp.setFirstName("John");

        assertNull(this.checkAttribute);
        assertNotNull(this.checkForSetAttribute);
        assertEquals("firstName", this.checkForSetAttribute);
    }

    @Test
    public void verifyCheckFetchedWithFetchGroup() {
        Employee emp = new Employee();
        TestFetchGroup fg = new TestFetchGroup();
        fg.setOnEntity((FetchGroupTracker) emp);

        assertNull(this.checkAttribute);
        assertNull(this.checkForSetAttribute);

        emp.getFirstName();

        assertNull(this.checkForSetAttribute);
        assertNotNull(this.checkAttribute);
        assertEquals("firstName", this.checkAttribute);
    }

    @Test
    public void verifyCheckFetchedForSetWithFetchGroup_OneToOne() {
        Employee emp = new Employee();
        TestFetchGroup fg = new TestFetchGroup();
        fg.setOnEntity((FetchGroupTracker) emp);

        assertNull(this.checkAttribute);
        assertNull(this.checkForSetAttribute);

        emp.setAddress(new Address());

        assertNull(this.checkAttribute);
        assertNotNull(this.checkForSetAttribute);
        assertEquals("address", this.checkForSetAttribute);
    }

    @Test
    public void verifyCheckFetchedWithFetchGroup_OneToOne() {
        Employee emp = new Employee();
        TestFetchGroup fg = new TestFetchGroup();
        fg.setOnEntity((FetchGroupTracker) emp);

        assertNull(this.checkAttribute);
        assertNull(this.checkForSetAttribute);

        Address addr = emp.getAddress();

        assertNull(addr);
        assertNull(this.checkForSetAttribute);
        assertNotNull(this.checkAttribute);
        assertEquals("address", this.checkAttribute);
    }

    @Test
    public void verifyCheckFetchedForSetWithFetchGroup_OneToMany() {
        Employee emp = new Employee();
        TestFetchGroup fg = new TestFetchGroup();
        fg.setOnEntity((FetchGroupTracker) emp);

        assertNull(this.checkAttribute);
        assertNull(this.checkForSetAttribute);

        emp.setPhoneNumbers(new ArrayList<PhoneNumber>());

        assertNull(this.checkAttribute);
        assertNotNull(this.checkForSetAttribute);
        assertEquals("phoneNumbers", this.checkForSetAttribute);
    }

    @Test
    public void verifyCheckFetchedWithFetchGroup_OneToMany() {
        Employee emp = new Employee();
        TestFetchGroup fg = new TestFetchGroup();
        fg.setOnEntity((FetchGroupTracker) emp);

        assertNull(this.checkAttribute);
        assertNull(this.checkForSetAttribute);

        List<PhoneNumber> phones = emp.getPhoneNumbers();

        assertNotNull(phones);
        assertTrue(phones instanceof ArrayList);
        assertTrue(phones.isEmpty());

        assertNull(this.checkForSetAttribute);
        assertNotNull(this.checkAttribute);
        assertEquals("phoneNumbers", this.checkAttribute);
    }

    class TestFetchGroup extends FetchGroup<Employee> {

        @Override
        public void checkFetched(FetchGroupTracker entity, String attributeName) {
            checkAttribute = attributeName;
        }

        @Override
        public void checkFetchedForSet(FetchGroupTracker entity, String attributeName) {
            checkForSetAttribute = attributeName;
        }

        // Bypass EntityFetchGroup wrapping done in FetchGroup
        @Override
        public void setOnEntity(FetchGroupTracker entity) {
            entity._persistence_setFetchGroup(this);
        }

    }
}
