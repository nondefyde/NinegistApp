package zumma.com.ninegistapp.ui.listeners;

import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.parse.ParseUser;

import java.util.HashMap;

import zumma.com.ninegistapp.model.Conversation;
import zumma.com.ninegistapp.ui.adapters.ChatAdapter;
import zumma.com.ninegistapp.ui.adapters.ChatArrayList;

/**
 * Created by Okafor on 24/02/2015.
 */
public class MyChatListener implements ChildEventListener {

    private static final String TAG = UserChatListeners.class.getSimpleName();

    private ChatAdapter chatAdapter;
    private ChatArrayList conversations;

    private ParseUser parseUser;

    public MyChatListener(ChatAdapter chatAdapter, ChatArrayList conversations) {
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
            if (conversation.getReport() == 2) {
                conversations.upDisplayedConversation(conversation);
                chatAdapter.notifyDataSetChanged();
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
