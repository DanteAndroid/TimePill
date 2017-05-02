package com.dante.diary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by yons on 17/4/14.
 */

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
//        Bundle bundle = intent.getExtras();
//        Log.d(TAG, "onReceive - " + intent.getAction());
//        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
//            String msg = bundle.getString(JPushInterface.EXTRA_MESSAGE);
//            Log.d(TAG, "收到了自定义消息。消息内容是：" + msg);
//
//        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
//            Intent i = new Intent(context, MainActivity.class);
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            context.startActivity(i);
//        }
    }
}
