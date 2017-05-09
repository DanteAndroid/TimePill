package com.dante.diary.chat;

import com.dante.diary.login.LoginManager;
import com.dante.diary.model.DataBase;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

/**
 * Created by yons on 17/4/24.
 */

public class Message implements IMessage, MessageContentType.Image {
    private String imageUrl;
    private String id;
    private String text;
    private Author author;
    private Date createdAt;

    public Message() {
    }

    public Message(String id, String text, Author author, Date createdAt) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.createdAt = createdAt;
    }

    public static Message getMessageFromMe(String text) {
        return new Message(LoginManager.getMyStringId(), text, new Author(LoginManager.getMyUser()), new Date());
    }

    public static Message getMessageFromOther(String id, String text) {
        return new Message(id, text, new Author(DataBase.getInstance().findUser(Integer.parseInt(id))), new Date());
    }

    @Override
    public String toString() {
        return "Message{" +
                "imageUrl='" + imageUrl + '\'' +
                ", id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", author=" + author +
                ", createdAt=" + createdAt +
                '}';
    }

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

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}