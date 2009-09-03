/*******************************************************************************
 * Copyright (c) 2009 Markus Karg. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Markus Karg - Initial implementation
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/  
package org.eclipse.persistence.extensions.platform.database;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Hashtable;

import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.platform.database.DatabasePlatform;
import org.eclipse.persistence.internal.databaseaccess.FieldTypeDefinition;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.queries.ValueReadQuery;

/**
 * Provides MaxDB specific behaviour.
 * 
 * @author Markus KARG (markus-karg@users.sourceforge.net)
 */
@SuppressWarnings("serial")
public final class MaxDBPlatform extends DatabasePlatform {

    private static final ExpressionOperator createConcatExpressionOperator() {
        return ExpressionOperator.simpleTwoArgumentFunction(
                ExpressionOperator.Concat, "CONCAT");
    }

    private static final ExpressionOperator createEqualOuterJoinOperator() {
        return ExpressionOperator.simpleRelation(
                ExpressionOperator.EqualOuterJoin, "(+)=");
    }

    private static final ExpressionOperator createTrim2ExpressionOperator() {
        return ExpressionOperator.simpleTwoArgumentFunction(
                ExpressionOperator.Trim2, "TRIM");
    }

    /*
     * TODO Add operators that are not contained in database platform already
     */

    @Override
    protected final Hashtable buildFieldTypes() {
        final Hashtable<Class, FieldTypeDefinition> fieldTypeMapping = new Hashtable<Class, FieldTypeDefinition>();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("BOOLEAN",
                false));
        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER",
                false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("FIXED(20)", false));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("FLOAT",
                false));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition(
                "DOUBLE PRECISION", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT",
                false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("SMALLINT",
                false));
        fieldTypeMapping.put(BigInteger.class, new FieldTypeDefinition(
                "DOUBLE PRECISION", false));
        fieldTypeMapping.put(BigDecimal.class, new FieldTypeDefinition(
                "DOUBLE PRECISION", false));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition(
                "DOUBLE PRECISION", false));
        fieldTypeMapping.put(String.class, new FieldTypeDefinition("CHAR"));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR"));
        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("LONG BYTE",
                false));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition(
                "LONG VARCHAR", false));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("LONG BYTE",
                false));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition(
                "LONG VARCHAR", false));
        fieldTypeMapping.put(Blob.class, new FieldTypeDefinition("LONG BYTE",
                false));
        fieldTypeMapping.put(Clob.class, new FieldTypeDefinition(
                "LONG VARCHAR", false));
        fieldTypeMapping
                .put(Date.class, new FieldTypeDefinition("DATE", false));
        fieldTypeMapping
                .put(Time.class, new FieldTypeDefinition("TIME", false));
        fieldTypeMapping.put(Timestamp.class, new FieldTypeDefinition("STAMP",
                false));
        return fieldTypeMapping;
    }


/**************
 *  @Override
 *  NOTE CHANGED FUNCTIONALITY SINCE ESSENTIALS, WILL REQUIRE ALTERNATE IMPLEMENTATION FOR ECLIPSELINK
***************/
    public final ValueReadQuery buildSelectQueryForNativeSequence(
            final String sequenceName, final Integer size) {
        return new ValueReadQuery("SELECT "
                + this.getQualifiedSequenceName(sequenceName)
                + ".NEXTVAL FROM DUAL");
    }

    @Override
    protected final String getCreateTempTableSqlBodyForTable(
            final DatabaseTable table) {
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
        return this.getTableQualifier().length() == 0 ? sequenceName : this
                .getTableQualifier()
                + "." + sequenceName;
    }

    @Override
    public final DatabaseTable getTempTableForTable(final DatabaseTable table) {
        return new DatabaseTable("$" + table.getName(), "TEMP");
    }

    @Override
    protected final void initializePlatformOperators() {
        super.initializePlatformOperators();
        this.addOperator(MaxDBPlatform.createConcatExpressionOperator());
        this.addOperator(MaxDBPlatform.createTrim2ExpressionOperator());
        this.addOperator(MaxDBPlatform.createEqualOuterJoinOperator());
    }

/**************
 *  @Override
 *  This will only qualify as an override when isMaxDB appears in DatabasePlatform
***************/
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
        return true;
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
    public final boolean supportsStoredFunctions() {
        return true;
    }

}
