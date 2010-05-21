package model;

import javax.persistence.*;

@Entity(name="LargeProject")
@Table(name="LPROJECT")
@DiscriminatorValue("1")
@NamedQuery(
	name="findWithBudgetLargerThan",
	query="SELECT OBJECT(project) FROM LargeProject project WHERE project.budget >= :amount"
)
public class LargeProject extends Project {
	private double budget;

	public double getBudget() { 
        return budget; 
    }
    
	public void setBudget(double budget) { 
		this.budget = budget; 
	}
}
