package model.meta;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.eclipse.persistence.dynamic.EntityTypeBuilder;

@Entity
@Table(name = "CUSTOM_REL_MTM")
@DiscriminatorValue("M:M")
public class ManyToManyRelationship extends OneToOneRelationship {

    @Column(name = "JOIN_TABLE")
    private String joinTable;

    protected ManyToManyRelationship() {
        super();
    }

    protected ManyToManyRelationship(String name, CustomType referenceType, String fieldName) {
        super(name, referenceType, fieldName);
    }

    public String getJoinTable() {
        return this.joinTable;
    }

    @Override
    protected void addToType(EntityTypeBuilder factory) {
        // TODO
        throw new UnsupportedOperationException();
    }

}
