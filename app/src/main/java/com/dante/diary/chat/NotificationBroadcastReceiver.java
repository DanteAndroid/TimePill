package com.dante.diary.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.dante.diary.base.Constants;
import com.dante.diary.login.LoginActivity;

/**
 * Created by yons on 17/4/24.
 */

class NotificationBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationBroadcastRe";

    private void gotoLoginActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");

        if (ChatService.getClient() == null) {
            Log.d(TAG, "onReceive: ChatService.getClient() == null ");
            gotoLoginActivity(context);

        } else {
            String conversationId = intent.getStringExtra(Constants.CONVERSATION_ID);
            Log.d(TAG, "onReceive: conversationId " + conversationId);
            if (!TextUtils.isEmpty(conversationId)) {
                goChatActivity(context, intent);
            }
        }
    }

    private void goChatActivity(Context context, Intent intent) {
        Intent startActivityIntent = new Intent(context, ConversationActivity.class);
        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityIntent.putExtra(Constants.FROM_ID, intent.getStringExtra(Constants.FROM_ID));
        context.startActivity(startActivityIntent);
    }
}
