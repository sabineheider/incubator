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
 *     dclarke - Dynamic Persistence INCUBATION - Enhancement 200045
 *               http://wiki.eclipse.org/EclipseLink/Development/JPA/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package example.employee;

import java.util.Calendar;

import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPAEntityTypeFactory;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sessions.server.Server;

/**
 * Factory for the creation of the dynamic {@link EntityTypeImpl}'s required for
 * the employee example.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class EntityTypeFactory {

    public static void createTypes(EntityManagerFactory emf, String packageName, boolean createMissingTables) {
        String packagePrefix = packageName.endsWith(".") ? packageName : packageName + ".";
        Server session = JpaHelper.getServerSession(emf);

        JPAEntityTypeFactory employee = new JPAEntityTypeFactory(session, packagePrefix + "Employee", "D_EMPLOYEE");
        JPAEntityTypeFactory address = new JPAEntityTypeFactory(session, packagePrefix + "Address", "D_ADDRESS");
        JPAEntityTypeFactory phone = new JPAEntityTypeFactory(session, packagePrefix + "PhoneNumber", "D_PHONE");
        JPAEntityTypeFactory period = new JPAEntityTypeFactory(session, packagePrefix + "Employee");

        configureEmployee(employee, address, phone, period);
        configureAddress(address);
        configurePhone(phone, employee);
        configurePeriod(period);

        employee.addToSession(session, false);
        address.addToSession(session, false);
        phone.addToSession(session, false);
        period.addToSession(session, createMissingTables);
    }

    private static void configurePhone(JPAEntityTypeFactory phone, JPAEntityTypeFactory employee) {
        phone.addPrimaryKeyFields("PHONE_TYPE", "EMP_ID");

        phone.addDirectMapping("type", String.class, "PHONE_TYPE");
        phone.addDirectMapping("ownerId", int.class, "EMP_ID").readOnly();
        phone.addDirectMapping("areaCode", String.class, "AREA_CODE");
        phone.addDirectMapping("number", String.class, "PNUMBER");

        phone.addOneToOneMapping("owner", employee.getType(), "EMP_ID", "EMP_ID");
    }

    private static void configureAddress(JPAEntityTypeFactory address) {
        address.addPrimaryKeyFields("ADDR_ID");

        address.addDirectMapping("id", int.class, "ADDR_ID");
        address.addDirectMapping("street", String.class, "STREET");
        address.addDirectMapping("city", String.class, "CITY");
        address.addDirectMapping("province", String.class, "PROV");
        address.addDirectMapping("postalCode", String.class, "P_CODE");
        address.addDirectMapping("country", String.class, "COUNTRY");
    }

    private static void configureEmployee(JPAEntityTypeFactory employee, JPAEntityTypeFactory address, JPAEntityTypeFactory phone, JPAEntityTypeFactory period) {
        employee.addPrimaryKeyFields("EMP_ID");

        employee.addDirectMapping("id", int.class, "EMP_ID");
        employee.addDirectMapping("firstName", String.class, "F_NAME");
        employee.addDirectMapping("lastName", String.class, "L_NAME");
        employee.addDirectMapping("gender", String.class, "GENDER");
        employee.addDirectMapping("salary", int.class, "SALARY");

        OneToOneMapping addressMapping = employee.addOneToOneMapping("address", address.getType(), "ADDR_ID");
        addressMapping.setCascadePersist(true);
        addressMapping.setIsPrivateOwned(true);
        employee.addOneToOneMapping("manager", employee.getType(), "MANAGER_ID").setIsPrivateOwned(true);
        // type.addOneToManyMapping("phoneNumbers", phoneNumberClass,
        // "OWNER_ID", "EMP_ID");
        employee.addAggregateObjectMapping("period", period.getType(), true);

        // type.getDescriptor().setSequenceNumberName("EMP_SEQ");
        // type.getDescriptor().setSequenceNumberFieldName("EMP_ID");
    }

    private static void configurePeriod(JPAEntityTypeFactory period) {
        period.addDirectMapping("startDate", Calendar.class, "START_DATE");
        period.addDirectMapping("endDate", Calendar.class, "END_DATE");
    }

}
