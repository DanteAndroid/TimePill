package com.dante.diary.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.utils.Utils;
import com.dante.diary.BuildConfig;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.bugtags.library.Bugtags;

import io.realm.Realm;

/**
 * Created by yons on 17/3/3.
 */
public class App extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private RefWatcher refWatcher;

    public static RefWatcher getWatcher(Context context) {
        App application = (App) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        refWatcher = LeakCanary.install(this);
        Bugtags.start("1ddf7128d535505cc4adbda213e8c12f", this, Bugtags.BTGInvocationEventNone);
        Realm.init(this);
        Utils.init(this);

        if (BuildConfig.DEBUG) {
        }

    }
}
