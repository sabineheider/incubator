package org.eclipse.persistence.testing.tests.dynamic.orm.comics;

import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.sessions.factories.SessionManager;
import org.eclipse.persistence.sessions.factories.XMLSessionConfigLoader;
import org.eclipse.persistence.sessions.server.Server;

public class SessionHelper {

    private static final String SESSION_NAME = "dynamic-comics";

    public static Server getComicsSession() {
        if (!SessionManager.getManager().getSessions().containsKey(SESSION_NAME)) {

            DynamicClassLoader dcl = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());

            new EntityTypeBuilder(dcl.creatDynamicClass("model.Issue"), null);
            new EntityTypeBuilder(dcl.creatDynamicClass("model.Publisher"), null);
            new EntityTypeBuilder(dcl.creatDynamicClass("model.Title"), null);

            XMLSessionConfigLoader loader = new XMLSessionConfigLoader();
            loader.setClassLoader(dcl);
            loader.setSessionName(SESSION_NAME);

            Server session = (Server) SessionManager.getManager().getSession(loader);

            return session;
        }
        return (Server) SessionManager.getManager().getSession(SESSION_NAME);
    }

}
