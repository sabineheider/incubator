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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import javax.persistence.PersistenceContext;

import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroup.FetchItem;
import org.junit.Test;

import testing.EclipseLinkJPATest;

/**
 * Simple tests to verify the functionality of FetchGroup API
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
@PersistenceContext(unitName = "employee")
public class FetchGroupAPITests extends EclipseLinkJPATest {

    @Test
    public void verifyDefaultConstructor() {
        FetchGroup fg = new FetchGroup();

        assertNull("default FetchGroup name not null", fg.getName());
        assertTrue(fg.getFetchItems().isEmpty());
    }

    @Test
    public void verifyNameConstructor() {
        FetchGroup fg = new FetchGroup("Test");

        assertEquals("Test name not set", "Test", fg.getName());
        assertTrue(fg.getFetchItems().isEmpty());
        fg.toString();
    }

    @Test
    public void verifyNameConstructor_Null() {
        FetchGroup fg = new FetchGroup(null);

        assertNull("Null name not set", fg.getName());
        assertTrue(fg.getFetchItems().isEmpty());
        fg.toString();
    }

    private void verifyInvalid(String arg) {
        FetchGroup fg = new FetchGroup();

        try {
            fg.addAttribute(arg);
        } catch (IllegalArgumentException iae) {
            return;
        }
        fail("IllegalArgumentException expected but not caught.");
    }

    @Test
    public void verifyInvalidAdd_null() {
        verifyInvalid(null);
    }

    @Test
    public void verifyInvalidAdd_empty() {
        verifyInvalid("");
    }

    @Test
    public void verifyInvalidAdd_dot() {
        verifyInvalid(".");
        verifyInvalid("..");
        verifyInvalid(". ");
        verifyInvalid(" .");
        verifyInvalid(" . ");
        verifyInvalid(". .");
    }

    @Test
    public void verifyInvalidAdd_startWithDot() {
        verifyInvalid(".name");
    }

    @Test
    public void verifyInvalidAdd_endWithDot() {
        verifyInvalid("name.");
    }

    @Test
    public void verifyInvalidAdd_space() {
        verifyInvalid(" ");
        verifyInvalid("\t");
        verifyInvalid("\n");
        verifyInvalid("\r");
    }

    /**
     * Verify that {@link FetchGroup#getFetchItem(String)} works properly on an
     * empty FetchGroup.
     */
    @Test
    public void verifygetFetchItem_EmptyFG() {
        FetchGroup fg = new FetchGroup();

        assertTrue(fg.getFetchItems().isEmpty());

        assertNull(fg.getFetchItem("test"));
        assertNull(fg.getFetchItem("a.b"));

        assertTrue(fg.getFetchItems().isEmpty());
    }

    @Test
    public void verifyAddAttribute() {
        FetchGroup fg = new FetchGroup();

        fg.addAttribute("test");

        assertEquals(1, fg.getFetchItems().size());
        assertTrue(fg.getFetchItems().containsKey("test"));
        assertNotNull(fg.getFetchItems().get("test"));

        FetchItem item = fg.getFetchItem("test");
        assertNotNull(item);

        assertEquals("test", item.getAttributeName());
    }

    @Test
    public void verifyAddAttribute_Nested() {
        FetchGroup fg = new FetchGroup();

        fg.addAttribute("test.test");

        assertEquals(1, fg.getFetchItems().size());
        assertTrue(fg.getFetchItems().containsKey("test"));

        FetchItem testFI = (FetchItem) fg.getFetchItems().get("test");
        assertNotNull(testFI);
        assertEquals("test", testFI.getAttributeName());
        assertNotNull(testFI.getFetchGroup());
        assertEquals("test", testFI.getFetchGroup().getName());

        testFI = fg.getFetchItem("test");
        assertNotNull(testFI);
        assertEquals("test", testFI.getAttributeName());
        assertEquals(1, testFI.getFetchGroup().getFetchItems().size());
        assertTrue(testFI.getFetchGroup().getFetchItems().containsKey("test"));
        assertNotNull(testFI.getFetchGroup().getFetchItem("test"));
    }

    @Test
    public void verifyAdd2AttributesNestedFG() {
        FetchGroup fg = new FetchGroup();

        fg.addAttribute("a.b");
        fg.addAttribute("a.c");

        assertEquals(1, fg.getFetchItems().size());
        assertTrue(fg.getFetchItems().containsKey("a"));

        FetchItem aItem = fg.getFetchItem("a");
        FetchGroup aFG = aItem.getFetchGroup();

        assertNotNull(aItem);
        assertNotNull(aFG);
        assertFalse(aItem.useDefaultFetchGroup());
        assertEquals(2, aFG.getFetchItems().size());
        assertEquals("a", aFG.getName());

        FetchItem bItem = aFG.getFetchItem("b");
        assertNotNull(bItem);
        assertEquals("b", bItem.getAttributeName());
        assertNull(bItem.getFetchGroup());
        assertTrue(bItem.useDefaultFetchGroup());
        assertSame(bItem, fg.getFetchItem("a.b"));

        FetchItem cItem = aFG.getFetchItem("c");
        assertNotNull(cItem);
        assertEquals("c", cItem.getAttributeName());
        assertNull(cItem.getFetchGroup());
        assertTrue(cItem.useDefaultFetchGroup());
        assertSame(cItem, fg.getFetchItem("a.c"));
    }

    @Test
    public void verifyAdd2AttributesNestedFG_parentFirst() {
        FetchGroup fg = new FetchGroup();

        fg.addAttribute("a");
        fg.addAttribute("a.b");
        fg.addAttribute("a.c");

        assertEquals(1, fg.getFetchItems().size());
        assertTrue(fg.getFetchItems().containsKey("a"));

        FetchItem aItem = fg.getFetchItem("a");
        FetchGroup aFG = aItem.getFetchGroup();

        assertNotNull(aItem);
        assertNotNull(aFG);
        assertFalse(aItem.useDefaultFetchGroup());
        assertEquals(2, aFG.getFetchItems().size());
        assertEquals("a", aFG.getName());

        FetchItem bItem = aFG.getFetchItem("b");
        assertNotNull(bItem);
        assertEquals("b", bItem.getAttributeName());
        assertNull(bItem.getFetchGroup());
        assertTrue(bItem.useDefaultFetchGroup());
        assertSame(bItem, fg.getFetchItem("a.b"));

        FetchItem cItem = aFG.getFetchItem("c");
        assertNotNull(cItem);
        assertEquals("c", cItem.getAttributeName());
        assertNull(cItem.getFetchGroup());
        assertTrue(cItem.useDefaultFetchGroup());
        assertSame(cItem, fg.getFetchItem("a.c"));
    }

    @Test
    public void verifyAddAttribute_Nested2() {
        FetchGroup fg = new FetchGroup();

        fg.addAttribute("test.test.test");

        assertEquals(1, fg.getFetchItems().size());
        assertTrue(fg.getFetchItems().containsKey("test"));

        FetchItem testFI = (FetchItem) fg.getFetchItems().get("test");
        assertNotNull(testFI);
        assertEquals("test", testFI.getAttributeName());

        testFI = fg.getFetchItem("test");
        assertNotNull(testFI);
        assertEquals("test", testFI.getAttributeName());
        assertNotNull(testFI.getFetchGroup());

        FetchItem testFI2 = (FetchItem) testFI.getFetchGroup().getFetchItems().get("test");
        assertNotNull(testFI2);
        assertEquals("test", testFI2.getAttributeName());
        assertNotNull(testFI2.getFetchGroup());
        assertEquals("test.test", testFI2.getFetchGroup().getName());
        assertFalse(testFI2.getFetchGroup().getFetchItems().isEmpty());

        testFI2 = testFI.getFetchGroup().getFetchItem("test");
        assertNotNull(testFI2);
        assertEquals("test", testFI2.getAttributeName());
        assertFalse(testFI2.getFetchGroup().getFetchItems().isEmpty());
        assertEquals(1, testFI2.getFetchGroup().getFetchItems().size());
        assertTrue(testFI2.getFetchGroup().getFetchItems().containsKey("test"));
        assertNotNull(testFI2.getFetchGroup().getFetchItems().get("test"));

        testFI2 = fg.getFetchItem("test.test");
        assertNotNull(testFI2);
        assertEquals("test.test", testFI2.getFetchGroup().getName());
    }

    @Test
    public void verifyAdd() {
        FetchGroup fg = new FetchGroup();

        fg.addAttribute("test");

        assertEquals(1, fg.getFetchItems().size());
        assertTrue(fg.getFetchItems().containsKey("test"));
        assertNotNull(fg.getFetchItems().get("test"));
        assertNotNull(fg.getFetchItem("test"));
    }

    @Test
    public void verifyAdd_Nested() {
        FetchGroup<?> fg = new FetchGroup();
        fg.addAttribute("test.test");

        assertEquals(1, fg.getFetchItems().size());
        assertTrue(fg.getFetchItems().containsKey("test"));

        FetchItem testFI = fg.getFetchItems().get("test");
        assertNotNull(testFI);
        assertEquals("test", testFI.getAttributeName());

        testFI = fg.getFetchItem("test");
        assertNotNull(testFI);
        assertEquals("test", testFI.getAttributeName());
        assertEquals(1, testFI.getFetchGroup().getFetchItems().size());
        assertTrue(testFI.getFetchGroup().getFetchItems().containsKey("test"));
        assertNotNull(testFI.getFetchGroup().getFetchItem("test"));
    }

}
