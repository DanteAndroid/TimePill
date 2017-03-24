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

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API.BASE_URL)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(GsonConfig.gson));

    private static TimeApi api;


    private static <T> T createService(Class<T> serviceClass) {
        return createService(serviceClass, null, null);
    }

    private static <T> T createService(
            Class<T> serviceClass, String username, String password) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            String authToken = Credentials.basic(username, password);
            return createService(serviceClass, authToken);
        }
        return createService(serviceClass, null, null);
    }

    private static <T> T createService(
            Class<T> serviceClass, final String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            AuthenticationInterceptor interceptor =
                    new AuthenticationInterceptor(authToken);

            //debug 模式开启log
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG ?
                    HttpLoggingInterceptor.Level.BASIC : HttpLoggingInterceptor.Level.NONE);
            httpClient.addInterceptor(logging);

            if (!httpClient.interceptors().contains(interceptor)) {
                HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
                httpClient.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
                httpClient.addInterceptor(interceptor);
                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }
        return retrofit.create(serviceClass);
    }

    public static TimeApi getTimeApi(String name, String password) {
        if (api == null) {
            api = createService(TimeApi.class, name, password);
        }
        return api;
    }

    public static MultipartBody.Part createMultiPart(String name, File file) {
        Log.d("test", "createMultiPart: " + file);
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("image/*"),
                        file);

        return MultipartBody.Part.createFormData(name, file.getName(), requestFile);
    }


}
