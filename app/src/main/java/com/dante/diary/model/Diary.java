package com.dante.diary.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yons on 17/3/3.
 */

public class Diary extends RealmObject {
    /**{
     * "id":11242696,
     * "user_id":100158137,
     * "notebook_id":516405,
     * "notebook_subject":"不要想太多",
     * "content":"犀利本性不用收敛 在这里放心放飞自我～",
     * "created":"2017-03-06 15:11:55",
     * "updated":"2017-03-06 15:11:55",
     * "type":2,
     * "comment_count":0,
     * "photoUrl":"http://s.timepill.net/s/w640/photos/2017-03-06/gxfnljgz.jpg",
     * "photoThumbUrl":"http://s.timepill.net/s/w240-h320/photos/2017-03-06/gxfnljgz.jpg",
     * "user":{
     * "id":100158137,
     * "name":"向隅",
     * "iconUrl":"http://s.timepill.net/user_icon/20031/s100158137.jpg?v=50"
     * }
     * }
     */

    @PrimaryKey
    private int id;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("notebook_id")
    private int notebookId;
    @SerializedName("notebook_subject")
    private String notebookSubject;
    private String content;
    private Date created;
    private int type;
    @SerializedName("comment_count")
    private int commentCount;
    private String photoUrl;
    private String photoThumbUrl;
    private User user;

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

    public int getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(int notebookId) {
        this.notebookId = notebookId;
    }

    public String getNotebookSubject() {
        return notebookSubject;
    }

    public void setNotebookSubject(String notebookSubject) {
        this.notebookSubject = notebookSubject;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoThumbUrl() {
        return photoThumbUrl;
    }

    public void setPhotoThumbUrl(String photoThumbUrl) {
        this.photoThumbUrl = photoThumbUrl;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

 
}
