package org.eclipse.persistence.testing.models.jpa.advanced;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Vector;
import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.internal.weaving.PersistenceWeaved;
import org.eclipse.persistence.internal.weaving.PersistenceWeavedChangeTracking;

// Referenced classes of package org.eclipse.persistence.testing.models.jpa.advanced:
//            PhoneNumberPK, Employee

public class PhoneNumber
    implements Serializable, PersistenceWeaved, ChangeTracker, PersistenceWeavedChangeTracking
{
    /* member class not found */
    class PhoneStatus {}


    private String number;
    private String type;
    private Employee owner;
    private String areaCode;
    private Collection status;
    protected transient PropertyChangeListener _persistence_listener;

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

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        String s = getNumber();
        this.number = number;
        _persistence_propertyChange("number", s, number);
    }

    public Collection getStatus()
    {
        return status;
    }

    public void setStatus(Collection status)
    {
        Collection collection = getStatus();
        this.status = status;
        _persistence_propertyChange("status", collection, status);
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        String s = getType();
        this.type = type;
        _persistence_propertyChange("type", s, type);
    }

    public String getAreaCode()
    {
        return areaCode;
    }

    public void setAreaCode(String areaCode)
    {
        String s = getAreaCode();
        this.areaCode = areaCode;
        _persistence_propertyChange("areaCode", s, areaCode);
    }

    public Employee getOwner()
    {
        return owner;
    }

    public void setOwner(Employee owner)
    {
        Employee employee = getOwner();
        this.owner = owner;
        _persistence_propertyChange("owner", employee, owner);
    }

    public void removeStatus(PhoneStatus status)
    {
        getStatus().remove(status);
    }

    public List buildPK()
    {
        List pk = new Vector();
        pk.add(getOwner().getId());
        pk.add(getType());
        return pk;
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
}
