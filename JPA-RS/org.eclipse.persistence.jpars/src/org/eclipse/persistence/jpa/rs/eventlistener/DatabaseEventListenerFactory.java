package org.eclipse.persistence.jpa.rs.eventlistener;

import org.eclipse.persistence.platform.database.events.DatabaseEventListener;

public interface DatabaseEventListenerFactory {
    
    public DatabaseEventListener createDatabaseEventListener();

}
