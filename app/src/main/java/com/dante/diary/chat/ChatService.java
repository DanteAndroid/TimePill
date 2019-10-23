package com.dante.diary.chat;

import android.util.Log;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.dante.diary.login.LoginManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by yons on 17/4/14.
 */

public class ChatService {
    private static final String TAG = "ChatService";
    private static AVIMClient client;

    public static void send(String sendToId, String sendToName, String msg) {
        getClient().open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if (e == null) {
                    buildConversation(sendToId, sendToName, msg);
                }
            }
        });
    }

    private static void buildConversation(String sendToId, String sendToName, final String msg) {
        ChatService.getClient().getQuery()
                .withMembers(Collections.singletonList(sendToId), true)
                .findInBackground(new AVIMConversationQueryCallback() {
                    @Override
                    public void done(List<AVIMConversation> list, AVIMException e) {
                        if (e == null && list != null && list.size() > 0) {
                            sendMessage(list.get(0), msg);

                        } else {
                            getClient().createConversation(Arrays.asList(sendToId, LoginManager.getMyStringId()),
                                    String.format("与 %s 的私信", sendToName), null, new AVIMConversationCreatedCallback() {
                                        @Override
                                        public void done(AVIMConversation conversation, AVIMException e) {
                                            if (e == null) {
                                                sendMessage(conversation, msg);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private static void sendMessage(AVIMConversation conversation, String msg) {
        conversation.sendMessage(getTextMessage(msg), new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                if (e == null) {
//                    Toast.makeText(App.context, R.string.message_sent, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @NonNull
    private static AVIMTextMessage getTextMessage(String msg) {
        AVIMTextMessage message = new AVIMTextMessage();
        message.setText(msg);
        return message;
    }

    public static AVIMClient getClient() {
        if (client == null) {
            loginChatServer();
        }
        return client;
    }

    public static void loginChatServer() {
        client = AVIMClient.getInstance(String.valueOf(LoginManager.getMyId()));
        client.open(new AVIMClientCallback() {

            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e == null) {
                    Log.d(TAG, "done: loginChatServer" + client.getClientId());
                }
            }
        });
    }

}
