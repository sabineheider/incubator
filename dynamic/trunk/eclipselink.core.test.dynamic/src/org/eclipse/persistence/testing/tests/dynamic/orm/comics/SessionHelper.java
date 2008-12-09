package org.eclipse.persistence.testing.tests.dynamic.orm.comics;

import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.sessions.factories.SessionManager;
import org.eclipse.persistence.sessions.factories.XMLSessionConfigLoader;
import org.eclipse.persistence.sessions.server.Server;

public class SessionHelper {

	private static final String SESSION_NAME = "dynamic-comics";

	public static Server getComicsSession() {
		if (!SessionManager.getManager().getSessions().containsKey(SESSION_NAME)) {

			DynamicClassLoader classLoader = new DynamicClassLoader(null);

			classLoader.createDynamicClass("model.Issue");
			classLoader.createDynamicClass("model.Publisher");
			classLoader.createDynamicClass("model.Title");

			XMLSessionConfigLoader loader = new XMLSessionConfigLoader();
			loader.setClassLoader(classLoader);
			loader.setSessionName(SESSION_NAME);

			return (Server) SessionManager.getManager().getSession(loader);
		}
		return (Server) SessionManager.getManager().getSession(SESSION_NAME);
	}
}
