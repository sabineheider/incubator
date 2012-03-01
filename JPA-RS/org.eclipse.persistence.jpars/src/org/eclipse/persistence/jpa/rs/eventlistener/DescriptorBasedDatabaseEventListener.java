package org.eclipse.persistence.jpa.rs.eventlistener;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.platform.database.events.DatabaseEventListener;
import org.eclipse.persistence.sessions.Session;

public interface DescriptorBasedDatabaseEventListener extends DatabaseEventListener {

    public void register(Session session, ClassDescriptor descriptor);
}
