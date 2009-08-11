package model.meta;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.helper.DynamicConversionManager;
import org.eclipse.persistence.internal.jpa.CMP3Policy;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;

@Entity
@Table(name = "CUSTOM_TYPE")
public class CustomType {

    @Id
    @Column(name = "TYPE_NAME")
    private String name;

    @Column(name = "CLASS_NAME")
    private String className;

    @ManyToOne
    @JoinColumn(name = "PARENT_TYPE")
    private CustomType parentType;

    @OneToMany(mappedBy = "parentType", cascade = CascadeType.ALL)
    private List<CustomType> subTypes;

    private String typeIndicator;

    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL)
    private List<CustomField> fields;

    @Column(name = "TABLE_NAME")
    private String tableName;

    @Transient
    private EntityTypeImpl entityType;

    public CustomType() {
        this.fields = new ArrayList<CustomField>();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className
     *            the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the parentType
     */
    public CustomType getParentType() {
        return parentType;
    }

    /**
     * @param parentType
     *            the parentType to set
     */
    public void setParentType(CustomType parentType) {
        this.parentType = parentType;
    }

    /**
     * @return the subTypes
     */
    public List<CustomType> getSubTypes() {
        return subTypes;
    }

    /**
     * @return the typeIndicator
     */
    public String getTypeIndicator() {
        return typeIndicator;
    }

    /**
     * @param typeIndicator
     *            the typeIndicator to set
     */
    public void setTypeIndicator(String typeIndicator) {
        this.typeIndicator = typeIndicator;
    }

    /**
     * @return the fields
     */
    public List<CustomField> getFields() {
        return fields;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName
     *            the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return the {@link EntityTypeImpl} cached during
     *         {@link #createType(EntityManagerFactory, boolean, boolean)}
     */
    public EntityType getEntityType() {
        return this.entityType;
    }

    protected CustomField addField(CustomField field) {
        getFields().add(field);
        field.setType(this);
        return field;
    }

    public CustomField addField(String name, String javaType, String fieldName) {
        CustomField field = new CustomField(name, javaType, fieldName);
        return addField(field);
    }

    public OneToOneRelationship addOneToOne(String name, CustomType referenceType, String fieldName) {
        OneToOneRelationship field = new OneToOneRelationship(name, referenceType, fieldName);
        return (OneToOneRelationship) addField(field);
    }

    public ManyToOneRelationship addManyToOne(String name, CustomType referenceType, String fieldName) {
        OneToOneRelationship field = new OneToOneRelationship(name, referenceType, fieldName);
        return (ManyToOneRelationship) addField(field);
    }

    public ManyToManyRelationship addManyToMany(String name, CustomType referenceType, String fieldName) {
        OneToOneRelationship field = new OneToOneRelationship(name, referenceType, fieldName);
        return (ManyToManyRelationship) addField(field);
    }

    /**
     * Create a dynamic type defined by a {@link EntityTypeImpl} instance
     * 
     * @param emf
     * @param persist
     *            indicates if this type instance should be persisted.
     * @param createSchema
     *            if true a table is created for the specified object
     * @return
     */
    public EntityType createType(EntityManagerFactory emf, boolean persist, boolean createSchema) {
        if (persist) {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(this);
            em.getTransaction().commit();
            em.close();
        }

        Server session = JpaHelper.getServerSession(emf);
        DynamicConversionManager dcm = DynamicConversionManager.getDynamicConversionManager(session);
        Class dynamicClass = dcm.createDynamicClass(getClassName());

        EntityTypeImpl entityType = new EntityTypeImpl(dynamicClass, getTableName());
        entityType.getDescriptor().setAlias(getName());

        for (CustomField field : getFields()) {
            field.addToType(dcm, entityType);
        }

        if (entityType.getDescriptor().getCMPPolicy() == null) {
            entityType.getDescriptor().setCMPPolicy(new CMP3Policy());
        }

        session.addDescriptor(entityType.getDescriptor());

        if (createSchema) {
            new DynamicSchemaManager(session).createTables(entityType);
        }

        entityType.getDescriptor().setProperty(EntityTypeImpl.DESCRIPTOR_PROPERTY, entityType);
        this.entityType = entityType;
        return entityType;
    }

    public String getIdFieldName() {
        for (CustomField field : getFields()) {
            if (field.isId()) {
                return field.getFieldName();
            }
        }
        throw new IllegalStateException("No Id field found on class: " + getName());
    }
}
