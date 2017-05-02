package com.dante.diary.chat;

import android.util.Log;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.dante.diary.login.LoginManager;

import java.util.Arrays;

/**
 * Created by yons on 17/4/14.
 */

public class ChatService {
    private static final String TAG = "ChatService";
    private static AVIMClient client;

    public static void send(int sendToId, String msg) {
        String clientId = String.valueOf(LoginManager.getMyId());

        AVIMClient dante = AVIMClient.getInstance(clientId);
        dante.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if (e == null) {
                    dante.createConversation(Arrays.asList(String.valueOf(sendToId)), "私信", null, new AVIMConversationCreatedCallback() {
                        @Override
                        public void done(AVIMConversation conversation, AVIMException e) {
                            if (e == null) {
                                AVIMTextMessage message = new AVIMTextMessage();
                                message.setText(msg);
                                conversation.sendMessage(message, new AVIMConversationCallback() {
                                    @Override
                                    public void done(AVIMException e) {
                                        if (e == null) {
                                            Log.d(TAG, "send done: 發送成功");
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

    }

    public static AVIMClient getClient() {
        return client;
    }

    public static void loginChatServer() {
        client = AVIMClient.getInstance(String.valueOf(LoginManager.getMyId()));
        client.open(new AVIMClientCallback() {

            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e == null) {
                    Log.d(TAG, "done: loginChatServer");
                }
            }
        });

    }

}
