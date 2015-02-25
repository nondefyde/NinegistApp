package zumma.com.ninegistapp.ui.listeners;

import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.parse.ParseUser;

import java.util.HashMap;

import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.model.Conversation;
import zumma.com.ninegistapp.ui.adapters.ChatAdapter;
import zumma.com.ninegistapp.ui.adapters.ChatArrayList;

/**
 * Created by Okafor on 20/02/2015.
 */
public class UserChatListeners implements ChildEventListener {


    private static final String TAG = UserChatListeners.class.getSimpleName();

    private ChatAdapter chatAdapter;
    private ChatArrayList conversations;

    private ParseUser parseUser;

    public UserChatListeners(ChatAdapter chatAdapter, ChatArrayList conversations) {
        this.chatAdapter = chatAdapter;
        this.conversations = conversations;

        parseUser = ParseUser.getCurrentUser();
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, " na wa onChildAdded " + dataSnapshot.getValue() + "   " + s);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, " onChildChanged " + dataSnapshot.getValue() + "   " + s);

        if (dataSnapshot != null) {
            HashMap<String,String> map = (HashMap<String, String>) dataSnapshot.getValue();
            Conversation conversation = dataSnapshot.getValue(Conversation.class);

            if (!conversation.getFromId().equals(parseUser.getObjectId()) && conversation.getUniqkey() != null) {

                String fromId = conversation.getFromId();
                String toId = conversation.getToId();

                conversation.setUniqkey(map.get("uniqkey"));
                conversation.setSent(false);

                if (conversation.getReport() != 2){

                    conversations.addNewChat(conversation);
                    chatAdapter.notifyDataSetChanged();

                    Log.d(TAG, " Conversation " + conversation.toString());
                    Firebase uFirebaseRef = new Firebase(ParseConstants.FIREBASE_URL)
                            .child("9Gist")
                            .child(toId)
                            .child("chats")
                            .child(fromId)
                            .child(conversation.getUniqkey())
                            .child("report");

                    uFirebaseRef.setValue(2);
                }
                Log.d(TAG, " onChildChanged delivered report" + conversation.getReport());
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
