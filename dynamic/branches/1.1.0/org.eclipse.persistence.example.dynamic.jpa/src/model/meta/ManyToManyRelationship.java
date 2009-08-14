package model.meta;

import javax.persistence.*;

import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.dynamic.RelationalMappingFactory;

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
    protected void addToType(RelationalMappingFactory factory) {
        // TODO
        throw new UnsupportedOperationException();
    }

}
