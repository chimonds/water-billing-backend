package ke.co.suncha.simba.aqua.account.scheme;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 7/27/16.
 */
@Entity
@Table(name = "account_schemes")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Scheme implements Serializable {
    @Id
    @Column(name = "scheme_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long schemeId;

    @NotNull
    @Column(name = "name", unique = true)
    private String name;

    public Long getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
