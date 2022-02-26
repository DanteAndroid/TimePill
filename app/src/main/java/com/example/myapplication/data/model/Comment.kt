package com.example.myapplication.data.model

import androidx.room.Embedded
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(
    primaryKeys = ["id"]
)
data class Comment(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("recipient_id") val recipientId: Int,
    @SerializedName("dairy_id") val diaryId: Int,
    val content: String,
    val created: Date,
    @Embedded(prefix = "comment_")
    val user: Diary.BaseUser,
    @Embedded(prefix = "recipient_")
    val recipient: Diary.BaseUser?,
    val id: Int
) {
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
}