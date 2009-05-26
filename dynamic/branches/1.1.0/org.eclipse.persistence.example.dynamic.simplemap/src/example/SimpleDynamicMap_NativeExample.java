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
 *     dclarke - SimpleDynamicMap Example - Bug 277731
 *               http://wiki.eclipse.org/EclipseLink/Examples/JPA/Dynamic/SimpleDynamicMap
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package example;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;


public class SimpleDynamicMap_NativeExample {

    /**
     * 
     */
    public static void main(String[] args) {
        SimpleDynamicMap_NativeExample example = new SimpleDynamicMap_NativeExample();

        DatabaseSession session = example.createSession();

        ClassDescriptor descriptor = example.createDynamicType(session);

        example.persistDynamicInstances(session, descriptor);
        example.queryDynamicInstances(session, descriptor);
        example.updateDyanmicInstances(session, descriptor);
        example.deleteDynamicInstances(session, descriptor);

        example.removeDynamicType(session, descriptor);

        try {

        } finally {
            session.logout();
        }
    }

    /**
     * Create a new dynamic type called 'SimpleType' with the class name of
     * 'model.SimpleType'. The generated class would look like: <code>
     * package model;
     * public class SimpleType extends example.dynamic.DynamicEntity {}
     * </code>
     * 
     * The attributes defined in the mapping give the class the apparent
     * structure of: <code>
     * package model;
     * public class SimpleType extends example.dynamic.DynamicEntity {
     *    Integer id;
     *    String value;
     * }
     * </code>
     */
    public ClassDescriptor createDynamicType(DatabaseSession session) {
        DynamicClassLoader loader = DynamicClassLoader.getLoader(session, DynamicMapEntity.class);

        Class javaClass = loader.createDynamicClass("model.SimpleType");

        RelationalDescriptor descriptor = new RelationalDescriptor();

        descriptor.setJavaClass(javaClass);
        descriptor.setTableName("DYNAMIC_SIMPLE");
        descriptor.setPrimaryKeyFieldName("ID");

        DirectToFieldMapping mapping = (DirectToFieldMapping) descriptor.addDirectMapping("id", "ID");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, Integer.class));
        mapping = (DirectToFieldMapping) descriptor.addDirectMapping("value", "VALUE");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, String.class));

        session.addDescriptor(descriptor);

        new SchemaManager(session).replaceDefaultTables();

        return descriptor;
    }

    public Map persistDynamicInstances(DatabaseSession session, ClassDescriptor descriptor) {
        UnitOfWork uow = session.acquireUnitOfWork();

        Map entity = (Map) uow.newInstance(descriptor.getJavaClass());
        entity.put("id", 1);
        entity.put("value", "value-1");

        uow.commit();

        return entity;
    }

    public List<Map> queryDynamicInstances(DatabaseSession session, ClassDescriptor descriptor) {
        ReadAllQuery query = new ReadAllQuery(descriptor.getJavaClass());
        ExpressionBuilder eb = query.getExpressionBuilder();
        query.setSelectionCriteria(eb.get("value").like("v%"));

        return (List<Map>) session.executeQuery(query);
    }

    public Map updateDyanmicInstances(DatabaseSession session, ClassDescriptor descriptor) {
        UnitOfWork uow = session.acquireUnitOfWork();

        ReadObjectQuery query = new ReadObjectQuery(descriptor.getJavaClass());
        ExpressionBuilder eb = query.getExpressionBuilder();
        query.setSelectionCriteria(eb.get("id").equal(1));

        Map entity = (Map) uow.executeQuery(query);

        entity.put("value", "value-1+");

        uow.commit();
        
        return entity;
    }

    public void deleteDynamicInstances(DatabaseSession session, ClassDescriptor descriptor) {
        UnitOfWork uow = session.acquireUnitOfWork();

        ReadObjectQuery query = new ReadObjectQuery(descriptor.getJavaClass());
        ExpressionBuilder eb = query.getExpressionBuilder();
        query.setSelectionCriteria(eb.get("id").equal(1));

        Map entity = (Map) uow.executeQuery(query);

        uow.deleteObject(entity);

        uow.commit();
    }

    public void removeDynamicType(DatabaseSession session, ClassDescriptor descriptor) {
        session.getIdentityMapAccessor().initializeIdentityMap(descriptor.getJavaClass());

        session.getDescriptors().remove(descriptor.getJavaClass());
        session.getProject().getAliasDescriptors().remove(descriptor.getAlias());
        session.getProject().getOrderedDescriptors().remove(descriptor);

    }

    /**
     * Create a database session with no descriptors. The property values used
     * to populate the DatabaseLogin object are loaded from a 'test.properties'
     * file stored in the user's home.
     * 
     * The test.properties file could appear as:
     * 
     * eclipselink.jdbc.driver=oracle.jdbc.OracleDriver
     * eclipselink.jdbc.url=jdbc:oracle:thin:@localhost:1521:ORCL
     * eclipselink.jdbc.user=scott eclipselink.jdbc.pwd=tiger
     * eclipselink.target-
     * database=org.eclipse.persistence.platform.database.OraclePlatform
     * 
     * 
     * @return newly created and logged in Session
     */
    public DatabaseSession createSession() {
        Properties testProps = new Properties();
        FileInputStream in = null;

        try {
            in = new FileInputStream("test.properties");
            testProps.load(in);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        DatabaseLogin login = new DatabaseLogin();
        login.setDriverClassName((String) testProps.get(PersistenceUnitProperties.JDBC_DRIVER));
        login.setDatabaseURL((String) testProps.get(PersistenceUnitProperties.JDBC_URL));
        login.setUserName((String) testProps.get(PersistenceUnitProperties.JDBC_USER));
        login.setPassword((String) testProps.get(PersistenceUnitProperties.JDBC_PASSWORD));
        login.setPlatformClassName((String) testProps.get(PersistenceUnitProperties.TARGET_DATABASE));

        DatabaseSession session = new Project(login).createDatabaseSession();
        session.getSessionLog().setLevel(SessionLog.FINE);
        session.login();

        return session;
    }
}
