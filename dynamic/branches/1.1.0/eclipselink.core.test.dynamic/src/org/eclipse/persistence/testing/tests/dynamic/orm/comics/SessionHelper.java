package org.eclipse.persistence.testing.tests.dynamic.orm.comics;

import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;
import org.eclipse.persistence.sessions.factories.SessionManager;
import org.eclipse.persistence.sessions.factories.XMLSessionConfigLoader;
import org.eclipse.persistence.sessions.server.Server;

public class SessionHelper {

    private static final String SESSION_NAME = "dynamic-comics";

    public static Server getComicsSession() {
        if (!SessionManager.getManager().getSessions().containsKey(SESSION_NAME)) {

            DynamicClassLoader dcl = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());

            new DynamicTypeBuilder(dcl.createDynamicClass("model.Issue"), null);
            new DynamicTypeBuilder(dcl.createDynamicClass("model.Publisher"), null);
            new DynamicTypeBuilder(dcl.createDynamicClass("model.Title"), null);

            XMLSessionConfigLoader loader = new XMLSessionConfigLoader();
            loader.setClassLoader(dcl);
            loader.setSessionName(SESSION_NAME);

            Server session = (Server) SessionManager.getManager().getSession(loader);

            return session;
        }
        return (Server) SessionManager.getManager().getSession(SESSION_NAME);
    }

}
