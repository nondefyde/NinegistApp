package zumma.com.ninegistapp.service.listeners;

import android.content.Context;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.parse.ParseUser;

import java.util.HashMap;

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

            Log.d(TAG, " onChildAdded 3 "+dataSnapshot.getValue() +"   "+s);
            if (messageChat.getReport() == 2){
                chatHelper.upDateFriendStatusChat(context,messageChat,1,4);
            }

//
//            if (messageChat!= null && !messageChat.getFromId().equals(parseUser.getObjectId()) && messageChat.getReport() == 0){
//                Log.d(TAG, " onChildAdded 2 "+dataSnapshot.getValue() +"   "+s);
//                chatHelper.InsertChatMessage(messageChat.getFromId(),messageChat);
//
//            }else{
//
//            }
        }

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, " onChildChanged "+dataSnapshot.getValue() +"   "+s);

        if (dataSnapshot != null) {
            HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();

            MessageChat messageChat = dataSnapshot.getValue(MessageChat.class);
            if (messageChat!= null && !messageChat.getFromId().equals(parseUser.getObjectId()) && messageChat.getReport() == 1){
                Log.d(TAG, " onChildAdded 2 "+dataSnapshot.getValue() +"   "+s);
                chatHelper.InsertChatMessage(messageChat.getFromId(),messageChat);

            }
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
}
