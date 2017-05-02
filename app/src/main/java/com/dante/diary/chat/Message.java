package com.dante.diary.chat;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Date;

/**
 * Created by yons on 17/4/24.
 */

public class Message implements IMessage {

    private String id;
    private String text;
    private Author author;
    private Date createdAt;

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Author getUser() {
        return author;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}