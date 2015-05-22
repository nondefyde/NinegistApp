package zumma.com.ninegistapp.ui.fragments;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.ParseUser;
import com.rockerhieu.emojicon.EmojiKeyboard;
import com.rockerhieu.emojicon.EmojiconEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.custom.CustomFragment;
import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.database.table.MessageTable;
import zumma.com.ninegistapp.model.MessageChat;
import zumma.com.ninegistapp.model.MessageObject;
import zumma.com.ninegistapp.ui.Components.LayoutListView;
import zumma.com.ninegistapp.ui.activities.ViewProfile;
import zumma.com.ninegistapp.ui.adapters.ChatAdapter;
import zumma.com.ninegistapp.ui.helpers.ChatHelper;
import zumma.com.ninegistapp.ui.helpers.GDate;
import zumma.com.ninegistapp.utils.FileHelper;

public class ChatFragment extends CustomFragment implements View.OnClickListener {


    private static final String TAG = ChatFragment.class.getSimpleName();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri cameraUri;

    private ChatAdapter adapter;
    private EmojiconEditText chat_text_edit;
    private ImageView chat_smile_button;
    private ImageView chat_image_button;
    private String user_id;
    private String friend_id;
    private UserChileEventListener userChileEventListener;



    private boolean keyboardListenersAttached = false;
    private ViewGroup rootLayout;

    private SharedPreferences preferences;

    private Firebase user_baseRef;
    private Firebase friend_baseRef;
    private Firebase connectionStatus;
    private Firebase lastSeen;
    private boolean onlineStatus;
    private boolean firebase_status = false;
    private String status;
    final Firebase connectedRef = new Firebase(ParseConstants.FIREBASE_URL + "/.info/connected");
    private SetSubtitle mConnect;

    private ChatHelper chatHelper;

    public static final String FRIEND_ID_PROFILE = "friends_id_profile";

    ArrayList<MessageObject> chatList = new ArrayList<MessageObject>();
    EmojiKeyboard emojiKeyboard;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        if(firebase_status){
            mConnect.onSet(status);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_edit) {
            Intent intent = new Intent(getActivity(), ViewProfile.class);
            intent.putExtra(FRIEND_ID_PROFILE, friend_id);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.menu_chat) {
            openImageIntent();
        }
        return false;
    }

    public void initChat() {

        chatHelper = new ChatHelper(getActivity());
        mConnect = (SetSubtitle) getActivity();

        user_id = ParseUser.getCurrentUser().getObjectId();
        friend_id = getArguments().getString(ParseConstants.KEY_USER_ID);

        user_baseRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(user_id).child("roasters").child("Chat").child(friend_id);
        friend_baseRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(friend_id).child("roasters").child("Chat").child(user_id);
        connectionStatus = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(friend_id).child("basicInfo").child("connectionStatus");
        lastSeen = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist").child(friend_id).child("basicInfo").child("lastOnline");


        connectionStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if((boolean)dataSnapshot.getValue()){
                    onlineStatus = true;
                    firebase_status = true;
                    status = "Online";
                    mConnect.onSet(status);
                }
                else{
                    onlineStatus = false;
                    firebase_status = true;
                    lastSeen.addValueEventListener(new ValueEventListener() {
                        @TargetApi(Build.VERSION_CODES.CUPCAKE)
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String last_online = dataSnapshot.getValue().toString();
                            Long updateAt = Long.parseLong(last_online);
                            long now = new Date().getTime();
                            String convertedDate = DateUtils.getRelativeTimeSpanString(
                                    updateAt,
                                    now + 1000,
                                    DateUtils.SECOND_IN_MILLIS
                            ).toString();
                            status = "Last online: "+convertedDate;
                            mConnect.onSet(status);
                        }

                        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            getActivity().getActionBar().setSubtitle(null);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        userChileEventListener = new UserChileEventListener();
//        friendChileEventListener = new FriendChileEventListener();

        user_baseRef.addChildEventListener(userChileEventListener);
//        friend_baseRef.addChildEventListener(friendChileEventListener);

        chatHelper.initChatList(friend_id, chatList);
        adapter = new ChatAdapter(getActivity(), chatList);
    }

    private void sendMessage() {

        if (chat_text_edit.length() == 0)
            return;

        GDate date = new GDate();

        String str = chat_text_edit.getText().toString();
        String time = date.getCurrent_time();

        final MessageChat chatObject = new MessageChat(user_id, friend_id, time, true, 1, str);
        chatHelper.InsertChatMessage(friend_id, chatObject);
        chatHelper.upDateFriendStatusChat(getActivity(), chatObject, 1, 2);

        chatList.add(chatObject);
        adapter.notifyDataSetChanged();
        Log.d(TAG, chatObject.toString());
        chat_text_edit.setText(null);


        friend_baseRef.push().setValue(chatObject, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                HashMap<String, Object> update = new HashMap<String, Object>();
                update.put("uniqkey", firebase.getKey());
                update.put("report", 1);
                Firebase firebase1 = friend_baseRef;
                firebase1.child(firebase.getKey()).updateChildren(update);

                chatHelper.upDateDeliveredConversation(chatObject, chatList);
                chatHelper.upDateFriendStatusChat(getActivity(), chatObject, 1, 3);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private class UserChileEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, " na wa onChildAdded " + dataSnapshot.getValue() + "   " + s);
            if (dataSnapshot != null) {
                HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                MessageChat messageChat = dataSnapshot.getValue(MessageChat.class);
                if (messageChat.getFromId().equals(friend_id) && messageChat.getReport() == 2) {
                    chatHelper.upDisplayedConversation(messageChat, chatList);
                    chatHelper.upDateFriendStatusChat(getActivity(), messageChat, 1, 0);
                } else if(!messageChat.getFromId().equals(friend_id)){
                    chatHelper.upDisplayedConversation(messageChat, chatList);
                    chatHelper.upDateFriendStatusChat(getActivity(), messageChat, 1, 4);
                }

                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, " onChildChanged " + dataSnapshot.getValue() + "   " + s);

            if (dataSnapshot != null) {
                HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                MessageChat messageChat = dataSnapshot.getValue(MessageChat.class);
                messageChat.setSent(false);
                if (messageChat.getFromId().equals(friend_id)) {
                    chatHelper.InsertChatMessage(friend_id, messageChat);
                    Log.d(TAG, " recieved chat  ");
                    chatHelper.upDateFriendStatusChat(getActivity(), messageChat, 1, 0);
                    chatList.add(messageChat);
                    adapter.notifyDataSetChanged();


                    messageChat.setReport(2);

                    Log.d(TAG, " FriendChileEventListener onChildChanged has been delivered and read ");

                    Firebase firebase1 = new Firebase(ParseConstants.FIREBASE_URL)
                            .child("9Gist")
                            .child(messageChat.getFromId())
                            .child("roasters")
                            .child("Chat")
                            .child(messageChat.getToId())
                            .child(messageChat.getUniqkey());

                    firebase1.setValue(messageChat);
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

    @Override
    public void onPause() {
        emojiKeyboard.dismissEmojiKeyboard();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        user_baseRef.removeEventListener(userChileEventListener);

        if (keyboardListenersAttached) {
            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(keyboardLayoutListener);
        }
    }

    public void onClick(View view) {
        super.onClick(view);

        if (view.getId() == R.id.chat_send_button)
            sendMessage();
        else if(view.getId() == R.id.chat_smile_button){
            emojiKeyboard.showEmoji();
        }
        else if(view.getId() == R.id.chat_image_button){

        }
    }

    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        View view = paramLayoutInflater.inflate(R.layout.chat_layout, null);
        initChat();

        LayoutListView localListView = (LayoutListView) view.findViewById(R.id.chat_list_view);

        localListView.setAdapter(this.adapter);
        localListView.setTranscriptMode(2);
        localListView.setStackFromBottom(true);

        chat_text_edit = ((EmojiconEditText) view.findViewById(R.id.chat_text_edit));
        chat_text_edit.setInputType(131073);

        chat_smile_button = (ImageView) view.findViewById(R.id.chat_smile_button);
        chat_image_button = (ImageView) view.findViewById(R.id.chat_image_button);

        setTouchNClick(view.findViewById(R.id.chat_send_button));

        setHasOptionsMenu(true);

        super.onResume();

        sendMessageRead();

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit().putInt(friend_id,1).commit();


        attachKeyboardListeners(view);

        return view;
    }


    public void updateFriendMessageCount() {

        String SEL = FriendTable.COLUMN_ID + "=?";
        String[] arg = {friend_id};

        ContentValues values = new ContentValues();
        values.put(FriendTable.COLUMN_MSG_COUNT, 0);
        values.put(FriendTable.COLUMN_STATUS_ICON, 0);

        int update = getActivity().getContentResolver().update(FriendTable.CONTENT_URI, values, SEL, arg);
        if (update > 0) {
            Log.d(TAG, "message count updated ");
        }
    }

    public void sendMessageRead() {

        String SELECTION = MessageTable.COLUMN_FROM + "=? AND " + MessageTable.COLUMN_REPORT + "=1";
        String[] args = {friend_id};

        Log.d(TAG, " sendMessageRead "+friend_id);
        Cursor cursor = getActivity().getContentResolver().query(MessageTable.CONTENT_URI, null, SELECTION, args, null);
        if (cursor != null && cursor.getCount() > 0) {

            int indexID = cursor.getColumnIndex(MessageTable.COLUMN_ID);
            int indexFrom = cursor.getColumnIndex(MessageTable.COLUMN_FROM);
            int indexTo = cursor.getColumnIndex(MessageTable.COLUMN_TO);
            int indexMsg = cursor.getColumnIndex(MessageTable.COLUMN_MESSAGE);
            int indexSent = cursor.getColumnIndex(MessageTable.COLUMN_SENT);
            int indexTime = cursor.getColumnIndex(MessageTable.COLUMN_TIME);
            int indexPrivate = cursor.getColumnIndex(MessageTable.COLUMN_PRIVATE);
            int indexType = cursor.getColumnIndex(MessageTable.COLUMN_MESSAGE_TYPE);
            int indexReport = cursor.getColumnIndex(MessageTable.COLUMN_REPORT);
            int indexCreatedAt = cursor.getColumnIndex(MessageTable.COLUMN_CREATED_AT);
            int indexUnique = cursor.getColumnIndex(MessageTable.COLUMN_UNIQ_ID);

            Log.d(TAG, " sendMessageRead 2"+friend_id);
            cursor.moveToFirst();
            do {

                final String id = cursor.getString(indexID);
                String fromId = cursor.getString(indexFrom);
                String toId = cursor.getString(indexTo);
                String msg = cursor.getString(indexMsg);
                boolean sent = cursor.getInt(indexSent) == 1 ? true : false;
                String date = cursor.getString(indexTime);
                int type = cursor.getInt(indexType);
                boolean flag = cursor.getInt(indexPrivate) == 1 ? true : false;
                int report = 2;
                long created = cursor.getLong(indexCreatedAt);
                final String uniq = cursor.getString(indexUnique);

                final MessageChat messageChat = new MessageChat(fromId, toId, date, sent, flag, type, report, created, uniq, msg);

                Log.d(TAG, " unig of unreported = " + uniq);

                Firebase firebase1 = new Firebase(ParseConstants.FIREBASE_URL)
                        .child("9Gist")
                        .child(friend_id)
                        .child("roasters")
                        .child("Chat")
                        .child(user_id)
                        .child(uniq);


                firebase1.setValue(messageChat, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        ContentValues values = new ContentValues();
                        values.put(MessageTable.COLUMN_REPORT, 2);
                        Log.d(TAG, " sendMessageRead = " + values);

                        String SEL = MessageTable.COLUMN_ID + "=?";
                        String[] arg = {id};
                        int update = getActivity().getContentResolver().update(MessageTable.CONTENT_URI, values, SEL, arg);
                        if (update > 0) {
                            Log.d(TAG, "unig #value = " + uniq);
                            chatHelper.upDateFriendStatusChat(getActivity(), messageChat, 1, 0);
                        }
                    }
                });
            } while (cursor.moveToNext());
            cursor.close();
        }else{
            cursor.close();
        }

    }

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int heightDiff = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
            int contentViewTop = getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();

            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());

            if(heightDiff <= contentViewTop){
                onHideKeyboard();

                Intent intent = new Intent("KeyboardWillHide");
                broadcastManager.sendBroadcast(intent);
            } else {
                int keyboardHeight = heightDiff - contentViewTop;
                onShowKeyboard(keyboardHeight);

                Intent intent = new Intent("KeyboardWillShow");
                intent.putExtra("KeyboardHeight", keyboardHeight);
                broadcastManager.sendBroadcast(intent);
            }
        }
    };

    protected void onShowKeyboard(int keyboardHeight) {
        chat_smile_button.setVisibility(View.VISIBLE);
        chat_image_button.setVisibility(View.INVISIBLE);
        chat_smile_button.setOnClickListener(this);
    }
    protected void onHideKeyboard() {
        chat_smile_button.setVisibility(View.INVISIBLE);
        chat_image_button.setVisibility(View.VISIBLE);
        chat_smile_button.setOnClickListener(null);
    }

    protected void attachKeyboardListeners(View view) {
        emojiKeyboard = new EmojiKeyboard(getActivity(), view);
        if (keyboardListenersAttached) {
            return;
        }

        rootLayout = (ViewGroup) view.findViewById(R.id.chat_layout);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

        keyboardListenersAttached = true;
    }

    public interface SetSubtitle{
        void onSet(String status);
    }

    private void openImageIntent() {

        File imageFile = null;
        try {
            imageFile = createImageFile();
        } catch (IOException e) {
            return;
        }
        cameraUri = Uri.fromFile(imageFile);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getActivity().getPackageManager();
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
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                boolean isCamera;
                if (resultCode == getActivity().RESULT_OK) {
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
                        byte[] ourByte = FileHelper.getByteArrayFromFile(getActivity(), cameraUri);
                        String baseString = Base64.encodeToString(ourByte, Base64.DEFAULT);
                        Toast.makeText(getActivity(), baseString, Toast.LENGTH_LONG).show();
                    } else {
                        byte[] ourByte = FileHelper.getByteArrayFromFile(getActivity(), data.getData());
                        String baseString = Base64.encodeToString(ourByte, Base64.DEFAULT);
                        Toast.makeText(getActivity(), baseString, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

}
