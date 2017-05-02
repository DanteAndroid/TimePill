package com.dante.diary.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.bumptech.glide.Glide;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.model.User;
import com.dante.diary.utils.UiUtils;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

/**
 * Created by yons on 17/4/24.
 */

public class ConversationActivity extends BaseActivity {
    private static final String TAG = "PMActivity";
    String fromId;
    @BindView(R.id.messagesList)
    MessagesList messagesList;
    private AVIMConversation conversation;
    private MessagesListAdapter<Message> adapter;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_conversation;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        fromId = getIntent().getStringExtra(Constants.FROM_ID);
        getConversation(fromId);

        UiUtils.showSnack(toolbar, getIntent().getStringExtra(Constants.DATA));
    }

    private void getConversation(String fromId) {
        ChatService.getClient().getQuery()
                .withMembers(Arrays.asList(fromId), true)
                .findInBackground(new AVIMConversationQueryCallback() {
                    @Override
                    public void done(List<AVIMConversation> list, AVIMException e) {
                        if (e == null) {
                            setConversation(list);
                        } else {
                            UiUtils.showSnackLong(toolbar, e.getMessage());
                        }
                    }
                });

    }

    private void setConversation(List<AVIMConversation> list) {
        if (null != list && list.size() > 0) {
            conversation = list.get(0);
            fetch(conversation);
        } else {
            Log.d(TAG, "setConversation: list is empty");
        }
    }

    private void fetch(AVIMConversation conversation) {
        if (conversation != null) {
            conversation.queryMessages(new AVIMMessagesQueryCallback() {
                @Override
                public void done(List<AVIMMessage> list, AVIMException e) {
                    if (e == null) {
                        fetchMessages(list);
                    } else {
                        UiUtils.showSnackLong(toolbar, e.getMessage());
                    }
                }
            });
        }

    }

    private void fetchMessages(List<AVIMMessage> list) {
        adapter = new MessagesListAdapter<>(fromId, (imageView, url) -> Glide.with(ConversationActivity.this)
                .load(url)
                .into(imageView));
        messagesList.setAdapter(adapter);

        adapter.addToEnd(convert(list), false);
    }

    private List<Message> convert(List<AVIMMessage> list) {
        List<Message> messages = new ArrayList<>();

        Log.d(TAG, "convert: " + list.size());
        for (AVIMMessage m : list) {
            if (m instanceof AVIMTextMessage) {
                AVIMTextMessage text = (AVIMTextMessage) m;
                Message message = new Message();
                message.setText(text.getText());
                message.setCreatedAt(new Date(m.getTimestamp()));
                message.setId(m.getMessageId());
                User u = new User(Integer.parseInt(text.getFrom()));
                u.setName(text.getFrom());
                u.setAvatarUrl(text.getText());

                message.setAuthor(new Author(u));
                Log.d(TAG, "convert: add message");
                messages.add(message);
            }
        }

        return messages;
    }

}
