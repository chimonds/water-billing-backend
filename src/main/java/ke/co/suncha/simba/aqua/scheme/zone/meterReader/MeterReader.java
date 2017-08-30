package ke.co.suncha.simba.aqua.scheme.zone.meterReader;

import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.aqua.scheme.zone.Zone;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 8/30/17.
 */
@Entity
@Table(name = "meter_readers")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MeterReader implements Serializable {
    @Id
    @Column(name = "meter_reader_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long meterReaderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();


    public Long getMeterReaderId() {
        return meterReaderId;
    }

    public void setMeterReaderId(Long meterReaderId) {
        this.meterReaderId = meterReaderId;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }
}