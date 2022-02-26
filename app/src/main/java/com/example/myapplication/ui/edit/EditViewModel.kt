package com.example.myapplication.ui.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Diary
import com.example.myapplication.data.model.Notebook
import com.example.myapplication.net.NetService
import com.example.myapplication.ui.home.TimePillRepository
import com.example.myapplication.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(private val repository: TimePillRepository) :
    ViewModel() {

    fun fetchNotebooks(userId: Int): LiveData<List<Notebook>> {
        viewModelScope.safeLaunch {
            repository.getNotebooks(userId)
        }
        return repository.getAllNotebooks(userId)
    }

    private val _notebook: MutableLiveData<Notebook> = MutableLiveData()

    fun createNotebook(data: Map<String, Any>, coverFile: File?): LiveData<Notebook> {
        viewModelScope.safeLaunch {
            val result = repository.createNotebook(data)
            _notebook.value = result
            updateCover(result, coverFile)
        }
        return _notebook
    }

    fun updateNotebook(
        notebookId: Int,
        data: Map<String, Any>,
        coverFile: File?
    ): LiveData<Notebook> {
        viewModelScope.safeLaunch {
            val result = repository.updateNotebook(notebookId, data)
            _notebook.value = result
            updateCover(result, coverFile)
        }
        return _notebook
    }

    private val _diary: MutableLiveData<Diary> = MutableLiveData()

    fun createDiary(
        notebookId: Int,
        content: String,
        isTopic: Boolean = false,
        file: File? = null,
    ): LiveData<Diary> {
        viewModelScope.safeLaunch {
            val result = repository.createDiary(
                notebookId,
                NetService.getRequestBody(content),
                if (isTopic) NetService.getRequestBody("1") else null,
                file?.let { NetService.createMultiPart("photo", it) })
            _diary.value = result
        }
        return _diary
    }

    fun updateDiary(
        diaryId: Int, content: String, notebookId: Int
    ): LiveData<Diary> {
        viewModelScope.safeLaunch {
            val result = repository.updateDiary(diaryId, content, notebookId)
            _diary.value = result
        }
        return _diary
    }

    private val deleteSuccess: MutableLiveData<Boolean> = MutableLiveData()

    @Suppress("BlockingMethodInNonBlockingContext")
    fun deleteNotebook(notebookId: Int): LiveData<Boolean> {
        viewModelScope.safeLaunch {
            val result = repository.deleteNotebook(notebookId)
            deleteSuccess.value = result.isSuccessful
        }
        return deleteSuccess
    }

    private fun updateCover(result: Notebook, coverFile: File?) {
        if (coverFile == null) return
        viewModelScope.safeLaunch {
            repository.setNotebookCover(
                result.id,
                NetService.createMultiPart("cover", coverFile)
            )
        }
    }


}