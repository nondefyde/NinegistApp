package zumma.com.ninegistapp.ui.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import zumma.com.ninegistapp.MainActivity;
import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.ui.fragments.ChatFragment;

public class ViewProfile extends Activity {

    private String friend_id;
    private ImageView imageView;
    private TextView textView;

    private boolean picture_exists;
    private Bitmap bitmap;

    private static final String TAG = ViewProfile.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Intent intent = getIntent();
        friend_id = intent.getStringExtra(ChatFragment.FRIEND_ID_PROFILE);
        imageView = (ImageView) findViewById(R.id.picture_image_view);
        textView = (TextView) findViewById(R.id.status_update_edit_view);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        init();
    }

    private void init(){
        if(friend_id != null){

            final String SELECTION = FriendTable.COLUMN_ID + "=?";
            String[] projection = {FriendTable.COLUMN_ID, FriendTable.COLUMN_PROFILE_PICTURE};
            final String[] args = {friend_id};
            Cursor cursor = getContentResolver().query(FriendTable.CONTENT_URI, projection, SELECTION, args, null);

            if (cursor != null && cursor.getCount() > 0) {
                int indexID = cursor.getColumnIndex(FriendTable.COLUMN_ID);
                int indexProfilePics = cursor.getColumnIndex(FriendTable.COLUMN_PROFILE_PICTURE);
                cursor.moveToFirst();
                do {

                    byte[] pic_byte = cursor.getBlob(indexProfilePics);
                    if (pic_byte != null){
                        bitmap = BitmapFactory.decodeByteArray(pic_byte, 0, pic_byte.length);
                        if (bitmap != null) {
                            imageView.setImageDrawable(null);
                            imageView.setImageBitmap(bitmap);
                            picture_exists = true;
                        }
                    }else{
                        Firebase picture_base = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(friend_id).child("basicInfo").child("picture");
                        picture_base.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null) {
                                    Log.d(TAG, dataSnapshot.toString() + " -onChildAdded");
                                    String imageString = dataSnapshot.getValue().toString();
                                    byte[] decodedImage = Base64.decode(imageString, Base64.DEFAULT);
                                    bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
                                    if (bitmap != null) {
                                        imageView.setImageDrawable(null);
                                        imageView.setImageBitmap(bitmap);
                                        picture_exists = true;
                                        ContentValues values = new ContentValues();
                                        values.put(FriendTable.COLUMN_PROFILE_PICTURE,decodedImage);
                                        int pics_inserted = getContentResolver().update(FriendTable.CONTENT_URI, values, SELECTION, args);
                                        if (pics_inserted > 0){
                                            Log.d(TAG, " pics_inserted");
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                    }

                }while (cursor.moveToNext());
                cursor.close();
            }

            Firebase status_base = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(friend_id).child("basicInfo").child("status");
            status_base.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null) {
                        String statusString = dataSnapshot.getValue().toString();
                        if (statusString != null) {
                            textView.setText(statusString);
                        }
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(item.getItemId() == R.id.share_view_profile){
            if(picture_exists){
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, friend_id, friend_id);
                Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Image Saved For: " + friend_id);
                Intent intent = new Intent(ViewProfile.this, MainActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(this, "Your friend has no profile image", Toast.LENGTH_SHORT).show();
            }
        }

        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
