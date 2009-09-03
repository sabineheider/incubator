/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle., Bill Blalock All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Bill Blalock
 * 
 * Patterned after:
 *   org.eclipse.persistence.platform.database.DB2MainframePlatform      
 ******************************************************************************/  
package org.eclipse.persistence.extensions.platform.database;

import java.util.Vector;

import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.platform.database.DB2Platform;
import org.eclipse.persistence.queries.ValueReadQuery;

/**
 *    <B>Purpose</B>: Provides specific behavior for DB2 Universal Database for iSeries.<P>
 *    IBM iSeries has SYSIBM schema for compatibility with DB2.  However the primary schema
 *    for SQL system tables is QSYS2.  All tables, table structures and behavior of QSYS2 
 *    tables are not duplicated in SYSIBM schema.<P>
 *    <B>Responsibilities</B>:
 *        <UL>
 *            <LI>Override getNativeTableInfo() using QSYS2 schema and different column name.</LI>
 *        		 <UL>
 *               <LI>SYSTABLES in QSYS2 schema, not in SYSIBM schema</LI>
 *               <LI>SYSTABLE = 'N' replaces TBCREATOR NOT IN ('SYS', 'SYSTEM')</LI>
 *               <LI>CREATOR replaces TBCREATOR (not defined in QSYS2/SYSTABLES catalog.</LI> 
 *               </UL>
 *            <LI>Override getTimestampQuery() using QSYS2 schema instead of SYSIBM.
 *        </UL>
 *
 * @since Eclipselink 1.0
 * @author Bill Blalock
 */
@SuppressWarnings("serial")
public class DB2UDBiSeriesPlatform extends DB2Platform {

	public DB2UDBiSeriesPlatform() {
		super();
	}
	/**
     * Return the catalog information through using the native SQL catalog selects
     * of DB2 Universal Database for iSeries.
     * This is required because many JDBC driver do not support meta-data.
     * Wildcards can be passed as arguments.
     * @param table
     * @param creator
     * @param session
     * @return Catalog information returned from iSeries.
     * @overide
     */
    @SuppressWarnings("unchecked")
	public Vector getNativeTableInfo(String table, String creator, AbstractSession session) {
        String query = "SELECT * FROM QSYS2.SYSTABLES WHERE SYSTABLE = 'N'";
        if (table != null) {
            if (table.indexOf('%') != -1) {
                query = query + " AND TBNAME LIKE " + table;
            } else {
                query = query + " AND TBNAME = " + table;
            }
        }
        if (creator != null) {
            if (creator.indexOf('%') != -1) {
                query = query + " AND CREATOR LIKE " + creator;
            } else {
                query = query + " AND CREATOR = " + creator;
            }
        }
        return session.executeSelectingCall(new org.eclipse.persistence.queries.SQLCall(query));
    }

    /**
     * This method returns the query to select the timestamp
     * from the DB2 Universal Database for iSeries server.
     * @return ValueReadQuery to retrieve current timestamp from iSeries.
     * @override
     */
    public ValueReadQuery getTimestampQuery() {
                if (timestampQuery == null) {
            timestampQuery = new ValueReadQuery();
            timestampQuery.setSQLString("SELECT DISTINCT CURRENT TIMESTAMP FROM QSYS2.SYSTABLES");
        }
        return timestampQuery;
    }



}