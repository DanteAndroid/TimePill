package com.dante.diary.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yons on 17/3/3.
 */

public class Notebook extends RealmObject{

    /**
     * id : 15
     * user_id : 12
     * subject : 开发日记
     * description :
     * created : 2010-03-17
     * expired : 2013-06-18
     * privacy : 10  privacy访问限制，1私密，10公开
     * cover : 1
     * isExpired : true
     * coverUrl : http://tpimg.net/book_cover/0/15.jpg
     * isPublic : true
     */

    @PrimaryKey
    private int id;
    @SerializedName("user_id")
    private int userId;
    private String subject;
    private String description;
    private String created;
    private String expired;
    private int privacy;
    private int cover;
    private boolean isExpired;
    private String coverUrl;
    private boolean isPublic;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    /**
     * privacy访问限制，1私密，10公开
     * @return privacy的 int值
     */
    public int getPrivacy() {
        return privacy;
    }

    public void setPrivacy(int privacy) {
        this.privacy = privacy;
    }

    public int getCover() {
        return cover;
    }

    public void setCover(int cover) {
        this.cover = cover;
    }

    public boolean isIsExpired() {
        return isExpired;
    }

    public void setIsExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public boolean isIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
