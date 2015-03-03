package zumma.com.ninegistapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.model.Conversation;
import zumma.com.ninegistapp.ui.helpers.GDate;

/**
 * Created by Okafor on 01/03/2015.
 */
public class StaticHelpers {

    private static final String TAG = StaticHelpers.class.getSimpleName();

    public static void upDateFriendListWithInComingChat(Context context,Conversation conversation) {

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

    public static void upDateFriendListWithInChat(Context context,Conversation conversation,int status_type) {

        String SELECTION = FriendTable.COLUMN_ID + "=?";
        String[] args = {conversation.getToId()};

        String message = conversation.getMsg();
        String formatted_message = message.length() > 30 ? message.substring(0, 30) + "..." : message;
        GDate date = new GDate();

        ContentValues values = new ContentValues();
        values.put(FriendTable.COLUMN_STATUS, formatted_message);
        values.put(FriendTable.COLUMN_UPDATED_AT, date.getTimeStamp());
        values.put(FriendTable.COLUMN_STATUS_ICON, status_type);
        int update = context.getContentResolver().update(FriendTable.CONTENT_URI, values, SELECTION, args);
        if (update > 0) {
            Log.d(TAG, " FriendTable updated  " + update);
        }
    }
}
