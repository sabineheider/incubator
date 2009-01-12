package org.eclipse.persistence.testing.sdo.helper.jaxbhelper.mappings;

import java.util.List;

public class Root {

    private String id;
    private String name;
    private List<String> simpleList;
    private Child1 child1;
    private List<Child2> child2;
    
    public Root() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSimpleList() {
        return simpleList;
    }

    public void setSimpleList(List<String> simpleList) {
        this.simpleList = simpleList;
    }

    public Child1 getChild1() {
        return child1;
    }

    public void setChild1(Child1 child1) {
        this.child1 = child1;
    }

    public List<Child2> getChild2() {
        return child2;
    }

    public void setChild2(List<Child2> child2) {
        this.child2 = child2;
    }
    
}