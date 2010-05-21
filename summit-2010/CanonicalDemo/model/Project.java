package model;

import java.util.*;
import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.InheritanceType.*;
import static javax.persistence.DiscriminatorType.*;


@Table(name="PROJECT")
@DiscriminatorColumn(name="PROJ_TYPE", discriminatorType=INTEGER)
@DiscriminatorValue("0")
@NamedQuery(
	name="findProjectByName",
	query="SELECT OBJECT(project) FROM Project project WHERE project.name = :name"
)
public class Project implements Serializable {
    public int pre_update_count = 0;
    public int post_update_count = 0;
    public int pre_remove_count = 0;
    public int post_remove_count = 0;
    public int pre_persist_count = 0;
    public int post_persist_count = 0;
    public int post_load_count = 0;
    
	private Integer id;
	private int version;
	private String name;
	private String description;
	private Employee teamLeader;
	private Collection<Employee> teamMembers;

	public Project () {}

	@Column(name="DESCRIP")
	public String getDescription() { 
        return description; 
    }

	public Integer getId() { 
        return id; 
    }
    
	@Basic
	public String getName() { 
        return name; 
    }

	@Version
	public int getVersion() { 
        return version; 
    }

	public Employee getTeamLeader() {
        return teamLeader; 
    }

	public Collection<Employee> getTeamMembers() { 
        return teamMembers; 
    }

	public void setDescription(String description) { 
        this.description = description; 
    }

	public void setId(Integer id) { 
        this.id = id; 
    }

	public void setName(String name) { 
        this.name = name; 
    }

	protected void setVersion(int version) { 
        this.version = version; 
    }
    
	public void setTeamLeader(Employee teamLeader) { 
        this.teamLeader = teamLeader; 
    }
    
	public void setTeamMembers(Collection<Employee> employees) {
		this.teamMembers = employees;
	}
    
    @PrePersist
	public void prePersist() {
        ++pre_persist_count;
	}

	@PreRemove
	public void preRemove() {
        ++pre_remove_count;
	}

	@PreUpdate
	public void preUpdate() {
        ++pre_update_count;
	}

	@PostLoad
	public void postLoad() {
        ++post_load_count;
	}

	@PostPersist
	public void postPersist() {
        ++post_persist_count;
	}

	@PostRemove
	public void postRemove() {
        ++post_remove_count;
	}

	@PostUpdate
	public void postUpdate() {
        ++post_update_count;
	}
}
