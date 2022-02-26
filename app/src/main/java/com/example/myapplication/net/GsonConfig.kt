package com.example.myapplication.net

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonConfig {

    val dateFormat = "yyyy-MM-dd HH:mm:ss"

    val gson: Gson = GsonBuilder()
        .setDateFormat(dateFormat)
        .create()


}