package model.meta;

import static javax.persistence.CascadeType.ALL;

import java.util.*;

import javax.persistence.*;

import org.eclipse.persistence.annotations.TypeConverter;
import org.eclipse.persistence.dynamic.EntityType;
import org.eclipse.persistence.internal.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.dynamic.EntityTypeImpl;
import org.eclipse.persistence.internal.jpa.CMP3Policy;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.sessions.server.Server;
import org.eclipse.persistence.tools.schemaframework.*;

@Entity
@Table(name = "CUSTOM_TYPE")
@TypeConverter(name = "class-string", dataType = String.class, objectType = Class.class)
public class CustomType {

    @Id
    @Column(name="TYPE_NAME")
    private String name;

    @Column(name="CLASS_NAME")
    private String className;

    @ManyToOne
    @JoinColumn(name = "PARENT_TYPE")
    private CustomType parentType;

    @OneToMany(mappedBy = "parentType", cascade = ALL)
    private List<CustomType> subTypes;

    private String typeIndicator;

    @OneToMany(mappedBy = "type", cascade = ALL)
    private List<CustomField> fields;

    @Column(name = "TABLE_NAME")
    private String tableName;

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
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public CustomField addField(String name, Class javaType, String fieldName) {
        CustomField field = new CustomField(name, javaType, fieldName, this);
        getFields().add(field);
        return field;
    }

    public CustomField addRelationship(String name, Class javaType, CustomType referenceType, String fieldName) {
        CustomField field = new CustomRelationship(name, javaType, referenceType, fieldName, this);
        getFields().add(field);
        return field;
    }

    /**
     * 
     * @param emf
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
        DynamicClassLoader loader = DynamicClassLoader.getLoader(session);
        Class dynamicClass = loader.createDynamicClass(getClassName());

        EntityTypeImpl entityType = new EntityTypeImpl(dynamicClass, getTableName());
        entityType.getDescriptor().setAlias(getName());

        for (CustomField field : getFields()) {
            field.addToType(entityType);
        }
        
        if (entityType.getDescriptor().getCMPPolicy() == null) {
            entityType.getDescriptor().setCMPPolicy(new CMP3Policy());
        }
        
        session.addDescriptor(entityType.getDescriptor());

        if (createSchema) {
            TableCreator creator = new DefaultTableGenerator(session.getProject()).generateDefaultTableCreator();
            
            TableDefinition tableDef = null;
            for (Iterator i = creator.getTableDefinitions().iterator(); tableDef == null && i.hasNext();) {
                TableDefinition td = (TableDefinition) i.next();
                if (td.getName().equalsIgnoreCase(getTableName())) {
                    tableDef = td;
                }
            }
            
            new SchemaManager(session).replaceObject(tableDef);
        }
        
        return entityType;
    }
}
