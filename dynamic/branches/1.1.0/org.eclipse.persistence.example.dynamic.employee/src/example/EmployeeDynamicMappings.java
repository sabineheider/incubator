package example;
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


import java.util.Calendar;

import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPAEntityTypeBuilder;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sessions.server.Server;

/**
 * Factory for the creation of the dynamic {@link EntityTypeImpl}'s required for
 * the employee example.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class EmployeeDynamicMappings {

    public static void createTypes(EntityManagerFactory emf, String packageName, boolean createMissingTables) {
        String packagePrefix = packageName.endsWith(".") ? packageName : packageName + ".";
        Server session = JpaHelper.getServerSession(emf);

        JPAEntityTypeBuilder employee = new JPAEntityTypeBuilder(session, packagePrefix + "Employee", null, "D_EMPLOYEE", "D_SALARY");
        JPAEntityTypeBuilder address = new JPAEntityTypeBuilder(session, packagePrefix + "Address", null, "D_ADDRESS");
        JPAEntityTypeBuilder phone = new JPAEntityTypeBuilder(session, packagePrefix + "PhoneNumber", null, "D_PHONE");
        JPAEntityTypeBuilder period = new JPAEntityTypeBuilder(session, packagePrefix + "EmploymentPeriod", null);
        JPAEntityTypeBuilder project = new JPAEntityTypeBuilder(session, packagePrefix + "Project", null, "D_PROJECT");
        JPAEntityTypeBuilder smallProject = new JPAEntityTypeBuilder(session, packagePrefix + "SmallProject", project.getType(), "D_PROJECT");
        JPAEntityTypeBuilder largeProject = new JPAEntityTypeBuilder(session, packagePrefix + "LargeProject", project.getType(), "D_LPROJECT");

        
        configureAddress(address);
        configureEmployee(employee, address, phone, period, project);
        configurePhone(phone, employee);
        configurePeriod(period);
        configureProject(project, smallProject, largeProject, employee);
        configureSmallProject(smallProject, project);
        configureLargeProject(largeProject, project);

        employee.addManyToManyMapping("projects", project.getType(), "D_PROJ_EMP");

        EntityTypeBuilder.addToSession(session, true, true, employee.getType(), address.getType(), phone.getType(), period.getType(), project.getType(), smallProject.getType(), largeProject.getType());
    }

    private static void configurePhone(JPAEntityTypeBuilder phone, JPAEntityTypeBuilder employee) {
        phone.setPrimaryKeyFields("PHONE_TYPE", "EMP_ID");

        phone.addDirectMapping("type", String.class, "PHONE_TYPE");
        phone.addDirectMapping("ownerId", int.class, "EMP_ID").readOnly();
        phone.addDirectMapping("areaCode", String.class, "AREA_CODE");
        phone.addDirectMapping("number", String.class, "PNUMBER");

        phone.addOneToOneMapping("owner", employee.getType(), "EMP_ID");
    }

    private static void configureAddress(JPAEntityTypeBuilder address) {
        address.setPrimaryKeyFields("ADDR_ID");

        address.addDirectMapping("id", int.class, "ADDR_ID");
        address.addDirectMapping("street", String.class, "STREET");
        address.addDirectMapping("city", String.class, "CITY");
        address.addDirectMapping("province", String.class, "PROV");
        address.addDirectMapping("postalCode", String.class, "P_CODE");
        address.addDirectMapping("country", String.class, "COUNTRY");

        address.configureSequencing("ADDR_SEQ", "ADDR_ID");
    }

    private static void configureEmployee(JPAEntityTypeBuilder employee, JPAEntityTypeBuilder address, JPAEntityTypeBuilder phone, JPAEntityTypeBuilder period, JPAEntityTypeBuilder project) {
        employee.setPrimaryKeyFields("EMP_ID");

        employee.addDirectMapping("id", int.class, "D_EMPLOYEE.EMP_ID");
        employee.addDirectMapping("firstName", String.class, "D_EMPLOYEE.F_NAME");
        employee.addDirectMapping("lastName", String.class, "D_EMPLOYEE.L_NAME");
        employee.addDirectMapping("gender", String.class, "D_EMPLOYEE.GENDER");
        employee.addDirectMapping("salary", int.class, "D_SALARY.SALARY");

        OneToOneMapping addressMapping = employee.addOneToOneMapping("address", address.getType(), "ADDR_ID");
        addressMapping.setCascadeAll(true);
        addressMapping.setIsPrivateOwned(true);

        employee.addOneToOneMapping("manager", employee.getType(), "MANAGER_ID");

        OneToManyMapping phoneMapping = employee.addOneToManyMapping("phoneNumbers", phone.getType(), "OWNER_ID");
        phoneMapping.setCascadeAll(true);
        phoneMapping.setIsPrivateOwned(true);

        employee.addAggregateObjectMapping("period", period.getType(), true);
        employee.addOneToManyMapping("managedEmployees", employee.getType(), "MANAGER_ID");

        employee.configureSequencing("EMP_SEQ", "EMP_ID");
    }

    private static void configurePeriod(JPAEntityTypeBuilder period) {
        period.addDirectMapping("startDate", Calendar.class, "START_DATE");
        period.addDirectMapping("endDate", Calendar.class, "END_DATE");
    }

    private static void configureProject(JPAEntityTypeBuilder project, JPAEntityTypeBuilder smallProject, JPAEntityTypeBuilder largeProject, JPAEntityTypeBuilder employee) {
        project.setPrimaryKeyFields("PROJ_ID");

        project.addDirectMapping("id", int.class, "PROJ_ID");
        project.addDirectMapping("name", String.class, "NAME");
        project.addDirectMapping("description", String.class, "DESCRIP");

        project.addOneToOneMapping("teamLeader", employee.getType(), "EMP_ID");

        ClassDescriptor descriptor = project.getType().getDescriptor();

        descriptor.getInheritancePolicy().setClassIndicatorFieldName("PROJ_TYPE");
        descriptor.getInheritancePolicy().addClassIndicator(smallProject.getType().getJavaClass(), "S");
        descriptor.getInheritancePolicy().addClassIndicator(largeProject.getType().getJavaClass(), "L");
        descriptor.getInheritancePolicy().addClassIndicator(project.getType().getJavaClass(), "P");

        project.configureSequencing("PROJ_SEQ", "PROJ_ID");
    }

    private static void configureLargeProject(JPAEntityTypeBuilder largeProject, JPAEntityTypeBuilder project) {
        largeProject.setPrimaryKeyFields("PROJ_ID");

        ClassDescriptor descriptor = largeProject.getType().getDescriptor();
        descriptor.getInheritancePolicy().setClassIndicatorFieldName("PROJ_TYPE");
        descriptor.getInheritancePolicy().setParentClass(project.getType().getJavaClass());

        largeProject.addDirectMapping("budget", double.class, "BUDGET");
        largeProject.addDirectMapping("milestone", Calendar.class, "MILESTONE");
    }

    private static void configureSmallProject(JPAEntityTypeBuilder smallProject, JPAEntityTypeBuilder project) {
        smallProject.setPrimaryKeyFields("PROJ_ID");

        ClassDescriptor descriptor = smallProject.getType().getDescriptor();
        descriptor.getInheritancePolicy().setParentClass(project.getType().getJavaClass());
    }

}
