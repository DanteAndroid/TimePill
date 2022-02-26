package com.example.myapplication.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.User
import com.example.myapplication.net.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: LoginRepository) :
    ViewModel() {

    private val _login = MutableLiveData<Resource<User>>()

    val login: LiveData<Resource<User>> = _login

    fun register(email: String, password: String, name: String) {
        _login.value = Resource.loading(null)
        viewModelScope.launch {
            try {
                val user = repository.register(email, password, name)
                _login.value = Resource.success(user)
            } catch (e: Exception) {
                _login.value = Resource.error(e.message!!, null)
            }
        }
    }

    fun login(email: String, password: String) {
        _login.value = Resource.loading(null)
        viewModelScope.launch {
            try {
                val result = repository.login(email, password)
                _login.value = Resource.success(result)
            } catch (e: Exception) {
                _login.value = Resource.error(e.message!!, null)
            }
        }
    }


}