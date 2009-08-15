package model.meta;

import javax.persistence.*;

import org.eclipse.persistence.dynamic.EntityTypeFactory;

@Entity
@Table(name = "CUSTOM_REL_MTO")
@DiscriminatorValue("M:1")
public class ManyToOneRelationship extends OneToOneRelationship {

    @Column(name = "MAPPED_BY")
    private String mappedBy;

    protected ManyToOneRelationship() {
        super();
    }

    protected ManyToOneRelationship(String name, CustomType referenceType, String fieldName) {
        super(name, referenceType, fieldName);
    }

    public String getMappedBy() {
        return this.mappedBy;
    }

    @Override
    protected void addToType(EntityTypeFactory factory) {
        // TODO: Target Field Name?
        factory.addOneToOneMapping(getName(), null, getFieldName(), "TODO_BLAH");
    }

}
