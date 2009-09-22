package org.eclipse.persistence.testing.tests.dynamic.simple;

import static junit.framework.Assert.*;

import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.Assert;

public class SimpleTypeCompositeKey extends SimpleType {
    @Override
    public void verifyConfig() throws Exception {
        super.verifyConfig();

        EntityType type = getSimpleType();
        assertNotNull(type.getDescriptor().getCMPPolicy());
        // assertEquals(Object[].class, ((DynamicIdentityPolicy)
        // type.getDescriptor().getCMPPolicy()).getPKClass());
    }

    @Override
    protected EntityType createSimpleType() {
        Server session = JpaHelper.getServerSession(emf);
        DynamicClassLoader dcl = DynamicClassLoader.lookup(session);
        Class<?> javaType = dcl.creatDynamicClass("model.Simple");

        EntityTypeBuilder typeBuilder = new EntityTypeBuilder(javaType, null, "SIMPLE_TYPE");
        typeBuilder.setPrimaryKeyFields("SID1", "SID2");
        typeBuilder.addDirectMapping("id1", int.class, "SID1");
        typeBuilder.addDirectMapping("id2", int.class, "SID2");
        typeBuilder.addDirectMapping("value1", String.class, "VAL_1");
        typeBuilder.addDirectMapping("value2", boolean.class, "VAL_2");
        typeBuilder.addDirectMapping("value3", Calendar.class, "VAL_3");
        typeBuilder.addDirectMapping("value4", Character.class, "VAL_4");

        EntityTypeBuilder.addToSession(session, true, false, typeBuilder.getType());

        return typeBuilder.getType();
    }

    @Override
    protected void assertDefaultValues(DynamicEntity simpleInstance) {
        assertNotNull(simpleInstance);

        assertTrue("id1 not set on new instance", simpleInstance.isSet("id1"));
        assertEquals("id1 not default value", 0, simpleInstance.get("id1"));
        assertTrue("id2 not set on new instance", simpleInstance.isSet("id2"));
        assertEquals("id2 not default value", 0, simpleInstance.get("id2"));
        assertFalse("value1  set on new instance", simpleInstance.isSet("value1"));
        assertTrue("value2 not set on new instance", simpleInstance.isSet("value2"));
        assertEquals("value2 not default value", false, simpleInstance.get("value2"));
        assertFalse("value3 set on new instance", simpleInstance.isSet("value3"));
        assertFalse("value4  set on new instance", simpleInstance.isSet("value4"));
    }

    public DynamicEntity createSimpleInstance(EntityManagerFactory emf, int id) {
        EntityManager em = emf.createEntityManager();
        EntityType simpleEntityType = DynamicHelper.getType(JpaHelper.getServerSession(emf), "Simple");
        Assert.assertNotNull(simpleEntityType);

        DynamicEntity simpleInstance = simpleEntityType.newInstance();
        simpleInstance.set("id1", id);
        simpleInstance.set("id2", id);
        simpleInstance.set("value2", true);

        assertEquals(0, ((Number) em.createQuery("SELECT COUNT(s) FROM Simple s").getSingleResult()).intValue());

        em.getTransaction().begin();
        em.persist(simpleInstance);
        em.getTransaction().commit();

        assertEquals(1, ((Number) em.createQuery("SELECT COUNT(s) FROM Simple s").getSingleResult()).intValue());

        DynamicEntity foundEntity = find(em, 1);

        assertNotNull(foundEntity);
        assertEquals(simpleInstance.get("id1"), foundEntity.get("id1"));
        assertEquals(simpleInstance.get("id2"), foundEntity.get("id2"));
        assertEquals(simpleInstance.get("value1"), foundEntity.get("value1"));
        assertEquals(simpleInstance.get("value2"), foundEntity.get("value2"));

        em.close();

        return simpleInstance;
    }

    @Override
    protected DynamicEntity find(EntityManager em, int id) {
        return (DynamicEntity) em.find(getSimpleType().getJavaClass(), new Object[] { id, id });
    }

}
