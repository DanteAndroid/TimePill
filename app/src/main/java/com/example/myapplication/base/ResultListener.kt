package com.example.myapplication.base

/**
 * @author Du Wenyu
 * 2021/6/6
 */
interface ResultListener {

    fun onSuccess()
    fun onFailed(message: String? = null)

}