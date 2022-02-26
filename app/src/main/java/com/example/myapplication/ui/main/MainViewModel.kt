package com.example.myapplication.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Topic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepository) :
    ViewModel() {

    private val _topic = MutableLiveData<Topic>()
    val topic: LiveData<Topic> = _topic

    fun fetchTopic() {
        viewModelScope.launch {
            repository.getTopic()?.also {
                _topic.value = it
            }
        }
    }

    fun hasTopic(): Boolean {
        return _topic.value != null
    }

}