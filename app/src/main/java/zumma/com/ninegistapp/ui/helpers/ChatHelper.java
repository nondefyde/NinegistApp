package zumma.com.ninegistapp.ui.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.parse.ParseUser;

import java.util.ArrayList;

import zumma.com.ninegistapp.StaticMethods;
import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.database.table.MessageTable;
import zumma.com.ninegistapp.model.MessageChat;
import zumma.com.ninegistapp.model.MessageObject;

/**
 * Created by Okafor on 12/03/2015.
 */
public class ChatHelper {

    private static final String TAG = ChatHelper.class.getSimpleName();

    private Context context;
    private ParseUser parseUser;

    public ChatHelper(Context context) {
        this.context = context;
        parseUser =  ParseUser.getCurrentUser();
    }

    public void upDateDeliveredConversation(MessageChat conversation, ArrayList<MessageObject> messageObjects) {
        for (int i = 0; i < messageObjects.size(); i++) {
            MessageChat messageChat = (MessageChat) messageObjects.get(i);
            if (conversation.getCreated_at() == messageChat.getCreated_at()) {
                messageChat.setReport(1);
                messageObjects.remove(i);
                messageObjects.add(i, messageChat);
            }
        }

        String SELECTION = MessageTable.COLUMN_FRIEND_ID + "=? AND " + MessageTable.COLUMN_CREATED_AT + "=?";
        String[] args = {conversation.getToId(), conversation.getCreated_at() + ""};

        ContentValues values = new ContentValues();
        values.put(MessageTable.COLUMN_REPORT, 1);
        values.put(MessageTable.COLUMN_UNIQ_ID, conversation.getUniqkey());

        int update = context.getContentResolver().update(MessageTable.CONTENT_URI, values, SELECTION, args);
        if (update > 0) {
            Log.d(TAG, " update delivered happened  " + update);
        }

    }

    public void upDisplayedConversation(MessageChat conversation, ArrayList<MessageObject> messageObjects) {

        for (int i = 0; i < messageObjects.size(); i++) {
            MessageChat messageChat = (MessageChat) messageObjects.get(i);
            if (conversation.getCreated_at() == messageChat.getCreated_at()) {
                messageChat.setReport(2);
                messageObjects.remove(i);
                messageObjects.add(i, messageChat);
                Log.d(TAG, " upDisplayedConversation List 2 " + messageObjects.toString());
            }
        }
        String SELECTION = MessageTable.COLUMN_FRIEND_ID + "=? AND " + MessageTable.COLUMN_CREATED_AT + "=?";
        String[] args = {conversation.getToId(), conversation.getCreated_at() + ""};

        ContentValues values = new ContentValues();
        values.put(MessageTable.COLUMN_REPORT, 2);

        int update = context.getContentResolver().update(MessageTable.CONTENT_URI, values, SELECTION, args);
        if (update > 0) {
            Log.d(TAG, " update displayed happened  " + update);
        }
    }

    public void initChatList(String friend_id, ArrayList<MessageObject> messageObjects){

        String SELECTION = MessageTable.COLUMN_FRIEND_ID + "=?";
        String[] args = {friend_id};

        Cursor cursor = context.getContentResolver().query(MessageTable.CONTENT_URI, null, SELECTION, args, MessageTable.COLUMN_CREATED_AT);

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

            cursor.moveToFirst();

            do {

                String id = cursor.getString(indexID);
                String fromId = cursor.getString(indexFrom);
                String toId = cursor.getString(indexTo);
                String msg = cursor.getString(indexMsg);
                boolean sent = cursor.getInt(indexSent) == 1 ? true : false;
                String date = cursor.getString(indexTime);
                int type = cursor.getInt(indexType);
                boolean flag = cursor.getInt(indexPrivate) == 1 ? true : false;
                int report = cursor.getInt(indexReport);
                long created = cursor.getLong(indexCreatedAt);
                String uniq = cursor.getString(indexUnique);

                MessageChat messageChat = new MessageChat(fromId,toId,date,sent,flag,type,report,created,uniq,msg);
//                Log.d(TAG, conversation.toString() );
                messageObjects.add(messageChat);

            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    public void InsertChatMessage(String friend_id, MessageChat messageObject) {

        boolean chatExit = validateComingChat(friend_id,messageObject);
        Log.d(TAG, " chat exist " + chatExit);

        if (chatExit == false){
            ContentValues values = new ContentValues();

            values.put(MessageTable.COLUMN_FROM, messageObject.getFromId());
            values.put(MessageTable.COLUMN_TO, messageObject.getToId());
            values.put(MessageTable.COLUMN_MESSAGE, messageObject.getMessage());
            values.put(MessageTable.COLUMN_SENT, messageObject.isSent());
            values.put(MessageTable.COLUMN_TIME, messageObject.getDate());
            values.put(MessageTable.COLUMN_FRIEND_ID, friend_id);
            values.put(MessageTable.COLUMN_MESSAGE_TYPE, friend_id);
            values.put(MessageTable.COLUMN_PRIVATE, messageObject.isIs_private());
            values.put(MessageTable.COLUMN_REPORT, messageObject.getReport());
            values.put(MessageTable.COLUMN_CREATED_AT, messageObject.getCreated_at());
            values.put(MessageTable.COLUMN_UNIQ_ID, messageObject.getUniqkey());

            Uri uri = context.getContentResolver().insert(MessageTable.CONTENT_URI, values);
            if (uri != null) {
                Log.d(TAG, " messageChat inserted " + messageObject);
            }

            if (!messageObject.getFromId().equals(parseUser.getObjectId()) && messageObject.getReport() != 2){
                upDateFriendListWithInComingChat(context,messageObject,1);
                StaticMethods.sendChatNotification(context,messageObject.getMessage());
            }

        }
    }


    public void upDateFriendListWithInComingChat(Context context,MessageChat messageChat,int icon_type) {

        String SELECTION = FriendTable.COLUMN_ID + "=?";
        switch (icon_type){
            case 1:
                String[] args = {messageChat.getFromId()};

                int mCount = 0;

                Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null, SELECTION, args, null);
                if (cursor != null && cursor.getCount() > 0) {
                    int messageCountIndex = cursor.getColumnIndex(FriendTable.COLUMN_MSG_COUNT);
                    cursor.moveToFirst();
                    do {
                        mCount = cursor.getInt(messageCountIndex);
                        Log.d(TAG, " Message Count  " + mCount);

                        String message = messageChat.getMessage();
                        String formatted_message = message.length() > 30 ? message.substring(0, 30) + "..." : message;
                        GDate date = new GDate();

                        ContentValues values = new ContentValues();
                        values.put(FriendTable.COLUMN_MSG_COUNT, mCount + 1);
                        values.put(FriendTable.COLUMN_STATUS, formatted_message);
                        values.put(FriendTable.COLUMN_UPDATED_AT, date.getTimeStamp());
                        values.put(FriendTable.COLUMN_STATUS_ICON, icon_type);
                        int update = context.getContentResolver().update(FriendTable.CONTENT_URI, values, SELECTION, args);
                        if (update > 0) {
                            Log.d(TAG, " FriendTable updated  " + update);
                        }
                        break;
                    } while (cursor.moveToNext());
                }
                break;
            case 2:
                String[] arg = {messageChat.getToId()};

                String message = messageChat.getMessage();
                String formatted_message = message.length() > 30 ? message.substring(0, 30) + "..." : message;
                GDate date = new GDate();

                ContentValues values = new ContentValues();
                values.put(FriendTable.COLUMN_STATUS, formatted_message);
                values.put(FriendTable.COLUMN_UPDATED_AT, date.getTimeStamp());
                values.put(FriendTable.COLUMN_STATUS_ICON, icon_type);
                int update = context.getContentResolver().update(FriendTable.CONTENT_URI, values, SELECTION, arg);
                if (update > 0) {
                    Log.d(TAG, " FriendTable updated  " + update);
                }
                break;
        }
    }


    private boolean validateComingChat(String friend_id, MessageChat messageChat){
        String SELECTION = MessageTable.COLUMN_FRIEND_ID + "=? AND "+MessageTable.COLUMN_CREATED_AT+"=?";
        String[] args = {friend_id, messageChat.getCreated_at()+""};
        Cursor cursor = context.getContentResolver().query(MessageTable.CONTENT_URI, null, SELECTION, args, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        return false;
    }

    public void upDateFriendStatusChat(Context context,MessageChat messageChat,int type, int icon_type){

        ContentValues values = null;
        int update = 0;

        String SELECTION = FriendTable.COLUMN_ID + "=?";
        String message = messageChat.getMessage();
        String formatted_message = message.length() > 30 ? message.substring(0, 30) + "..." : message;
        GDate date = new GDate();

        if (type == 1){

            values = new ContentValues();
            values.put(FriendTable.COLUMN_STATUS, formatted_message);
            values.put(FriendTable.COLUMN_UPDATED_AT, date.getTimeStamp());
            values.put(FriendTable.COLUMN_STATUS_ICON, icon_type);

            if(icon_type != 0){
                String[] arg1 = {messageChat.getToId()};
                update = context.getContentResolver().update(FriendTable.CONTENT_URI, values, SELECTION, arg1);

            }else{
                values.put(FriendTable.COLUMN_MSG_COUNT,0);
                String[] arg1 = {messageChat.getFromId()};
                update = context.getContentResolver().update(FriendTable.CONTENT_URI, values, SELECTION, arg1);
            }
            if (update > 0) {
                Log.d(TAG, " upDateFriendStatusChat FriendTable updated  " + update);
            }
        }
    }


}
