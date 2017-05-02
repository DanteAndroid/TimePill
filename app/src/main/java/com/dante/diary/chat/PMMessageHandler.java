package com.dante.diary.chat;

import android.content.Intent;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.dante.diary.R;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.NotificationUtils;
import com.dante.diary.login.LoginManager;
import com.dante.diary.utils.SpUtil;

import java.util.HashSet;
import java.util.Set;

import static com.dante.diary.base.App.context;

/**
 * Created by yons on 17/4/14.
 */

public class PMMessageHandler extends AVIMMessageHandler {
    private static final String TAG = "PMMessageHandler";

    @Override
    public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
        if (message instanceof AVIMTextMessage) {
            AVIMTextMessage textMessage = ((AVIMTextMessage) message);
            if (!textMessage.getFrom().equals(String.valueOf(LoginManager.getMyId()))) {
                dealMessage(((AVIMTextMessage) message).getText());
                if (NotificationUtils.isShowNotification(conversation.getConversationId())) {
                    sendNotification(message, conversation);
                }
            }
        }
    }

    private void sendNotification(AVIMMessage message, AVIMConversation conversation) {
        String text = message instanceof AVIMTextMessage ?
                ((AVIMTextMessage) message).getText() : context.getString(R.string.unspport_message_type);

        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra(Constants.CONVERSATION_ID, conversation.getConversationId());
        intent.putExtra(Constants.DATA, ((AVIMTextMessage) message).getText());
        intent.putExtra(Constants.FROM_ID, message.getFrom());

        Set<String> notificationFromIds = new HashSet<>();
        notificationFromIds.add(message.getFrom());
        SpUtil.save("notifications", notificationFromIds);

        NotificationUtils.showNotification(Integer.parseInt(message.getFrom()),
                String.valueOf(message.getTimestamp()), text, intent);
    }

    private void dealMessage(String text) {

    }

    public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {

    }
}