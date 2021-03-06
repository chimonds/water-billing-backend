/*
 * The MIT License
 *
 * Copyright 2015 Maitha Manyala.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ke.co.suncha.simba.admin.models;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * User role entity class
 *
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Entity
@Table(name = "user_roles")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonSerialize
public class UserRole extends SimbaBaseEntity implements Serializable {
    // ==============
    // PRIVATE FIELDS
    // ==============

    // An autogenerated id (unique for each user in the db)
    @Id
    @NotNull
    @Column(name = "user_role_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long userRoleId;

    // The user role name
    @NotNull
    @Column(name = "role_name", unique = true)
    private String name = "Not Assigned";

    // The user role description
    @Column(name = "role_description")
    private String description = "";


    @JsonIgnore
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "user_role_system_actions")
    private List<SystemAction> systemActions;


    // ==============
    // PUBLIC FIELDS
    // ==============

    /**
     * @return the systemActions
     */
    public List<SystemAction> getSystemActions() {
        return systemActions;
    }

    /**
     * @param systemActions the systemActions to set
     */
    public void setSystemActions(List<SystemAction> systemActions) {
        this.systemActions = systemActions;
    }


    /**
     * @return the userRoleId
     */
    public long getUserRoleId() {
        return userRoleId;
    }

    /**
     * @param userRoleId the userRoleId to set
     */
    public void setUserRoleId(long userRoleId) {
        this.userRoleId = userRoleId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UserRole [userRoleId=" + userRoleId + ", name=" + name + ", description=" + description + ", dateAdded=" + this.getCreatedOn() + ", systemActions=" + systemActions + "]";
    }


}