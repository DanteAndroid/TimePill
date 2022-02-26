/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.data.model.Comment
import com.example.myapplication.data.model.Diary
import com.example.myapplication.data.model.DiaryDetail

/**
 * The Data Access Object for the [Diary] class.
 */
@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary ORDER BY created DESC")
    fun getDiaries(): LiveData<List<Diary>>

    @Query("SELECT * FROM diary WHERE notebookId = :notebookId")
    fun getDiaries(notebookId: Int): LiveData<List<Diary>>

    @Query("SELECT *  FROM diary WHERE id = :diaryId ")
    fun getDiary(diaryId: Int): LiveData<Diary>

    /**
     * This query will tell Room to query both the [Diary] and [Comment] tables and handle
     * the object mapping.
     */
    @Transaction
    @Query("SELECT * FROM diary WHERE id IN (SELECT DISTINCT(diaryId) FROM comment)")
    fun getDiaryDetails(): LiveData<List<DiaryDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaries(diary: List<Diary>): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: Diary)

    @Delete
    suspend fun deleteDiary(diary: Diary)

    @Query("DELETE FROM diary WHERE id = :diaryId ")
    suspend fun deleteDiary(diaryId: Int)

}
