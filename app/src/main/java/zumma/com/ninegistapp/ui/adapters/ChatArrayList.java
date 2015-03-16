package zumma.com.ninegistapp.ui.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

import zumma.com.ninegistapp.StaticHelpers;
import zumma.com.ninegistapp.database.table.MessageTable;
import zumma.com.ninegistapp.model.Conversation;

/**
 * Created by Okafor on 21/02/2015.
 */
public class ChatArrayList extends ArrayList<Conversation> {

    private static final String TAG = ChatArrayList.class.getSimpleName();

    private Context context;
    private String friend_id;

    public ChatArrayList(Context context, String friend_id) {
        this.context = context;
        this.friend_id = friend_id;
    }

    public boolean addNew(Conversation conversation){
        saveConversation(conversation);
        return super.add(conversation);
    }

    public void saveConversation(Conversation conversation){

        ContentValues values = new ContentValues();

        values.put(MessageTable.COLUMN_FROM,conversation.getFromId());
        values.put(MessageTable.COLUMN_TO,conversation.getToId());
        values.put(MessageTable.COLUMN_MESSAGE,conversation.getMsg());
        values.put(MessageTable.COLUMN_SENT,conversation.isSent());
        values.put(MessageTable.COLUMN_TIME,conversation.getDate());
        values.put(MessageTable.COLUMN_FRIEND_ID,friend_id);
        values.put(MessageTable.COLUMN_PRIVATE,conversation.getPflag());
        values.put(MessageTable.COLUMN_REPORT,conversation.getReport());
        values.put(MessageTable.COLUMN_CREATED_AT,conversation.getCreated_at());
        values.put(MessageTable.COLUMN_UNIQ_ID,conversation.getUniqkey());

        Uri uri = context.getContentResolver().insert(MessageTable.CONTENT_URI, values);
        if (uri != null){
            Log.d(TAG, " uri  "+uri.toString());
        }
    }

    public void upDateDeliveredConversation(Conversation conversation){

        for (int i = this.size() - 1; i > 0; i-- ){
            Conversation conver = this.get(i);
            if (conver.getUniqkey().equals(conversation.getUniqkey()) && conver.getReport() == 1){
                conver.setReport(1);
                this.remove(i);
                this.add(i,conver);
            }
        }

        String SELECTION = MessageTable.COLUMN_FRIEND_ID + "=? AND "+ MessageTable.COLUMN_CREATED_AT +"=?";
        String[] args = {friend_id, conversation.getCreated_at()+""};

        ContentValues values = new ContentValues();
        values.put(MessageTable.COLUMN_REPORT,conversation.getReport());
        values.put(MessageTable.COLUMN_UNIQ_ID,conversation.getUniqkey());

        int update = context.getContentResolver().update(MessageTable.CONTENT_URI, values,SELECTION,args);
        if (update > 0){
            Log.d(TAG, " update delivered happened  "+update);
        }
    }

    public void upDisplayedConversation(Conversation conversation){

        for (int i = this.size() - 1; i > 0; i-- ){
            Conversation conver = this.get(i);
            if (conver.getUniqkey().equals(conversation.getUniqkey()) && conver.getReport() == 1){
                conver.setReport(2);
                this.remove(i);
                this.add(i,conver);
            }
        }
        String SELECTION = MessageTable.COLUMN_FRIEND_ID + "=? AND "+ MessageTable.COLUMN_CREATED_AT +"=?";
        String[] args = {friend_id, conversation.getCreated_at()+""};

        ContentValues values = new ContentValues();
        values.put(MessageTable.COLUMN_REPORT,conversation.getReport());

        int update = context.getContentResolver().update(MessageTable.CONTENT_URI, values,SELECTION,args);
        if (update > 0){
            Log.d(TAG, " update displayed happened  "+update);
        }
        StaticHelpers.upDateFriendListWithInChat(context, conversation, 4);
    }

    public void initChatList(){

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

                int flag = cursor.getInt(indexPrivate);
                int report = cursor.getInt(indexReport);
                long created = cursor.getLong(indexCreatedAt);
                String uniq = cursor.getString(indexUnique);

                Conversation conversation = new Conversation(fromId,toId,date,sent,msg,flag,report,created,uniq);
//                Log.d(TAG, conversation.toString() );

                add(conversation);

            } while (cursor.moveToNext());
        }

        cursor.close();
    }
}
