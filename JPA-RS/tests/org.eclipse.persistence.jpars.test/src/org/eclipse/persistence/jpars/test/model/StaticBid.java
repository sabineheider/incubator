package org.eclipse.persistence.jpars.test.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.PrivateOwned;

@Entity
@Table(name="ST_AUC_BID")
public class StaticBid {

    @Id
    @GeneratedValue
    private int id;
    
    private double bid;
    
    private long time;
    
  @Transient
  //  @PrivateOwned
    private StaticUser user;
 
  @Transient
   // @PrivateOwned
    private StaticAuction auction;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public StaticUser getUser() {
        return user;
    }

    public void setUser(StaticUser user) {
        this.user = user;
    }

    public StaticAuction getAuction() {
        return auction;
    }

    public void setAuction(StaticAuction auction) {
        this.auction = auction;
    }

}
