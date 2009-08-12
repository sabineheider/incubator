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

import java.util.*;

import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.mappings.OneToOneMapping;

/**
 * Factory for the creation of the dynamic {@link EntityTypeImpl}'s required for
 * the employee example.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class EntityTypeFactory {

    public static List<EntityTypeImpl> createTypes(EntityManagerFactory emf, String packageName) {
        String packagePrefix = packageName.endsWith(".") ? packageName : packageName + ".";
        DynamicConversionManager dcm = DynamicConversionManager.lookup(JpaHelper.getServerSession(emf));

        Class employeeClass = dcm.createDynamicClass(packagePrefix + "Employee");
        Class addressClass = dcm.createDynamicClass(packagePrefix + "Address");
        Class phoneNumberClass = dcm.createDynamicClass(packagePrefix + "PhoneNumber");
        // Class employmentPeriodClass = dcm.createDynamicClass(packagePrefix +
        // "EmploymentPeriod");

        List<EntityTypeImpl> entityTypes = new ArrayList<EntityTypeImpl>();

        entityTypes.add(createEmployeeType(employeeClass, addressClass, phoneNumberClass));
        entityTypes.add(createAddressType(addressClass));
        entityTypes.add(createPhoneNumberType(phoneNumberClass, employeeClass));
        // entityTypes.add(createEmploymentPeriodType(employmentPeriodClass,
        // employeeClass));

        return entityTypes;
    }

    private static EntityTypeImpl createPhoneNumberType(Class phoneNumberClass, Class employeeClass) {
        EntityTypeImpl type = new EntityTypeImpl(phoneNumberClass, "D_PHONE");

        type.addDirectMapping("type", String.class, "PHONE_TYPE", true);
        type.addDirectMapping("ownerId", int.class, "EMP_ID", true).readOnly();
        type.addDirectMapping("areaCode", String.class, "AREA_CODE", false);
        type.addDirectMapping("number", String.class, "PNUMBER", false);
        type.addOneToOneMapping("owner", employeeClass, "EMP_ID", "EMP_ID");

        return type;
    }

    private static EntityTypeImpl createAddressType(Class addressClass) {
        EntityTypeImpl type = new EntityTypeImpl(addressClass, "D_ADDRESS");

        type.addDirectMapping("id", int.class, "ADDR_ID", true);
        type.addDirectMapping("street", String.class, "STREET", false);
        type.addDirectMapping("city", String.class, "CITY", false);
        type.addDirectMapping("province", String.class, "PROV", false);
        type.addDirectMapping("postalCode", String.class, "P_CODE", false);
        type.addDirectMapping("country", String.class, "COUNTRY", false);
        
        //type.getDescriptor().setSequenceNumberName("ADDR_SEQ");
        //type.getDescriptor().setSequenceNumberFieldName("ADDR_ID");

        return type;
    }

    private static EntityTypeImpl createEmployeeType(Class employeeClass, Class addressClass, Class phoneNumberClass) {
        EntityTypeImpl type = new EntityTypeImpl(employeeClass, "D_EMPLOYEE");

        type.addDirectMapping("id", int.class, "EMP_ID", true);
        type.addDirectMapping("firstName", String.class, "F_NAME", false);
        type.addDirectMapping("lastName", String.class, "L_NAME", false);
        type.addDirectMapping("gender", String.class, "GENDER", false);
        // TODO: Need to add secondary SALARY Table
        type.addDirectMapping("salary", int.class, "SALARY", false);

        OneToOneMapping addressMapping = type.addOneToOneMapping("address", addressClass, "ADDR_ID", "ADDR_ID");
        addressMapping.setCascadePersist(true);
        addressMapping.setIsPrivateOwned(true);
        type.addOneToOneMapping("manager", employeeClass, "MANAGER_ID", "EMP_ID").setIsPrivateOwned(true);
        // type.addOneToManyMapping("phoneNumbers", phoneNumberClass,
        // "OWNER_ID", "EMP_ID");
        // type.addAggregateObjectMapping("period", employmentPeriodClass);

        //type.getDescriptor().setSequenceNumberName("EMP_SEQ");
        //type.getDescriptor().setSequenceNumberFieldName("EMP_ID");

        return type;
    }

    private static EntityTypeImpl createEmploymentPeriodType(Class employmentPeriodClass, Class employeeClass) {
        EntityTypeImpl type = new EntityTypeImpl(employmentPeriodClass, "D_EMPLOYEE");

        type.addDirectMapping("startDate", Calendar.class, "START_DATE", false);
        type.addDirectMapping("endDate", Calendar.class, "END_DATE", false);

        type.getDescriptor().descriptorIsAggregate();

        return type;
    }

}
