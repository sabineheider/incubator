package org.eclipse.persistence.testing.models.jpa.advanced;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Vector;
import org.eclipse.persistence.internal.weaving.PersistenceWeaved;
import org.eclipse.persistence.internal.weaving.PersistenceWeavedFetchGroups;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

// Referenced classes of package org.eclipse.persistence.testing.models.jpa.advanced:
//            PhoneNumberPK, Employee

public class PhoneNumber
    implements Serializable, PersistenceWeaved, FetchGroupTracker, PersistenceWeavedFetchGroups
{
    /* member class not found */
    class PhoneStatus {}


    private String number;
    private String type;
    private Employee owner;
    private String areaCode;
    private Collection status;
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

    public String getNumber()
    {
        _persistence_checkFetched("number");
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public Collection getStatus()
    {
        _persistence_checkFetched("status");
        return status;
    }

    public void setStatus(Collection status)
    {
        this.status = status;
    }

    public String getType()
    {
        _persistence_checkFetched("type");
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getAreaCode()
    {
        _persistence_checkFetched("areaCode");
        return areaCode;
    }

    public void setAreaCode(String areaCode)
    {
        this.areaCode = areaCode;
    }

    public Employee getOwner()
    {
        _persistence_checkFetched("owner");
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
