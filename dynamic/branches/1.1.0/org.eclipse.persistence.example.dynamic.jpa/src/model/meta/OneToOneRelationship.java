package model.meta;

import javax.persistence.*;

import org.eclipse.persistence.dynamic.EntityTypeBuilder;

@Entity
@Table(name = "CUSTOM_REL_OTO")
@DiscriminatorValue("1:1")
public class OneToOneRelationship extends CustomField {

    @ManyToOne
    @JoinColumn(name = "REF_TYPE")
    private CustomType referenceType;

    protected OneToOneRelationship() {
        super();
    }

    protected OneToOneRelationship(String name, CustomType referenceType, String fieldName) {
        super(name, referenceType.getClassName(), fieldName);
        this.referenceType = referenceType;
    }

    /**
     * @return the referenceType
     */
    public CustomType getReferenceType() {
        return this.referenceType;
    }

    @Override
    protected void addToType(EntityTypeBuilder factory) {
        factory.addOneToOneMapping(getName(), getReferenceType().getEntityType(), getFieldName(), getReferenceType().getIdFieldName());
    }

}
