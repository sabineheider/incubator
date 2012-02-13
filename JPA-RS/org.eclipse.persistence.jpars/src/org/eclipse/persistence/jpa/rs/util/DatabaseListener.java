package org.eclipse.persistence.jpa.rs.util;

import org.eclipse.persistence.platform.database.events.DatabaseEventListener;

public interface DatabaseListener extends DatabaseEventListener {

    public void addChangeListener(ChangeListener listener);
    
    public void removeChangeListener(ChangeListener listener);
    
}
