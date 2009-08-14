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
 * 		dclarke - initial JPA Employee example using XML (bug 217884)
 ******************************************************************************/
package testing.employee;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.Test;

import example.employee.EntityTypeFactory;
import example.employee.Sample;

/**
 * Utility class to create the database schema and populate it for the Employee
 * JPA example using XML configuration. This
 * 
 * @author dclarke
 * @since EclipseLink 1.1
 */
public class CreateDatabase {

    @Test
    public void createSchemaAndPopulate() {
        populate(new HashMap());
    }

    public static void populate(Map properties) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("custom-types", properties);

        Server session = JpaHelper.getServerSession(emf);
        EntityTypeFactory.createTypes(emf, "model.dynamic.employee", false);

        SchemaManager dsm = new SchemaManager(session);
        dsm.replaceDefaultTables();
        dsm.createSequences();

        EntityManager em = null;

        try {
            em = emf.createEntityManager();

            em.getTransaction().begin();
            new Sample(emf).persistAll(em);
            em.getTransaction().commit();
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
                emf.close();
            }
        }
    }

    public static void main(String[] args) {
        new CreateDatabase().createSchemaAndPopulate();
    }

}
