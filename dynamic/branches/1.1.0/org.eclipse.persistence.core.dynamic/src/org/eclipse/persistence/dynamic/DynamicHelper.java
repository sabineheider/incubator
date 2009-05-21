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
 *     dclarke - Dynamic Persistence INCUBATION - Enhancement 200045
 *     			 http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.dynamic;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.helper.*;
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.sequencing.*;
import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.tools.schemaframework.FieldDefinition;
import org.eclipse.persistence.tools.schemaframework.TableDefinition;

/**
 * A DynamicSessionFactory is the factory to bootstrap a session that will use
 * dynamic entity types as well as being a helper class for using these types.
 * The standard TopLink interface has many dependencies on the concrete class.
 * These concrete classes will not be available for the application developer
 * and instead they must use aliases or assumed String class names. This factory
 * will assist in the writing of TopLink enabled applications.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator
 */
public class DynamicHelper {

    /**
     * Lookup the dynamic type for an alias. This is required to get the type
     * for factory creation but can also be used to provide the application with
     * access to the meta model (type and properties) allowing for dynamic use
     * as well as optimized data value retrieval from an entity.
     * 
     * @param typeName
     * @return
     */
    public static EntityType getType(DatabaseSession session, String typeName) {
        if (session == null) {
            throw new IllegalArgumentException("No session provided");
        }

        EntityType type = null;
        try {
            ClassDescriptor cd = session.getClassDescriptorForAlias(typeName);
            type = getType(cd);
        } catch (NullPointerException e) { // Workaround for bug ???
            throw DynamicEntityException.invalidTypeName(typeName);
        }
        if (type == null) {
            throw DynamicEntityException.invalidTypeName(typeName);
        }

        return type;
    }

    public static EntityType getType(ClassDescriptor descriptor) {
        return EntityTypeImpl.getType(descriptor);
    }

    public static boolean isDynamicType(ClassDescriptor descriptor) {
        return EntityTypeImpl.isDynamicType(descriptor);
    }

    /**
     * Remove a dynamic type from the system.
     * 
     * This implementation assumes that the dynamic type has no relationships to
     * it and that it is not involved in an inheritance relationship. If there
     * are concurrent processes using this type when it is removed some
     * exceptions may occur.
     * 
     * @param session
     * @param typeName
     */
    public static void removeType(DatabaseSession session, String typeName) {
        EntityType type = getType(session, typeName);

        if (type != null) {
            session.getIdentityMapAccessor().initializeIdentityMap(type.getJavaClass());
            session.getProject().getOrderedDescriptors().remove(type.getDescriptor());
            session.getProject().getDescriptors().remove(type.getJavaClass());
        }
    }

    public static class Configure implements SessionCustomizer {

        public void customize(Session session) throws Exception {
            for (Iterator i = session.getProject().getDescriptors().values().iterator(); i.hasNext();) {
                ClassDescriptor desc = (ClassDescriptor) i.next();
                EntityTypeImpl.getType(desc);
            }

        }

    }

    /**
     * 
     * @param type
     * @param session
     */
    public static TableDefinition createTable(EntityType type, Session session) {
        ClassDescriptor desc = type.getDescriptor();
        TableDefinition tblDef = new TableDefinition();
        DatabaseTable dbTbl = desc.getTables().get(0);
        tblDef.setName(dbTbl.getName());
        tblDef.setQualifier(dbTbl.getTableQualifier());

        // build each field definition and figure out which table it goes
        Iterator fieldIter = desc.getFields().iterator();
        DatabaseField dbField = null;

        while (fieldIter.hasNext()) {
            dbField = (DatabaseField) fieldIter.next();

            boolean isPKField = false;

            // first check if the filed is a pk field in the default table.
            isPKField = desc.getPrimaryKeyFields().contains(dbField);

            // then check if the field is a pk field in the secondary table(s),
            // this is only applied to the multiple tables case.
            Map secondaryKeyMap = desc.getAdditionalTablePrimaryKeyFields().get(dbField.getTable());

            if (secondaryKeyMap != null) {
                isPKField = isPKField || secondaryKeyMap.containsValue(dbField);
            }

            // build or retrieve the field definition.
            FieldDefinition fieldDef = getFieldDefFromDBField(dbField, isPKField, session);
            if (isPKField) {
                // Check if the generation strategy is IDENTITY
                String sequenceName = desc.getSequenceNumberName();
                DatabaseLogin login = session.getProject().getLogin();
                Sequence seq = login.getSequence(sequenceName);
                if (seq instanceof DefaultSequence) {
                    seq = login.getDefaultSequence();
                }
                // The native sequence whose value should be aquired after
                // insert is identity sequence
                boolean isIdentity = seq instanceof NativeSequence && seq.shouldAcquireValueAfterInsert();
                fieldDef.setIsIdentity(isIdentity);
            }

            if (!tblDef.getFields().contains(fieldDef)) {
                tblDef.addField(fieldDef);
            }
        }

        return tblDef;
    }

    /**
     * Build a field definition object from a database field.
     */
    private static FieldDefinition getFieldDefFromDBField(DatabaseField dbField, boolean isPrimaryKey, Session session) {
        FieldDefinition fieldDef = new FieldDefinition();
        fieldDef.setName(dbField.getName());

        if (dbField.getColumnDefinition() != null && dbField.getColumnDefinition().length() > 0) {
            // This column definition would include the complete definition of
            // the
            // column like type, size, "NULL/NOT NULL" clause, unique key clause
            fieldDef.setTypeDefinition(dbField.getColumnDefinition());
        } else {
            Class fieldType = dbField.getType();

            // Check if the user field is a String and only then allow the
            // length specified
            // in the @Column annotation to be set on the field.
            if ((fieldType != null)) {
                if (fieldType.equals(ClassConstants.STRING) || fieldType.equals(ClassConstants.APCHAR) || fieldType.equals(ClassConstants.ACHAR)) {
                    // The field size is defaulted to "255" or use the user
                    // supplied length
                    fieldDef.setSize(dbField.getLength());
                } else {
                    if (dbField.getPrecision() > 0) {
                        fieldDef.setSize(dbField.getPrecision());
                        fieldDef.setSubSize(dbField.getScale());
                    }
                }
            }

            if ((fieldType == null) || (!fieldType.isPrimitive() && (session.getPlatform().getFieldTypeDefinition(fieldType) == null))) {
                // TODO: log a warning for inaccessiable type or not convertable
                // type.
                AbstractSessionLog.getLog().log(SessionLog.FINEST, "field_type_set_to_java_lang_string", dbField.getQualifiedName(), fieldType);

                // set the default type (lang.String) to all un-resolved java
                // type, like null, Number, util.Date, NChar/NType, Calendar
                // sql.Blob/Clob, Object, or unknown type). Please refer to bug
                // 4352820.
                fieldDef.setType(ClassConstants.STRING);
            } else {
                // need to convert the primitive type if applied.
                fieldDef.setType(ConversionManager.getObjectClass(fieldType));
            }

            fieldDef.setShouldAllowNull(dbField.isNullable());
            fieldDef.setUnique(dbField.isUnique());
        }

        fieldDef.setIsPrimaryKey(isPrimaryKey);

        return fieldDef;
    }

}
