package com.example.myapplication.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(
    primaryKeys = ["id", "notebookId"],
//    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["user_id"], childColumns = ["userId"])],
    indices = [Index("id")]
)
@Parcelize
data class Diary(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("notebook_id") val notebookId: Int,
    @SerializedName("notebook_subject") val notebookSubject: String,
    val content: String,
    val created: Date,
    val type: Int,
    @SerializedName("comment_count") val commentCount: Int,
    val photoUrl: String?,
    val photoThumbUrl: String?,
    @Embedded
    var user: BaseUser?,
    val id: Int
) : Parcelable {

    @Parcelize
    class BaseUser(
        @ColumnInfo(name = "user_id")
        val id: Int,
        val name: String,
        @SerializedName("iconUrl")
        val avatarUrl: String?
    ) : Parcelable
}


class DiariesResult<T>(
    val count: Int = 0,
    val page: Int = 0,
    val page_size: Int = 0,
    val diaries: T
)