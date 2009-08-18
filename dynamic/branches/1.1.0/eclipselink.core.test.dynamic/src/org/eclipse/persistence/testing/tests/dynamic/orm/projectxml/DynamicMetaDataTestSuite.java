package org.eclipse.persistence.testing.tests.dynamic.orm.projectxml;

//javase imports
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Date;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.sessions.factories.MissingDescriptorListener;
import org.eclipse.persistence.internal.sessions.factories.ObjectPersistenceWorkbenchXMLProject;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLUnmarshaller;
import org.eclipse.persistence.platform.database.oracle.Oracle11Platform;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.junit.BeforeClass;
import org.junit.Test;

public class DynamicMetaDataTestSuite {

    // JUnit static fixtures
    static Project p;
    static DatabaseSession ds;

    @SuppressWarnings({ "unchecked", "deprecation" })
    @BeforeClass
    public static void setUp() throws Exception {
        final String username = "scott";
        final String password = "tiger";
        final String url = "jdbc:oracle:thin:@localhost:1521:ORCL";

        ObjectPersistenceWorkbenchXMLProject runtimeProject = new ObjectPersistenceWorkbenchXMLProject();

        XMLContext context = new XMLContext(runtimeProject);
        Session opmSession = context.getSession(runtimeProject);
        opmSession.getEventManager().addListener(new MissingDescriptorListener());
        XMLUnmarshaller unmarshaller = context.createUnmarshaller();

        DynamicClassLoader dcl = DynamicClassLoader.lookup(null);

        InputStream in = dcl.getResourceAsStream("org/eclipse/persistence/testing/tests/dynamic/orm/projectxml/simple-map-project.xml");
        p = (Project) unmarshaller.unmarshal(in);
        in.close();

        for (Iterator<ClassDescriptor> i = p.getDescriptors().values().iterator(); i.hasNext();) {
            ClassDescriptor desc = i.next();
            if (desc.getJavaClass() == null) {
                new EntityTypeBuilder(dcl, desc, null);
            }
        }

        p.convertClassNamesToClasses(dcl);

        DatabaseLogin login = new DatabaseLogin();
        login.setUserName(username);
        login.setPassword(password);
        login.setConnectionString(url);
        login.setDriverClassName("oracle.jdbc.OracleDriver");
        login.setDatasourcePlatform(new Oracle11Platform());
        login.bindAllParameters();
        login.getDatasourcePlatform().getConversionManager().setLoader(dcl);
        p.setDatasourceLogin(login);

        ds = p.createDatabaseSession();
        ds.setLogLevel(SessionLog.FINE);
        ds.login();

        new DynamicSchemaManager(ds).createTables(new EntityType[0]);
        ds.executeNonSelectingSQL("DELETE FROM SIMPLETABLE");

        EntityType type = DynamicHelper.getType(ds, "simpletableType");

        DynamicEntity entity = type.newInstance();
        entity.set("id", new BigInteger("1"));
        entity.set("name", "Doug");
        entity.set("since", new Date(100, 06, 06));

        ds.writeObject(entity);
    }

    @Test
    public void readAll() {
        EntityType type = DynamicHelper.getType(ds, "simpletableType");
        
        Vector<Object> allObjects = ds.readAllObjects(type.getJavaClass());
        for (Object o : allObjects) {
            System.out.println(o);
        }
        
        System.identityHashCode(allObjects);
    }

}