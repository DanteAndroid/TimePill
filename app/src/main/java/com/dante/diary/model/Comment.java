package com.dante.diary.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by yons on 17/3/3.
 */

public class Comment extends RealmObject{
    /**
     * id : 104
     * user_id : 23
     * recipient_id : 0
     * dairy_id : 94
     * content : 为什么我没有提醒……
     * created : 2010-03-19 11:57:25
     * user : {"id":23,"name":"裴痦痦是小恶魔"}
     * recipient : {"id":12,"name":"张××"}
     */

    private int id;
    @SerializedName("user_id")
    private int userIid;
    @SerializedName("recipient_id")
    private int recipientId;
    @SerializedName("dairy_id")
    private int dairyId;
    private String content;
    private Date created;
    private User user;
    private User recipient;

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserIid() {
        return userIid;
    }

    public void setUserIid(int userIid) {
        this.userIid = userIid;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public int getDairyId() {
        return dairyId;
    }

    public void setDairyId(int dairyId) {
        this.dairyId = dairyId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


}
