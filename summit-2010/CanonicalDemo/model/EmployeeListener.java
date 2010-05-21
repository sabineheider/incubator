package model;

import javax.persistence.*;
import java.util.EventListener;

public class EmployeeListener implements EventListener {
    public static int PRE_PERSIST_COUNT = 0;
    public static int POST_PERSIST_COUNT = 0;
    public static int PRE_REMOVE_COUNT = 0;
    public static int POST_REMOVE_COUNT = 0;
    public static int PRE_UPDATE_COUNT = 0;
    public static int POST_UPDATE_COUNT = 0;
    public static int POST_LOAD_COUNT = 0;

    public static String PRE_UPDATE_NAME_PREFIX = "PRE_UPDATE_NAME_PREFIX";
    
	@PrePersist
	public void prePersist(Object emp) {
        PRE_PERSIST_COUNT++;
	}

	@PreRemove
	public void preRemove(Object emp) {
        PRE_REMOVE_COUNT++;
	}

	@PreUpdate
	public void preUpdate(Object emp) {
        PRE_UPDATE_COUNT++;
        Employee employee = (Employee)emp;
        if(employee.getFirstName() != null && employee.getFirstName().startsWith(PRE_UPDATE_NAME_PREFIX)) {
            employee.setFirstName(employee.getFirstName().substring(PRE_UPDATE_NAME_PREFIX.length()));
        }
        if(employee.getLastName() != null && employee.getLastName().startsWith(PRE_UPDATE_NAME_PREFIX)) {
            employee.setLastName(employee.getLastName().substring(PRE_UPDATE_NAME_PREFIX.length()));
        }
	}

	@PostLoad
	public void postLoad(Employee emp) {
        POST_LOAD_COUNT++;
	}

	@PostPersist
	public void postPersist(Object emp) {
        POST_PERSIST_COUNT++;
	}

	@PostRemove
	public void postRemove(Object emp) {
        POST_REMOVE_COUNT++;
	}

	@PostUpdate
	public void postUpdate(Object emp) {
        POST_UPDATE_COUNT++;
	}
}
