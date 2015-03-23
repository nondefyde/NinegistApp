package zumma.com.ninegistapp.ui.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.custom.CustomFragment;
import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.database.table.MessageTable;
import zumma.com.ninegistapp.model.MessageChat;
import zumma.com.ninegistapp.model.MessageObject;
import zumma.com.ninegistapp.ui.Components.LayoutListView;
import zumma.com.ninegistapp.ui.activities.ViewProfile;
import zumma.com.ninegistapp.ui.adapters.ChatAdapter;
import zumma.com.ninegistapp.ui.helpers.ChatHelper;
import zumma.com.ninegistapp.ui.helpers.GDate;

public class ChatFragment extends CustomFragment {


    private static final String TAG = ChatFragment.class.getSimpleName();
    private ChatAdapter adapter;
    private EditText txt;
    private String user_id;
    private String friend_id;
    private UserChileEventListener userChileEventListener;

    private SharedPreferences preferences;

    private Firebase user_baseRef;
    private Firebase friend_baseRef;
    private Firebase connectionStatus;
    private Firebase lastSeen;
    private boolean onlineStatus;

    private ChatHelper chatHelper;

    public static final String FRIEND_ID_PROFILE = "friends_id_profile";


    ArrayList<MessageObject> chatList = new ArrayList<MessageObject>();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_edit) {
            Intent intent = new Intent(getActivity(), ViewProfile.class);
            intent.putExtra(FRIEND_ID_PROFILE, friend_id);
            startActivity(intent);
            return true;
        }
        return false;
    }

    public void initChat() {

        chatHelper = new ChatHelper(getActivity());

        user_id = ParseUser.getCurrentUser().getObjectId();
        friend_id = getArguments().getString(ParseConstants.KEY_USER_ID);

        user_baseRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(user_id).child("roasters").child("Chat").child(friend_id);
        friend_baseRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(friend_id).child("roasters").child("Chat").child(user_id);
        connectionStatus = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(friend_id).child("basicInfo").child("connectionStatus");
        lastSeen = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(friend_id).child("basicInfo").child("lastOnline");

        connectionStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == true){
                    onlineStatus = true;
                    setUpSubtitle("Online");
                }
                else{
                    onlineStatus = false;
                    lastSeen.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String last_online = dataSnapshot.getValue().toString();
                            Long updateAt = Long.parseLong(last_online);
                            long now = new Date().getTime();
                            String convertedDate = DateUtils.getRelativeTimeSpanString(
                                    updateAt,
                                    now + 1000,
                                    DateUtils.SECOND_IN_MILLIS
                            ).toString();
                            setUpSubtitle("Last online: "+convertedDate);
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        userChileEventListener = new UserChileEventListener();
//        friendChileEventListener = new FriendChileEventListener();

        user_baseRef.addChildEventListener(userChileEventListener);
//        friend_baseRef.addChildEventListener(friendChileEventListener);

        chatHelper.initChatList(friend_id, chatList);
        adapter = new ChatAdapter(getActivity(), chatList);
    }

    private void setUpSubtitle(String status){
        if(onlineStatus){
            getActivity().getActionBar().setSubtitle(status);
        }
        else{
            getActivity().getActionBar().setSubtitle(status);
        }
    }

    private void sendMessage() {

        if (this.txt.length() == 0)
            return;

        GDate date = new GDate();

        String str = this.txt.getText().toString();
        String time = date.getCurrent_time();

        final MessageChat chatObject = new MessageChat(user_id, friend_id, time, true, 1, str);
        chatHelper.InsertChatMessage(friend_id, chatObject);
        chatHelper.upDateFriendStatusChat(getActivity(), chatObject, 1, 2);

        chatList.add(chatObject);
        adapter.notifyDataSetChanged();
        Log.d(TAG, chatObject.toString());
        this.txt.setText(null);


        friend_baseRef.push().setValue(chatObject, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                HashMap<String, Object> update = new HashMap<String, Object>();
                update.put("uniqkey", firebase.getKey());
                update.put("report", 1);
                Firebase firebase1 = friend_baseRef;
                firebase1.child(firebase.getKey()).updateChildren(update);

                chatHelper.upDateDeliveredConversation(chatObject, chatList);
                chatHelper.upDateFriendStatusChat(getActivity(), chatObject, 1, 3);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private class UserChileEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, " na wa onChildAdded " + dataSnapshot.getValue() + "   " + s);
            if (dataSnapshot != null) {
                HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                MessageChat messageChat = dataSnapshot.getValue(MessageChat.class);
                if (messageChat.getFromId().equals(friend_id) && messageChat.getReport() == 2) {
                    chatHelper.upDisplayedConversation(messageChat, chatList);
                    chatHelper.upDateFriendStatusChat(getActivity(), messageChat, 1, 0);
                } else if(!messageChat.getFromId().equals(friend_id)){
                    chatHelper.upDisplayedConversation(messageChat, chatList);
                    chatHelper.upDateFriendStatusChat(getActivity(), messageChat, 1, 4);
                }

                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, " onChildChanged " + dataSnapshot.getValue() + "   " + s);

            if (dataSnapshot != null) {
                HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                MessageChat messageChat = dataSnapshot.getValue(MessageChat.class);
                messageChat.setSent(false);
                if (messageChat.getFromId().equals(friend_id)) {
                    chatHelper.InsertChatMessage(friend_id, messageChat);
                    Log.d(TAG, " recieved chat  ");
                    chatHelper.upDateFriendStatusChat(getActivity(), messageChat, 1, 0);
                    chatList.add(messageChat);
                    adapter.notifyDataSetChanged();


                    messageChat.setReport(2);

                    Log.d(TAG, " FriendChileEventListener onChildChanged has been delivered and read ");

                    Firebase firebase1 = new Firebase(ParseConstants.FIREBASE_URL)
                            .child("9Gist")
                            .child(messageChat.getFromId())
                            .child("roasters")
                            .child("Chat")
                            .child(messageChat.getToId())
                            .child(messageChat.getUniqkey());

                    firebase1.setValue(messageChat);
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        user_baseRef.removeEventListener(userChileEventListener);
    }

    public void onClick(View paramView) {
        super.onClick(paramView);

        if (paramView.getId() == R.id.chat_send_button)
            sendMessage();
    }

    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        View view = paramLayoutInflater.inflate(R.layout.chat_layout, null);
        initChat();

        LayoutListView localListView = (LayoutListView) view.findViewById(R.id.chat_list_view);

        localListView.setAdapter(this.adapter);
        localListView.setTranscriptMode(2);
        localListView.setStackFromBottom(true);

        this.txt = ((EditText) view.findViewById(R.id.chat_text_edit));
        this.txt.setInputType(131073);
        setTouchNClick(view.findViewById(R.id.chat_send_button));

        setHasOptionsMenu(true);

        super.onResume();

        sendMessageRead();

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit().putInt(friend_id,1).commit();

        return view;
    }


    public void updateFriendMessageCount() {

        String SEL = FriendTable.COLUMN_ID + "=?";
        String[] arg = {friend_id};

        ContentValues values = new ContentValues();
        values.put(FriendTable.COLUMN_MSG_COUNT, 0);
        values.put(FriendTable.COLUMN_STATUS_ICON, 0);

        int update = getActivity().getContentResolver().update(FriendTable.CONTENT_URI, values, SEL, arg);
        if (update > 0) {
            Log.d(TAG, "message count updated ");
        }
    }

    public void sendMessageRead() {

        String SELECTION = MessageTable.COLUMN_FROM + "=? AND " + MessageTable.COLUMN_REPORT + "=1";
        String[] args = {friend_id};

        Log.d(TAG, " sendMessageRead "+friend_id);
        Cursor cursor = getActivity().getContentResolver().query(MessageTable.CONTENT_URI, null, SELECTION, args, null);
        if (cursor != null && cursor.getCount() > 0) {

            int indexID = cursor.getColumnIndex(MessageTable.COLUMN_ID);
            int indexFrom = cursor.getColumnIndex(MessageTable.COLUMN_FROM);
            int indexTo = cursor.getColumnIndex(MessageTable.COLUMN_TO);
            int indexMsg = cursor.getColumnIndex(MessageTable.COLUMN_MESSAGE);
            int indexSent = cursor.getColumnIndex(MessageTable.COLUMN_SENT);
            int indexTime = cursor.getColumnIndex(MessageTable.COLUMN_TIME);
            int indexPrivate = cursor.getColumnIndex(MessageTable.COLUMN_PRIVATE);
            int indexType = cursor.getColumnIndex(MessageTable.COLUMN_MESSAGE_TYPE);
            int indexReport = cursor.getColumnIndex(MessageTable.COLUMN_REPORT);
            int indexCreatedAt = cursor.getColumnIndex(MessageTable.COLUMN_CREATED_AT);
            int indexUnique = cursor.getColumnIndex(MessageTable.COLUMN_UNIQ_ID);

            Log.d(TAG, " sendMessageRead 2"+friend_id);
            cursor.moveToFirst();
            do {

                final String id = cursor.getString(indexID);
                String fromId = cursor.getString(indexFrom);
                String toId = cursor.getString(indexTo);
                String msg = cursor.getString(indexMsg);
                boolean sent = cursor.getInt(indexSent) == 1 ? true : false;
                String date = cursor.getString(indexTime);
                int type = cursor.getInt(indexType);
                boolean flag = cursor.getInt(indexPrivate) == 1 ? true : false;
                int report = 2;
                long created = cursor.getLong(indexCreatedAt);
                final String uniq = cursor.getString(indexUnique);

                final MessageChat messageChat = new MessageChat(fromId, toId, date, sent, flag, type, report, created, uniq, msg);

                Log.d(TAG, " unig of unreported = " + uniq);

                Firebase firebase1 = new Firebase(ParseConstants.FIREBASE_URL)
                        .child("9Gist")
                        .child(friend_id)
                        .child("roasters")
                        .child("Chat")
                        .child(user_id)
                        .child(uniq);


                firebase1.setValue(messageChat, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        ContentValues values = new ContentValues();
                        values.put(MessageTable.COLUMN_REPORT, 2);
                        Log.d(TAG, " sendMessageRead = " + values);

                        String SEL = MessageTable.COLUMN_ID + "=?";
                        String[] arg = {id};
                        int update = getActivity().getContentResolver().update(MessageTable.CONTENT_URI, values, SEL, arg);
                        if (update > 0) {
                            Log.d(TAG, "unig #value = " + uniq);
                            chatHelper.upDateFriendStatusChat(getActivity(), messageChat, 1, 0);
                        }
                    }
                });
            } while (cursor.moveToNext());
            cursor.close();
        }else{
            cursor.close();
        }

    }
}
