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
 *     dclarke - JPA DAS INCUBATOR - Enhancement 258057
 *     			 http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing.jaxb;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.*;

import model.Employee;

import org.junit.BeforeClass;
import org.junit.Test;

import testing.jpa.EclipseLinkJPATest;

@PersistenceContext(unitName = "employee")
public class ReadEmployeesFromDB extends EclipseLinkJPATest {

	private static JAXBContext jaxbContext;

	@Test
	public void simple() throws Exception {
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		EntityManager em = getEntityManager();

		List<Employee> emps = em.createNamedQuery("Employee.findAll").getResultList();

		new File("./data/temp/").mkdirs();

		for (Employee employee : emps) {
			FileOutputStream out = new FileOutputStream("./data/temp/employee-" + employee.getId() + ".xml");
			marshaller.marshal(employee, System.out);
			marshaller.marshal(employee, out);
			out.close();
		}
	}

	@BeforeClass
	public static void initializeContext() throws JAXBException {
		jaxbContext = JAXBContext.newInstance("model");
	}
}
