package zumma.com.ninegistapp.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;

import com.firebase.client.Firebase;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.model.BasicInfo;
import zumma.com.ninegistapp.service.classes.FriendsSearch;

public class FriendsSearchService extends Service {

    private static final String TAG = FriendsSearchService.class.getSimpleName();
    FriendsSearchHandler friendsSearchHandler;
    Looper fLooper;

    ParseUser mCurrentUser;
    ParseRelation<ParseUser> mFriendsRelation;
    ResultReceiver resRec;


    private Firebase friends;



    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        fLooper = thread.getLooper();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        friendsSearchHandler = new FriendsSearchHandler(fLooper);

        friends = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(mCurrentUser.getObjectId());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Message message = friendsSearchHandler.obtainMessage();
        message.arg1 = startId;
        friendsSearchHandler.sendMessage(message);

        return START_NOT_STICKY;
    }

    private class FriendsSearchHandler extends Handler {
        public FriendsSearchHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            HashMap<String, BasicInfo> friendsArray = new HashMap<String,BasicInfo>();
            HashMap<String,String> roaster = new HashMap<String,String>();

            FriendsSearch FriendsSearch = new FriendsSearch();

            ArrayList<ContentValues> contentValues = new ArrayList<ContentValues>();
            Set<Map.Entry<String, String>> contacts = FriendsSearch.allUserContacts(getApplicationContext()).entrySet();

            for (Map.Entry<String, String> eachEntry : contacts) {
                String number = eachEntry.getValue();
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo(ParseConstants.KEY_USERNAME, "+"+number);
                Log.d(TAG, "username : " + number);
                ParseUser user = null;
                try {
                    user = query.getFirst();
                    if (user != null) {
                        Log.d(TAG, "number main : " + user.getUsername() + " name " + eachEntry.getValue());
                        if (!mCurrentUser.getUsername().equals(user.getUsername())) {
                            ContentValues values = FriendsSearch.toContentValues(user, eachEntry.getKey(), mCurrentUser);
                            contentValues.add(values);
                            Log.d(TAG, "values " + values.toString());
                            String SELECTION = FriendTable.COLUMN_ID + "=?";
                            String[] args = {values.getAsString(FriendTable.COLUMN_ID)};
                            Cursor cursor = getContentResolver().query(FriendTable.CONTENT_URI, null, SELECTION, args, null);
                            if (cursor.getCount() == 0) {
                                boolean inserted = FriendsSearch.insertFriend(getApplicationContext(), values);
                                if (inserted) {
                                    BasicInfo info = new BasicInfo(user);
                                    friendsArray.put(user.getObjectId(),info);
                                    roaster.put(user.getObjectId(),"a new Chat");

                                    Log.d(TAG, " inserted values " + values.toString());
                                    mFriendsRelation.add(user);
                                    mCurrentUser.saveInBackground();
                                    friends.child(ParseConstants.ROASTER).child(ParseConstants.CHAT).setValue(roaster);
                                }
                            }

                            cursor.close();
                            friends.child(ParseConstants.FRIENDS).setValue(friendsArray);
                        }
                    }
                } catch (ParseException e) {
                    Log.d(TAG, "  ParseException "+e.getMessage() +"number : "+number +"  name "+eachEntry.getKey());
                }
            }



            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            preferences.edit().putBoolean(ParseConstants.NG_FRIENDS, true).apply();
            stopSelf(msg.arg1);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
