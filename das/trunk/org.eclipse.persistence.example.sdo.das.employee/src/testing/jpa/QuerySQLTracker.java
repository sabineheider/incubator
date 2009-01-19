/*******************************************************************************
 * Copyright (c) 1998, 2008 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    dclarke - JPA DAS INCUBATOR - Enhancement 258057
 *              http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing.jpa;

import java.io.StringWriter;
import java.util.*;

import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;
import org.eclipse.persistence.internal.helper.Helper;
import org.eclipse.persistence.logging.*;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.*;

/**
 * 
 * @author dclarke
 * @since TopLink 10.1.3
 */
public class QuerySQLTracker extends SessionEventAdapter {
	private List<QueryResult> queries;

	/**
	 * Constructs and installs the event listener and sql tracking session log
	 * 
	 * @param session
	 */
	private QuerySQLTracker(Session session) {
		session.getEventManager().addListener(this);
		session.setSessionLog(new SQLTrackingSessionLog(session, this));
		reset();
	}

	public static QuerySQLTracker install(Session session) {
		if (session.getSessionLog() instanceof SQLTrackingSessionLog) {
			return ((SQLTrackingSessionLog) session.getSessionLog()).getTracker();
		}
		return new QuerySQLTracker(session);
	}

	/**
	 * Helper method to retrieve a tracker from a session where it was installed
	 * If the session exists but does not have a tracler installed then an
	 * exception is thrown.
	 */
	public static QuerySQLTracker getTracker(Session session) {
		if (session == null) {
			return null;
		}
		SessionLog sessionLog = session.getSessionLog();

		if (sessionLog instanceof QuerySQLTracker.SQLTrackingSessionLog) {
			return ((QuerySQLTracker.SQLTrackingSessionLog) sessionLog).getTracker();
		}
		throw new RuntimeException("Could not retireve QuerySQLTracke from session: " + session);
	}

	/**
	 * Reset the lists of SQL and queries being tracked
	 */
	public void reset() {
		this.queries = new ArrayList<QueryResult>();
	}

	public List<QueryResult> getQueries() {
		return this.queries;
	}

	protected void addBuiltObject(DatabaseQuery query, Object object) {

	}

	protected QuerySQLTracker.QueryResult getCurrentResult() {
		if (getQueries().size() == 0) {
			getQueries().add(new QueryResult(null));
			// throw new RuntimeException("Received SQL without a Query ???");
		}
		return getQueries().get(getQueries().size() - 1);
	}

	public int getTotalSQLCalls() {
		int totalSQLCalls = 0;

		for (QueryResult result : getQueries()) {
			totalSQLCalls += result.sqlStatements.size();
		}

		return totalSQLCalls;
	}

	public int getTotalSQLCalls(String startsWith) {
		int sqlCalls = 0;

		for (QueryResult result : getQueries()) {
			for (String sql : result.sqlStatements) {
				String sub = sql.substring(0, startsWith.length());
				if (sub.equalsIgnoreCase(startsWith)) {
					sqlCalls++;
				}
			}
		}

		return sqlCalls;
	}

	public int getTotalSQLSELECTCalls() {
		return getTotalSQLCalls("SELECT");
	}

	public int getTotalSQLINSERTCalls() {
		return getTotalSQLCalls("INSERT");
	}

	public int getTotalSQLUPDATECalls() {
		return getTotalSQLCalls("UPDATE");
	}

	public int getTotalSQLDELETECalls() {
		return getTotalSQLCalls("DELETE");
	}

	public void preExecuteQuery(SessionEvent event) {
		if (event.getQuery() == null) {
			System.out.println("HOW?");
		}
		QueryResult result = new QueryResult(event.getQuery());
		getQueries().add(result);
	}

	public void postExecuteQuery(SessionEvent event) {
		if (getCurrentResult().query == null) {
			getCurrentResult().setQuery(event.getQuery());
		}
		getCurrentResult().setResult(event.getResult());
	}

	protected class QueryResult {
		private DatabaseQuery query;
		private String resultString = null;
		private List<String> sqlStatements = new ArrayList<String>();

		QueryResult(DatabaseQuery q) {
			query = q;
		}

		protected void setQuery(DatabaseQuery query) {
			this.query = query;
		}

		protected void setResult(Object queryResult) {
			StringWriter writer = new StringWriter();
			writer.write(Helper.getShortClassName(query));
			writer.write("[" + System.identityHashCode(query) + "]");
			writer.write(" result = ");

			Object result = queryResult;
			if (queryResult instanceof Collection) {
				result = ((Collection) queryResult).toArray();
			}

			if (result == null) {
				writer.write("NONE");
			} else {
				if (result instanceof Object[]) {
					Object[] results = (Object[]) result;
					writer.write("<" + results.length + "> [");
					for (int index = 0; index < results.length; index++) {
						if (index > 0) {
							writer.write(", ");
						}
						writer.write(results[index] + "");
					}
					writer.write("]");
					resultString = writer.toString();
				} else {
					writer.write(result.toString());
				}
			}

			this.resultString = writer.toString();
		}

		public void addSQL(String sql) {
			sqlStatements.add(sql);
		}

		public String toString() {
			if (this.resultString == null) {
				setResult(null);
			}
			return this.resultString;
		}
	}

	/**
	 * This custom SessionLog implementation wraps the existng one and redirects
	 * all SQL calls to the tracker. All messages are also passed to the orginal
	 * tracker.
	 */
	public class SQLTrackingSessionLog extends DefaultSessionLog {
		private QuerySQLTracker tracker;

		private SessionLog originalLog;

		protected SQLTrackingSessionLog(Session session, QuerySQLTracker aTracker) {
			this.tracker = aTracker;
			this.originalLog = session.getSessionLog();
			setLevel(this.originalLog.getLevel());
			setSession(session);
			setWriter(this.originalLog.getWriter());
		}

		public QuerySQLTracker getTracker() {
			return this.tracker;
		}

		public synchronized void log(SessionLogEntry entry) {

			if (entry.getNameSpace() != null && entry.getNameSpace().equalsIgnoreCase(SessionLog.SQL)) {
				getTracker().getCurrentResult().addSQL(entry.getMessage());
			}
			super.log(entry);
		}
	}

	public void printResults(String prefix) {
		System.out.println(prefix + "-QuerySQLTracker-Queries:");

		int sql = 0;
		for (int index = 0; index < getQueries().size(); index++) {
			QueryResult result = getQueries().get(index);

			System.out.println("\t" + (index + 1) + "> " + result);

			for (int sqlNum = 0; sqlNum < result.sqlStatements.size(); sqlNum++) {
				sql++;
				System.out.println("\t\t" + (index + 1) + "." + (sqlNum + 1) + "-" + sql + "> " + result.sqlStatements.get(sqlNum));
			}
		}

		System.out.println(prefix + "-QuerySQLTracker-Queries: " + getQueries().size());
		System.out.println(prefix + "-QuerySQLTracker-INSERT: " + getTotalSQLINSERTCalls());
		System.out.println(prefix + "-QuerySQLTracker-SELECT: " + getTotalSQLSELECTCalls());
		System.out.println(prefix + "-QuerySQLTracker-UPDATE: " + getTotalSQLUPDATECalls());
		System.out.println(prefix + "-QuerySQLTracker-DELETE: " + getTotalSQLDELETECalls());
	}

	static class DescriptorBuildCounter extends DescriptorEventAdapter {
		private QuerySQLTracker querySQLTracker;

		private DescriptorBuildCounter(QuerySQLTracker tracker) {
			this.querySQLTracker = tracker;
		}

		public void postBuild(DescriptorEvent event) {
			this.querySQLTracker.addBuiltObject(event.getQuery(), event.getSource());
		}
	}
}