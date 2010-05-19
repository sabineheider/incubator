package org.eclipse.persistence.testing.models.jpa.advanced;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Vector;
import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.indirection.ValueHolder;
import org.eclipse.persistence.indirection.WeavedAttributeValueHolderInterface;
import org.eclipse.persistence.internal.descriptors.PersistenceEntity;
import org.eclipse.persistence.internal.descriptors.PersistenceObject;
import org.eclipse.persistence.internal.weaving.*;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

// Referenced classes of package org.eclipse.persistence.testing.models.jpa.advanced:
//            PhoneNumberPK, Employee

public class PhoneNumber
    implements Serializable, Cloneable, PersistenceWeaved, PersistenceEntity, PersistenceObject, FetchGroupTracker, PersistenceWeavedFetchGroups, PersistenceWeavedLazy, ChangeTracker, PersistenceWeavedChangeTracking
{
    /* member class not found */
    class PhoneStatus {}


    private String number;
    private String type;
    private Employee owner;
    private Integer id;
    private String areaCode;
    private Collection status;
    public static final long serialVersionUID = 0x120adfdddd107d7bL;
    protected transient Object _persistence_primaryKey;
    protected WeavedAttributeValueHolderInterface _persistence_owner_vh;
    protected transient PropertyChangeListener _persistence_listener;
    protected transient FetchGroup _persistence_fetchGroup;
    protected transient boolean _persistence_shouldRefreshFetchGroup;
    protected transient Session _persistence_session;

    public PhoneNumber()
    {
        this("", "###", "#######");
    }

    public PhoneNumber(String type, String theAreaCode, String theNumber)
    {
        this.type = type;
        areaCode = theAreaCode;
        number = theNumber;
        owner = null;
        status = new Vector();
    }

    public void addStatus(PhoneStatus status)
    {
        getStatus().add(status);
    }

    public Integer getId()
    {
        return _persistence_get_id();
    }

    public void setId(Integer id)
    {
        _persistence_set_id(id);
    }

    public String getNumber()
    {
        return _persistence_get_number();
    }

    public void setNumber(String number)
    {
        _persistence_set_number(number);
    }

    public Collection getStatus()
    {
        return _persistence_get_status();
    }

    public void setStatus(Collection status)
    {
        _persistence_set_status(status);
    }

    public String getType()
    {
        return _persistence_get_type();
    }

    public void setType(String type)
    {
        _persistence_set_type(type);
    }

    public String getAreaCode()
    {
        return _persistence_get_areaCode();
    }

    public void setAreaCode(String areaCode)
    {
        _persistence_set_areaCode(areaCode);
    }

    public Employee getOwner()
    {
        return _persistence_get_owner();
    }

    public void setOwner(Employee owner)
    {
        _persistence_set_owner(owner);
    }

    public void removeStatus(PhoneStatus status)
    {
        getStatus().remove(status);
    }

    public String toString()
    {
        StringWriter writer = new StringWriter();
        writer.write("PhoneNumber[");
        writer.write(getType());
        writer.write("]: (");
        writer.write(getAreaCode());
        writer.write(") ");
        int numberLength = getNumber().length();
        writer.write(getNumber().substring(0, Math.min(3, numberLength)));
        if(numberLength > 3)
        {
            writer.write("-");
            writer.write(getNumber().substring(3, Math.min(7, numberLength)));
        }
        return writer.toString();
    }

    public PhoneNumberPK buildPK()
    {
        PhoneNumberPK pk = new PhoneNumberPK();
        pk.setId(getOwner().getId());
        pk.setType(getType());
        return pk;
    }

    public Object _persistence_post_clone()
    {
        if(_persistence_owner_vh != null)
        {
            _persistence_owner_vh = (WeavedAttributeValueHolderInterface)_persistence_owner_vh.clone();
        }
        _persistence_listener = null;
        _persistence_fetchGroup = null;
        _persistence_session = null;
        _persistence_primaryKey = null;
        return this;
    }

    public Object _persistence_shallow_clone()
    {
        return super.clone();
    }

    public Object _persistence_getId()
    {
        return _persistence_primaryKey;
    }

    public void _persistence_setId(Object obj)
    {
        _persistence_primaryKey = obj;
    }

    public Object _persistence_new(PersistenceObject persistenceobject)
    {
        return new PhoneNumber(persistenceobject);
    }

    public PhoneNumber(PersistenceObject persistenceobject)
    {
    }

    public Object _persistence_get(String s)
    {
        if(s == "id")
        {
            return id;
        }
        if(s == "status")
        {
            return status;
        }
        if(s == "areaCode")
        {
            return areaCode;
        }
        if(s == "owner")
        {
            return owner;
        }
        if(s == "number")
        {
            return number;
        }
        if(s == "type")
        {
            return type;
        } else
        {
            return null;
        }
    }

    public void _persistence_set(String s, Object obj)
    {
        if(s == "id")
        {
            id = (Integer)obj;
            return;
        }
        if(s == "status")
        {
            status = (Collection)obj;
            return;
        }
        if(s == "areaCode")
        {
            areaCode = (String)obj;
            return;
        }
        if(s == "owner")
        {
            owner = (Employee)obj;
            return;
        }
        if(s == "number")
        {
            number = (String)obj;
            return;
        }
        if(s == "type")
        {
            type = (String)obj;
            return;
        } else
        {
            return;
        }
    }

    public Integer _persistence_get_id()
    {
        _persistence_checkFetched("id");
        return id;
    }

    public void _persistence_set_id(Integer integer)
    {
        _persistence_get_id();
        _persistence_propertyChange("id", id, integer);
        id = integer;
        return;
    }

    public Collection _persistence_get_status()
    {
        _persistence_checkFetched("status");
        return status;
    }

    public void _persistence_set_status(Collection collection)
    {
        _persistence_get_status();
        _persistence_propertyChange("status", status, collection);
        status = collection;
        return;
    }

    public String _persistence_get_areaCode()
    {
        _persistence_checkFetched("areaCode");
        return areaCode;
    }

    public void _persistence_set_areaCode(String s)
    {
        _persistence_get_areaCode();
        _persistence_propertyChange("areaCode", areaCode, s);
        areaCode = s;
        return;
    }

    protected void _persistence_initialize_owner_vh()
    {
        if(_persistence_owner_vh == null)
        {
            _persistence_owner_vh = new ValueHolder(owner);
            _persistence_owner_vh.setIsNewlyWeavedValueHolder(true);
        }
    }

    public WeavedAttributeValueHolderInterface _persistence_get_owner_vh()
    {
        _persistence_initialize_owner_vh();
        if(_persistence_owner_vh.isCoordinatedWithProperty() || _persistence_owner_vh.isNewlyWeavedValueHolder())
        {
            Employee employee = (Employee)getOwner();
            if(employee != _persistence_owner_vh.getValue())
            {
                setOwner(employee);
            }
        }
        return _persistence_owner_vh;
    }

    public void _persistence_set_owner_vh(WeavedAttributeValueHolderInterface weavedattributevalueholderinterface)
    {
        _persistence_owner_vh = weavedattributevalueholderinterface;
        if(weavedattributevalueholderinterface.isInstantiated())
        {
            Employee employee = getOwner();
            Object obj = weavedattributevalueholderinterface.getValue();
            if(employee != obj)
            {
                setOwner((Employee)obj);
            }
        }
    }

    public Employee _persistence_get_owner()
    {
        _persistence_checkFetched("owner");
        _persistence_initialize_owner_vh();
        owner = (Employee)_persistence_owner_vh.getValue();
        return owner;
    }

    public void _persistence_set_owner(Employee employee)
    {
        _persistence_get_owner();
        _persistence_propertyChange("owner", owner, employee);
        owner = employee;
        _persistence_owner_vh.setValue(employee);
        return;
    }

    public String _persistence_get_number()
    {
        _persistence_checkFetched("number");
        return number;
    }

    public void _persistence_set_number(String s)
    {
        _persistence_get_number();
        _persistence_propertyChange("number", number, s);
        number = s;
        return;
    }

    public String _persistence_get_type()
    {
        _persistence_checkFetched("type");
        return type;
    }

    public void _persistence_set_type(String s)
    {
        _persistence_get_type();
        _persistence_propertyChange("type", type, s);
        type = s;
        return;
    }

    public PropertyChangeListener _persistence_getPropertyChangeListener()
    {
        return _persistence_listener;
    }

    public void _persistence_setPropertyChangeListener(PropertyChangeListener propertychangelistener)
    {
        _persistence_listener = propertychangelistener;
    }

    public void _persistence_propertyChange(String s, Object obj, Object obj1)
    {
        if(_persistence_listener != null && obj != obj1)
        {
            _persistence_listener.propertyChange(new PropertyChangeEvent(this, s, obj, obj1));
        }
    }

    public Session _persistence_getSession()
    {
        return _persistence_session;
    }

    public void _persistence_setSession(Session session)
    {
        _persistence_session = session;
    }

    public FetchGroup _persistence_getFetchGroup()
    {
        return _persistence_fetchGroup;
    }

    public void _persistence_setFetchGroup(FetchGroup fetchgroup)
    {
        _persistence_fetchGroup = fetchgroup;
    }

    public boolean _persistence_shouldRefreshFetchGroup()
    {
        return _persistence_shouldRefreshFetchGroup;
    }

    public void _persistence_setShouldRefreshFetchGroup(boolean flag)
    {
        _persistence_shouldRefreshFetchGroup = flag;
    }

    public void _persistence_resetFetchGroup()
    {
    }

    public boolean _persistence_isAttributeFetched(String s)
    {
        return _persistence_fetchGroup == null || _persistence_fetchGroup.containsAttribute(s);
    }

    public void _persistence_checkFetched(String s)
    {
        if(!_persistence_isAttributeFetched(s))
        {
            JpaHelper.loadUnfetchedObject(this);
        }
    }
}
