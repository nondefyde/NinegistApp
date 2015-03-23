package zumma.com.ninegistapp.service.listeners;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import zumma.com.ninegistapp.database.table.FriendTable;

/**
 * Created by Okafor on 16/03/2015.
 */
public class TriggerListener implements ValueEventListener {

    private static final String TAG = TriggerListener.class.getSimpleName();
    private Context context;
    private String friend_id;

    public TriggerListener(Context context, String friend_id) {
        this.context = context;
        this.friend_id = friend_id;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if(dataSnapshot.getValue() != null) {
            Log.d(TAG, "Data Changed = "+friend_id +"'s Profile Picture Changed.");
            String SELECTION = FriendTable.COLUMN_ID + "=?";
            String[] projection = {FriendTable.COLUMN_ID, FriendTable.COLUMN_PROFILE_PICTURE};
            String[] args = {friend_id};

            Log.d(TAG, dataSnapshot.toString() + " -onChildAdded");
            String imageString = dataSnapshot.getValue().toString();
            byte[] decodedImage = Base64.decode(imageString, Base64.DEFAULT);
            Bitmap byteImage = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
            if (byteImage != null) {

                ContentValues values = new ContentValues();
                values.put(FriendTable.COLUMN_PROFILE_PICTURE,decodedImage);
                int pics_inserted = context.getContentResolver().update(FriendTable.CONTENT_URI, values, SELECTION, args);
                if (pics_inserted > 0){
                    Log.d(TAG, " pics_inserted from listener");
                }
            }
        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
