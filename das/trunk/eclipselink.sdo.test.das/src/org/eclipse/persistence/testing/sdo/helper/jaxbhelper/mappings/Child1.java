package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.mappings;

import java.util.Vector;

public class Child1 {

    private int id;
    private Child2 child2;
    private Vector child2Collection;

    public Child1() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Child2 getChild2() {
        return child2;
    }

    public void setChild2(Child2 child2) {
        this.child2 = child2;
    }

    public Vector getChild2Collection() {
        return child2Collection;
    }

    public void setChild2Collection(Vector child2Collection) {
        this.child2Collection = child2Collection;
    }

}
