package model;

import java.util.*;
import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

@EntityListeners(EmployeeListener.class)
@Table(name="EMPLOYEE")
@SecondaryTable(name="SALARY")
@PrimaryKeyJoinColumn(name="EMP_ID", referencedColumnName="EMP_ID")
@NamedQueries({
  @NamedQuery(
	name="findAllEmployeesByFirstName",
	query="SELECT OBJECT(employee) FROM Employee employee WHERE employee.firstName = :firstname"
  ),
  @NamedQuery(
	name="constuctEmployees",
	query="SELECT new Employee(employee.firstName, employee.lastName) FROM Employee employee")
}
)
public class Employee implements Serializable {
	private int salary;
	private int version;

	private Integer id;

	private String firstName;
	private String lastName;
	
	private Collection<PhoneNumber> phoneNumbers;
	private Collection<Project> projects;
	private Collection<Employee> managedEmployees;
	
	private Address address;
	private EmploymentPeriod period;

    private Employee manager;
    
	public Employee () {
        this.phoneNumbers = new Vector<PhoneNumber>();
        this.projects = new Vector<Project>();
        this.managedEmployees = new Vector<Employee>();
	}
    
    public Employee(String firstName, String lastName){
        this();
        this.firstName = firstName;
        this.lastName = lastName;
    }

	public Address getAddress() { 
        return address; 
    }

    @Column(name="F_NAME")
	public String getFirstName() { 
        return firstName; 
    }

    @Transient
	public String getLastName() { 
        return lastName; 
    }

	public Integer getId() { 
        return id; 
    }

	public Collection<Employee> getManagedEmployees() { 
        return managedEmployees; 
    }

	@ManyToOne(cascade=PERSIST, fetch=LAZY)
	public Employee getManager() { 
        return manager; 
    }

	public EmploymentPeriod getPeriod() {
		return period;
	}

	public Collection<PhoneNumber> getPhoneNumbers() { 
        return phoneNumbers; 
    }

	public Collection<Project> getProjects() { 
        return projects; 
    }

    @Column(table="SALARY")
	public int getSalary() { 
        return salary; 
    }

	@Version
	@Column(name="VERSION")
	public int getVersion() { 
        return version; 
    }

	public void setAddress(Address address) {
		this.address = address;
	}

	public void setFirstName(String name) { 
		this.firstName = name; 
    }

	public void setId(Integer id) { 
        this.id = id; 
    }

	public void setLastName(String name) { 
        this.lastName = name; 
    }

	public void setManagedEmployees(Collection<Employee> managedEmployees) {
		this.managedEmployees = managedEmployees;
	}
    
	public void setManager(Employee manager) {
		this.manager = manager;
	}

	public void setPeriod(EmploymentPeriod period) {
		this.period = period;
	}

	public void setPhoneNumbers(Collection<PhoneNumber> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public void setProjects(Collection<Project> projects) {
		this.projects = projects;
	}
    
	public void setSalary(int salary) { 
        this.salary = salary; 
    }

	protected void setVersion(int version) {
		this.version = version;
	}
}
