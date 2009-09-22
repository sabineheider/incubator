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

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.*;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;

/**
 * 
 * @author dclarke
 * @since EclipseLink - Dynamic Incubator (1.1.0-branch)
 */
public class Transactions {

    /**
     * New entities with new related related entities can be persisted using
     * <code>EntityManager.persist(newEntity)</code>. The cascade setting on the
     * mappings determine how the related entities are handled. In this case
     * Employee has its relationship to Address and PhoneNumber configured with
     * cascade-all so the associated new entities will also be persisted.
     */
    public DynamicEntity createUsingPersist(Session session) {
        EntityType empType = DynamicHelper.getType(session, "Employee");
        EntityType addrType = DynamicHelper.getType(session, "Address");
        EntityType phoneType = DynamicHelper.getType(session, "PhoneNumber");

        DynamicEntity emp = (DynamicEntity) empType.newInstance();
        emp.set("firstName", "Sample");
        emp.set("lastName", "Employee");
        emp.set("gender", "Male");
        emp.set("salary", 123456);

        DynamicEntity address = (DynamicEntity) addrType.newInstance();
        emp.set("address", address);

        DynamicEntity phone = (DynamicEntity) phoneType.newInstance();
        phone.set("type", "Mobile");
        phone.set("areaCode", "613");
        phone.set("number", "555-1212");
        phone.set("owner", emp);
        emp.<Collection<DynamicEntity>> get("phoneNumbers").add(phone);

        UnitOfWork uow = session.acquireUnitOfWork();
        uow.registerNewObject(emp);
        uow.commit();

        return emp;
    }

    /**
	 * 
	 */
    public DynamicEntity createUsingMerge(Session session) {
        ClassDescriptor empDescriptor = DynamicHelper.getType(session, "Employee").getDescriptor();
        ClassDescriptor addrDescriptor = DynamicHelper.getType(session, "Address").getDescriptor();
        ClassDescriptor phoneDescriptor = DynamicHelper.getType(session, "PhoneNumber").getDescriptor();

        DynamicEntity emp = (DynamicEntity) empDescriptor.getInstantiationPolicy().buildNewInstance();
        emp.set("firstName", "Sample");
        emp.set("lastName", "Employee");
        emp.set("gender", "Male");
        emp.set("salary", 123456);

        DynamicEntity address = (DynamicEntity) addrDescriptor.getInstantiationPolicy().buildNewInstance();
        emp.set("address", address);

        DynamicEntity phone = (DynamicEntity) phoneDescriptor.getInstantiationPolicy().buildNewInstance();
        phone.set("type", "Mobile");
        phone.set("areaCode", "613");
        phone.set("number", "555-1212");
        phone.set("owner", emp);
        emp.<Collection<DynamicEntity>> get("phoneNumbers").add(phone);

        UnitOfWork uow = session.acquireUnitOfWork();
        // When merging the managed instance is returned from the call.
        // Further usage within the transaction must be done with this managed
        // entity.
        emp = (DynamicEntity) uow.registerNewObject(emp);
        uow.commit();

        return emp;
    }

    /**
     * 
     * @param em
     * @throws Exception
     */
    public void pessimisticLocking(Session session) throws Exception {

        // Find the Employee with the minimum ID
        int minId = Queries.minimumEmployeeId(session);

        UnitOfWork uow = session.acquireUnitOfWork();

        // Lock Employee
        ReadObjectQuery query = DynamicHelper.newReadObjectQuery(session, "Employee");
        query.setSelectionCriteria(query.getExpressionBuilder().get("id").equal(minId));
        query.setLockMode(ObjectLevelReadQuery.LOCK);

        DynamicEntity emp = (DynamicEntity) uow.executeQuery(query);

        emp.set("salary", emp.<Integer> get("salary") - 1);

        uow.commit();
    }

    /**
     * This example illustrates the use of a query returning an entity and data
     * from a related entity within a transaction. The returned entities are
     * managed and thus any changes are reflected in the database upon flush.
     * 
     * @param em
     * @throws Exception
     */
    public void updateEmployeeWithCity(Session session) throws Exception {
        UnitOfWork uow = session.acquireUnitOfWork();

        List<DynamicEntity> emps = new Queries().readAllEmployeesWithAddress(uow);
        DynamicEntity emp = emps.get(0);
        emp.set("salary", emp.<Integer> get("salary") + 1);

        uow.writeChanges();

        uow.release();
    }

}
