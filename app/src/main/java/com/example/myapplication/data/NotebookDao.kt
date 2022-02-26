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
import com.example.myapplication.data.model.Notebook

/**
 * The Data Access Object for the [Notebook] class.
 */
@Dao
interface NotebookDao {

    @Query("SELECT * FROM Notebook where userId = :userId")
    fun getNotebooks(userId: Int): LiveData<List<Notebook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotebooks(Notebook: List<Notebook>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotebook(Notebook: Notebook)

    @Delete
    suspend fun deleteNotebook(notebookId: Notebook)

    @Query("DELETE FROM Notebook WHERE id = :notebookId ")
    suspend fun deleteNotebook(notebookId: Int)

}
