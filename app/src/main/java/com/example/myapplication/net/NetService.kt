package com.example.myapplication.net

import android.text.TextUtils
import com.example.myapplication.BuildConfig
import com.example.myapplication.util.BASE_URL
import com.example.myapplication.util.HttpsUtils
import okhttp3.Credentials.basic
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

object NetService {
    private var retrofit: Retrofit? = null
    private val client: OkHttpClient.Builder = OkHttpClient.Builder()
    private var builder: Retrofit.Builder? = null
    private var api: TimeApi? = null
    var registerApi: TimeApi? = null
        get() {
            field = createService(TimeApi::class.java)
            return field
        }
        private set

    private fun <T> createService(serviceClass: Class<T>): T {
        return createService(serviceClass, null, null)
    }

    fun <T> createServiceWithBaseUrl(apiClass: Class<T>?, baseUrl: String): T {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36"
                    )
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            })
            .addInterceptor(logging).build()
        builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonConfig.gson))
        return builder!!.build().create(apiClass)
    }

    private fun <T> createService(
        serviceClass: Class<T>, username: String?, password: String?
    ): T {
        builder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonConfig.gson))
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            val authToken = basic(username!!, password!!)
            return createService(serviceClass, authToken)
        }
        return createService(serviceClass, null)
    }

    private fun <T> createService(
        serviceClass: Class<T>, authToken: String?
    ): T {
        if (!TextUtils.isEmpty(authToken)) {
            val interceptor = AuthenticationInterceptor(authToken!!)
            if (!client.interceptors().contains(interceptor)) {
                client.addInterceptor(interceptor)
            }
        }
        val sslParams = HttpsUtils.getSslSocketFactory(null, null, null)
        client.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
        val logging = HttpLoggingInterceptor()
        logging.setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
        client.addInterceptor(logging)
        retrofit = builder!!.client(client.build()).build()
        return retrofit!!.create(serviceClass)
    }

    fun getTimeApi(name: String, password: String): TimeApi {
        api = createService(TimeApi::class.java, name, password)
        return api!!
    }

    fun createMultiPart(name: String, file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/*".toMediaType())
        return MultipartBody.Part.createFormData(name, file.name, requestFile)
    }

    fun getRequestBody(s: String): RequestBody {
        return s.toRequestBody("text/plain".toMediaType())
    }
}