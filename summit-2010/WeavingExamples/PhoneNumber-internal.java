package org.eclipse.persistence.testing.models.jpa.advanced;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Vector;
import org.eclipse.persistence.internal.descriptors.PersistenceEntity;
import org.eclipse.persistence.internal.descriptors.PersistenceObject;
import org.eclipse.persistence.internal.weaving.PersistenceWeaved;

// Referenced classes of package org.eclipse.persistence.testing.models.jpa.advanced:
//            PhoneNumberPK, Employee

public class PhoneNumber
    implements Serializable, Cloneable, PersistenceWeaved, PersistenceEntity, PersistenceObject
{
    /* member class not found */
    class PhoneStatus {}


    private String number;
    private String type;
    private Employee owner;
    private String areaCode;
    private Collection status;
    public static final long serialVersionUID = 0x120adfdddd107d7bL;
    protected transient Object _persistence_primaryKey;

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
        this.number = number;
    }

    public Collection getStatus()
    {
        return status;
    }

    public void setStatus(Collection status)
    {
        this.status = status;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getAreaCode()
    {
        return areaCode;
    }

    public void setAreaCode(String areaCode)
    {
        this.areaCode = areaCode;
    }

    public Employee getOwner()
    {
        return owner;
    }

    public void setOwner(Employee owner)
    {
        this.owner = owner;
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

    public Object _persistence_post_clone()
    {
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
}
