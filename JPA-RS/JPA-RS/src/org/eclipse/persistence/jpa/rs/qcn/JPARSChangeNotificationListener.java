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
 * 		tware - initial 
 ******************************************************************************/
package org.eclipse.persistence.jpa.rs.qcn;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import oracle.jdbc.OracleStatement;
import oracle.jdbc.driver.OracleConnection;

import org.eclipse.persistence.annotations.DatabaseChangeNotificationType;
import org.eclipse.persistence.descriptors.CacheIndex;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.expressions.SQLSelectStatement;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.internal.identitymaps.CacheId;
import org.eclipse.persistence.internal.identitymaps.CacheKey;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.queries.ValueReadQuery;
import org.eclipse.persistence.sessions.Session;

import org.eclipse.persistence.jpa.rs.util.ChangeListener;

/**
 * TODO
 * 
 * @author tware
 * @since EclipseLink 2.4.0
 */
public class JPARSChangeNotificationListener extends  org.eclipse.persistence.platform.database.oracle.dcn.OracleChangeNotificationListener {
    
	public Set<ChangeListener> changeListeners = null;
	
    public JPARSChangeNotificationListener() {
        super();
        changeListeners = new HashSet<ChangeListener>();
    }
    
    public void addChangeListener(ChangeListener changeListener){
    	changeListeners.add(changeListener);
    }
    
    public Set<ChangeListener> getChangeListeners(){
    	return changeListeners;
    }
    
    public void remove(Session session) {
        super.remove(session);
    }
    
    /**
     * INTERNAL:
     * Register the event listener with the database.
     */
    public void register(Session session) {
        final AbstractSession databaseSession = (AbstractSession)session;
        // Determine which tables should be tracked for change events.
        this.descriptorsByTable = new HashMap<DatabaseTable, ClassDescriptor>();
        for (ClassDescriptor descriptor : session.getDescriptors().values()) {
            if (!descriptor.getTables().isEmpty()) {
                if ((descriptor.getCachePolicy().getDatabaseChangeNotificationType() != null)
                            && (descriptor.getCachePolicy().getDatabaseChangeNotificationType() != DatabaseChangeNotificationType.NONE)) {
                    this.descriptorsByTable.put(descriptor.getTables().get(0), descriptor);
                }
            }
        }
        Accessor accessor = databaseSession.getAccessor();
        accessor.incrementCallCount(databaseSession);
        try {
            OracleConnection connection = (OracleConnection)databaseSession.getServerPlatform().unwrapConnection(accessor.getConnection());
            databaseSession.log(SessionLog.FINEST, SessionLog.CONNECTION, "dcn_registering");
            Properties properties = new Properties();
            properties.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, "true");
            properties.setProperty(OracleConnection.DCN_IGNORE_INSERTOP, "false");
            try {
                // Register with the database change notification, the connection is not relevant, the events occur after the connection is closed,
                // and a different connection can be used to unregister the event listener.
                this.register = connection.registerDatabaseChangeNotification(properties);
                final List<DatabaseField> fields = new ArrayList<DatabaseField>();
                fields.add(new DatabaseField(ROWID));
                this.register.addListener(new JPARSDatabaseChangeListener(this, databaseSession));
                // Register each table for database events, this is done by executing a select from the table.
                for (DatabaseTable table : this.descriptorsByTable.keySet()) {
                    OracleStatement statement = (OracleStatement)connection.createStatement();
                    statement.setDatabaseChangeRegistration(this.register);                
                    try {
                        statement.executeQuery("SELECT ROWID FROM " + table.getQualifiedName()).close();
                        databaseSession.log(SessionLog.FINEST, SessionLog.CONNECTION, "dcn_register_table", table.getQualifiedName());
                    } catch (Exception failed) {
                        // This will fail if the table does not exist,
                        // just log the error to allow table creation to work.
                        databaseSession.logThrowable(SessionLog.WARNING, SessionLog.SQL, failed);
                    } finally {
                        statement.close();
                    }
                }
            } catch (SQLException exception) {
                throw DatabaseException.sqlException(exception, databaseSession.getAccessor(), databaseSession, false);
            }
        } finally {
            accessor.decrementCallCount();
        }
    }
    
    /**
     * Initialize the descriptor to receive database change events.
     * This is called when the descriptor is initialized.
     */
    public void initialize(final ClassDescriptor descriptor, AbstractSession session) {
        if (descriptor.getOptimisticLockingPolicy() == null) {
            boolean requiresLocking = descriptor.hasMultipleTables();
            for (DatabaseMapping mapping : descriptor.getMappings()) {
                if (mapping.isCollectionMapping()) {
                    requiresLocking = true;
                }
            }
            if (requiresLocking) {
                session.log(SessionLog.WARNING, SessionLog.EJB_OR_METADATA, "locking_required_for_database_change_notification", descriptor.getJavaClass());
            }
        }
        final DatabaseField rowId = descriptor.buildField(new DatabaseField(ROWID));
        final List<DatabaseField> fields = new ArrayList<DatabaseField>();
        fields.add(rowId);
        // May already have the index if has inheritance.
        CacheIndex existingIndex = descriptor.getCachePolicy().getCacheIndex(fields);
        if (existingIndex == null) {
            if (descriptor.isChildDescriptor()) {
                existingIndex = descriptor.getInheritancePolicy().getRootParentDescriptor().getCachePolicy().getCacheIndex(fields);
            }
            if (existingIndex == null) {
                existingIndex = new CacheIndex(fields);
                existingIndex.setIsUpdatable(false);
                existingIndex.setIsInsertable(false);
            }
            descriptor.getCachePolicy().addCacheIndex(existingIndex);
        }
        
        final CacheIndex index = existingIndex;
        rowId.setInsertable(false);
        rowId.setUpdatable(false);
        rowId.setCreatable(false);
        descriptor.getFields().add(rowId);
        descriptor.getAllFields().add(rowId);
        
        final ValueReadQuery rowIdQuery = new ValueReadQuery();
        rowIdQuery.setName(ROWID);
        SQLSelectStatement sqlStatement = new SQLSelectStatement();
        sqlStatement.setWhereClause(descriptor.getObjectBuilder().getPrimaryKeyExpression());
        sqlStatement.addField(rowId);
        sqlStatement.addTable(descriptor.getTables().get(0));
        rowIdQuery.setSQLStatement(sqlStatement);
        sqlStatement.normalize(session, null);
        
        descriptor.getEventManager().addListener(new DescriptorEventAdapter() {
            @Override
            public void postMerge(DescriptorEvent event) {
                if ((event.getChangeSet() != null) && event.getChangeSet().hasChanges()) {
                    Object id = event.getChangeSet().getId();
                    CacheKey cacheKey = event.getChangeSet().getActiveCacheKey();
                    if (cacheKey == null) {
                        cacheKey = event.getSession().getParent().getIdentityMapAccessorInstance().getIdentityMapManager().getCacheKeyForObject(id, descriptor.getJavaClass(), descriptor, false);
                     }
                    cacheKey.setTransactionId(event.getSession().getProperty(ORA_TRANSACTION_ID));
                    if (event.getChangeSet().isNew()) {
                        AbstractRecord row = descriptor.getObjectBuilder().buildRowFromPrimaryKeyValues(id, event.getSession());
                        Object rowid = event.getSession().executeQuery(rowIdQuery, row);
                        CacheId indexValue = new CacheId(new Object[]{rowid});
                        event.getSession().getParent().getIdentityMapAccessorInstance().getIdentityMapManager().putCacheKeyByIndex(index, indexValue, cacheKey, descriptor);
                    }
                }
            }
            @Override
            public void postUpdate(DescriptorEvent event) {
                Object txId = event.getSession().getProperty(ORA_TRANSACTION_ID);
                if (txId == null) {
                    txId = event.getSession().executeQuery(transactionIdQuery);
                    event.getSession().setProperty(ORA_TRANSACTION_ID, txId);
                }
            }
            
            @Override
            public void postInsert(DescriptorEvent event) {
                Object txId = event.getSession().getProperty(ORA_TRANSACTION_ID);
                if (txId == null) {
                    txId = event.getSession().executeQuery(transactionIdQuery);
                    event.getSession().setProperty(ORA_TRANSACTION_ID, txId);
                }
            }
        });
    }
    
    
    public void removeChangeListener(ChangeListener changeListener){
    	changeListeners.remove(changeListener);
    }
    
    public void objectUpdated(Object object){
    	for (ChangeListener listener: changeListeners){
    		listener.objectUpdated(object);
    	}
    }
    
    public void objectInserted(Object object){
    	for (ChangeListener listener: changeListeners){
        	listener.objectInserted(object);
    	}
    }

}
