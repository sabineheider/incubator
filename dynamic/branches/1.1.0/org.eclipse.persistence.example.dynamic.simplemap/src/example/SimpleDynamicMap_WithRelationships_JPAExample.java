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
import org.eclipse.persistence.indirection.ValueHolder;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.jpa.CMP3Policy;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.ManyToManyMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.sessions.server.Server;
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

    private ClassDescriptor descriptorA;
    private ClassDescriptor descriptorB;
    private ClassDescriptor descriptorC;

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
        Server session = JpaHelper.getServerSession(emf);
        DynamicClassLoader loader = DynamicClassLoader.getLoader(session, DynamicMapEntity.class);

        // Create SimpleTypeA with direct mappings
        Class javaClassA = loader.createDynamicClass("model." + TYPE_A);
        descriptorA = new RelationalDescriptor();
        descriptorA.setJavaClass(javaClassA);
        descriptorA.setTableName("DYNAMIC_A");
        descriptorA.setPrimaryKeyFieldName("A_ID");
        descriptorA.setCMPPolicy(new CMP3Policy());
        DirectToFieldMapping mapping = (DirectToFieldMapping) descriptorA.addDirectMapping("id", "A_ID");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, Integer.class));
        mapping = (DirectToFieldMapping) descriptorA.addDirectMapping("value", "VALUE");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, String.class));

        // Create SimpleTypeB with direct mappings
        Class javaClassB = loader.createDynamicClass("model." + TYPE_B);
        descriptorB = new RelationalDescriptor();
        descriptorB.setJavaClass(javaClassB);
        descriptorB.setTableName("DYNAMIC_B");
        descriptorB.setPrimaryKeyFieldName("B_ID");
        descriptorB.setCMPPolicy(new CMP3Policy());
        mapping = (DirectToFieldMapping) descriptorB.addDirectMapping("id", "B_ID");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, Integer.class));
        mapping = (DirectToFieldMapping) descriptorB.addDirectMapping("a-id", "A_ID");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, Integer.class));
        mapping = (DirectToFieldMapping) descriptorB.addDirectMapping("value", "VALUE");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, Calendar.class));

        // Create SimpleTypeC with direct mappings
        Class javaClassC = loader.createDynamicClass("model." + TYPE_C);
        descriptorC = new RelationalDescriptor();
        descriptorC.setJavaClass(javaClassC);
        descriptorC.setTableName("DYNAMIC_C");
        descriptorC.setPrimaryKeyFieldName("C_ID");
        descriptorC.setCMPPolicy(new CMP3Policy());
        mapping = (DirectToFieldMapping) descriptorC.addDirectMapping("id", "C_ID");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, Integer.class));
        mapping = (DirectToFieldMapping) descriptorC.addDirectMapping("value", "VALUE");
        mapping.setAttributeAccessor(new ValueAccessor(mapping, byte[].class));

        // Add uni-directional 1:M.
        OneToManyMapping aToBMapping = new OneToManyMapping();
        aToBMapping.setAttributeName("bs");
        aToBMapping.setReferenceClass(descriptorB.getJavaClass());
        aToBMapping.useBasicIndirection();
        aToBMapping.setAttributeAccessor(new ValueAccessor(aToBMapping, IndirectList.class));
        aToBMapping.addTargetForeignKeyFieldName("A_FK", "A_ID");
        descriptorA.addMapping(aToBMapping);

        // Add M:M
        ManyToManyMapping aToCMapping = new ManyToManyMapping();
        aToCMapping.setAttributeName("cs");
        aToCMapping.setReferenceClass(descriptorC.getJavaClass());
        aToCMapping.useBasicIndirection();
        aToCMapping.setAttributeAccessor(new ValueAccessor(aToCMapping, IndirectList.class));
        aToCMapping.setRelationTableName("DYNAMIC_JOIN_A_C");
        aToCMapping.addSourceRelationKeyFieldName("A_ID", "A_ID");
        aToCMapping.addTargetRelationKeyFieldName("C_ID", "C_ID");
        descriptorA.addMapping(aToCMapping);

        session.addDescriptor(descriptorC);
        session.addDescriptor(descriptorB);
        session.addDescriptor(descriptorA);

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
            sm.dropTable(((ManyToManyMapping) descriptorA.getMappingForAttributeName("cs")).getRelationTableName());
            sm.dropTable(descriptorC.getTableName());
            sm.dropTable(descriptorB.getTableName());
            sm.dropTable(descriptorA.getTableName());
        } catch (DatabaseException dbe) {

        }

        sm.createDefaultTables(true);
    }

    public void persistDynamicInstances(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        Map entityA = (Map) descriptorA.getInstantiationPolicy().buildNewInstance();
        entityA.put("id", 1);
        entityA.put("value", "value-1");

        Map entityB = (Map) descriptorB.getInstantiationPolicy().buildNewInstance();
        entityB.put("id", 1);
        entityB.put("a-id", 1);
        entityB.put("value", Calendar.getInstance());

        // Add entityB to A's 1:M
        List bs = new ArrayList();
        bs.add(entityB);
        entityA.put("bs", new ValueHolder(bs));

        Map entityC1 = (Map) descriptorC.getInstantiationPolicy().buildNewInstance();
        entityC1.put("id", 1);
        Map entityC2 = (Map) descriptorC.getInstantiationPolicy().buildNewInstance();
        entityC2.put("id", 2);

        // Add entityB to A's 1:M
        List cs = new ArrayList();
        cs.add(entityC1);
        cs.add(entityC2);
        entityA.put("cs", new ValueHolder(cs));

        em.persist(entityC1);
        em.persist(entityC2);
        em.persist(entityB);
        em.persist(entityA);

        em.getTransaction().commit();
        em.close();
    }

    public List<Map> queryDynamicInstances(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery("SELECT s FROM SimpleTypeA s WHERE s.value LIKE 'v%'").getResultList();
        } finally {
            em.close();
        }
    }

    public void updateDyanmicInstances(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        Map entityA = (Map) em.find(descriptorA.getJavaClass(), 1);
        entityA.put("value", "value-1+");

        Map entityB = (Map) descriptorB.getInstantiationPolicy().buildNewInstance();
        entityB.put("id", 2);
        entityB.put("value", Calendar.getInstance());

        em.persist(entityB);

        List<Map> bs = (List<Map>) ((ValueHolderInterface) entityA.get("bs")).getValue();
        bs.add(entityB);
        entityB.put("a-id", 1);

        em.getTransaction().commit();
        em.close();
    }

    public void deleteDynamicInstances(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        Map entity = (Map) em.find(descriptorA.getJavaClass(), 1);
        em.remove(entity);

        List<DynamicMapEntity> bs = (List<DynamicMapEntity>) ((ValueHolderInterface) entity.get("bs")).getValue();
        bs.clear();
        for (DynamicMapEntity b : bs) {
            em.remove(b);
        }

        List<DynamicMapEntity> cs = (List<DynamicMapEntity>) ((ValueHolderInterface) entity.get("cs")).getValue();
        cs.clear();
        for (DynamicMapEntity c : cs) {
            em.remove(c);
        }

        em.getTransaction().commit();
        em.close();
    }

    public void removeDynamicTypes(EntityManagerFactory emf) {
        Server session = JpaHelper.getServerSession(emf);

        session.getIdentityMapAccessor().initializeIdentityMap(descriptorA.getJavaClass());

        session.getDescriptors().remove(descriptorA.getJavaClass());
        session.getProject().getAliasDescriptors().remove(TYPE_A);
        session.getProject().getOrderedDescriptors().remove(descriptorA);

        session.getDescriptors().remove(descriptorB.getJavaClass());
        session.getProject().getAliasDescriptors().remove(TYPE_B);
        session.getProject().getOrderedDescriptors().remove(descriptorB);

        session.getDescriptors().remove(descriptorC.getJavaClass());
        session.getProject().getAliasDescriptors().remove(TYPE_C);
        session.getProject().getOrderedDescriptors().remove(descriptorC);
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
