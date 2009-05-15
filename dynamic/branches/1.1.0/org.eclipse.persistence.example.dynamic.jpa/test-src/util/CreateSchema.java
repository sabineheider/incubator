package util;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

import org.junit.Test;

public class CreateSchema {

    @Test
    public void test() {
        createSchema();
    }
    
    public static void main(String[] args) {
        createSchema();
    }

    public static void createSchema() {
        Map properties = new HashMap();
        
        properties.put("eclipselink.ddl-generation.output-mode", "database");
        properties.put("eclipselink.ddl-generation","drop-and-create-tables");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("custom-types", properties);
        EntityManager em = emf.createEntityManager();

        //em.createQuery("SELECT ct FROM CustomType ct").getResultList();

        em.close();
        emf.close();
    }
}
