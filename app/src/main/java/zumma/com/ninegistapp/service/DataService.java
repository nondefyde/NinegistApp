package zumma.com.ninegistapp.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.parse.ParseUser;

import java.util.ArrayList;

import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.database.table.ChatTable;
import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.service.listeners.ChatListeners;
import zumma.com.ninegistapp.service.listeners.FriendListener;

public class DataService extends Service {

    private static final String TAG = DataService.class.getSimpleName();
    SharedPreferences preferences;
    private FriendListener friendListener;
    private ChatListeners chatListener;
    private ParseUser mCurrentUser;
    private Firebase chatFirebase;
    private String user_id;
    private ArrayList<String> frList;

    public DataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "service started", Toast.LENGTH_LONG).show();

        friendListener = new FriendListener(this);
        chatListener = new ChatListeners(this);

        mCurrentUser = ParseUser.getCurrentUser();
        user_id = mCurrentUser.getObjectId();

        chatFirebase = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(user_id).child("chats");
        chatFirebase.addChildEventListener(chatListener);
//
//        if (frList.size() > 0){
//            for(String friendId : frList){
//                Firebase mFirebaseRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(friendId);
//                mFirebaseRef.addValueEventListener(friendListener);
//                Log.d(TAG," PATH : "+ mFirebaseRef.getPath().toString());
//            }
//        }

        return START_STICKY;
    }


    @Override
    public void onCreate() {
        Toast.makeText(this, "service started", Toast.LENGTH_LONG).show();

        frList = getChatUser();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_LONG).show();
//
//        if (frList.size() > 0){
//            for(String userId : frList){
//                Firebase mFirebaseRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist/").child(userId);
//                mFirebaseRef.removeEventListener(chatListener);
//            }
//        }

        chatFirebase.removeEventListener(chatListener);
    }


    public ArrayList getChatUser() {

        ArrayList<String> users = new ArrayList<String>();

        Cursor cursor = getContentResolver().query(FriendTable.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            int indexID = cursor.getColumnIndex(ChatTable.COLUMN_ID);
            cursor.moveToFirst();
            do {
                String id = cursor.getString(indexID);
                users.add(id);

            } while (cursor.moveToNext());
        }


        return users;
    }

}
