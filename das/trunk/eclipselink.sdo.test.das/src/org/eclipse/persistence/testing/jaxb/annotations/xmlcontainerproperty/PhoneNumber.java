package org.eclipse.persistence.testing.jaxb.annotations.xmlcontainerproperty;

import javax.xml.bind.annotation.*;

public class PhoneNumber {

	@XmlValue
	public String number;
	
	@XmlTransient
	public Employee owningEmployee;
	
	public boolean equals(Object o) {
		if(!(o instanceof PhoneNumber)) {
			return false;
		}
		
		PhoneNumber obj = (PhoneNumber)o;
		return number.equals(obj.number) && owningEmployee.id == obj.owningEmployee.id;
		
	}	
}
