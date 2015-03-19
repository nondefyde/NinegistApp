package zumma.com.ninegistapp.ui.activities;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseFile;
import com.parse.ParseUser;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.custom.CustomActivity;
import zumma.com.ninegistapp.utils.FileHelper;


public class Profile extends CustomActivity implements View.OnClickListener {

    private static final String TAG = Profile.class.getSimpleName();
    private ParseUser parseUser;
    private ImageView imageView;
    private ImageView edit_image;
    private ImageView edit_status;
    private File imageFile;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri outputCropUri;
    private Uri cameraUri;
    private boolean imageSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        initComponent();
    }

    private void initComponent() {

        ActionBar localActionBar = getActionBar();
        localActionBar.setTitle("Your Profile");
        localActionBar.setDisplayHomeAsUpEnabled(false);
        localActionBar.setHomeButtonEnabled(true);
        localActionBar.setDisplayShowHomeEnabled(false);

        imageView = (ImageView) findViewById(R.id.image);
        edit_image = (ImageView) findViewById(R.id.edit_image);
        edit_status = (ImageView) findViewById(R.id.edit_status);

        edit_image.setOnClickListener(this);
        edit_status.setOnClickListener(this);

        parseUser = ParseUser.getCurrentUser();

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_done) {
            executeOkAction();
        }
        return super.onOptionsItemSelected(item);
    }

    public void executeOkAction(){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_image:
                openImageIntent();
                break;
            case R.id.edit_profile:

                break;
            default:
                Toast.makeText(Profile.this, "Default", Toast.LENGTH_LONG).show();
                break;
        }
    }


    private void updatePhoto() {
        if (imageSelected) {
            if (imageSelected) {
                byte[] ourByte = FileHelper.getByteArrayFromFile(this, outputCropUri);

            }

        } else {
            Toast.makeText(this, "Select an image or click cancel to proceed with no image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImageIntent() {

//        cameraUri = Uri.fromFile(imageFile);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

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

    private void loadImage() {
//        Picasso.with(SelectPicture.this)
//                .load(outputCropUri)
//                .placeholder(R.drawable.ic_contact_picture_180_holo_light)
//                .fit()
//                .centerCrop()
//                .into(imageView);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), outputCropUri);
            OutputStream os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
            outputCropUri = Uri.fromFile(imageFile);
            os.flush();
            os.close();
            //imageView.setImageDrawable(null);
            Picasso.with(this)
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
    }




    public void init(){
        if (parseUser.getParseFile("profileImage") != null){

            ParseFile file = parseUser.getParseFile(ParseConstants.KEY_FILE);
            Uri fileUri = Uri.parse(file.getUrl());

            Picasso.with(this)
                    .load(fileUri)
                    .placeholder(R.drawable.ic_contact_picture_180_holo_light)
                    .fit()
                    .centerCrop()
                    .skipMemoryCache()
                    .into(imageView);
        }
    }
}
