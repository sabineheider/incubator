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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.indirection.IndirectList;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.eclipse.persistence.internal.jpa.CMP3Policy;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.ManyToManyMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;

public class SimpleDynamicMap_WithRelationships_JPAExample {

    /**
     * 
     */
    public static void main(String[] args) {
        SimpleDynamicMap_WithRelationships_JPAExample example = new SimpleDynamicMap_WithRelationships_JPAExample();

        EntityManagerFactory emf = example.createEMF();

        try {
            example.createDynamicTypes(emf);

            example.persistDynamicInstances(emf);
            example.queryDynamicInstances(emf);
            example.updateDyanmicInstances(emf);
            example.deleteDynamicInstances(emf);

            example.removeDynamicTypes(emf);

        } finally {
            emf.close();
        }
    }

    public static final String TYPE_A = "SimpleTypeA";
    public static final String TYPE_B = "SimpleTypeB";
    public static final String TYPE_C = "SimpleTypeC";

    /**
     * Create the following dynamic types
     * 
     * SimpleTypeA -> DYNAMIC_A int id -> A_ID (PK) String value -> VALUE B bs
     * (1:M)) mappedBy B.a List<C> cs (M:M)-> A_C_JOIN(A_ID, C_ID)
     * 
     * SimpleTypeB -> DYNAMIC_B int id -> B_ID (PK) A a (M:1) -> A_FK (FK)
     * Calendar value -> VALUE
     * 
     * SimpleTypeC -> DYNAMIC_C int id -> C_ID byte[] value -> LOB
     */
    public void createDynamicTypes(EntityManagerFactory emf) {
        ServerSession session = (ServerSession) JpaHelper.getServerSession(emf);
        DynamicConversionManager dcm = DynamicConversionManager.getDynamicConversionManager(session);

        // Create SimpleTypeA with direct mappings
        Class javaClassA = dcm.createDynamicClass("model." + TYPE_A);
        RelationalDescriptor descriptorA = new RelationalDescriptor();
        descriptorA.setJavaClass(javaClassA);
        descriptorA.setTableName("DYNAMIC_A");
        descriptorA.setPrimaryKeyFieldName("A_ID");
        descriptorA.setCMPPolicy(new CMP3Policy());
        DirectToFieldMapping mapping = (DirectToFieldMapping) descriptorA.addDirectMapping("id", "A_ID");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, Integer.class));
        mapping = (DirectToFieldMapping) descriptorA.addDirectMapping("value", "VALUE");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, String.class));

        // Create SimpleTypeB with direct mappings
        Class javaClassB = dcm.createDynamicClass("model." + TYPE_B);
        RelationalDescriptor descriptorB = new RelationalDescriptor();
        descriptorB.setJavaClass(javaClassB);
        descriptorB.setTableName("DYNAMIC_B");
        descriptorB.setPrimaryKeyFieldName("B_ID");
        descriptorB.setCMPPolicy(new CMP3Policy());
        mapping = (DirectToFieldMapping) descriptorB.addDirectMapping("id", "B_ID");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, Integer.class));
        mapping = (DirectToFieldMapping) descriptorB.addDirectMapping("value", "VALUE");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, Calendar.class));

        // Create SimpleTypeC with direct mappings
        Class javaClassC = dcm.createDynamicClass("model." + TYPE_C);
        RelationalDescriptor descriptorC = new RelationalDescriptor();
        descriptorC.setJavaClass(javaClassC);
        descriptorC.setTableName("DYNAMIC_C");
        descriptorC.setPrimaryKeyFieldName("C_ID");
        descriptorC.setCMPPolicy(new CMP3Policy());
        mapping = (DirectToFieldMapping) descriptorC.addDirectMapping("id", "C_ID");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, Integer.class));
        mapping = (DirectToFieldMapping) descriptorC.addDirectMapping("value", "VALUE");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, byte[].class));

        // Add 1:M: A.bs
        OneToManyMapping aToBMapping = new OneToManyMapping();
        aToBMapping.setAttributeName("bs");
        aToBMapping.setReferenceClass(descriptorB.getJavaClass());
        aToBMapping.useTransparentList();
        aToBMapping.setCascadeAll(true);
        aToBMapping.setAttributeAccessor(new ValueAccessor(aToBMapping, IndirectList.class));
        aToBMapping.addTargetForeignKeyFieldName("A_FK", "A_ID");
        descriptorA.addMapping(aToBMapping);

        // Add M:M: A.cs
        ManyToManyMapping aToCMapping = new ManyToManyMapping();
        aToCMapping.setAttributeName("cs");
        aToCMapping.setReferenceClass(descriptorC.getJavaClass());
        aToCMapping.useTransparentList();
        aToCMapping.setCascadeAll(true);
        aToCMapping.setAttributeAccessor(new ValueAccessor(aToCMapping, IndirectList.class));
        aToCMapping.setRelationTableName("DYNAMIC_JOIN_A_C");
        aToCMapping.addSourceRelationKeyFieldName("A_ID", "A_ID");
        aToCMapping.addTargetRelationKeyFieldName("C_ID", "C_ID");
        descriptorA.addMapping(aToCMapping);

        // Add 1:1: B.a (fetch=EAGER)
        OneToOneMapping bToAMapping = new OneToOneMapping();
        bToAMapping.setAttributeName("a");
        bToAMapping.setReferenceClass(descriptorA.getJavaClass());
        bToAMapping.dontUseIndirection();
        bToAMapping.setAttributeAccessor(new ValueAccessor(bToAMapping, descriptorA.getJavaClass()));
        bToAMapping.addForeignKeyFieldName("A_FK", "A_ID");
        descriptorB.addMapping(bToAMapping);

        List<ClassDescriptor> descriptors = new ArrayList<ClassDescriptor>();
        descriptors.add(descriptorA);
        descriptors.add(descriptorB);
        descriptors.add(descriptorC);
        session.addDescriptors(descriptors);

        createTables(emf);
    }

    /**
     * Replace all of the tables.
     * 
     * @param emf
     */
    private void createTables(EntityManagerFactory emf) {
        SchemaManager sm = new SchemaManager(JpaHelper.getServerSession(emf));

        try {
            sm.dropTable(DynamicMapHelper.getDescriptor(emf, TYPE_B).getTableName());
            sm.dropTable(((ManyToManyMapping) DynamicMapHelper.getDescriptor(emf, TYPE_A).getMappingForAttributeName("cs")).getRelationTableName());
            sm.dropTable(DynamicMapHelper.getDescriptor(emf, TYPE_A).getTableName());
            sm.dropTable(DynamicMapHelper.getDescriptor(emf, TYPE_C).getTableName());
        } catch (DatabaseException dbe) {

        }

        sm.createDefaultTables(false);
    }

    public void persistDynamicInstances(EntityManagerFactory emf) {
        System.out.println("*** START: SimpleDynamicMap_WithRelationships_JPAExample.persistDynamicInstances ***");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        Map entityA = DynamicMapHelper.newInstance(emf, TYPE_A);
        entityA.put("id", 1);
        entityA.put("value", "value-1");

        Map entityB = DynamicMapHelper.newInstance(emf, TYPE_B);
        entityB.put("id", 1);
        entityB.put("a", entityA);
        entityB.put("value", Calendar.getInstance());

        // Add entityB to A's 1:M
        List bs = new ArrayList();
        bs.add(entityB);
        entityA.put("bs", bs);

        Map entityC1 = DynamicMapHelper.newInstance(emf, TYPE_C);
        entityC1.put("id", 1);
        entityC1.put("value", "TEST DATA".getBytes());
        Map entityC2 = DynamicMapHelper.newInstance(emf, TYPE_C);
        entityC2.put("id", 2);
        entityC2.put("value", "TEST DATA FOR C2".getBytes());

        // Add entityB to A's 1:M
        List cs = new ArrayList();
        cs.add(entityC1);
        cs.add(entityC2);
        entityA.put("cs", cs);

        em.persist(entityC1);
        em.persist(entityC2);
        em.persist(entityB);
        em.persist(entityA);

        em.getTransaction().commit();
        em.close();

        JpaHelper.getServerSession(emf).getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    public List<Map> queryDynamicInstances(EntityManagerFactory emf) {
        System.out.println("*** START: SimpleDynamicMap_WithRelationships_JPAExample.queryDynamicInstances ***");
        EntityManager em = emf.createEntityManager();

        try {
            List<Map> as = em.createQuery("SELECT s FROM SimpleTypeA s WHERE s.value LIKE 'v%'").getResultList();

            return as;
        } finally {
            em.close();
        }
    }

    public void updateDyanmicInstances(EntityManagerFactory emf) {
        System.out.println("*** START: SimpleDynamicMap_WithRelationships_JPAExample.updateDyanmicInstances ***");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        Map entityA = (Map) em.find(DynamicMapHelper.getClass(emf, TYPE_A), 1);
        entityA.put("value", "value-1+");

        Map entityB = DynamicMapHelper.newInstance(emf, TYPE_B);
        entityB.put("id", 2);
        entityB.put("a", entityA);
        entityB.put("value", Calendar.getInstance());

        em.persist(entityB);

        List<Map<String, Object>> bs = (List<Map<String, Object>>) entityA.get("bs");
        bs.add(entityB);
        entityB.put("a", entityA);

        em.getTransaction().commit();
        em.close();
    }

    public void deleteDynamicInstances(EntityManagerFactory emf) {
        System.out.println("*** START: SimpleDynamicMap_WithRelationships_JPAExample.deleteDynamicInstances ***");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        Map entityA = (Map) em.find(DynamicMapHelper.getClass(emf, TYPE_A), 1);
        em.remove(entityA);

        em.getTransaction().commit();
        em.close();
    }

    /**
     * Remove the persistent type from the meta-model and clear the cache.
     * 
     * Application should ensure there are no operations active against the
     * persistence context using this type when it is being removed.
     */
    public void removeDynamicTypes(EntityManagerFactory emf) {
        Server session = JpaHelper.getServerSession(emf);

        Class classA = DynamicMapHelper.getClass(emf, TYPE_A);
        Class classB = DynamicMapHelper.getClass(emf, TYPE_B);
        Class classC = DynamicMapHelper.getClass(emf, TYPE_C);

        // Must clear the cache first
        session.getIdentityMapAccessor().initializeIdentityMap(classA);
        session.getIdentityMapAccessor().initializeIdentityMap(classB);
        session.getIdentityMapAccessor().initializeIdentityMap(classC);

        session.getDescriptors().remove(classA);
        session.getProject().getOrderedDescriptors().remove(session.getProject().getAliasDescriptors().remove(TYPE_A));

        session.getDescriptors().remove(classB);
        session.getProject().getOrderedDescriptors().remove(session.getProject().getAliasDescriptors().remove(TYPE_B));

        session.getDescriptors().remove(classC);
        session.getProject().getOrderedDescriptors().remove(session.getProject().getAliasDescriptors().remove(TYPE_C));
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
    public EntityManagerFactory createEMF() {
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

        return Persistence.createEntityManagerFactory("dynamic-simple-map", testProps);
    }
}
