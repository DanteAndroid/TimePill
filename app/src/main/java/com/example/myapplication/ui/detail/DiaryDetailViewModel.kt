package com.example.myapplication.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Comment
import com.example.myapplication.data.model.Diary
import com.example.myapplication.net.Status
import com.example.myapplication.ui.home.TimePillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(private val repository: TimePillRepository) :
    ViewModel() {

    private val _status = MutableLiveData<Status>()
    val status: LiveData<Status> = _status

    fun getDiary(diaryId: Int): LiveData<Diary> {
        return repository.getDiary(diaryId)
    }

    fun reportDiary(userId: Int, diaryId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.reportDiary(userId, diaryId)
            if (result.isSuccessful) {
                onSuccess.invoke()
            }
        }
    }

    fun deleteDiary(diaryId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteDiary(diaryId)
            if (result.isSuccessful) {
                onSuccess.invoke()
            }
        }
    }

    fun deleteComment(commentId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteComment(commentId)
            if (result.isSuccessful) {
                onSuccess.invoke()
            }
        }
    }

    private val _comment = MutableLiveData<Comment>()
    val comment: LiveData<Comment> = _comment

    fun postComment(diaryId: Int, data: Map<String, Any>): LiveData<Comment> {
        viewModelScope.launch {
            val result = repository.postComment(diaryId, data)
            _comment.value = result
        }
        return _comment
    }

    fun getComments(diaryId: Int): LiveData<List<Comment>> {
        return repository.getDiaryComments(diaryId)
    }


    fun fetchComments(diaryId: Int) {
        viewModelScope.launch {
            try {
                _status.value = Status.LOADING
                repository.getComments(diaryId)
                _status.value = Status.SUCCESS
            } catch (e: Exception) {
                _status.value = Status.ERROR
                e.printStackTrace()
            }
        }
    }

}