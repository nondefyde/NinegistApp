package zumma.com.ninegistapp.service.listeners;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

/**
 * Created by Okafor on 16/03/2015.
 */
public class TriggerListener implements ChildEventListener {

    private static final String TAG = TriggerListener.class.getSimpleName();
    private Context context;

    public TriggerListener(Context context) {
        this.context = context;
    }





    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, " onChildAdded "+dataSnapshot.getValue() +"   "+s);
        Toast.makeText(context,TAG+" onChildAdded "+dataSnapshot.getValue() +"   "+1, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, " onChildChanged " + dataSnapshot.getValue() + "   " + s);
        Toast.makeText(context,TAG+" onChildChanged "+dataSnapshot.getValue() +"   "+2, Toast.LENGTH_LONG).show();
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
