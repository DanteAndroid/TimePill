package com.example.myapplication.data.model

import android.os.Parcelable
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(primaryKeys = ["id", "userId"])
data class Notebook(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val subject: String,
    val description: String?,
    val created: String,
    val expired: String,
    /**
     * privacy访问限制，1私密，10公开
     */
    val privacy: Int,
    val cover: Int,
    val isExpired: Boolean,
    val coverUrl: String,
    val isPublic: Boolean
) : Parcelable {
    /**
     * id : 15
     * user_id : 12
     * subject : 开发日记
     * description :
     * created : 2010-03-17
     * expired : 2013-06-18
     * privacy : 10  privacy访问限制，1私密，10公开
     * cover : 1
     * isExpired : true
     * coverUrl : http://tpimg.net/book_cover/0/15.jpg
     * isPublic : true
     */
}

class NotebooksResult<T>(
    val count: Int = 0,
    val page: Int = 0,
    val page_size: Int = 0,
    var items: T
)