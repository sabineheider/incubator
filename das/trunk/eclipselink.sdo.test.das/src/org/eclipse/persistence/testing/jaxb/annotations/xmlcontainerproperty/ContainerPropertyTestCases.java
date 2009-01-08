package org.eclipse.persistence.testing.jaxb.annotations.xmlcontainerproperty;

import java.util.Vector;

import org.eclipse.persistence.testing.jaxb.*;

public class ContainerPropertyTestCases extends JAXBTestCases {

	private static final String XML_RESOURCE = "org/eclipse/persistence/testing/jaxb/extension/xmlcontainerproperty/containeraccessor.xml";
	
	public ContainerPropertyTestCases(String name) throws Exception {
		super(name);
		setClasses(new Class[] {Employee.class, Address.class, PhoneNumber.class});
		setControlDocument(XML_RESOURCE);
	}
	
	public Employee getControlObject() {
		Employee emp = new Employee();
		emp.id = 10;
		emp.firstName = "Jane";
		emp.lastName = "Doe";
		emp.address = new Address();
		emp.address.street = "123 Fake Street";
		emp.address.city = "Ottawa";
		emp.address.owningEmployee = emp;
		emp.phoneNumbers = new Vector<PhoneNumber>();
		
		PhoneNumber num1 = new PhoneNumber();
		num1.number = "123-4567";
		num1.owningEmployee = emp;
		emp.phoneNumbers.add(num1);
		
		PhoneNumber num2 = new PhoneNumber();
		num2.number = "234-5678";
		num2.owningEmployee = emp;
		emp.phoneNumbers.add(num2);
		
		return emp;
	}
}
