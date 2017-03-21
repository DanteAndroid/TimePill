package com.dante.diary.notification;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.dante.diary.model.TipResult;

/**
 * Created by yons on 17/3/17.
 */

public class Notification implements MultiItemEntity {

    private int itemType;
    private int dairyId;
    private int commentId;
    private TipResult.Content.UserBean commentUser;
    private TipResult.Content.UserBean followUser;

    public int getDairyId() {
        return dairyId;
    }

    public void setDairyId(int dairyId) {
        this.dairyId = dairyId;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public TipResult.Content.UserBean getCommentUser() {
        return commentUser;
    }

    public void setCommentUser(TipResult.Content.UserBean commentUser) {
        this.commentUser = commentUser;
    }

    public TipResult.Content.UserBean getFollowUser() {
        return followUser;
    }

    public void setFollowUser(TipResult.Content.UserBean followUser) {
        this.followUser = followUser;
    }

    public Notification(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
