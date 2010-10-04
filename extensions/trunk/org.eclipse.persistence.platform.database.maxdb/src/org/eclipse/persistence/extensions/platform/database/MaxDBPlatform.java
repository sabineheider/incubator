/*******************************************************************************
 * Copyright (c) 2009,2010 Markus Karg, SAP. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Markus Karg - Initial implementation
 * Andreas Fischbach - get tests running with maxdb
 *
 * This code is being developed under INCUBATION and is not currently included
 * in the automated EclipseLink build. The API in this code may change, or
 * may never be included in the product. Please provide feedback through mailing
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.platform.database;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.expressions.ListExpressionOperator;
import org.eclipse.persistence.internal.databaseaccess.DatabaseCall;
import org.eclipse.persistence.internal.databaseaccess.FieldTypeDefinition;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.queries.ValueReadQuery;
import org.eclipse.persistence.tools.schemaframework.FieldDefinition;

/**
 * Provides MaxDB specific behaviour.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @author afischbach
 * @author agoerler
 */
@SuppressWarnings("serial")
public final class MaxDBPlatform extends DatabasePlatform {

    private static final int MAX_BINARY_LENGTH = 8000; // FIXME review this

    /**
     * Maximum length of type VARCHAR UNICODE
     *
     * ({@link http://maxdb.sap.com/doc/7_8/45/33337d9faf2b34e10000000a1553f7/content.htm})
     */
    private static final int MAX_VARCHAR_UNICODE_LENGTH = 4000; //

    @Override
    public boolean isForUpdateCompatibleWithDistinct() {
        return false;
    }

    @Override
    public String getSelectForUpdateString() {
        return " WITH LOCK EXCLUSIVE";
    }

    @Override
    public String getSelectForUpdateNoWaitString() {
        return " WITH LOCK (NOWAIT) EXCLUSIVE";
    }

    public MaxDBPlatform(){
        super();
        this.pingSQL = "SELECT 1 FROM DUAL";
    }

    @Override
    protected final Hashtable buildFieldTypes() {
        final Hashtable<Class, FieldTypeDefinition> fieldTypeMapping = new Hashtable<Class, FieldTypeDefinition>();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("SMALLINT", false)); // TODO boolean
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DOUBLE PRECISION", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("FIXED", 19));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("FLOAT", false));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("DOUBLE PRECISION", false));

        fieldTypeMapping.put(BigInteger.class, new FieldTypeDefinition("FIXED",19));
        fieldTypeMapping.put(BigDecimal.class, new FieldTypeDefinition("FIXED", 38));

        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR", 1, "UNICODE"));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("VARCHAR", 255, "UNICODE"));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("VARCHAR", 255, "UNICODE"));
        fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR", 255, "UNICODE"));

        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("CHAR", 255, "BYTE"));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("CHAR", 255, "BYTE"));
        fieldTypeMapping.put(Blob.class, new FieldTypeDefinition("LONG BYTE", false));
        fieldTypeMapping.put(Clob.class, new FieldTypeDefinition("LONG UNICODE", false));

        fieldTypeMapping.put(Date.class, new FieldTypeDefinition("DATE", false));
        fieldTypeMapping.put(Time.class, new FieldTypeDefinition("TIME", false));
        fieldTypeMapping.put(Timestamp.class, new FieldTypeDefinition("TIMESTAMP", false));
        return fieldTypeMapping;
    }

    @Override
    public boolean supportsIndividualTableLocking() {
        return false;
    }

    @Override
    protected void printFieldTypeSize(Writer writer, FieldDefinition field, FieldTypeDefinition fieldType) throws IOException {
        String typeName = fieldType.getName();
        /* byte[] < 8000 map to CHAR BYTE, longer ones to LONG BYTE */
        Class javaFieldType = field.getType();
        /*    backward mapping big_bad_table ser_data 10000 - forwardmapper */
        if( ( javaFieldType == null && "CHAR".equals(typeName) && "BYTE".equals(fieldType.getTypesuffix())) ||
                (javaFieldType != null && (javaFieldType.equals(Byte[].class) || javaFieldType.equals(byte[].class))) ) {
            if(field.getSize() > MAX_BINARY_LENGTH || field.getSize() == 0)  {
               fieldType = new FieldTypeDefinition("LONG BYTE", false);
            }
        } else if ("VARCHAR".equals(typeName) && "UNICODE".equals(fieldType.getTypesuffix())) {
            if (field.getSize() > MAX_VARCHAR_UNICODE_LENGTH) {
                fieldType = new FieldTypeDefinition("LONG UNICODE", false);
            }
        }


        super.printFieldTypeSize(writer, field, fieldType);
        if (fieldType.getTypesuffix() != null) {
            writer.append(" " + fieldType.getTypesuffix());
        }
    }


    @Override
    protected final void initializePlatformOperators() {
        super.initializePlatformOperators();
        this.addOperator(MaxDBPlatform.createConcatExpressionOperator());
        this.addOperator(MaxDBPlatform.createTrim2ExpressionOperator());
        this.addOperator(MaxDBPlatform.createToNumberOperator());
        this.addOperator(MaxDBPlatform.createNullifOperator());
        this.addOperator(MaxDBPlatform.createCoalesceOperator());
        this.addOperator(MaxDBPlatform.createTodayExpressionOperator());
        this.addNonBindingOperator(MaxDBPlatform.createNullValueOperator());
    }

    private static final ExpressionOperator createConcatExpressionOperator() {
        return ExpressionOperator.simpleLogicalNoParens(ExpressionOperator.Concat, "||");
    }

    private static final ExpressionOperator createTodayExpressionOperator() {
        return ExpressionOperator.simpleLogicalNoParens(ExpressionOperator.Today, "DATE");
    }

    private static final ExpressionOperator createTrim2ExpressionOperator() {
        return ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Trim2, "TRIM");
    }

    private static final ExpressionOperator createNullValueOperator() {
        return ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Nvl, "VALUE");
    }

    /* see bug 316774 */
    private static final ExpressionOperator createCoalesceOperator() {
        ListExpressionOperator operator = (ListExpressionOperator) ExpressionOperator.coalesce();
        operator.setStartString("VALUE(");
        operator.setSelector(ExpressionOperator.Coalesce);
        return operator;
    }

    private static final ExpressionOperator createToNumberOperator() {
        return ExpressionOperator.simpleFunction(ExpressionOperator.ToNumber, "NUM");
    }

    private static final ExpressionOperator createNullifOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(ExpressionOperator.FunctionOperator);
        exOperator.setSelector(ExpressionOperator.NullIf);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(4);
        v.addElement(" (CASE WHEN ");
        v.addElement(" = ");
        v.addElement(" THEN NULL ELSE ");
        v.addElement(" END) ");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        int[] indices = {0, 1, 0};
        exOperator.setArgumentIndices(indices);
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    @Override
    public boolean shouldOptimizeDataConversion() {
        return true;
    }

    protected void addNonBindingOperator(ExpressionOperator operator) {
        operator.setIsBindingSupported(false);
        addOperator(operator);
    }

    public final ValueReadQuery buildSelectQueryForSequenceObject(final String sequenceName, final Integer size) {
        return new ValueReadQuery("SELECT " + this.getQualifiedSequenceName(sequenceName) + ".NEXTVAL FROM DUAL");
    }

    // not checked starts here; Andreas <°)))><

    @Override
    protected final String getCreateTempTableSqlBodyForTable(final DatabaseTable table) {
        return " LIKE " + table.getQualifiedName();
    }

    @Override
    protected final String getCreateTempTableSqlPrefix() {
        return "CREATE TABLE ";
    }

    @Override
    public final int getMaxFieldNameSize() {
        return 32;
    }

    private final String getQualifiedSequenceName(final String sequenceName) {
        return this.getTableQualifier().length() == 0 ? sequenceName : this.getTableQualifier() + "." + sequenceName;
    }

    @Override
    public final DatabaseTable getTempTableForTable(final DatabaseTable table) {
        return new DatabaseTable("$" + table.getName(), "TEMP");
    }

    @Override
    public final boolean isMaxDB() {
        return true;
    }

    @Override
    public final boolean shouldAlwaysUseTempStorageForModifyAll() {
        return true;
    }

    @Override
    public final boolean shouldBindLiterals() {
        return false;
    }

    @Override
    public final boolean shouldPrintOuterJoinInWhereClause() {
        return false;
    }

    @Override
    public final boolean shouldUseJDBCOuterJoinSyntax() {
        return false;
    }

    /*
     * TODO Removemove this function.
     */
    @Override
    public final boolean supportsForeignKeyConstraints() {
        return false;
    }

    @Override
    public final boolean supportsLocalTempTables() {
        return true;
    }

    @Override
    public final boolean supportsNativeSequenceNumbers() {
        return true;
    }

    @Override
    public boolean supportsSequenceObjects() {
        return true;
    }

    @Override
    public final boolean supportsStoredFunctions() {
        return true;
    }

    @Override
	public boolean canBatchWriteWithOptimisticLocking(DatabaseCall call) {
    	return true;
	}

	@Override
	public int executeBatch(Statement statement, boolean isStatementPrepared)
			throws SQLException {
		if (isStatementPrepared) {
			statement.executeBatch();
			return statement.getUpdateCount();
		} else {
			int[] updateCounts = statement.executeBatch();
			return updateCounts.length;
		}
	}

}
