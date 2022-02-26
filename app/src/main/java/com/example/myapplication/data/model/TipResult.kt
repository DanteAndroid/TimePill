package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

class TipResult(
    val id: Int,
    val user_id: Int,
    val link_id: Int,
    val link_user_id: Int,
    val itemType: Int,
    val content: Content,
    val created: Date,
    val read: Int
) {
    /**
     * 是评论还是关注
     */
    val isComment: Boolean get() = itemType == TYPE_COMMENT

    /**
     * id : 19981849
     * user_id : 100158434
     * link_id : 12
     * link_user_id : 100158434
     * type : 2
     * content : {"followUser":{"id":12,"name":"张××"}}
     * created : 2017-03-17 16:37:55
     * read : 0
     */

    inner class Content(
        @SerializedName("dairy_id")
        val dairyId: Int,
        @SerializedName("comment_id")
        val commentId: Int,
        @SerializedName("author")
        val commentUser: UserBean? = null,
        @SerializedName("user")
        val followUser: UserBean? = null
    ) {
        /**
         * dairyId : 11299719
         * commentId : 19568450
         * author : {"name":"张××","id":12}
         * user : {"id":12,"name":"张××"}
         */

        inner class UserBean(
            val id: Int = 0,
            val name: String
        ) {
            /**
             * id : 12
             * name : 张××
             */
        }
    }

    companion object {
        const val TYPE_FOLLOW = 2
        const val TYPE_COMMENT = 1
    }
}