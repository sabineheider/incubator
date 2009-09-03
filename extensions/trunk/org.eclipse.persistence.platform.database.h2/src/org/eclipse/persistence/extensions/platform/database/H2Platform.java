/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Mike Keith
 * 
 * Patterned after:
 *   org.eclipse.persistence.platform.database.DB2MainframePlatform      
 ******************************************************************************/  
package org.eclipse.persistence.extensions.platform.database;

import java.io.*;
import java.util.*;

import org.eclipse.persistence.internal.databaseaccess.DatabaseCall;
import org.eclipse.persistence.internal.databaseaccess.FieldTypeDefinition;
import org.eclipse.persistence.internal.expressions.ExpressionSQLPrinter;
import org.eclipse.persistence.internal.expressions.SQLSelectStatement;
import org.eclipse.persistence.platform.database.HSQLPlatform;
import org.eclipse.persistence.queries.ValueReadQuery;
import org.eclipse.persistence.queries.ReadQuery;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sequencing.NativeSequence;

public class H2Platform extends HSQLPlatform {
   private static final long serialVersionUID = -2935483687958482934L;

   public H2Platform() {
      super();
      setPingSQL("SELECT 1");
      setSupportsAutoCommit(true);
   }

   @Override
   public final boolean isHSQL() {
      return false;
   }

   @Override
   public void printSQLSelectStatement(DatabaseCall call, ExpressionSQLPrinter printer, SQLSelectStatement statement) {
      super.printSQLSelectStatement(call, printer, statement);
      ReadQuery query = statement.getQuery();
      if(query != null && query.isReadQuery() && !query.isUserDefined() && !query.isCallQuery()) {
         int firstRow = query.getFirstResult();
         int maxRows = query.getMaxRows();
         if(maxRows > 0 || firstRow > 0) {
            printer.printString(" LIMIT ");
            if(maxRows > 0) {
               maxRows -= firstRow;
            }
            else {
               // h2 syntax for limit clause requires max value (offset without limit is not possible)
               maxRows = Integer.MAX_VALUE;
            }
            printer.printPrimitive(maxRows);
            if(firstRow > 0) {
               printer.printString(" OFFSET ");
               printer.printPrimitive(firstRow);
            }
            call.setIgnoreFirstRowMaxResultsSettings(true);
         }
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   protected Hashtable buildFieldTypes() {
      Hashtable fieldTypeMapping = super.buildFieldTypes();
      fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE", false));
      fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("TIME", false));
      fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("TIMESTAMP", false));
      return fieldTypeMapping;
   }

   @Override
   public boolean isAlterSequenceObjectSupported() {
      return true;
   }

   @Override
   public ValueReadQuery buildSelectQueryForSequenceObject(String seqName,
Integer size) {
      return new ValueReadQuery(new StringBuilder(20 +
seqName.length()).append("CALL NEXT VALUE FOR ").append(seqName).toString());
   }

   @Override
   public Writer buildSequenceObjectAlterIncrementWriter(Writer writer, String
fullSeqName, int increment) throws IOException {
      return writer.append("ALTER SEQUENCE ").append(fullSeqName).append("INCREMENT BY ").append(Integer.toString(increment));
   }

   @Override
   public Writer buildSequenceObjectCreationWriter(Writer writer, String
fullSeqName, int increment, int start) throws IOException {
      return writer.append("CREATE SEQUENCE IF NOT EXISTS").append(fullSeqName).append(" START WITH ").append(Integer.toString(start))
            .append(" INCREMENT BY ").append(Integer.toString(increment));
   }

   @Override
   protected Sequence createPlatformDefaultSequence() {
      return new NativeSequence();
   }

   @Override
   public boolean supportsIdentity() {
      return true;
   }

   @Override
   public ValueReadQuery buildSelectQueryForIdentity() {
      return new ValueReadQuery("CALL IDENTITY()");
   }

   @Override
   public void printFieldIdentityClause(Writer writer) throws
ValidationException {
      try {
         writer.append(" IDENTITY");
      }
      catch(IOException e) {
         throw ValidationException.logIOError(e);
      }
   }

   @Override
   public boolean supportsForeignKeyConstraints() {
      return true;
   }

   @Override
   public boolean supportsLocalTempTables() {
      return true;
   }

   @Override
   public boolean supportsGlobalTempTables() {
      return true;
   }

   @Override
   protected String getCreateTempTableSqlPrefix() {
      return "CREATE TEMPORARY TABLE IF NOT EXISTS ";
   }

   @Override
   public boolean supportsNativeSequenceNumbers() {
      return true;
   }

   @Override
   public boolean supportsStoredFunctions() {
      return true;
   }

   @Override
   public ValueReadQuery getTimestampQuery() {
      return new ValueReadQuery("SELECT CURRENT_TIMESTAMP()");
   }

   @Override
   protected void initializePlatformOperators() {
      super.initializePlatformOperators();
      addOperator(ExpressionOperator.simpleMath(ExpressionOperator.Concat, "||"));
   }
}