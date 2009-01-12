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
 *    dclarke - JPA DAS INCUBATOR - Enhancement 258057
 *              http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package testing.das;

import model.Gender;

import org.junit.Test;

import commonj.sdo.DataObject;

/**
 * 
 * @author dclarke EclipseLink 1.1
 */
public class TestEmployeeDAS_Delete extends TestEmployeeDAS {

	@Test
	public void deleteEmployee() {
		DataObject newEmpDO = TestEmployeeDAS_Create.createNewEmployee(this, "Delete", "Me", Gender.Male);

		getDAS().remove(newEmpDO);
	}

}
