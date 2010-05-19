package org.eclipse.persistence.testing.models.jpa.fieldaccess.advanced;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Vector;
import org.eclipse.persistence.indirection.ValueHolder;
import org.eclipse.persistence.indirection.WeavedAttributeValueHolderInterface;
import org.eclipse.persistence.internal.weaving.PersistenceWeaved;
import org.eclipse.persistence.internal.weaving.PersistenceWeavedLazy;

// Referenced classes of package org.eclipse.persistence.testing.models.jpa.fieldaccess.advanced:
//            Employee

public class PhoneNumber
    implements Serializable, PersistenceWeaved, PersistenceWeavedLazy
{
    /* member class not found */
    class PhoneStatus {}


    private String number;
    private String type;
    private Employee owner;
    private String areaCode;
    private Collection status;
    protected WeavedAttributeValueHolderInterface _persistence_owner_vh;

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
            Employee employee = _persistence_get_owner();
            if(employee != _persistence_owner_vh.getValue())
            {
                _persistence_set_owner(employee);
            }
        }
        return _persistence_owner_vh;
    }

    public void _persistence_set_owner_vh(WeavedAttributeValueHolderInterface weavedattributevalueholderinterface)
    {
        _persistence_owner_vh = weavedattributevalueholderinterface;
        if(weavedattributevalueholderinterface.isInstantiated())
        {
            Employee employee = _persistence_get_owner();
            Object obj = weavedattributevalueholderinterface.getValue();
            if(employee != obj)
            {
                _persistence_set_owner((Employee)obj);
            }
        }
    }

    public Employee _persistence_get_owner()
    {
        _persistence_initialize_owner_vh();
        owner = (Employee)_persistence_owner_vh.getValue();
        return owner;
    }

    public void _persistence_set_owner(Employee employee)
    {
        _persistence_get_owner();
        owner = employee;
        _persistence_owner_vh.setValue(employee);
        return;
    }
}
