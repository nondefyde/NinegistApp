package zumma.com.ninegistapp.ui.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import zumma.com.ninegistapp.MainActivity;
import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.utils.FileHelper;

public class SelectPicture extends Activity implements View.OnClickListener {

    private static final String TAG = SelectPicture.class.getSimpleName();

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri outputCropUri;
    private Uri cameraUri;
    private ImageView imageView;
    private EditText statusUpdate;
    private File imageFile;
    private boolean imageSelected = false;
    private String status;
    private Firebase firebase;
    private Firebase status_base;
    private Firebase home_base;

//    public static String TRIGGERS = "triggers";
//    public static String STATUS_TRIGGER = "status_trigger";
//    public static String IMAGE_TRIGGER = "image_trigger";
//    private int stat;
//    private int img;

    private boolean statusChanged;
    private boolean imageChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_picture);

//        if (getSharedPreferences(TRIGGERS, 0).getInt(STATUS_TRIGGER, -1) == -1) {
//            stat = 0;
//            getSharedPreferences(TRIGGERS, 0).edit().putInt(STATUS_TRIGGER, stat).commit();
//            Log.d(TAG, "Inside Preferences");
//        } else {
//            stat = getSharedPreferences(TRIGGERS, 0).getInt(STATUS_TRIGGER, -1);
//            Log.d(TAG, "Inside Else Preferences");
//        }
//
//        if (getSharedPreferences(TRIGGERS, 0).getInt(IMAGE_TRIGGER, -1) == -1) {
//            img = 0;
//            getSharedPreferences(TRIGGERS, 0).edit().putInt(IMAGE_TRIGGER, img).commit();
//        } else {
//            img = getSharedPreferences(TRIGGERS, 0).getInt(IMAGE_TRIGGER, -1);
//        }

        firebase = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(ParseUser.getCurrentUser().getObjectId()).child("basicInfo").child("picture");
        status_base = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(ParseUser.getCurrentUser().getObjectId()).child("basicInfo").child("status");
        home_base = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(ParseUser.getCurrentUser().getObjectId()).child("triggers");
        findViewById(R.id.picure_button_ok).setOnClickListener(this);
        findViewById(R.id.picture_button_cancel).setOnClickListener(this);
        findViewById(R.id.pic_image_button).setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.picture_image);
        statusUpdate = (EditText) findViewById(R.id.status_update_edit);
        setUpImageFile();
        setUpStatus();
        initImage();
    }

    private void setUpStatus() {
        status_base.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.toString() + " -onChildAdded");
                if(dataSnapshot.getValue() != null) {
                    String status = dataSnapshot.getValue().toString();
                    if (!status.equals("")) {
                        statusUpdate.setText(status);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void setUpImageFile() {
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "9NineGist" + File.separator);
        if (root.mkdirs() || root.exists()) {
            //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + "picture" + ".jpg";
            imageFile = new File(root, imageFileName);
        }
    }

    private void setUpPicture() {
        firebase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.toString() + " -onChildAdded");
                String imageString = dataSnapshot.getValue().toString();
                byte[] decodedImage = Base64.decode(imageString, Base64.DEFAULT);
                Bitmap byteImage = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
                if (byteImage != null) {
                    //Uri.fromFile(new File(getCacheDir(), "cropped"))
//                    testView.setImageDrawable(null);
//                    testView.setImageBitmap(byteImage);
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
        getMenuInflater().inflate(R.menu.menu_select_picture, menu);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pic_image_button:
                openImageIntent();
                break;
            case R.id.picture_button_cancel:
                cancelUpdate();
                break;
            case R.id.picure_button_ok:
                updatePhoto();
//                uploadImage();
                break;
            default:
                Toast.makeText(this, "Default", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void uploadImage() {

        if (imageSelected) {

        } else {
            ParseObject object = createMessage(outputCropUri);
            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
//                      set the trigger for all friends online......
                        Toast.makeText(SelectPicture.this, " profile Picture updated", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(SelectPicture.this, " profile Picture error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }


    private void cancelUpdate() {
        Intent intent = new Intent(SelectPicture.this, MainActivity.class);
        startActivity(intent);
    }

    private void openImageIntent() {

        cameraUri = Uri.fromFile(imageFile);
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        //Get All Packages/Apps that responds to camera intents
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        //For each returned as ResolveInfo:
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE);
    }

    private void updatePhoto() {
        statusChanged = false;
        imageChanged = false;
        if (imageSelected || statusUpdate.getText().toString().trim().length() > 0) {
            if (statusUpdate.getText().toString().trim().length() > 0) {
                status = statusUpdate.getText().toString();
                status_base.setValue(status.trim(), new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError != null) {
                            Log.d(TAG, "Data could not be saved. " + firebaseError.getMessage());
                        } else {
                            Log.d(TAG, "Status Changed");
                            statusChanged = true;
                            //updateTriggers();
                            //stat++;
                            //home_base.child("statusTrigger").setValue(stat);
                            //getSharedPreferences(TRIGGERS, 0).edit().putInt(STATUS_TRIGGER, stat).commit();
                            //Log.d(TAG, "Inside else of "+ stat);
                        }
                    }
                });
            }
            if (imageSelected) {
                byte[] ourByte = FileHelper.getByteArrayFromFile(this, outputCropUri);
                String image = Base64.encodeToString(ourByte, Base64.DEFAULT);
                firebase.setValue(image, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError != null) {
                            Log.d(TAG, "Data could not be saved. " + firebaseError.getMessage());
                        }
                        else {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), outputCropUri);
                                OutputStream os = new FileOutputStream(imageFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                                os.flush();
                                os.close();
                                Log.d(TAG, "Image Changed");
                                imageChanged = true;
                            } catch (Exception e) {
                                Log.d(TAG, "Exception Occurred! - " + e.getMessage());
                            }
                            //updateTriggers();
                            //img++;
                            //home_base.child("imageTrigger").setValue(img);
                            //getSharedPreferences(TRIGGERS, 0).edit().putInt(IMAGE_TRIGGER, img).commit();
                        }
                    }
                });
            }
            Intent intent = new Intent(SelectPicture.this, MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Select an image or click cancel to proceed with no image.", Toast.LENGTH_SHORT).show();
        }
    }

/*    private void updateTriggers(){
        Log.d(TAG, "imageChanged: " +imageChanged +"-- statusChanged: "+ statusChanged);
        if(imageChanged && statusChanged){
            home_base.setValue(1);
            Log.d(TAG, "Image and Status Changed - Triggers");
        }
        if(imageChanged && !statusChanged){
            home_base.setValue(2);
            Log.d(TAG, "Image Changed - Triggers");
        }
        if(!imageChanged && statusChanged){
            Log.d(TAG, "Status Changed - Triggers");
            home_base.setValue(3);
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        outputCropUri = Uri.fromFile(imageFile); //Uri.fromFile(new File(getCacheDir(), "cropped"));
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                boolean isCamera;
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        isCamera = true;
                    } else {
                        final String action = data.getAction();
                        if (action == null) {
                            isCamera = false;
                        } else {
                            isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                        }
                    }
                    if (isCamera) {
                        new Crop(cameraUri).output(outputCropUri).asSquare().start(this);
                    } else {
                        new Crop(data.getData()).output(outputCropUri).asSquare().start(this);
                    }
                }
                break;
            case Crop.REQUEST_CROP:
                loadImage();
                break;
            default:
                break;
        }
    }

    private void initImage() {
        outputCropUri = Uri.fromFile(imageFile);
        if (imageFile.exists()) {
            imageView.setImageDrawable(null);
            imageView.setImageURI(outputCropUri);
        }
    }

    private void loadImage() {
//        Picasso.with(SelectPicture.this)
//                .load(outputCropUri)
//                .placeholder(R.drawable.ic_contact_picture_180_holo_light)
//                .fit()
//                .centerCrop()
//                .into(imageView);
        try {
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), outputCropUri);
//            OutputStream os = new FileOutputStream(imageFile);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
//            outputCropUri = Uri.fromFile(imageFile);
//            os.flush();
//            os.close();
            //imageView.setImageDrawable(null);
            outputCropUri = Uri.fromFile(imageFile);
            Picasso.with(SelectPicture.this)
                    .load(outputCropUri)
                    .placeholder(R.drawable.ic_contact_picture_180_holo_light)
                    .fit()
                    .centerCrop()
                    .skipMemoryCache()
                    .into(imageView);
            imageSelected = true;
        } catch (Exception e) {
            Log.d(TAG, "Exception Occurred! - " + e.getMessage());
        }


//        ParseObject object = createMessage(null);
//        object.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e == null)
//            }
//        });
    }


    private ParseObject createMessage(Uri mMediaUri) {

        ParseObject message = new ParseObject(ParseConstants.CLASS_PROFILE_IMAGE);
        message.put(ParseConstants.OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
        fileBytes = FileHelper.reduceImageForUpload(fileBytes);
        String fileName = FileHelper.getFileName(this, mMediaUri, ParseConstants.TYPE_IMAGE);
        ParseFile file = new ParseFile(fileName, fileBytes);
        message.put(ParseConstants.KEY_FILE, file);

        return message;
    }

}