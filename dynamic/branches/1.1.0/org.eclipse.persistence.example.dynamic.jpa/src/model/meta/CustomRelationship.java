package model.meta;

import javax.persistence.*;

import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
 
@Entity
@Table(name="CUSTOM_REL")
@DiscriminatorValue("R")
public class CustomRelationship extends CustomField {

    @ManyToOne
    @JoinColumn(name = "REF_TYPE")
    private CustomType referenceType;

    protected CustomRelationship() {
        super();
    }

    protected CustomRelationship(String name, String javaType, CustomType referenceType, String fieldName, CustomType customType) {
        super(name, javaType, fieldName, customType);
        this.referenceType = referenceType;
    }

    /**
     * @return the referenceType
     */
    public CustomType getReferenceType() {
        return this.referenceType;
    }

    protected void addToType(EntityTypeImpl entityType) {
        entityType.addOneToOneMapping(getName(), null, getFieldName());
    }

}
