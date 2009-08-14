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

import org.eclipse.persistence.dynamic.RelationalMappingFactory;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.jpa.JpaHelper;
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

        RelationalMappingFactory employee = new RelationalMappingFactory(session, packagePrefix + "Employee", "D_EMPLOYEE");
        RelationalMappingFactory address = new RelationalMappingFactory(session, packagePrefix + "Address", "D_ADDRESS");
        RelationalMappingFactory phone = new RelationalMappingFactory(session, packagePrefix + "PhoneNumber", "D_PHONE");
        RelationalMappingFactory period = new RelationalMappingFactory(session, packagePrefix + "Employee");

        configureEmployee(employee, address, phone, period);
        configureAddress(address);
        configurePhone(phone, employee);
        configurePeriod(period);

        employee.addToSession(session, false);
        address.addToSession(session, false);
        phone.addToSession(session, false);
        period.addToSession(session, createMissingTables);
    }

    private static void configurePhone(RelationalMappingFactory phone, RelationalMappingFactory employee) {
        phone.addDirectMapping("type", String.class, "PHONE_TYPE", true);
        phone.addDirectMapping("ownerId", int.class, "EMP_ID", true).readOnly();
        phone.addDirectMapping("areaCode", String.class, "AREA_CODE", false);
        phone.addDirectMapping("number", String.class, "PNUMBER", false);
        phone.addOneToOneMapping("owner", employee.getType(), "EMP_ID", "EMP_ID");
    }

    private static void configureAddress(RelationalMappingFactory address) {
        address.addDirectMapping("id", int.class, "ADDR_ID", true);
        address.addDirectMapping("street", String.class, "STREET", false);
        address.addDirectMapping("city", String.class, "CITY", false);
        address.addDirectMapping("province", String.class, "PROV", false);
        address.addDirectMapping("postalCode", String.class, "P_CODE", false);
        address.addDirectMapping("country", String.class, "COUNTRY", false);
    }

    private static void configureEmployee(RelationalMappingFactory employee, RelationalMappingFactory address, RelationalMappingFactory phone, RelationalMappingFactory period) {
        employee.addDirectMapping("id", int.class, "EMP_ID", true);
        employee.addDirectMapping("firstName", String.class, "F_NAME", false);
        employee.addDirectMapping("lastName", String.class, "L_NAME", false);
        employee.addDirectMapping("gender", String.class, "GENDER", false);
        employee.addDirectMapping("salary", int.class, "SALARY", false);

        OneToOneMapping addressMapping = employee.addOneToOneMapping("address", address.getType(), "ADDR_ID", "ADDR_ID");
        addressMapping.setCascadePersist(true);
        addressMapping.setIsPrivateOwned(true);
        employee.addOneToOneMapping("manager", employee.getType(), "MANAGER_ID", "EMP_ID").setIsPrivateOwned(true);
        // type.addOneToManyMapping("phoneNumbers", phoneNumberClass,
        // "OWNER_ID", "EMP_ID");
        employee.addAggregateObjectMapping("period", period.getType(), true);

        // type.getDescriptor().setSequenceNumberName("EMP_SEQ");
        // type.getDescriptor().setSequenceNumberFieldName("EMP_ID");
    }

    private static void configurePeriod(RelationalMappingFactory period) {
        period.addDirectMapping("startDate", Calendar.class, "START_DATE", false);
        period.addDirectMapping("endDate", Calendar.class, "END_DATE", false);
    }

}
