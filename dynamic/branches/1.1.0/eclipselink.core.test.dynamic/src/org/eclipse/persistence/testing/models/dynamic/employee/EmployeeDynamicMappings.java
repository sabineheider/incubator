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
package org.eclipse.persistence.testing.models.dynamic.employee;

import java.sql.Date;
import java.util.Calendar;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.EntityTypeBuilder;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sessions.DatabaseSession;

/**
 * Factory for the creation of the dynamic {@link EntityTypeImpl}'s required for
 * the employee example.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class EmployeeDynamicMappings {

    public static void createTypes(DatabaseSession session, String packageName, boolean createMissingTables) {
        String packagePrefix = packageName.endsWith(".") ? packageName : packageName + ".";
        DynamicClassLoader dcl = DynamicClassLoader.lookup(session);

        Class<?> employeeClass = dcl.createDynamicClass(packagePrefix + "Employee");
        Class<?> addressClass = dcl.createDynamicClass(packagePrefix + "Address");
        Class<?> phoneClass = dcl.createDynamicClass(packagePrefix + "PhoneNumber");
        Class<?> periodClass = dcl.createDynamicClass(packagePrefix + "EmploymentPeriod");
        Class<?> projectClass = dcl.createDynamicClass(packagePrefix + "Project");
        Class<?> smallProjectClass = dcl.createDynamicClass(packagePrefix + "SmallProject", projectClass);
        Class<?> largeProjectClass = dcl.createDynamicClass(packagePrefix + "LargeProject", projectClass);

        EntityTypeBuilder employee = new EntityTypeBuilder(employeeClass, null, "D_EMPLOYEE", "D_SALARY");
        EntityTypeBuilder address = new EntityTypeBuilder(addressClass, null, "D_ADDRESS");
        EntityTypeBuilder phone = new EntityTypeBuilder(phoneClass, null, "D_PHONE");
        EntityTypeBuilder period = new EntityTypeBuilder(periodClass, null);
        EntityTypeBuilder project = new EntityTypeBuilder(projectClass, null, "D_PROJECT");
        EntityTypeBuilder smallProject = new EntityTypeBuilder(smallProjectClass, project.getType(), "D_PROJECT");
        EntityTypeBuilder largeProject = new EntityTypeBuilder(largeProjectClass, project.getType(), "D_LPROJECT");

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

    private static void configurePhone(EntityTypeBuilder phone, EntityTypeBuilder employee) {
        phone.setPrimaryKeyFields("PHONE_TYPE", "EMP_ID");

        phone.addDirectMapping("type", String.class, "PHONE_TYPE");
        phone.addDirectMapping("ownerId", int.class, "EMP_ID").readOnly();
        phone.addDirectMapping("areaCode", String.class, "AREA_CODE");
        phone.addDirectMapping("number", String.class, "PNUMBER");

        phone.addOneToOneMapping("owner", employee.getType(), "EMP_ID");
    }

    private static void configureAddress(EntityTypeBuilder address) {
        address.setPrimaryKeyFields("ADDR_ID");

        address.addDirectMapping("id", int.class, "ADDR_ID");
        address.addDirectMapping("street", String.class, "STREET");
        address.addDirectMapping("city", String.class, "CITY");
        address.addDirectMapping("province", String.class, "PROV");
        address.addDirectMapping("postalCode", String.class, "P_CODE");
        address.addDirectMapping("country", String.class, "COUNTRY");

        address.configureSequencing("ADDR_SEQ", "ADDR_ID");
    }

    private static void configureEmployee(EntityTypeBuilder employee, EntityTypeBuilder address, EntityTypeBuilder phone, EntityTypeBuilder period, EntityTypeBuilder project) {
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

        OneToManyMapping phoneMapping = employee.addOneToManyMapping("phoneNumbers", phone.getType(), "EMP_ID");
        phoneMapping.setCascadeAll(true);
        phoneMapping.setIsPrivateOwned(true);

        employee.addAggregateObjectMapping("period", period.getType(), true);
        employee.addOneToManyMapping("managedEmployees", employee.getType(), "MANAGER_ID");

        employee.configureSequencing("EMP_SEQ", "EMP_ID");
    }

    private static void configurePeriod(EntityTypeBuilder period) {
        period.addDirectMapping("startDate", Date.class, "START_DATE");
        period.addDirectMapping("endDate", Date.class, "END_DATE");
    }

    private static void configureProject(EntityTypeBuilder project, EntityTypeBuilder smallProject, EntityTypeBuilder largeProject, EntityTypeBuilder employee) {
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

    private static void configureLargeProject(EntityTypeBuilder largeProject, EntityTypeBuilder project) {
        largeProject.setPrimaryKeyFields("PROJ_ID");

        ClassDescriptor descriptor = largeProject.getType().getDescriptor();
        descriptor.getInheritancePolicy().setClassIndicatorFieldName("PROJ_TYPE");
        descriptor.getInheritancePolicy().setParentClass(project.getType().getJavaClass());

        largeProject.addDirectMapping("budget", double.class, "BUDGET");
        largeProject.addDirectMapping("milestone", Calendar.class, "MILESTONE");
    }

    private static void configureSmallProject(EntityTypeBuilder smallProject, EntityTypeBuilder project) {
        smallProject.setPrimaryKeyFields("PROJ_ID");

        ClassDescriptor descriptor = smallProject.getType().getDescriptor();
        descriptor.getInheritancePolicy().setParentClass(project.getType().getJavaClass());
    }

}
