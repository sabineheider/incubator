package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.mappings;

public class Child2 {

    private int id;
    private Child1 child1;
    private Child1 child1Attribute;
    
    public Child2() {
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Child1 getChild1() {
        return child1;
    }

    public void setChild1(Child1 child1) {
        this.child1 = child1;
    }

    public Child1 getChild1Attribute() {
        return child1Attribute;
    }

    public void setChild1Attribute(Child1 child1Attribute) {
        this.child1Attribute = child1Attribute;
    }
   
}