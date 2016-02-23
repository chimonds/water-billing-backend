package ke.co.suncha.simba.aqua.models;

import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by maitha.manyala on 2/22/16.
 */
@Entity
@Table(name = "account_categories")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountCategory extends SimbaBaseEntity implements Serializable {
    @Id
    @Column(name = "category_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long categoryId;

    @NotNull
    @Column(name = "name", unique = true)
    private String name;

    @NotNull
    @Column(name = "description")
    private String description;

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
