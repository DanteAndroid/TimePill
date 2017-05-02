package com.dante.diary.model;

/**
 * Created by yons on 17/4/28.
 */

public class Topic {

    /**
     * id : 1384
     * title : 假期
     * intro : 假期你准备去哪？
     * created : 2017-04-28 00:00:00
     * imageUrl : http://s.timepill.net/s/w640/topic/qznp8u.jpg
     */

    private int id;
    private String title;
    private String intro;
    private String created;
    private String imageUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
