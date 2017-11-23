package com.dante.diary.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.blankj.utilcode.utils.ClipboardUtils;
import com.bumptech.glide.Glide;
import com.dante.diary.R;
import com.dante.diary.base.App;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.User;
import com.dante.diary.utils.UiUtils;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import rx.Subscriber;

/**
 * Created by yons on 17/4/24.
 */

public class ConversationActivity extends BaseActivity implements MessagesListAdapter.SelectionListener, MessageInput.InputListener {
    private static final String TAG = "PMActivity";
    public static boolean isInConversation;
    @BindView(R.id.input)
    MessageInput input;
    String otherId;
    @BindView(R.id.messagesList)
    MessagesList messagesList;
    String myId;
    private Menu menu;
    private AVIMConversation conversation;
    private MessagesListAdapter<Message> adapter;
    private User otherUser;
    private int selectionCount;
    private Message lastMessage;

    public static void chat(Activity a, int otherId) {
        Intent intent = new Intent(App.context, ConversationActivity.class);
        intent.putExtra(Constants.OTHER_ID, String.valueOf(otherId));
        a.startActivity(intent);
    }


    @Override
    protected int initLayoutId() {
        return R.layout.activity_conversation;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        myId = String.valueOf(LoginManager.getMyId());
        otherId = getIntent().getStringExtra(Constants.OTHER_ID);
        otherUser = DataBase.getInstance().findUser(Integer.parseInt(otherId));
        if (otherUser == null) {
            fetchUser(Integer.parseInt(otherId));
        } else {
            fetchConversation();
        }

        adapter = new MessagesListAdapter<>(LoginManager.getMyStringId(), (imageView, url) -> Glide.with(ConversationActivity.this)
                .load(url)
                .into(imageView));
        adapter.setOnMessageLongClickListener(message -> {
            ClipboardUtils.copyText(message.getText());
            UiUtils.showSnack(messagesList, R.string.copyed);
        });
//        adapter.enableSelectionMode(count -> adapter.copySelectedMessagesText(ConversationActivity.this, message -> message.getText(), false));
        messagesList.setAdapter(adapter);
        input.setInputListener(this);
//        input.setAttachmentsListener(() -> {
//            Message imageMessage= Message.getMessageFromMe("");
//            imageMessage.setImageUrl("");
//            adapter.addToStart(imageMessage, true);
//        });
    }

    private void fetchConversation() {
        ChatService.getClient().getQuery()
                .withMembers(Arrays.asList(otherId), true)
                .findInBackground(new AVIMConversationQueryCallback() {
                    @Override
                    public void done(List<AVIMConversation> list, AVIMException e) {
                        if (e == null) {
                            if (null != list && list.size() > 0) {
                                conversation = list.get(0);
                                fetch(conversation);
                                Log.d(TAG, "AVIMConversation: list size " + list.size());

                            } else {
                                Toast.makeText(ConversationActivity.this, R.string.conversation_is_empty, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            UiUtils.showSnackLong(getToolbar(), "获取对话失败");
                            Log.e(TAG, "fetchConversation: " + e.getMessage());
                        }
                    }
                });

    }


    private void fetch(AVIMConversation conversation) {
        if (conversation != null) {
            conversation.queryMessages(new AVIMMessagesQueryCallback() {
                @Override
                public void done(List<AVIMMessage> list, AVIMException e) {
                    if (e == null) {
                        Log.d(TAG, "fetchMessage: " + list.size());
                        fetchMessage(list);
                    } else {
                        UiUtils.showSnackLong(getToolbar(), "获取消息失败");
                        Log.e(TAG, "fetchConversation: " + e.getMessage());
                    }
                }
            });
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Message message) {
        if (lastMessage != null && lastMessage.getText().equals(message.getText())) {
            return;
        }
        lastMessage = message;
        adapter.addToStart(message, true);
    }

    private void fetchMessage(List<AVIMMessage> list) {
        ArrayList<Message> messages = new ArrayList<>();
        for (AVIMMessage m : list) {
            AVIMTextMessage message = (AVIMTextMessage) m;
            User user = DataBase.getInstance().findUser(Integer.parseInt(message.getFrom()));
            Message msg = new Message(message.getMessageId(),
                    message.getText(), new Author(user), new Date(message.getTimestamp()));
            messages.add(msg);
//            adapter.addToStart(msg, false);
        }
        adapter.addToEnd(messages, true);
        getToolbar().setTitle(String.format("与 %s 的私信", otherUser.getName()));
    }


    private void fetchUser(int id) {
        LoginManager.getApi().getProfile(id)
                .compose(applySchedulers())
                .subscribe(new Subscriber<User>() {

                    @Override
                    public void onCompleted() {
                        fetchConversation();
                    }

                    @Override
                    public void onError(Throwable e) {
                        UiUtils.showSnack(messagesList, R.string.get_profile_failed);
                    }

                    @Override
                    public void onNext(User user) {
                        ConversationActivity.this.otherUser = user;
                        getBase().save(user);
                    }
                });
    }


    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
//        menu.findItem(R.id.action_delete).setVisible(count > 0);
//        menu.findItem(R.id.action_copy).setVisible(count > 0);
    }

    @Override
    public boolean onSubmit(CharSequence charSequence) {
        if (charSequence != null && charSequence.length() > 1) {
            String text = charSequence.toString();
            sendMessage(text);
            adapter.addToStart(Message.getMessageFromMe(text), true);
            return true;
        } else {
            UiUtils.showSnack(messagesList, R.string.say_more);
        }
        return false;
    }

    private void sendMessage(String text) {
        ChatService.send(otherId, otherUser.getName(), text);
    }


    @Override
    protected void onStart() {
        super.onStart();
        isInConversation = true;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        isInConversation = false;
        EventBus.getDefault().unregister(this);
    }
}
