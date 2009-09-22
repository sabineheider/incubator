package org.eclipse.persistence.testing.tests.dynamic;

import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.testing.util.QuerySQLTracker;
import org.junit.After;
import org.junit.AfterClass;

public class EclipseLinkORMTest {

    protected static DatabaseSession sharedSession;

    private Session session;

    protected DatabaseSession getSharedSession() {
        if (sharedSession == null) {
            sharedSession = createSharedSession();
        }

        return sharedSession;
    }

    protected Session getSession() {
        if (this.session == null) {
            this.session = getSharedSession();

            if (this.session.isServerSession()) {
                this.session = ((Server) this.session).acquireClientSession();
            }
        }

        return this.session;
    }

    protected DatabaseSession createSharedSession() {
        if (sharedSession != null) {
            if (sharedSession.isConnected()) {
                sharedSession.logout();
            }
        }
        try {
            sharedSession = DynamicTestHelper.createEmptySession();
            QuerySQLTracker.install(sharedSession);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return sharedSession;
    }

    protected QuerySQLTracker getQuerySQLTracker(Session session) {
        return QuerySQLTracker.getTracker(session);
    }

    @After
    public void cleanupClosedSharedSession() {
        if (this.session != null && this.session.isClientSession()) {
            this.session.release();
        }
        this.session = null;

        if (sharedSession != null) {
            if (!sharedSession.isConnected()) {
                sharedSession = null;
            } else {
                QuerySQLTracker.getTracker(sharedSession).reset();
            }
        }
    }

    @AfterClass
    public static void closeSharedSession() throws Exception {
        if (sharedSession != null && sharedSession.isConnected()) {
            sharedSession.logout();
            sharedSession = null;
        }
    }

}
