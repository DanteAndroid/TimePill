package com.example.myapplication.ui.home

import androidx.lifecycle.*
import com.example.myapplication.data.model.Diary
import com.example.myapplication.data.model.DiaryDetail
import com.example.myapplication.net.Resource
import com.example.myapplication.net.Status
import com.example.myapplication.util.CommonConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(private val repository: TimePillRepository) :
    ViewModel() {

    private val _diaries = MutableLiveData<Resource<List<Diary>>>()
    val diaries: LiveData<Resource<List<Diary>>> = _diaries

    val diariesCached = repository.getAllDiaries()
    private var page: Int = 1

    private val _sDiaries = MutableLiveData<Resource<List<Diary>>>()
    val sDiaries: LiveData<Resource<List<Diary>>> = _sDiaries

    private var diaryPage: Int = 1

    private val temp: MutableList<Diary> = mutableListOf()

    val specificDiariesCached = MediatorLiveData<List<Diary>>().apply {
        addSource(sDiaries) { it ->
            it.data?.let { diaries ->
                this.value = diaries
            }
        }
    }

    fun fetchDiaries(isRefresh: Boolean = false) {
        if (isLoading()) return

        if (isRefresh) page = 1
        _diaries.value = Resource.loading(null)
        viewModelScope.launch {
            try {
                val result = repository.getLatestDiaries(page, CommonConfig.PAGE_SIZE_DIARIES)
                _diaries.value = Resource.success(result)
                if (result.isNotEmpty()) {
                    page++
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _diaries.value = Resource.error(e.message!!, diariesCached.value)
            }
        }
    }

    fun fetchSpecificDiaries(type: Int, id: Int, isRefresh: Boolean = false) {
        if (isLoading()) return

        if (isRefresh) {
            temp.clear()
            diaryPage = 1
        }
        _sDiaries.value = Resource.loading(null)
        viewModelScope.launch {
            try {
                val result =
                    repository.getSpecificDiaries(
                        type,
                        id,
                        diaryPage,
                        CommonConfig.PAGE_SIZE_DIARIES
                    )
                temp.addAll(result)
                _sDiaries.value = Resource.success(temp)
                if (result.isNotEmpty()) {
                    diaryPage++
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _sDiaries.value = Resource.error(e.message!!, null)
            }
        }
    }

    fun getDiaryDetails(): LiveData<List<DiaryDetail>> {
        return repository.getDiaryDetails()
    }

    private fun isLoading(): Boolean = diaries.value?.status == Status.LOADING

}