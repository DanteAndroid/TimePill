package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(primaryKeys = ["user_id"])
data class User(
    @ColumnInfo(name = "user_id")
    val id: Int,
    val name: String,
    val intro: String?,
    @ColumnInfo(name = "user_created")
    val created: Date?,
    val state: Int,
    @SerializedName("iconUrl")
    val avatarUrl: String
) {

    fun toBaseUser(): Diary.BaseUser {
        return Diary.BaseUser(id, name, avatarUrl)
    }
    /**
     * id : 12
     * name : 张××
     * intro : 我是一个离开了家门
     * 没有工作的人
     * 漫无目的的四处飘荡
     * 寻找奇迹的人
     *
     * 我也渴望着一种幸福
     * 名字叫作婚姻
     * 我也渴望着一种温馨
     * 名字叫作爱人
     *
     * 我的家里还有个母亲
     * 她时时为我担心
     * 为了她我还有一点怕死
     * 不敢让她伤心
     * created : 2010-03-17 14:07:01
     * state : 1
     * iconUrl : http://tpimg.net/user_icon/0/s12.jpg?v=43
     */
}

class UsersResult<T>(val count: Int, val users: T)
