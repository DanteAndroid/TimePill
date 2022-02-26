/*
 * Copyright 2020 Google LLC
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

package com.example.myapplication.di

import com.blankj.utilcode.util.SPUtils
import com.example.myapplication.net.NetService
import com.example.myapplication.net.TimeApi
import com.example.myapplication.util.KEY_EMAIL
import com.example.myapplication.util.KEY_PASSWORD
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Singleton
    @Provides
    @RegisterApiClass
    fun provideRegisterApi(): TimeApi {
        return NetService.registerApi!!
    }

    @Singleton
    @Provides
    @TimeApiClass
    fun provideTimeApi(): TimeApi {
        return NetService.getTimeApi(
            SPUtils.getInstance().getString(KEY_EMAIL),
            SPUtils.getInstance().getString(KEY_PASSWORD)
        )
    }

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RegisterApiClass

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TimeApiClass
