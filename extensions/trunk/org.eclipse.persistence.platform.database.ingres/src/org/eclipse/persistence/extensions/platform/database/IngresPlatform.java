/*******************************************************************************
 * Copyright (c) 2008 Ingres Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Ingres Corporation (rajus01@ingres.com) - Initial API version
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/  
package org.eclipse.persistence.extensions.platform.database;

import java.io.*;
import java.util.*;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.internal.databaseaccess.FieldTypeDefinition;
import org.eclipse.persistence.internal.expressions.RelationExpression;
import org.eclipse.persistence.internal.helper.*;
import org.eclipse.persistence.queries.ValueReadQuery;
import org.eclipse.persistence.tools.schemaframework.FieldDefinition;
import org.eclipse.persistence.platform.database.DatabasePlatform;

/**
 * <p><b>Purpose</b>: Provides Ingres specific behavior.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Native SQL for Date, Time, & Timestamp.
 * <li> Native sequencing.
 * <li> Mapping of class types to database types for the schema framework.
 * <li> Pessimistic locking.
 * <li> Platform specific operators.
 * </ul>
 *
 */
public class IngresPlatform extends DatabasePlatform {
    
    public IngresPlatform() {
        super();
        this.pingSQL = "SELECT 1";
    }
    
    /**
     * Appends a Boolean value. 
     * In Ingres the following are the values that
     * are value for a boolean field
     * Valid literal values for the "true" state are: 
     *  TRUE, 't', 'true', 'y', 'yes', '1'
     * Valid literal values for the false" state are :
     *  FALSE, 'f', 'false', 'n', 'no', '0'
     *
     * To be consistent with the other data platforms we are using the values 
     * '1' and '0' for true and false states of a boolean field.
     */
    protected void appendBoolean(Boolean bool, Writer writer) throws IOException {
        if (bool.booleanValue()) {
            writer.write("\'1\'");
        } else {
            writer.write("\'0\'");
        }
    }
    
    /**
     * INTERNAL:
     * Initialize any platform-specific operators
     */
    protected void initializePlatformOperators() {
        super.initializePlatformOperators();
        addOperator(ExpressionOperator.simpleLogicalNoParens(ExpressionOperator.Concat, "||"));
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Nvl, "IFNULL"));
        addOperator(operatorLocate());
    }


    /**
     * INTERNAL:
     * This method returns the query to select the timestamp from the server
     * for Ingres.
     */
    public ValueReadQuery getTimestampQuery() {
        if (timestampQuery == null) {
            timestampQuery = new ValueReadQuery();
            timestampQuery.setSQLString("select date('now')");
        }
        return timestampQuery;

    }


    /**
     * This method is used to print the output parameter token when stored
     * procedures are called
     */
    public String getInOutputProcedureToken() {
        return "OUT";
    }

    /**
     * This is required in the construction of the stored procedures with
     * output parameters
     */
    public boolean shouldPrintOutputTokenAtStart() {
        return false;
    }

    /**
     * INTERNAL:
     * Answers whether platform is Ingres
     */
    public boolean isIngres() {
        return true;
    }

    /**
     * INTERNAL:
     */
    protected String getCreateTempTableSqlSuffix() {
        return " ON COMMIT PRESERVE ROWS";
    }

    /**
     *  INTERNAL:
     *  Indicates whether the platform supports identity.
     *  This method is to be used *ONLY* by sequencing classes
     */
    public boolean supportsIdentity() {
        return false;
    }

    /**
     * INTERNAL:
     * Returns query used to read back the value generated by Identity.
     * This method is called when identity NativeSequence is connected,
     * the returned query used until the sequence is disconnected.
     * If the platform supportsIdentity then (at least) one of buildSelectQueryForIdentity
     * methods should return non-null query.
     */
    public ValueReadQuery buildSelectQueryForIdentity() {
        ValueReadQuery selectQuery = new ValueReadQuery(); 
        selectQuery.setSQLString(""); 
        return selectQuery;
    }

    /**
     *  INTERNAL:
     *  Indicates whether the platform supports sequence objects.
     *  This method is to be used *ONLY* by sequencing classes
     */
    public boolean supportsSequenceObjects() {
        return true;
    }

    /**
     * INTERNAL:
     * Returns query used to read value generated by sequence object (like Oracle sequence).
     * This method is called when sequence object NativeSequence is connected,
     * the returned query used until the sequence is disconnected.
     * If the platform supportsSequenceObjects then (at least) one of buildSelectQueryForSequenceObject
     * methods should return non-null query.
     */
    public ValueReadQuery buildSelectQueryForSequenceObject(String seqName, Integer size) {
        return new ValueReadQuery("select " + getQualifiedName(seqName) + ".nextval");
    }

    /**
     * INTERNAL:
     */
     protected String getCreateTempTableSqlBodyForTable(DatabaseTable table) {
        // returning null includes fields of the table in body
        // see javadoc of DatabasePlatform#getCreateTempTableSqlBodyForTable(DataBaseTable)
        // for details
        return null;
     }

    /**
     * INTERNAL:
     * Append the receiver's field 'identity' constraint clause to a writer
     */
    public void printFieldIdentityClause(Writer writer) throws ValidationException {
        try {            
            writer.write(" SERIAL");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping = new Hashtable();

        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("INTEGER1", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("FLOAT"));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("DECIMAL", 15));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("DECIMAL",19));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("INTEGER", false));

        fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR", 255));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR", 1));

        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("LONG BYTE", false));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("LONG VARCHAR"));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("LONG BYTE", false));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("LONG VARCHAR"));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("LONG BYTE"));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("LONG VARCHAR"));        

        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE", false));
        fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("TIME", false));
        fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("TIMESTAMP", false));

        return fieldTypeMapping;
    }  
    
    /**
     * INTERNAL:
     * Override the default locate operator
     */
    protected ExpressionOperator operatorLocate() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.Locate);
        Vector v = new Vector(3);
        v.addElement("STRPOS(");
        v.addElement(", ");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(RelationExpression.class);
        return result;
    }


    /**
     * INTERNAL:
     */
    public boolean supportsGlobalTempTables() {
        return true;
    }

    /**
     * INTERNAL:
     */
    protected String getCreateTempTableSqlPrefix() {
        return "CREATE GLOBAL TEMPORARY TABLE ";
    }
                
    /**
     * INTERNAL:
     * returns the maximum number of characters that can be used in a field
     * name on this platform.
     */
    public int getMaxFieldNameSize() {
        return 32;
    }

    public void printFieldTypeSize(Writer writer, FieldDefinition field, 
            FieldTypeDefinition fieldType, boolean shouldPrintFieldIdentityClause) throws IOException {
        if(!shouldPrintFieldIdentityClause) {
            super.printFieldTypeSize(writer, field, fieldType);
        }
    }
    
    public void printFieldUnique(Writer writer,  boolean shouldPrintFieldIdentityClause) throws IOException {
        if(!shouldPrintFieldIdentityClause) {
            super.printFieldUnique(writer);
        }
    }

    /**
     * JDBC defines and outer join syntax, many drivers do not support this. So we normally avoid it.
     */
    public boolean shouldUseJDBCOuterJoinSyntax() {
        return false;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects.
     * Returns sql used to create sequence object in the database.
     */
    public Writer buildSequenceObjectCreationWriter(Writer writer, String fullSeqName, int increment, int start) throws IOException {
        writer.write("CREATE SEQUENCE ");
        writer.write(fullSeqName);
        if (increment != 1) {
            writer.write(" INCREMENT BY " + increment);
        }
        writer.write(" START WITH " + start);
        return writer;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects.
     * Returns sql used to delete sequence object from the database.
     */
    public Writer buildSequenceObjectDeletionWriter(Writer writer, String fullSeqName) throws IOException {
        writer.write("DROP SEQUENCE ");
        writer.write(fullSeqName);
        return writer;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects
     * and isAlterSequenceObjectSupported returns true.
     * Returns sql used to alter sequence object's increment in the database.
     */
    public Writer buildSequenceObjectAlterIncrementWriter(Writer writer, String fullSeqName, int increment) throws IOException {
        writer.write("ALTER SEQUENCE ");
        writer.write(fullSeqName);
        writer.write(" INCREMENT BY " + increment);
        return writer;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects
     * and it's possible to alter sequence object's increment in the database.
     */
    public boolean isAlterSequenceObjectSupported() {
        return true;
    }

    public String getPostConstraintDeletionString() {
        return " CASCADE ";
    }

     /**
     * INTERNAL:
     * Used for pessimistic locking.
     */
    public String getSelectForUpdateString() {
        return " FOR UPDATE";
    }

       /**
     * INTERNAL:
     * This syntax does no wait on the lock.
     */
    public String getSelectForUpdateNoWaitString() {
        return " FOR UPDATE";
    }

     /**
     * This syntax does no wait on the lock.
     * (i.e. In Oracle adding NOWAIT to the end will accomplish this)
     */
    public String getNoWaitString() {
        return "";
    }


}

