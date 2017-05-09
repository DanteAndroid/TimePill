package com.dante.diary.chat;

import android.util.Log;

import com.dante.diary.model.User;
import com.stfalcon.chatkit.commons.models.IUser;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by yons on 17/4/24.
 */

public class Author implements IUser {
    private String id;
    private String name;
    private String avatar;

    public Author(User u) {
        Log.d(TAG, "Author: " + u);
        this.id = String.valueOf(u.getId());
        this.name = u.getName();
        this.avatar = u.getAvatarUrl();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}