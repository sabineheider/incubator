package org.eclipse.persistence.testing.jaxb.annotations.xmlcontainerproperty;

import javax.xml.bind.annotation.*;


public class Address {
	public String street;
	public String city;
	
	@XmlTransient
	public Employee owningEmployee;
	
	public boolean equals(Object o) {
		if(!(o instanceof Address)) {
			return false;
		}
		Address obj = (Address)o;
		boolean equal = true;
		
		equal = equal && street.equals(obj.street);
		equal = equal && city.equals(obj.city);
		
		equal = equal && owningEmployee.id == obj.owningEmployee.id;
		
		return equal;
	}	
}
