package model;

public class PhoneNumberPK  {
    public Integer id;
	public String type;

    public PhoneNumberPK() {}

	public Integer getId() { 
        return id; 
    }
    
	public void setId(Integer id) {
		this.id = id;
	}

	public String getType() { 
        return type; 
    }
    
	public void setType(String type) {
		this.type = type;
	}
    
    public boolean equals(Object anotherPhoneNumber) {
        if (anotherPhoneNumber.getClass() != PhoneNumberPK.class) {
            return false;
        }
        return (getId().equals(((PhoneNumberPK)anotherPhoneNumber).getId()));
    }
}
