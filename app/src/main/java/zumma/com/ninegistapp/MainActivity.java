package zumma.com.ninegistapp;

/**
 * Created by Okafor on 03/02/2015.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zumma.com.ninegistapp.custom.CustomActivity;
import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.model.BasicInfo;
import zumma.com.ninegistapp.model.CircleTransform;
import zumma.com.ninegistapp.model.Conversation;
import zumma.com.ninegistapp.model.Data;
import zumma.com.ninegistapp.model.Friend;
import zumma.com.ninegistapp.model.User;
import zumma.com.ninegistapp.service.FriendsSearchService;
import zumma.com.ninegistapp.service.classes.FriendsSearch;
import zumma.com.ninegistapp.ui.activities.SelectPicture;
import zumma.com.ninegistapp.ui.adapters.LeftNavAdapter;
import zumma.com.ninegistapp.ui.adapters.RightNavAdapter;
import zumma.com.ninegistapp.ui.fragments.ChatFragment;
import zumma.com.ninegistapp.ui.fragments.FindMatch;
import zumma.com.ninegistapp.ui.fragments.FriendsFragment;
import zumma.com.ninegistapp.ui.fragments.Match;
import zumma.com.ninegistapp.ui.fragments.SettingFragment;

/**
 * The Class MainActivity is the base activity class of the application. This
 * activity is launched after the Login and it holds all the Fragments used in
 * the app. It also creates the Navigation Drawers on left and right side.
 */
public class MainActivity extends CustomActivity implements ChatFragment.SetSubtitle, FriendsFragment.CheckHome
{

    private static final String TAG = MainActivity.class.getSimpleName();
    SharedPreferences preferences;
    /** The drawer layout. */
    private DrawerLayout drawerLayout;
    /** ListView for left side drawer. */
    private ListView drawerLeft;
    /** ListView for right side drawer. */
    private ListView drawerRight;
    /** The drawer toggle. */
    private ActionBarDrawerToggle drawerToggle;
    /** The left navigation list adapter. */
    private LeftNavAdapter adapter;
    private ImageView iView;
    private Firebase myConnectionsRef;
    private Firebase lastOnline;
    final Firebase connectedRef = new Firebase(ParseConstants.FIREBASE_URL + "/.info/connected");

    private Bundle bundle;
    private boolean loaded;
    private byte[] image;

    /* (non-Javadoc)
     * @see com.newsfeeder.custom.CustomActivity#onCreate(android.os.Bundle)
     */
    private Menu menu;
    private boolean isChat;
    private boolean isSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupDrawer();
        setupContainer();

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        initUser();

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    // add this device to my connections list
                    // this value could contain info about the device or a timestamp too
                    myConnectionsRef.setValue(Boolean.TRUE);

                    // when this device disconnects, remove it
                    myConnectionsRef.onDisconnect().setValue(Boolean.FALSE);


                    // when I disconnect, update the last time I was seen online
                    lastOnline.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
                Log.d(TAG, "Listener was cancelled at .info/connected");
            }
        });
    }

    /**
     * Setup the drawer layout. This method also includes the method calls for
     * setting up the Left & Right side drawers.
     */
    private void setupDrawer()
    {

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view)
            {
                setActionBarTitle();
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                setActionBarTitle();
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.closeDrawers();

        setupLeftNavDrawer();
        setupRightNavDrawer();
    }

    /**
     * Setup the left navigation drawer/slider. You can add your logic to load
     * the contents to be displayed on the left side drawer. It will also setup
     * the Header and Footer contents of left drawer. This method also apply the
     * Theme for components of Left drawer.
     */
    private void setupLeftNavDrawer()
    {

        drawerLeft = (ListView) findViewById(R.id.left_drawer);

        View header = getLayoutInflater().inflate(R.layout.left_nav_header,
                null);
        iView = (ImageView) header.findViewById(R.id.main_profile_image);
        setUpHeaderImage();
        drawerLeft.addHeaderView(header);

        adapter = new LeftNavAdapter(this, getResources().getStringArray(
                R.array.arr_left_nav_list));
        drawerLeft.setAdapter(adapter);
        drawerLeft.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3)
            {
                drawerLayout.closeDrawers();
                //drawerLeft.setSelection(pos);
                Log.d(TAG, "am Position " + pos);
                adapter.setSelection(pos-1);
                if (pos != 0) {
                    if(pos == 1) {
                        launchFragment(pos - 1, null);
                    }
                    if(pos == 2) {
                        launchFragment(pos, null);
                    }
                }
                else
                    launchFragment(-2, null);

            }
        });
    }

    private void setUpHeaderImage(){
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "9NineGist" + File.separator);
        loaded = false;
        if(root.exists()){
            Log.d(TAG, "Root Exists");
            final String imageFileName = "JPEG_" + "picture" + ".jpg";
            final File imageFile = new File(root, imageFileName);
            if(imageFile.exists()){
                iView.setImageDrawable(null);
                Uri uri = Uri.fromFile(imageFile);
                Picasso.with(MainActivity.this)
                        .load(uri)
                        .resize(getResources().getInteger(R.integer.profile_width), getResources().getInteger(R.integer.profile_height))
                        .transform(new CircleTransform())
                        .placeholder(R.drawable.ic_contact_picture_180_holo_light)
                        .skipMemoryCache()
                        .into(iView);
                Log.d(TAG, "Profile Folder Exists and Image Found");
                loaded = true;
            }
            else{
                Firebase firebase = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(ParseUser.getCurrentUser().getObjectId()).child("basicInfo").child("picture");
                firebase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null) {
                            String imageString = dataSnapshot.getValue().toString();
                            byte[] decodedImage = Base64.decode(imageString, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
                            if (bitmap != null) {
                                try{
                                    OutputStream os = new FileOutputStream(imageFile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                                    os.flush();
                                    os.close();
                                    Uri uri = Uri.fromFile(imageFile);
                                    Picasso.with(MainActivity.this)
                                            .load(uri)
                                            .resize(getResources().getInteger(R.integer.profile_width), getResources().getInteger(R.integer.profile_height))
                                            .transform(new CircleTransform())
                                            .placeholder(R.drawable.ic_contact_picture_180_holo_light)
                                            .skipMemoryCache()
                                            .into(iView);
                                    Log.d(TAG, "Profile Folder Exists and Image Retrieved From Firebase and Saved to Folder");
                                } catch (Exception e) {
                                    Log.d(TAG, "Exception Occurred While Making Saving Image " + e.getMessage());
                                }
                                loaded = true;
                            }
                            else{
                                Picasso.with(MainActivity.this)
                                        .load(R.drawable.ic_contact_picture_180_holo_light)
                                        .resize(getResources().getInteger(R.integer.profile_width), getResources().getInteger(R.integer.profile_height))
                                        .transform(new CircleTransform())
                                        .into(iView);
                                Log.d(TAG, "Profile Folder Exists and No Picture in Folder or Online");
                                loaded = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.d(TAG, "OnCancelled "+ firebaseError.toString());
                    }
                });
            }
        }
        else{
            Log.d(TAG, "Root Does Not Exist");
            Firebase firebase = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(ParseUser.getCurrentUser().getObjectId()).child("basicInfo").child("picture");
            firebase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null) {
                        String imageString = dataSnapshot.getValue().toString();
                        byte[] decodedImage = Base64.decode(imageString, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
                        if (bitmap != null) {
                            final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "9NineGist" + File.separator);
                            if (root.mkdirs() || root.exists()) {
                                //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                String imageFileName = "JPEG_" + "picture" + ".jpg";
                                File imageFile = new File(root, imageFileName);
                                try {
                                    OutputStream os = new FileOutputStream(imageFile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                                    os.flush();
                                    os.close();
                                    Uri uri = Uri.fromFile(imageFile);
                                    Picasso.with(MainActivity.this)
                                            .load(uri)
                                            .resize(getResources().getInteger(R.integer.profile_width), getResources().getInteger(R.integer.profile_height))
                                            .transform(new CircleTransform())
                                            .placeholder(R.drawable.ic_contact_picture_180_holo_light)
                                            .skipMemoryCache()
                                            .into(iView);
                                    Log.d(TAG, "Profile Folder Does Not Exist. Created and Image Retrieved");
                                } catch (Exception e) {
                                    Log.d(TAG, "Exception Occurred While Making Saving Image-2 " + e.getMessage());
                                }
                            }
                            loaded = true;
                        }
                        else{
                            Picasso.with(MainActivity.this)
                                    .load(R.drawable.ic_contact_picture_180_holo_light)
                                    .resize(getResources().getInteger(R.integer.profile_width), getResources().getInteger(R.integer.profile_height))
                                    .transform(new CircleTransform())
                                    .into(iView);
                            Log.d(TAG, "No Profile Folder Found and No Online Profile Image");
                            loaded = true;
                        }
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.d(TAG, "OnCancelled "+ firebaseError.toString());

                }
            });
        }
        if(!loaded) {
            Picasso.with(MainActivity.this)
                    .load(R.drawable.ic_contact_picture_180_holo_light)
                    .resize(getResources().getInteger(R.integer.profile_width), getResources().getInteger(R.integer.profile_height))
                    .transform(new CircleTransform())
                    .into(iView);
            Log.d(TAG, "No Internet/Picture To Retrieve and No Profile Folder Exists");
        }
    }

    /**
     * Setup the right navigation drawer/slider. You can add your logic to load
     * the contents to be displayed on the right side drawer. It will also setup
     * the Header contents of right drawer.
     */
    private void setupRightNavDrawer()
    {
        drawerRight = (ListView) findViewById(R.id.right_drawer);

        View header = getLayoutInflater().inflate(R.layout.rigth_nav_header,
                null);
        header.setClickable(true);
        drawerRight.addHeaderView(header);

        ArrayList<Data> al = new ArrayList<Data>();
        al.add(new Data("Emely", R.drawable.img_f1));
        al.add(new Data("John", R.drawable.img_f2));
        al.add(new Data("Aaliyah", R.drawable.img_f3));
        al.add(new Data("Valentina", R.drawable.img_f4));
        al.add(new Data("Barbara", R.drawable.img_f5));

        ArrayList<Data> al1 = new ArrayList<Data>(al);
        al1.addAll(al);
        al1.addAll(al);
        al1.addAll(al);

        drawerRight.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                drawerLayout.closeDrawers();
                launchFragment(1, null);
            }
        });
        drawerRight.setAdapter(new RightNavAdapter(this, al1));
    }

    /**
     * Setup the container fragment for drawer layout. This method will setup
     * the grid view display of main contents. You can customize this method as
     * per your need to display specific content.
     */
    private void setupContainer()
    {
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {

                    @Override
                    public void onBackStackChanged()
                    {
                        setActionBarTitle();
                    }
                });
        launchFragment(0, null);
    }

    /**
     * This method can be used to attach Fragment on activity view for a
     * particular tab position. You can customize this method as per your need.
     *
     * @param pos
     *            the position of tab selected.
     */
    public void launchFragment(int pos, Bundle bundle)
    {
        this.bundle = bundle;
        isChat = false;
        isSetting = false;
        Fragment f = null;
        String title = null;
        if (pos == -1)
        {
            title = "Your Match";
            f = new Match();
        }
        else if (pos == -2)
        {
            title = "Profile";
            Intent intent = new Intent(this, SelectPicture.class);
            intent.putExtra("data",bundle);
            startActivity(intent);
        }
        else if (pos == 0)
        {
            title = "Home";
            Log.d(TAG, "am MainFrag Launch");
            f = new FriendsFragment();

        }
        else if (pos == 1)
        {
            title = bundle.getString(ParseConstants.ACTION_BAR_TITLE);
            image = bundle.getByteArray(ParseConstants.KEY_PROFILE_IMAGE);
            isChat = true;
            f = new ChatFragment();
            f.setArguments(bundle);

        }
        else if (pos == 2)
        {
            title = "Settings";
            isSetting = true;
            f = new SettingFragment();
//            f = new Settings();
        }
        else if (pos == 3)
        {
            title = "Find Match";
            f = new FindMatch();
        }

        if (f != null)
        {
            while (getSupportFragmentManager().getBackStackEntryCount() > 1)
            {
                getSupportFragmentManager().popBackStackImmediate();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, f)
                    .addToBackStack(title)
                    .commit();
            if (adapter != null && pos == 1)
                adapter.setSelection(-1);
            setActionBarTitle();
            invalidateOptionsMenu();
        }
    }

    /**
     * Set the action bar title text.
     */
    private void setActionBarTitle()
    {
        if (drawerLayout.isDrawerOpen(drawerLeft))
        {
            getActionBar().setTitle("Main Menu");
            getActionBar().setSubtitle(null);
            return;
        }
        if (drawerLayout.isDrawerOpen(drawerRight))
        {
            getActionBar().setTitle(R.string.all_matches);
            getActionBar().setSubtitle(null);
            return;
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            return;
        String title = getSupportFragmentManager().getBackStackEntryAt(
                getSupportFragmentManager().getBackStackEntryCount() - 1)
                .getName();
        getActionBar().setTitle(title);
        if (isChat && image != null && !title.equals("Home"))
        {
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            getActionBar().setIcon(drawable);
        }
        else{
            try {
                getActionBar().setIcon(getPackageManager().getApplicationIcon("zumma.com.ninegistapp"));
                getActionBar().setSubtitle(null);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "Package Name Not Found");
            }
        }
        // getActionBar().setLogo(R.drawable.icon);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPostCreate(android.os.Bundle)
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /* (non-Javadoc)
     * @see com.newsfeeder.custom.CustomActivity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        this.menu = menu;

//        if (drawerLayout.isDrawerOpen(drawerLeft)
//                || drawerLayout.isDrawerOpen(drawerRight))
//            menu.findItem(R.id.menu_chat).setVisible(false);
//        else if (drawerLayout.isDrawerOpen(drawerRight))
//            menu.findItem(R.id.menu_edit).setVisible(true);
        if(isChat){
            menu.findItem(R.id.menu_search).setVisible(false);
            menu.findItem(R.id.menu_edit).setVisible(true);
        }
        if(isSetting){
            menu.findItem(R.id.menu_search).setVisible(false);
        }

        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // If the nav drawer is open, hide action items related to the content
        // view
        // boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        // menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /* (non-Javadoc)
     * @see com.newsfeeder.custom.CustomActivity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home
                && drawerLayout.isDrawerOpen(drawerRight))
        {
            drawerLayout.closeDrawer(drawerRight);
            return true;
        }
        if (drawerToggle.onOptionsItemSelected(item))
        {
            drawerLayout.closeDrawer(drawerRight);
            return true;
        }

        if (item.getItemId() == R.id.menu_edit)
        {
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1)
            {
                getSupportFragmentManager().popBackStackImmediate();
            }
            else
                finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isNetwork = StaticMethods.haveNetworkConnection(this);
        if (isNetwork == true){
            Log.d(TAG, "network home available=" + isNetwork);
//
//            boolean search_flag = preferences.getBoolean(ParseConstants.NG_FRIENDS, false);
//            if (search_flag == false){
//
//            }

            Intent fIntent = new Intent(this, FriendsSearchService.class);
            startService(fIntent);


        }else{
            Toast.makeText(this, "Cannot connect to the network...", Toast.LENGTH_LONG).show();
        }
    }

    public void initUser(){

        boolean user_created = preferences.getBoolean(ParseConstants.USER_CREATED,false);

        String user_id = ParseUser.getCurrentUser().getObjectId();
        myConnectionsRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(user_id).child("basicInfo").child("connectionStatus");
        lastOnline = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(user_id).child("basicInfo").child("lastOnline");

        if (!user_created){

            Firebase mFirebaseRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(user_id);

            BasicInfo info = new BasicInfo(this,ParseUser.getCurrentUser());

            FriendsSearch friendsSearch = new FriendsSearch();

            Set<Map.Entry<String, String>> contacts = friendsSearch.allUserContacts(this).entrySet();
            HashMap<String,Friend> friendList = new HashMap<String,Friend>();

            Cursor cursor = getContentResolver().query(FriendTable.CONTENT_URI, null, null, null, null);

            int indexID = cursor.getColumnIndex(FriendTable.COLUMN_ID);
            int indexName = cursor.getColumnIndex(FriendTable.COLUMN_USERNAME);
            int indexNumber = cursor.getColumnIndex(FriendTable.COLUMN_PHONE_NUMBER);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                do {
                    List<Conversation> arrayList = new ArrayList<Conversation>();

                    String id = cursor.getString(indexID);
                    String name = cursor.getString(indexName);
                    String number = cursor.getString(indexNumber);

                    Friend friend = new Friend(name,number,arrayList);
                    friendList.put(id,friend);

                } while (cursor.moveToNext());

            }
            cursor.close();

            User user = new User(info, friendList);

            mFirebaseRef.setValue(user, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        Log.d(TAG,"Data could not be saved. " + firebaseError.getMessage());
                    } else {
                        Log.d(TAG, "Data saved successfully.");
                    }
                }
            });

            preferences.edit().putBoolean(ParseConstants.USER_CREATED, true).apply();
        }
    }

    @Override
    public void onSet(String status) {
        if(!drawerLayout.isDrawerOpen(drawerLeft)) {
            getActionBar().setSubtitle(status);
        }
    }

    @Override
    public void check() {
        adapter.setSelection(0);
    }

//    @Override
//    public void viewProfile(String friend_id) {
//        Intent intent = new Intent(this, ViewProfile.class);
//        intent.putExtra("EXTRA_SESSION_ID", friend_id);
//        startActivity(intent);
//    }
}