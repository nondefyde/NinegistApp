package zumma.com.ninegistapp.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.custom.CustomFragment;
import zumma.com.ninegistapp.ui.activities.AboutActivity;
import zumma.com.ninegistapp.ui.activities.SelectPicture;

public class SettingFragment extends CustomFragment {

    private static final String TAG = SettingFragment.class.getSimpleName();

    public static final String SETTING_PREFERENCES = "zumma.com.ninegistapp.ui.fragments";
    public static final String SETTINGS_PRIVATE = "private_chat";
    public static final String SETTINGS_GALLERY = "save_gallery";
    public static final String SETTINGS_COLOR = "color_theme";

    private SharedPreferences preferences;

    private Switch switchChat;
    private Switch saveGallery;
    private Button red;
    private Button white;
    private Button grey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.settings_ninegist, null);

        preferences = getActivity().getSharedPreferences(SETTING_PREFERENCES, 0);

        ListView list = (ListView) view.findViewById(R.id.list);
        switchChat = (Switch) view.findViewById(R.id.switch1_settings);
        saveGallery = (Switch) view.findViewById(R.id.switch2_settings);
        red = (Button) view.findViewById(R.id.themeRed_settings);
        white = (Button) view.findViewById(R.id.themeWhite_settings);
        grey = (Button) view.findViewById(R.id.themeGray_settings);

        ButtonHandler buttonHandler = new ButtonHandler();
        SwitchHandler switchHandler = new SwitchHandler();

        switchChat.setOnCheckedChangeListener(switchHandler);
        saveGallery.setOnCheckedChangeListener(switchHandler);

        red.setOnClickListener(buttonHandler);
        white.setOnClickListener(buttonHandler);
        grey.setOnClickListener(buttonHandler);

        setUpSwitch();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        startActivity(new Intent(getActivity(), SelectPicture.class));
                    break;
                    case 1:
                        startActivity(new Intent(getActivity(), AboutActivity.class));
                    break;
                    case 3:
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Restore Default Settings")
                                .setMessage("Are you sure you want to delete your personal settings?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putInt(SETTINGS_COLOR, -1).apply();
                                        editor.putBoolean(SETTINGS_PRIVATE, false).apply();
                                        editor.putBoolean(SETTINGS_GALLERY, false).apply();
                                        setUpSwitch();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    break;
                }
            }
        });
        return view;
    }

    private void setUpSwitch(){
        switchChat.setChecked(preferences.getBoolean(SETTINGS_PRIVATE, false));
        Log.d(TAG, "Private chat is: "+ preferences.getBoolean(SETTINGS_PRIVATE, false));
        saveGallery.setChecked(preferences.getBoolean(SETTINGS_GALLERY, false));
        Log.d(TAG, "Save to Gallery is: "+ preferences.getBoolean(SETTINGS_GALLERY, false));

        switch(preferences.getInt(SETTINGS_COLOR, -1)) {
            case 1:
                red.setSelected(true);
                Log.d(TAG, "Red: "+ preferences.getInt(SETTINGS_COLOR, -1));
            break;
            case 2:
                white.setSelected(true);
                Log.d(TAG, "White: "+ preferences.getInt(SETTINGS_COLOR, -1));
                break;
            case 3:
                grey.setSelected(true);
                Log.d(TAG, "Gray: "+ preferences.getInt(SETTINGS_COLOR, -1));
            break;
            default:
                red.setSelected(false);
                white.setSelected(false);
                grey.setSelected(false);
                Log.d(TAG, "Default: "+ preferences.getInt(SETTINGS_COLOR, -1));
            break;
        }
    }

    private class ButtonHandler implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SharedPreferences.Editor editor = preferences.edit();
            switch (v.getId()) {
                case R.id.themeRed_settings:
                    editor.putInt(SETTINGS_COLOR, 1).apply();
                    red.setSelected(true);
                    white.setSelected(false);
                    grey.setSelected(false);
                    Log.d(TAG, "Color set to red = 1");
                break;
                case R.id.themeWhite_settings:
                    editor.putInt(SETTINGS_COLOR, 2).apply();
                    red.setSelected(false);
                    white.setSelected(true);
                    grey.setSelected(false);
                    Log.d(TAG, "Color set to white = 2");
                break;
                case R.id.themeGray_settings:
                    editor.putInt(SETTINGS_COLOR, 3).apply();
                    red.setSelected(false);
                    white.setSelected(false);
                    grey.setSelected(true);
                    Log.d(TAG, "Color set to gray = 3");
                break;
            }
        }
    }

    private class SwitchHandler implements CompoundButton.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences.Editor editor = preferences.edit();
            switch (buttonView.getId()){
                case R.id.switch1_settings:
                    editor.putBoolean(SETTINGS_PRIVATE, isChecked).apply();
                    Log.d(TAG, "Chat is set to private: "+ isChecked);
                break;
                case R.id.switch2_settings:
                    editor.putBoolean(SETTINGS_GALLERY, isChecked).apply();
                    Log.d(TAG, "Save to gallery is set: "+ isChecked);
                break;
            }
        }
    }

}
