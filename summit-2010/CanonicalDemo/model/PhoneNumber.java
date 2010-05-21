package model;

import java.io.*;
import javax.persistence.*;

@Table(name="PHONENUMBER")
public class PhoneNumber implements Serializable {
	private String number;
	private String type;
	private Employee owner;
    private Integer id;
    private String areaCode;
	
    public PhoneNumber() {
        this("", "###", "#######");
    }

    public PhoneNumber(String type, String theAreaCode, String theNumber) {
        this.type = type;
        this.areaCode = theAreaCode;
        this.number = theNumber;
        this.owner = null;
    }

    public PhoneNumberPK buildPK(){
        PhoneNumberPK pk = new PhoneNumberPK();
        pk.setId(this.getOwner().getId());
        pk.setType(this.getType());
        return pk;
    }

	@Column(name="AREA_CODE")
	public String getAreaCode() { 
        return areaCode; 
    }

	public Integer getId() { 
        return id; 
    }

	public String getNumber() { 
        return number; 
    }

	public Employee getOwner() { 
        return owner; 
    }

	public String getType() { 
        return type; 
    }

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public void setId(Integer id) {
		this.id = id;
	}
    
	public void setNumber(String number) { 
        this.number = number; 
    }

	public void setType(String type) {
		this.type = type;
	}

	public void setOwner(Employee owner) {
		this.owner = owner;
	}
}
