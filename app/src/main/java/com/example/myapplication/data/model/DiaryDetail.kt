package com.example.myapplication.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * @author Dante
 * 2020/12/11
 */
data class DiaryDetail(

    @Embedded
    val diary: Diary,

    @Relation(parentColumn = "id", entityColumn = "diaryId")
    val comments: List<Comment> = emptyList()

)