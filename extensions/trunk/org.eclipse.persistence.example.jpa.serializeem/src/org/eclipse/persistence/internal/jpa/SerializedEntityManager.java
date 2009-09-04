package org.eclipse.persistence.internal.jpa;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.internal.sessions.ObjectChangeSet;
import org.eclipse.persistence.internal.sessions.RepeatableWriteUnitOfWork;
import org.eclipse.persistence.internal.sessions.UnitOfWorkChangeSet;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.1.2 Example
 */
public class SerializedEntityManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionName;

    private Map<?, ?> properties;

    private Map<Object, Object> cloneMappings;

    private Map<Object, Object> newObjectsCloneToOriginal;

    private Map<Object, ObjectChangeSet> changeSets;

    @SuppressWarnings("unchecked")
    public SerializedEntityManager(EntityManagerImpl entityManager) {
        this.sessionName = entityManager.getServerSession().getName();
        this.properties = entityManager.properties;

        if (entityManager.extendedPersistenceContext != null) {
            RepeatableWriteUnitOfWork uow = (RepeatableWriteUnitOfWork) entityManager.getUnitOfWork();
            this.cloneMappings = uow.getCloneMapping();
            this.newObjectsCloneToOriginal = uow.getNewObjectsCloneToOriginal();
            
            UnitOfWorkChangeSet uowcs = (UnitOfWorkChangeSet) uow.getUnitOfWorkChangeSet();
            if (uowcs != null) {
                this.changeSets = uowcs.getCloneToObjectChangeSet();
            }
        }
    }

    public String getSessionName() {
        return this.sessionName;
    }

    public Map<?, ?> getProperties() {
        return properties;
    }

    public Map<Object, Object> getCloneMappings() {
        return cloneMappings;
    }

    public Map<Object, ObjectChangeSet> getChangeSets() {
        return this.changeSets;
    }

    public Map<Object, Object> getNewObjectsCloneToOriginal() {
        return this.newObjectsCloneToOriginal;
    }

    public Object readResolve() throws ObjectStreamException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(getSessionName());
        EntityManagerImpl em = (EntityManagerImpl) emf.createEntityManager(this.properties);

        return new EntityManagerHandle(em, getCloneMappings(), getChangeSets(), getNewObjectsCloneToOriginal());
    }
}
