package zumma.com.ninegistapp.service.listeners;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.parse.ParseUser;

import java.util.HashMap;

import zumma.com.ninegistapp.StaticHelpers;
import zumma.com.ninegistapp.database.table.MessageTable;
import zumma.com.ninegistapp.model.Conversation;
import zumma.com.ninegistapp.model.MessageChat;
import zumma.com.ninegistapp.ui.helpers.ChatHelper;

/**
 * Created by Okafor on 20/02/2015.
 */
public class ChatListeners implements ChildEventListener {


    private static final String TAG = ChatListeners.class.getSimpleName() ;
    private Context context;
    private ChatHelper chatHelper;
    private ParseUser parseUser;

    public ChatListeners(Context context) {
        this.context = context;
        chatHelper = new ChatHelper(context);
        parseUser = ParseUser.getCurrentUser();
    }


    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, " onChildAdded "+dataSnapshot.getValue() +"   "+s);

        if (dataSnapshot != null) {
            HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();

            MessageChat messageChat = dataSnapshot.getValue(MessageChat.class);

            if (messageChat!= null && !messageChat.getFromId().equals(parseUser.getObjectId()) && messageChat.getReport() != 2){
                chatHelper.InsertChatMessage(messageChat.getFromId(),messageChat);
                chatHelper.upDateFriendListWithInComingChat(context,messageChat,1);
            }
        }

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, " onChildChanged "+dataSnapshot.getValue() +"   "+s);

//        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
//
//        Conversation conversation = null;
//        while (iterator.hasNext()) {
//            conversation = iterator.next().getValue(Conversation.class);
//        }
//
//        if (conversation.getUniqkey() != null) {
//            conversation.setSent(false);
//            saveConversation(conversation);
//            Log.d(TAG, " Conversation " + conversation);
//        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d(TAG, " onChildRemoved "+dataSnapshot.getValue());
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, " onChildMoved "+dataSnapshot.getValue() +"   "+s);
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        Log.d(TAG, " onCancelled "+firebaseError.getMessage() +"   ");
    }

    public void saveConversation(Conversation conversation) {

        ContentValues values = new ContentValues();

        values.put(MessageTable.COLUMN_FROM, conversation.getFromId());
        values.put(MessageTable.COLUMN_TO, conversation.getToId());
        values.put(MessageTable.COLUMN_MESSAGE, conversation.getMsg());
        values.put(MessageTable.COLUMN_SENT, conversation.isSent());
        values.put(MessageTable.COLUMN_TIME, conversation.getDate());
        values.put(MessageTable.COLUMN_FRIEND_ID, conversation.getFromId());
        values.put(MessageTable.COLUMN_PRIVATE, conversation.getPflag());
        values.put(MessageTable.COLUMN_REPORT, conversation.getReport());
        values.put(MessageTable.COLUMN_CREATED_AT, conversation.getCreated_at());
        values.put(MessageTable.COLUMN_UNIQ_ID, conversation.getUniqkey());

        Uri uri = context.getContentResolver().insert(MessageTable.CONTENT_URI, values);
        if (uri != null) {
            Log.d(TAG, " uri  " + uri.toString());
        }

        StaticHelpers.upDateFriendListWithInComingChat(context,conversation);
    }


}
