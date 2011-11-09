package org.eclipse.persistence.jpa.rs.util;

public interface ChangeListener {

    void objectUpdated(Object object); 

    void objectInserted(Object object);

}

