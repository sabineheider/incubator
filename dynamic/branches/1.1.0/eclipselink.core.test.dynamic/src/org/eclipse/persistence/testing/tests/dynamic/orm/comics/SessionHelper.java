package org.eclipse.persistence.testing.tests.dynamic.orm.comics;

import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.eclipse.persistence.sessions.factories.SessionManager;
import org.eclipse.persistence.sessions.factories.XMLSessionConfigLoader;
import org.eclipse.persistence.sessions.server.Server;

public class SessionHelper {

    private static final String SESSION_NAME = "dynamic-comics";

    public static Server getComicsSession() {
        if (!SessionManager.getManager().getSessions().containsKey(SESSION_NAME)) {
            DynamicConversionManager dcm = DynamicConversionManager.getDynamicConversionManager(null);

            dcm.createDynamicClass("model.Issue");
            dcm.createDynamicClass("model.Publisher");
            dcm.createDynamicClass("model.Title");

            XMLSessionConfigLoader loader = new XMLSessionConfigLoader();
            loader.setClassLoader(dcm.getLoader());
            loader.setSessionName(SESSION_NAME);

            return (Server) SessionManager.getManager().getSession(loader);
        }
        return (Server) SessionManager.getManager().getSession(SESSION_NAME);
    }
}
