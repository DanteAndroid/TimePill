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
import com.dante.diary.model.DataBase;
import com.dante.diary.model.User;
import com.dante.diary.utils.SpUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.dante.diary.base.App.context;

/**
 * Created by yons on 17/4/14.
 */

public class PMMessageHandler extends AVIMMessageHandler {
    private static final String TAG = "PMMessageHandler";
    private AVIMTextMessage textMessage;
    private AVIMConversation conversation;
    private Message lastMessage;

    public static void notifyNewMessage(Message m) {
        EventBus.getDefault().post(m);
    }

    @Override
    public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
        if (message instanceof AVIMTextMessage) {
            textMessage = ((AVIMTextMessage) message);
            this.conversation = conversation;
            try {
                if (!textMessage.getFrom().equals(String.valueOf(LoginManager.getMyId()))) {
                    dealMessage();
                }
            } catch (Exception e) {
                client.close(null);
            }

        }
    }

    private void sendNotification(AVIMMessage message, String fromName) {
        String text = message instanceof AVIMTextMessage ?
                ((AVIMTextMessage) message).getText() : context.getString(R.string.unspport_message_type);

        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(context, ConversationActivity.class);
//        intent.putExtra(Constants.CONVERSATION_ID, conversation.getConversationId());
        intent.putExtra(Constants.DATA, ((AVIMTextMessage) message).getText());
        intent.putExtra(Constants.OTHER_ID, message.getFrom());

        Set<String> notificationFromIds = new HashSet<>();
        notificationFromIds.add(message.getFrom());
        SpUtil.save("notifications", notificationFromIds);

        NotificationUtils.showNotification(Integer.parseInt(message.getFrom()),
                fromName + " 给您发了一条私信", text, intent);
    }

    private void dealMessage() {
        String fromId = textMessage.getFrom();
        String content = textMessage.getText();
        User user = DataBase.getInstance().findUser(Integer.parseInt(fromId));
        if (user == null) {
            fetchUser(Integer.parseInt(fromId), content);
        } else {
            Message m = new Message(fromId, content, new Author(user), new Date());
            notifyNewMessage(m);
            if (NotificationUtils.isShowNotification(conversation.getConversationId())
                    && !ConversationActivity.isInConversation) {
                sendNotification(textMessage, user.getName());
            }
        }

    }

    private void fetchUser(int id, String text) {
        LoginManager.getApi().getProfile(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<User>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(User user) {
                        Message m = new Message(String.valueOf(id), text, new Author(user), new Date());
                        notifyNewMessage(m);
                        DataBase.getInstance().save(user);

                        if (NotificationUtils.isShowNotification(conversation.getConversationId())
                                && !ConversationActivity.isInConversation) {
                            sendNotification(textMessage, user.getName());
                        }
                    }
                });
    }


    public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {

    }
}