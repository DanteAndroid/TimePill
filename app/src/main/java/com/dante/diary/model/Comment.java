package com.dante.diary.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yons on 17/3/3.
 */

public class Comment {
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
    private String created;
    private UserBean user;
    private RecipientBean recipient;

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

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public RecipientBean getRecipient() {
        return recipient;
    }

    public void setRecipient(RecipientBean recipient) {
        this.recipient = recipient;
    }

    public static class UserBean {
        /**
         * id : 23
         * name : 裴痦痦是小恶魔
         */

        private int id;
        private String name;

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
    }

    public static class RecipientBean {
        /**
         * id : 12
         * name : 张××
         */

        private int id;
        private String name;

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
    }
}
