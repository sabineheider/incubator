package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.jaxb;

public class Root {

    private Child1 child1;
    private Child2 child2;
    
    public Root() {
    }

    public Child1 getChild1() {
        return child1;
    }

    public void setChild1(Child1 child1) {
        this.child1 = child1;
    }

    public Child2 getChild2() {
        return child2;
    }

    public void setChild2(Child2 child2) {
        this.child2 = child2;
    }
    
}