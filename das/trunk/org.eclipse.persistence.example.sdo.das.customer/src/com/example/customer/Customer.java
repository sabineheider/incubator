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
 *     bdoughan - JPA DAS INCUBATOR - Enhancement 258057
 *     			 http://wiki.eclipse.org/EclipseLink/Development/SDO-JPA
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package com.example.customer;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

import static javax.persistence.CascadeType.ALL;

@Entity
public class Customer {
	
	@Id
	private int id;
	
	private String firstName;
	private String lastName;
	
	@ManyToOne(cascade=ALL)
	private Address billingAddress;
	
	@ManyToOne(cascade=ALL)
	private Address shippingAddress;
	
	@OneToMany(targetEntity=PhoneNumber.class, mappedBy="customer", cascade = ALL)
	private Set<PhoneNumber> phoneNumbers;

	@OneToOne
    private Spouse spouse;
	
	public Customer() {
		phoneNumbers = new HashSet<PhoneNumber>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Address getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(Address billingAddress) {
		this.billingAddress = billingAddress;
	}

	public Address getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(Address shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public Set<PhoneNumber> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(Set<PhoneNumber> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

    public Spouse getSpouse() {
        return spouse;
    }

    public void setSpouse(Spouse spouse) {
        this.spouse = spouse;
    }
	
}
