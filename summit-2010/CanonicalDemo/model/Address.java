package model;

import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import java.util.*;
import static javax.persistence.CascadeType.*;

@Table(name="ADDRESS")
@NamedNativeQuery(
    name="findAllSQLAddresses", 
    query="select * from CMP3_ANN_MERGE_ADDRESS",
    resultClass=Address.class
)
@NamedQuery(
    name="findAllAddressesByPostalCode", 
    query="SELECT OBJECT(address) FROM Address address WHERE address.postalCode = :postalcode"
)
public class Address implements Serializable {
	private Integer id;
	private String street;
	private String city;
    private String province;
    private String postalCode;
    private String country;
	private Collection<Employee> employees;

    public Address() {
        city = "";
        province = "";
        postalCode = "";
        street = "";
        country = "";
        this.employees = new Vector<Employee>();
    }

    public Address(String street, String city, String province, String country, String postalCode) {
        this.street = street;
        this.city = city;
        this.province = province;
        this.country = country;
        this.postalCode = postalCode;
        this.employees = new Vector<Employee>();
    }

	public String getCity() { 
        return city; 
    }

	public String getCountry() { 
        return country; 
    }

	public Collection<Employee> getEmployees() { 
        return employees; 
    }

	public Integer getId() { 
        return id; 
    }

	@Column(name="P_CODE")
	public String getPostalCode() { 
        return postalCode; 
    }

	public String getProvince() { 
        return province; 
    }

	public String getStreet() { 
        return street; 
    }

    public void setEmployees(Collection<Employee> employees) {
		this.employees = employees;
	}

	public void setId(Integer id) { 
        this.id = id; 
    }

	public void setCity(String city) { 
        this.city = city; 
    }

	public void setCountry(String country) { 
        this.country = country;
    }

	public void setPostalCode(String postalCode) { 
        this.postalCode = postalCode; 
    }

	public void setProvince(String province) { 
        this.province = province; 
    }

	public void setStreet(String street) { 
        this.street = street; 
    }
}
