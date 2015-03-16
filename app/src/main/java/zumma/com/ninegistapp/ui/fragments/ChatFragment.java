package zumma.com.ninegistapp.ui.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;

import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.custom.CustomFragment;
import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.database.table.MessageTable;
import zumma.com.ninegistapp.model.MessageChat;
import zumma.com.ninegistapp.model.MessageObject;
import zumma.com.ninegistapp.ui.Components.LayoutListView;
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
    private FriendChileEventListener friendChileEventListener;

    private Firebase user_baseRef;
    private Firebase friend_baseRef;

    private ChatHelper chatHelper;


    ArrayList<MessageObject> chatList = new ArrayList<MessageObject>();

    public void initChat() {

        chatHelper = new ChatHelper(getActivity());

        user_id = ParseUser.getCurrentUser().getObjectId();
        friend_id = getArguments().getString(ParseConstants.KEY_USER_ID);

        user_baseRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(user_id).child("roasters").child("Chat").child(friend_id);
        friend_baseRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(friend_id).child("roasters").child("Chat").child(user_id);

        userChileEventListener = new UserChileEventListener();
        friendChileEventListener = new FriendChileEventListener();

        user_baseRef.addChildEventListener(userChileEventListener);
        friend_baseRef.addChildEventListener(friendChileEventListener);

        chatHelper.initChatList(friend_id,chatList);
        adapter = new ChatAdapter(getActivity(), chatList);
    }

    private void sendMessage() {

        if (this.txt.length() == 0)
            return;

        GDate date = new GDate();

        String str = this.txt.getText().toString();
        String time = date.getCurrent_time();

        final MessageChat chatObject = new MessageChat(user_id, friend_id, time, true, 1,str);
        chatHelper.InsertChatMessage(friend_id,chatObject);

        chatList.add(chatObject);
        adapter.notifyDataSetChanged();
        Log.d(TAG, chatObject.toString());
        this.txt.setText(null);


        user_baseRef.push().setValue(chatObject, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                HashMap<String,Object> update = new HashMap<String, Object>();
                update.put("uniqkey",firebase.getKey());
                update.put("report",1);
                Firebase firebase1 = user_baseRef;
                firebase1.child(firebase.getKey()).updateChildren(update);

                chatObject.setSent(false);
                chatObject.setUniqkey(firebase.getKey());
                chatObject.setReport(1);
                friend_baseRef.push().setValue(chatObject, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        Log.d(TAG, "Saved both ways");
                        chatHelper.upDateDeliveredConversation(chatObject, chatList);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

    }




    private class UserChileEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, " na wa onChildAdded " + dataSnapshot.getValue() + "   " + s);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, " onChildChanged " + dataSnapshot.getValue() + "   " + s);
            if (dataSnapshot != null) {
                HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                int type = 1;
                switch (type){
                    case 1:
                        MessageChat messageChat = dataSnapshot.getValue(MessageChat.class);
                        if (messageChat.getFromId().equals(user_id)){
                            int report = messageChat.getReport();
                            ChatHelper chatHelper = new ChatHelper(getActivity());
                            if (report == 2){
                                chatHelper.upDisplayedConversation(messageChat,chatList);
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, " messageChat adapter update " + messageChat.getCreated_at());
                            }
                        }else{
                            Log.d(TAG, " i got to lse part");
                        }

                        Log.d(TAG, " messageChat report " + messageChat.getReport());
                        break;
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
        friend_baseRef.removeEventListener(friendChileEventListener);
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

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        sendMessageRead();
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

        String SELECTION = MessageTable.COLUMN_FRIEND_ID + "=? AND " + MessageTable.COLUMN_REPORT + "=?";
        String[] args = {friend_id, "1"};

        Cursor cursor = getActivity().getContentResolver().query(MessageTable.CONTENT_URI, null, SELECTION, args, null);
        if (cursor != null && cursor.getCount() > 0) {

            int indexUniq = cursor.getColumnIndex(MessageTable.COLUMN_UNIQ_ID);
            int indexID = cursor.getColumnIndex(MessageTable.COLUMN_ID);

            cursor.moveToFirst();
            do {

                final String id = cursor.getString(indexID);
                final String uniq = cursor.getString(indexUniq);

                Log.d(TAG, " unig of unreported = " + uniq);

                Firebase firebase1 = new Firebase(ParseConstants.FIREBASE_URL)
                        .child("9Gist")
                        .child(friend_id)
                        .child("roasters")
                        .child("Chat")
                        .child(user_id)
                        .child(uniq)
                        .child("report");

                firebase1.setValue(2, new Firebase.CompletionListener() {
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
                        }
                    }
                });
            } while (cursor.moveToNext());
            cursor.close();
        }
        cursor.close();
    }

    private class FriendChileEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            if (dataSnapshot != null) {
                HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                MessageChat messageChat = dataSnapshot.getValue(MessageChat.class);

                if (messageChat.getReport() != 2){
                    chatHelper.InsertChatMessage(friend_id,messageChat);
                    chatList.add(messageChat);
                    adapter.notifyDataSetChanged();
                }



                Log.d(TAG, " FriendChileEventListener onChildChanged has been delivered and read ");

                Firebase firebase1 = new Firebase(ParseConstants.FIREBASE_URL)
                        .child("9Gist")
                        .child(messageChat.getFromId())
                        .child("roasters")
                        .child("Chat")
                        .child(messageChat.getToId())
                        .child(messageChat.getUniqkey())
                        .child("report");

                firebase1.setValue(2);
//
//                HashMap<String,Object> update = new HashMap<String, Object>();
//                update.put("report",2);
//                Firebase firebase1 = user_baseRef;
//                firebase1.child(messageChat.getUniqkey()).updateChildren(update);

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


}
