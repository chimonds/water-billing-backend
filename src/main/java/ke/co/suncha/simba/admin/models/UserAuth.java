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
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ke.co.suncha.simba.admin.helpers.SimbaBaseEntity;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 */
@Entity
@Table(name = "users_auth")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserAuth extends SimbaBaseEntity implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -3913517433663111596L;

    // An autogenerated id (unique for each user in the db)
    @Id
    @NotNull
    @Column(name = "user_auth_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long userAuthId;

    @NotNull
    @Column(name = "auth_password")
    private String authPassword;

    @NotNull
    @Column(name = "auth_last_pass_change")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastChanged = Calendar.getInstance();

    @NotNull
    @Column(name = "auth_last_access")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastAccess = Calendar.getInstance();

    @Column(name = "auth_key", length = 100)
    private String authKey;

    @NotNull
    @Column(name = "rest_auth")
    private Boolean resetAuth = true;

    public Boolean getResetAuth() {
        return resetAuth;
    }

    public void setResetAuth(Boolean resetAuth) {
        this.resetAuth = resetAuth;
    }

    /**
     * @return the authPassword
     */
    public String getAuthPassword() {
        return authPassword;
    }

    /**
     * @param authPassword the authPassword to set
     */
    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    /**
     * @return the lastChanged
     */
    public Calendar getLastChanged() {
        return lastChanged;
    }

    /**
     * @param lastChanged the lastChanged to set
     */
    public void setLastChanged(Calendar lastChanged) {
        this.lastChanged = lastChanged;
    }

    /**
     * @return the lastAccess
     */
    public Calendar getLastAccess() {
        return lastAccess;
    }

    /**
     * @param lastAccess the lastAccess to set
     */
    public void setLastAccess(Calendar lastAccess) {
        this.lastAccess = lastAccess;
    }

    /**
     * @param userAuthId the userAuthId to set
     */
    public void setUserAuthId(long userAuthId) {
        this.userAuthId = userAuthId;
    }

    /**
     * @return the userAuthId
     */
    public long getUserAuthId() {
        return userAuthId;
    }


    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UserAuth [userAuthId=" + userAuthId + ", authPassword=" + authPassword + ", dateAdded=" + this.getCreatedOn() + ", lastChanged=" + lastChanged + ", lastAccess=" + lastAccess + "]";
    }
}