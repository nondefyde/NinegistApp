package zumma.com.ninegistapp.service.listeners;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

import java.util.Iterator;

import zumma.com.ninegistapp.database.table.ChatTable;
import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.model.Conversation;
import zumma.com.ninegistapp.ui.helpers.GDate;

/**
 * Created by Okafor on 20/02/2015.
 */
public class ChatListeners implements ChildEventListener {


    private static final String TAG = ChatListeners.class.getSimpleName() ;
    private Context context;

    public ChatListeners(Context context) {
        this.context = context;
    }


    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, " onChildAdded "+dataSnapshot.getValue() +"   "+s);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, " onChildChanged "+dataSnapshot.getValue() +"   "+s);

        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();

        Conversation conversation = null;
        while (iterator.hasNext()) {
            conversation = iterator.next().getValue(Conversation.class);
        }

        if (conversation.getUniqkey() != null) {
            conversation.setSent(false);
            saveConversation(conversation);
            Log.d(TAG, " Conversation " + conversation);
        }
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

        values.put(ChatTable.COLUMN_FROM, conversation.getFromId());
        values.put(ChatTable.COLUMN_TO, conversation.getToId());
        values.put(ChatTable.COLUMN_MESSAGE, conversation.getMsg());
        values.put(ChatTable.COLUMN_SENT, conversation.isSent());
        values.put(ChatTable.COLUMN_TIME, conversation.getDate());
        values.put(ChatTable.COLUMN_FRIEND_ID, conversation.getFromId());
        values.put(ChatTable.COLUMN_PRIVATE, conversation.getPflag());
        values.put(ChatTable.COLUMN_REPORT, conversation.getReport());
        values.put(ChatTable.COLUMN_CREATED_AT, conversation.getCreated_at());
        values.put(ChatTable.COLUMN_UNIQ_ID, conversation.getUniqkey());

        Uri uri = context.getContentResolver().insert(ChatTable.CONTENT_URI, values);
        if (uri != null) {
            Log.d(TAG, " uri  " + uri.toString());
        }

        upDateFriendListWithInComingChat(conversation);
    }

    public void upDateFriendListWithInComingChat(Conversation conversation) {

        String SELECTION = FriendTable.COLUMN_ID + "=?";
        String[] args = {conversation.getFromId()};

        int mCount = 0;

        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null, SELECTION, args, null);
        if (cursor != null && cursor.getCount() > 0) {
            int messageCountIndex = cursor.getColumnIndex(FriendTable.COLUMN_MSG_COUNT);
            cursor.moveToFirst();
            do {
                mCount = cursor.getInt(messageCountIndex);
                Log.d(TAG, " Message Count  " + mCount);

                String message = conversation.getMsg();
                String formatted_message = message.length() > 30 ? message.substring(0, 30) + "..." : message;
                GDate date = new GDate();

                ContentValues values = new ContentValues();
                values.put(FriendTable.COLUMN_MSG_COUNT, mCount + 1);
                values.put(FriendTable.COLUMN_STATUS, formatted_message);
                values.put(FriendTable.COLUMN_UPDATED_AT, date.getTimeStamp());
                values.put(FriendTable.COLUMN_STATUS_ICON, 1);
                int update = context.getContentResolver().update(FriendTable.CONTENT_URI, values, SELECTION, args);
                if (update > 0) {
                    Log.d(TAG, " FriendTable updated  " + update);
                }
                break;
            } while (cursor.moveToNext());
        }
    }
}
