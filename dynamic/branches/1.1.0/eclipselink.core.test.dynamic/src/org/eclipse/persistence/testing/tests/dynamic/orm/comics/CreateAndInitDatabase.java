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
 *     shsmith,dclarke - Dynamic Persistence INCUBATION - Enhancement 200045
 *     			 http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.testing.tests.dynamic.orm.comics;

import static junit.framework.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.Test;

public class CreateAndInitDatabase {

	private static final String DATA_HOME = "org/eclipse/persistence/testing/tests/dynamic/orm/comics/";

	@Test
	public void populate() throws Exception {
		Server server = null;
		Session session = null;
		UnitOfWork uow = null;

		try {
			server = SessionHelper.getComicsSession();
			session = server.acquireClientSession();

			uow = session.acquireUnitOfWork();

			URL publisherFileURL = getClass().getClassLoader().getResource(DATA_HOME + "publisher.tab");
			Map<Integer, DynamicEntity> publishers = loadPublishers(server, publisherFileURL);
			persist(uow, publishers);

			URL titleFileURL = getClass().getClassLoader().getResource(DATA_HOME + "title.tab");
			Map<Integer, DynamicEntity> titles = loadTitles(server, titleFileURL, publishers);
			persist(uow, titles);

			URL issueFileURL = getClass().getClassLoader().getResource(DATA_HOME + "issue.tab");
			Map<Integer, DynamicEntity> issues = loadIssues(server, issueFileURL, titles);
			persist(uow, issues);

			SchemaManager sm = new SchemaManager(server);
			sm.replaceDefaultTables();
			sm.replaceSequences();

			uow.commit();

			ReportQuery countQuery = new ReportQuery(DynamicHelper.getType(server, "Publisher").getJavaClass(), new ExpressionBuilder());
			countQuery.addCount();
			countQuery.setShouldReturnSingleValue(true);
			assertEquals(publishers.size(), ((Number) session.executeQuery(countQuery)).intValue());

			countQuery = new ReportQuery(DynamicHelper.getType(server, "Title").getJavaClass(), new ExpressionBuilder());
			countQuery.addCount();
			countQuery.setShouldReturnSingleValue(true);
			assertEquals(titles.size(), ((Number) session.executeQuery(countQuery)).intValue());

			countQuery = new ReportQuery(DynamicHelper.getType(server, "Issue").getJavaClass(), new ExpressionBuilder());
			countQuery.addCount();
			countQuery.setShouldReturnSingleValue(true);
			assertEquals(issues.size(), ((Number) session.executeQuery(countQuery)).intValue());
		} finally {
			if (uow != null && uow.isActive()) {
				uow.release();
			}
			if (session != null) {
				session.release();
			}
			if (server != null) {
				server.release();
			}
		}
	}

	private static void persist(UnitOfWork uow, Map<Integer, DynamicEntity> entities) {
		for (DynamicEntity entity : entities.values()) {
			uow.registerNewObject(entity);
		}
	}

	private static Map<Integer, DynamicEntity> loadIssues(Server server, URL fileURL, Map<Integer, DynamicEntity> titles) throws Exception {
		EntityType type = DynamicHelper.getType(server, "Issue");
		Map<Integer, DynamicEntity> issues = new HashMap<Integer, DynamicEntity>();

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(fileURL.openStream()));
			String nextLine = null;
			while ((nextLine = reader.readLine()) != null) {
				DynamicEntity issue = buildIssue(type, nextLine, titles);
				issues.put(issue.<Integer>get("id"), issue);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return issues;
	}

	private static DynamicEntity buildIssue(EntityType issueType, String line, Map<Integer, DynamicEntity> titles) {
		// TITLE,ISSUE_NUMBER,STORY_ARC,CONDITION,COMMENTS,COPIES,ID,TITLE_ID
		String[] columns = line.split("\t");
		DynamicEntity issue = issueType.newInstance();
		issue.set("number", Integer.valueOf(columns[1]));
		issue.set("condition", columns[3]);
		issue.set("comments", columns[4]);
		String numCopiesString = columns[5];
		if (numCopiesString.length() > 0) {
			issue.set("copies", Integer.valueOf(numCopiesString));
		}
		issue.set("id", Integer.valueOf(columns[6]));
		issue.set("title", titles.get(Integer.valueOf(columns[7])));
		return issue;
	}

	private static Map<Integer, DynamicEntity> loadPublishers(Server server, URL fileURL) throws Exception {
		EntityType type = DynamicHelper.getType(server, "Publisher");
		Map<Integer, DynamicEntity> publishers = new HashMap<Integer, DynamicEntity>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(fileURL.openStream()));
			String nextLine = null;
			while ((nextLine = reader.readLine()) != null) {
				DynamicEntity publisher = buildPublisher(type, nextLine);
				publishers.put(publisher.<Integer>get("id"), publisher);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return publishers;
	}

	private static DynamicEntity buildPublisher(EntityType publisherType, String line) {
		// NAME ID
		String[] columns = line.split("\t");
		assert columns.length == 2;
		DynamicEntity publisher = publisherType.newInstance();
		publisher.set("name", columns[0]);
		publisher.set("id", Integer.valueOf(columns[1]));
		return publisher;
	}

	private static Map<Integer, DynamicEntity> loadTitles(Server server, URL fileURL, Map<Integer, DynamicEntity> publishers) throws Exception {
		EntityType type = DynamicHelper.getType(server, "Title");
		Map<Integer, DynamicEntity> titles = new HashMap<Integer, DynamicEntity>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(fileURL.openStream()));
			String nextLine = null;
			while ((nextLine = reader.readLine()) != null) {
				DynamicEntity title = buildTitle(type, nextLine, publishers);
				titles.put(title.<Integer>get("id"), title);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return titles;
	}

	private static DynamicEntity buildTitle(EntityType type, String line, Map<Integer, DynamicEntity> publishers) {
		// NAME,PUBLISHER,FORMAT,SUBSCRIBED,ID,PUBLISHER_ID
		String[] columns = line.split("\t");

		DynamicEntity title = type.newInstance();
		title.set("name", columns[0]);
		title.set("format", columns[2]);
		title.set("id", Integer.valueOf(columns[4]));
		title.set("publisher", publishers.get(Integer.valueOf(columns[5])));
		return title;
	}

}
