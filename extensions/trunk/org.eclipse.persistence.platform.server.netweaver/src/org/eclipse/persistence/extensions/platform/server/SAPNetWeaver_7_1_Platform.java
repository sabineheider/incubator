+package org.eclipse.persistence.platform.server.sap;
+
+import javax.persistence.spi.PersistenceUnitInfo;
+
+import org.eclipse.persistence.internal.helper.JPAClassLoaderHolder;
+import org.eclipse.persistence.logging.AbstractSessionLog;
+import org.eclipse.persistence.platform.server.ServerPlatformBase;
+import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.transaction.sap.SAPNetWeaverTransactionController;

public class SAPNetWeaver_7_1_Platform extends ServerPlatformBase {

    private static final boolean NO_TEMP_CLASS_LOADER = false;

    public SAPNetWeaver_7_1_Platform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
    }

    @Override
    public Class getExternalTransactionControllerClass() {
        if (externalTransactionControllerClass == null){
            externalTransactionControllerClass = SAPNetWeaverTransactionController.class;
        }
        return externalTransactionControllerClass;
    }

    @Override
    public String getServerNameAndVersion() {
        String version = System.getProperty("SAP_J2EE_Engine_Version");
        if (version != null) {
            return version;
        }
        return super.getServerNameAndVersion();
    }

    @Override
    /**
     * SAP NetWeaver does not support dynamic byte code weaving. We return the original class loader
     * in order to prevent dynamic weaving. 
     */
    public JPAClassLoaderHolder getNewTempClassLoader(PersistenceUnitInfo puInfo) {
        ClassLoader realClassLoader = puInfo.getClassLoader();
        AbstractSessionLog.getLog().log(AbstractSessionLog.WARNING, "persistence_unit_processor_sap_temp_classloader_bypassed",//
                puInfo.getPersistenceUnitName(), realClassLoader);
        return new JPAClassLoaderHolder(realClassLoader, NO_TEMP_CLASS_LOADER);
    }    
}
