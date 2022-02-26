package com.example.myapplication.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.TipResult
import com.example.myapplication.net.Resource
import com.example.myapplication.net.Status
import com.example.myapplication.ui.home.TimePillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NotificationsViewModel @Inject constructor(private val repository: TimePillRepository) :
    ViewModel() {

    private val _tipResults = MutableLiveData<Resource<List<TipResult>>>()

    val tipResults: LiveData<Resource<List<TipResult>>> = _tipResults

    private var page: Int = 1

    fun fetchNotifications(isRefresh: Boolean = false) {
        if (isLoading()) return

        if (isRefresh) page = 1
        viewModelScope.launch {
            try {
                _tipResults.value = Resource.loading(null)
                val result = repository.getNotifications()
                val history = repository.getNotificationsHistory()
                result.toMutableList().addAll(history)
                _tipResults.value = Resource.success(result)
                if (result.isNotEmpty()) {
                    page++
                }
            } catch (e: Exception) {
                _tipResults.value = Resource.error(e.message!!, null)
                e.printStackTrace()
            }
        }

    }

    private fun isLoading(): Boolean = _tipResults.value?.status == Status.LOADING

}