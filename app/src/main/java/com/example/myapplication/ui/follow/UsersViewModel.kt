package com.example.myapplication.ui.follow

import androidx.lifecycle.*
import com.example.myapplication.data.model.Notebook
import com.example.myapplication.data.model.User
import com.example.myapplication.net.Resource
import com.example.myapplication.net.Status
import com.example.myapplication.ui.home.TimePillRepository
import com.example.myapplication.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UsersViewModel @Inject constructor(private val repository: TimePillRepository) :
    ViewModel() {

    private val _followers = MutableLiveData<Resource<List<User>>>()
    val followers: LiveData<Resource<List<User>>> = _followers

    private var page: Int = 1

    val isLoadingMore get() = page > 1

    fun fetchFollowers(isFollowing: Boolean = false, isRefresh: Boolean = false) {
        if (isLoading()) return

        if (isRefresh) page = 1
        viewModelScope.launch {
            try {
                _followers.value = Resource.loading(null)
                val result =
                    if (isFollowing) repository.getFollowings(page)
                    else repository.getFollowers(page)
                _followers.value = Resource.success(result.users)
                if (result.count > 0) {
                    page++
                }
            } catch (e: Exception) {
                _followers.value = Resource.error(e.message!!, null)
                e.printStackTrace()
            }
        }
    }

    private val user: MediatorLiveData<Resource<User>> = MediatorLiveData()

    fun fetchUser(userId: Int): LiveData<Resource<User>> {
        viewModelScope.launch {
            try {
                val result = repository.getProfile(userId)
                user.addSource(result) {
                    if (user.value?.data != it) {
                        user.value = Resource.success(it)
                    }
                }
            } catch (e: Exception) {
                user.value = Resource.error(e.message!!, null)
                e.printStackTrace()
            }
        }
        return user
    }

    private val notebooks: MutableLiveData<Resource<List<Notebook>>> = MutableLiveData()

    fun fetchNotebooks(userId: Int): LiveData<Resource<List<Notebook>>> {
        viewModelScope.launch {
            try {
                val result = repository.getNotebooks(userId)
                notebooks.value = Resource.success(result)
            } catch (e: Exception) {
                user.value = Resource.error(e.message!!, null)
                e.printStackTrace()
            }
        }
        return notebooks
    }

    fun cancelFollow(userId: Int, isFollowed: Boolean = false) {
        viewModelScope.safeLaunch {
            val result = if (isFollowed) repository.cancelFollowed(userId) else
                repository.cancelFollow(userId)
        }
    }

    private val followSuccess: MutableLiveData<Boolean> = MutableLiveData()

    fun follow(userId: Int): MutableLiveData<Boolean> {
        viewModelScope.safeLaunch {
            val result = repository.follow(userId).body()?.string()
            followSuccess.value = !result.isNullOrEmpty()
        }
        return followSuccess
    }

    private val hasFollow: MutableLiveData<Boolean> = MutableLiveData()

    @Suppress("BlockingMethodInNonBlockingContext")
    fun hasFollow(userId: Int): MutableLiveData<Boolean> {
        viewModelScope.safeLaunch {
            val result = repository.hasFollow(userId).body()?.string()
            hasFollow.value = !result.isNullOrEmpty()
        }
        return hasFollow
    }

    private fun isLoading(): Boolean = _followers.value?.status == Status.LOADING
}