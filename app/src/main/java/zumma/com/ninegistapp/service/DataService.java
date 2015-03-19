package zumma.com.ninegistapp.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.parse.ParseUser;

import java.util.ArrayList;

import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.database.table.MessageTable;
import zumma.com.ninegistapp.service.listeners.ChatListeners;
import zumma.com.ninegistapp.service.listeners.FriendListener;
import zumma.com.ninegistapp.service.listeners.TriggerListener;

public class DataService extends Service {

    private static final String TAG = DataService.class.getSimpleName();
    SharedPreferences preferences;
    private FriendListener friendListener;
    private ChatListeners chatListener;
    private ParseUser mCurrentUser;
    private Firebase chatFirebase;
    private TriggerListener triggerListener;
    private String user_id;
    private ArrayList<String> frList;

    private Firebase[] firebases;
    private Firebase[] triggerArray;

    public DataService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "service started", Toast.LENGTH_LONG).show();

//        friendListener = new FriendListener(this);
        chatListener = new ChatListeners(this);
        triggerListener  = new TriggerListener(this);

        mCurrentUser = ParseUser.getCurrentUser();
        user_id = mCurrentUser.getObjectId();

        if (frList.size() > 0){
            for(String friendId : frList){
                Firebase mFirebaseRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(user_id).child("roasters").child("Chat").child(friendId);
                mFirebaseRef.addChildEventListener(chatListener);

                Firebase trigers = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(friendId).child("triggers");
                trigers.addChildEventListener(triggerListener);

                Log.d(TAG, "LISTENER ADDED TO CHAT PATH : " + mFirebaseRef.getPath().toString());
//                Log.d(TAG, "LISTENER ADDED TO TRIGGERS PATH : " + trigers.getPath().toString());
            }
        }

        return START_STICKY;
    }


    @Override
    public void onCreate() {
        Toast.makeText(this, "service started", Toast.LENGTH_LONG).show();

        frList = getChatUser();
        firebases = new Firebase[frList.size()];
        triggerArray = new Firebase[frList.size()];

    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_LONG).show();
//
        if (firebases.length > 0){
            for(Firebase firebase : firebases){
                firebase.removeEventListener(chatListener);
            }

//            for (Firebase firebase : trigersArr){
//                firebase.removeEventListener(triggerListener);
//            }
        }
    }


    public ArrayList getChatUser() {

        ArrayList<String> users = new ArrayList<String>();

        Cursor cursor = getContentResolver().query(FriendTable.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            int indexID = cursor.getColumnIndex(MessageTable.COLUMN_ID);
            cursor.moveToFirst();
            do {
                String id = cursor.getString(indexID);
                users.add(id);

            } while (cursor.moveToNext());
        }


        return users;
    }

}
