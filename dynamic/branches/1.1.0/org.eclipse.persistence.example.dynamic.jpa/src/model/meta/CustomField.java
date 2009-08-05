package model.meta;

import java.io.Serializable;

import javax.persistence.*;

import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;

@Entity
@IdClass(CustomField.ID.class)
@Table(name = "CUSTOM_FIELD")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "FIELD_TYPE", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue("F")
public class CustomField {

    @Id
    @Column(name = "FIELD_NAME")
    private String name;

    @SuppressWarnings("unused")
    @Id
    @Column(name = "CUSTOM_TYPE", insertable = false, updatable = false)
    private String typeName;

    @Column(name = "JAVA_TYPE")
    private String javaType;

    @ManyToOne
    @JoinColumn(name = "CUSTOM_TYPE", nullable = false)
    private CustomType type;

    @Column(name = "COLUMN_NAME")
    private String fieldName;

    @Column(name = "IS_ID")
    private boolean isId;

    protected CustomField() {

    }

    protected CustomField(String name, String javaType, String fieldName, CustomType customType) {
        this();
        this.name = name;
        this.javaType = javaType;
        this.type = customType;
        this.typeName = customType.getName();
        this.fieldName = fieldName;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the javaType
     */
    public String getJavaType() {
        return javaType;
    }

    /**
     * @return the type
     */
    public CustomType getType() {
        return type;
    }

    protected String getFieldName() {
        return this.fieldName;
    }

    /**
     * @return the isId
     */
    public boolean isId() {
        return isId;
    }

    /**
     * @param isId
     *            the isId to set
     */
    public void setId(boolean isId) {
        this.isId = isId;
    }

    protected void addToType(DynamicConversionManager dcm, EntityTypeImpl entityType) {
        Class javaClass = dcm.convertClassNameToClass(getJavaType());
        entityType.addDirectMapping(getName(), javaClass, getFieldName(), isId());
        if (isId()) {
            entityType.getDescriptor().addPrimaryKeyFieldName(getFieldName());
        }
    }

    public static class ID implements Serializable {
        private String name;
        private String typeName;
        
        public ID() {
            
        }
        
        public ID(String name, String typeName) {
            this.name = name;
            this.typeName = typeName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

}
