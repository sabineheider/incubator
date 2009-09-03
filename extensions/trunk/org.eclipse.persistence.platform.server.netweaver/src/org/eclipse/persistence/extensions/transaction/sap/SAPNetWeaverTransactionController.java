package org.eclipse.persistence.transaction.sap;

import javax.transaction.TransactionManager;

import org.eclipse.persistence.transaction.JTATransactionController;

public class SAPNetWeaverTransactionController extends JTATransactionController {
    public static final String JNDI_TRANSACTION_MANAGER_NAME = "TransactionManager";

    @Override
    protected TransactionManager acquireTransactionManager() throws Exception {
        return (TransactionManager)jndiLookup(JNDI_TRANSACTION_MANAGER_NAME);
    }
}