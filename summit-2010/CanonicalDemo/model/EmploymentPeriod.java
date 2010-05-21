package model;

import java.sql.Date;
import java.io.*;
import javax.persistence.*;

public class EmploymentPeriod implements Serializable {
    private Date startDate;
    private Date endDate;

    public EmploymentPeriod() {}

	public Date getEndDate() { 
        return endDate; 
    }

	public Date getStartDate() { 
        return startDate; 
    }
    
	public void setEndDate(Date date) { 
        this.endDate = date; 
    }

	public void setStartDate(Date date) { 
        this.startDate = date; 
    }
}
