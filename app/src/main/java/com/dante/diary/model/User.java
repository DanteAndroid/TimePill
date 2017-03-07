package com.dante.diary.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yons on 17/3/3.
 */

public class User extends RealmObject{
    /**
     * id : 12
     * name : 张××
     * intro : 我是一个离开了家门
     没有工作的人
     漫无目的的四处飘荡
     寻找奇迹的人

     我也渴望着一种幸福
     名字叫作婚姻
     我也渴望着一种温馨
     名字叫作爱人

     我的家里还有个母亲
     她时时为我担心
     为了她我还有一点怕死
     不敢让她伤心
     * created : 2010-03-17 14:07:01
     * state : 1
     * iconUrl : http://tpimg.net/user_icon/0/s12.jpg?v=43
     */

    @PrimaryKey
    private int id;
    private String name;
    private String intro;
    private String created;
    private int state;
    @SerializedName("iconUrl")
    private String avatarUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
