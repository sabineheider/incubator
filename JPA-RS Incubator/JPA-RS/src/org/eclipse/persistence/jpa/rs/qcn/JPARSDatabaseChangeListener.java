/*******************************************************************************
 * Copyright (c) 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 		dclarke/tware - initial 
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs.qcn;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.persistence.descriptors.CacheIndex;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.internal.identitymaps.CacheId;
import org.eclipse.persistence.internal.identitymaps.CacheKey;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.platform.database.oracle.dcn.OracleChangeNotificationListener;
import org.eclipse.persistence.queries.ReadObjectQuery;

import oracle.jdbc.dcn.DatabaseChangeEvent;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.RowChangeDescription;
import oracle.jdbc.dcn.TableChangeDescription;
import oracle.jdbc.dcn.RowChangeDescription.RowOperation;

/**
 * TODO
 * 
 * @author tware
 * @since EclipseLink 2.4.0
 */
public class JPARSDatabaseChangeListener implements DatabaseChangeListener {

    private JPARSChangeNotificationListener owner = null;
    private AbstractSession session = null;
    
    public JPARSDatabaseChangeListener(JPARSChangeNotificationListener owner, AbstractSession session){
        this.owner = owner;
        this.session = session;
    }
    
    public void onDatabaseChangeNotification(DatabaseChangeEvent changeEvent) {
        session.log(SessionLog.FINEST, SessionLog.CONNECTION, "dcn_change_event", changeEvent);
        if (changeEvent.getTableChangeDescription() != null) {
            final List<DatabaseField> fields = new ArrayList<DatabaseField>();
            fields.add(new DatabaseField(OracleChangeNotificationListener.ROWID));
            for (TableChangeDescription tableChange : changeEvent.getTableChangeDescription()) {
                ClassDescriptor descriptor = owner.getDescriptorsByTable().get(new DatabaseTable(tableChange.getTableName()));
                if (descriptor != null) {
                    CacheIndex index = descriptor.getCachePolicy().getCacheIndex(fields);
                    for (RowChangeDescription rowChange : tableChange.getRowChangeDescription()) {
                        if (rowChange.getRowOperation().equals(RowOperation.INSERT) || rowChange.getRowOperation().equals(RowOperation.UPDATE)){
                            CacheId id = new CacheId(new Object[]{rowChange.getRowid().stringValue()});
                            CacheKey key = session.getIdentityMapAccessorInstance().getIdentityMapManager().getCacheKeyByIndex(
                                    index, id, true, descriptor);
                            Object updatedObject = null;
                            if ((key == null) || (key.getTransactionId() == null) || !key.getTransactionId().equals(changeEvent.getTransactionId(true))) {
                                ExpressionBuilder builder = new ExpressionBuilder();
                                Expression expression = builder.getField(OracleChangeNotificationListener.ROWID).equal(rowChange.getRowid());
                                ReadObjectQuery query = new ReadObjectQuery(descriptor.getJavaClass(), expression);

                                updatedObject = session.executeQuery(query);
                           } else {
                               updatedObject = key.getObject();
                           }
                            if (rowChange.getRowOperation().equals(RowOperation.INSERT)){
                                owner.objectInserted(updatedObject);
                            } else {
                                owner.objectUpdated(updatedObject);
                            }
                        }
                    }
                }
                
            }
            
        }
    }
}
