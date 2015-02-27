package zumma.com.ninegistapp.ui.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.model.CircleTransform;

public class PictureTest extends Activity {

    private static final String TAG = PictureTest.class.getSimpleName();

    private Firebase firebase;
    private ImageView testView;
    private CircleTransform circleTransform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_test);
        firebase = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(ParseUser.getCurrentUser().getObjectId()).child("basicInfo").child("picture");
        testView = (ImageView) findViewById(R.id.test_picture_image);
        firebase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.toString() + " -onChildAdded");
                String imageString = dataSnapshot.getValue().toString();
                byte[] decodedImage = Base64.decode(imageString, Base64.DEFAULT);
                Bitmap byteImage = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
                if (byteImage != null) {
                    //circleTransform = new CircleTransform();
                    //Bitmap circleBitmap = circleTransform.transform(byteImage);
                    Uri ourUri = uriImage(byteImage);
                    testView.setImageDrawable(null);
                    Picasso.with(PictureTest.this)
                            .load(ourUri)
                            .resize(R.integer.profile_width, R.integer.profile_height)
                            .transform(new CircleTransform())
                            .placeholder(R.drawable.ic_contact_picture_180_holo_light)
                            .into(testView);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_picture_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Uri uriImage(Bitmap bitmap) {
        //File filesDir = getAppContext().getFilesDir();
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "9NineGist" + File.separator);
        File imageFile = null;
        if(root.mkdirs() || root.exists()){
            //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + "profile_thumbnail" + ".jpg";
            imageFile = new File(root, imageFileName);
        }

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
            Uri outputUri = Uri.fromFile(imageFile);
            os.flush();
            os.close();
            return outputUri;
        } catch (Exception e) {
            Log.e(TAG, "Error writing bitmap", e);
            return null;
        }
    }
}
