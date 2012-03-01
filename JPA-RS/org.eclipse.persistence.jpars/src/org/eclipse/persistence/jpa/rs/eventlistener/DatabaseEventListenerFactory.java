package org.eclipse.persistence.jpa.rs.eventlistener;

public interface DatabaseEventListenerFactory {
    
    public DescriptorBasedDatabaseEventListener createDatabaseEventListener();

}
