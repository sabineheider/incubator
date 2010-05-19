package org.eclipse.persistence.testing.models.jpa.advanced;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Vector;

public class PhoneNumber
    implements Serializable {
    
    public enum PhoneStatus { ACTIVE, ASSIGNED, UNASSIGNED, DEAD }


    private String number;
    private String type;
    private Employee owner;
    private String areaCode;
    private Collection status;

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

}
