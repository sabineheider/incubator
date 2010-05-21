package model;

import javax.persistence.*;

@Entity(name="SmallProject")
@DiscriminatorValue("2")
public class SmallProject extends Project {
}
