package com.dante.diary.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.utils.Utils;
import com.bugtags.library.Bugtags;

import io.realm.Realm;

/**
 * Created by yons on 17/3/3.
 */
public class App extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
//    private RefWatcher refWatcher;

//    public static RefWatcher getWatcher(Context context) {
//        App application = (App) context.getApplicationContext();
//        return application.refWatcher;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
//        refWatcher = LeakCanary.install(this);
        Bugtags.start("6db43944fc0b79dce98107999a23f486", this, Bugtags.BTGInvocationEventNone);
        Realm.init(this);
        Utils.init(this);

//        JPushInterface.setDebugMode(BuildConfig.DEBUG);
//        JPushInterface.init(this);

    }
}
