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

import java.util.Collection;
import java.util.List;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.*;
import org.eclipse.persistence.sessions.Session;

/**
 * Simple query examples for the XML mapped Employee domain model.
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class Queries {

    /**
     * Simple example using dynamic JP QL to retrieve all Employee instances
     * sorted by lastName and firstName.
     */
    public List<DynamicEntity> readAllEmployees(DynamicHelper helper, Session session) {
        ReadAllQuery query = helper.newReadAllQuery("Employee");
        query.addAscendingOrdering("id");
        return (List<DynamicEntity>) session.executeQuery(query);
    }

    public List<DynamicEntity> readAllEmployeesWithAddress(DynamicHelper helper, Session session) {
        ReadAllQuery query = helper.newReadAllQuery("Employee");
        query.addJoinedAttribute("address");
        query.addAscendingOrdering("lastName");
        query.addAscendingOrdering("firstName");
        return (List<DynamicEntity>) session.executeQuery(query);
    }

    public List<DynamicEntity> readAllEmployeesWithAddressAndPhones(DynamicHelper helper, Session session) {
        ReadAllQuery query = helper.newReadAllQuery( "Employee");
        ExpressionBuilder eb = query.getExpressionBuilder();
        query.addJoinedAttribute("address");
        Expression managerExp = eb.get("manager");
        query.addJoinedAttribute(managerExp);
        query.addJoinedAttribute(managerExp.get("address"));
        query.addBatchReadAttribute(managerExp.get("phoneNumbers"));
        query.addAscendingOrdering("lastName");
        query.addAscendingOrdering("firstName");

        List<DynamicEntity> emps = (List<DynamicEntity>) session.executeQuery(query);

        for (DynamicEntity emp : emps) {
            emp.<DynamicEntity> get("manager").<Collection> get("phoneNumbers").size();
        }

        return emps;
    }

    public static int minimumEmployeeId(DynamicHelper helper, Session session) {
        ReportQuery query = helper.newReportQuery("Employee", new ExpressionBuilder());
        query.addMinimum("id");
        query.setShouldReturnSingleValue(true);
        return ((Number) session.executeQuery(query)).intValue();
    }

    public DynamicEntity minimumEmployee(DynamicHelper helper, Session session) {
        ReportQuery minIdQuery = helper.newReportQuery("Employee", new ExpressionBuilder());
        minIdQuery.addMinimum("id");

        ReadObjectQuery query = helper.newReadObjectQuery( "Employee");
        ExpressionBuilder eb = query.getExpressionBuilder();
        query.setSelectionCriteria(eb.get("id").in(minIdQuery));

        return (DynamicEntity) session.executeQuery(query);
    }

    public List<DynamicEntity> findUsingNativeReadAllQuery(DynamicHelper helper, Session session) {
        ReadAllQuery query = helper.newReadAllQuery("Employee");
        ExpressionBuilder eb = query.getExpressionBuilder();
        query.setSelectionCriteria(eb.get("gender").equal("Male"));

        return (List<DynamicEntity>) session.executeQuery(query);
    }

    public DynamicEntity minEmployeeWithAddressAndPhones(Session session) {
        Class<?> employeeClass = session.getClassDescriptorForAlias("Employee").getJavaClass();
        Class<?> phoneClass = session.getClassDescriptorForAlias("PhoneNumber").getJavaClass();

        ReportQuery minIdQuery = new ReportQuery(phoneClass, new ExpressionBuilder());
        minIdQuery.addMinimum("id");

        ReadObjectQuery query = new ReadObjectQuery(employeeClass);
        ExpressionBuilder eb = query.getExpressionBuilder();

        query.setSelectionCriteria(eb.get("id").in(minIdQuery));
        query.addJoinedAttribute("address");

        return (DynamicEntity) session.executeQuery(query);
    }
}
