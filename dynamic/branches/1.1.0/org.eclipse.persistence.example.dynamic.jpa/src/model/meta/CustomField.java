package model.meta;

import javax.persistence.*;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.TypeConverter;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;

@Entity
@Table(name = "CUSTOM_FIELD")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "FIELD_TYPE", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue("F")
@TypeConverter(name = "boolean-string", dataType = String.class, objectType = Boolean.class)
public class CustomField {

    @Id
    @Column(name="FIELD_NAME")
    private String name;

    @Column(name = "JAVA_TYPE")
    @Convert("class-string")
    private Class javaType;

    @ManyToOne
    @JoinColumn(name = "CUSTOM_TYPE", nullable = false)
    private CustomType type;

    @Column(name = "COLUMN_NAME")
    private String fieldName;

    @Column(name = "IS_ID")
    @Convert("boolean-string")
    private boolean isId;

    protected CustomField() {

    }

    protected CustomField(String name, Class javaType, String fieldName, CustomType customType) {
        this();
        this.name = name;
        this.javaType = javaType;
        this.type = customType;
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
    public Class getJavaType() {
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

    protected void addToType(EntityTypeImpl entityType) {
        entityType.addDirectMapping(getName(), getJavaType(), getFieldName());
        if (isId()) {
            entityType.getDescriptor().addPrimaryKeyFieldName(getFieldName());
        }
    }

}
