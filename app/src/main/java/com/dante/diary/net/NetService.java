package com.dante.diary.net;

import android.text.TextUtils;
import android.util.Log;

import com.dante.diary.BuildConfig;
import com.dante.diary.utils.AuthenticationInterceptor;

import java.io.File;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yons on 17/3/3.
 */

public class NetService {

    private static Retrofit retrofit;

    private static OkHttpClient.Builder client = new OkHttpClient.Builder();

    private static Retrofit.Builder builder;

    private static TimeApi api;
    private static TimeApi registerApi;


    private static <T> T createService(Class<T> serviceClass) {

        return createService(serviceClass, null, null);
    }

    public static <T> T createServiceWithBaseUrl(Class<T> apiClass, String baseUrl) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ?
                HttpLoggingInterceptor.Level.BASIC : HttpLoggingInterceptor.Level.NONE);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(logging).build();
        builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonConfig.gson));
        return builder.build().create(apiClass);
    }

    private static <T> T createService(
            Class<T> serviceClass, String username, String password) {
        builder = new Retrofit.Builder()
                .baseUrl(API.BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonConfig.gson));

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            String authToken = Credentials.basic(username, password);
            return createService(serviceClass, authToken);
        }

        return createService(serviceClass, null);
    }

    private static <T> T createService(
            Class<T> serviceClass, final String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            AuthenticationInterceptor interceptor =
                    new AuthenticationInterceptor(authToken);
            if (!client.interceptors().contains(interceptor)) {
                client.addInterceptor(interceptor);
            }
        }
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
        client.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ?
                HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        client.addInterceptor(logging);

        retrofit = builder.client(client.build()).build();
        return retrofit.create(serviceClass);
    }

    public static TimeApi getTimeApi(String name, String password) {
        Log.d("test", "getTimeApi: " + name);
        api = createService(TimeApi.class, name, password);
        return api;
    }

    public static MultipartBody.Part createMultiPart(String name, File file) {
        Log.d("test", "createMultiPart: " + file);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"),
                file);

        return MultipartBody.Part.createFormData(name, file.getName(), requestFile);
    }


    public static RequestBody getRequestBody(String s) {
        return RequestBody.create(MediaType.parse("text/plain"), s);
    }

    public static TimeApi getRegisterApi() {
        if (registerApi == null) {
            registerApi = createService(TimeApi.class);
        }
        return registerApi;
    }
}
