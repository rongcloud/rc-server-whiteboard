package com.rcloud.server.whiteboard.domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Table(name = "users")
public class Users implements Serializable {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    @Column(name="userId")
    private String userId;

    private String region;

    private String phone;

    private String nickname;

    @Column(name="portraitUri")
    private String portraitUri;

    @Column(name="passwordHash")
    private String passwordHash;

    @Column(name="passwordSalt")
    private String passwordSalt;

    @Column(name="rongCloudToken")
    private String rongCloudToken;


    @Column(name="stAccount")
    private String stAccount;



    private Long timestamp;

    @Column(name="createdAt")
    private Date createdAt;

    @Column(name="updatedAt")
    private Date updatedAt;




    private static final long serialVersionUID = 1L;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * @return phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @param nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * @return portraitUri
     */
    public String getPortraitUri() {
        return portraitUri;
    }

    /**
     * @param portraitUri
     */
    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    /**
     * @return passwordHash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * @param passwordHash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * @return passwordSalt
     */
    public String getPasswordSalt() {
        return passwordSalt;
    }

    /**
     * @param passwordSalt
     */
    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    /**
     * @return rongCloudToken
     */
    public String getRongCloudToken() {
        return rongCloudToken;
    }

    /**
     * @param rongCloudToken
     */
    public void setRongCloudToken(String rongCloudToken) {
        this.rongCloudToken = rongCloudToken;
    }


    /**
     * @param stAccount
     */
    public void setStAccount(String stAccount) {
        this.stAccount = stAccount;
    }


    /**
     * @return timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return createdAt
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @param updatedAt
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", region='" + region + '\'' +
                ", phone='" + phone + '\'' +
                ", nickname='" + nickname + '\'' +
                ", portraitUri='" + portraitUri + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", passwordSalt='" + passwordSalt + '\'' +
                ", rongCloudToken='" + rongCloudToken + '\'' +

                ", stAccount='" + stAccount + '\'' +

                ", timestamp=" + timestamp +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}