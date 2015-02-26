package zumma.com.ninegistapp.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.custom.CustomFragment;
import zumma.com.ninegistapp.ui.adapters.SettingsListAdapter;


public class Settings extends CustomFragment {
    private View current;

    private ListView listview;
    private String[] list = {"Help", "Name", "Profile"};
    protected AdapterView.OnItemClickListener mOnItemClickedListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String value = list[position];
            openSelectPicture();
//            if (value.equals("Profile")){
//
//            }
        }
    };
    private Integer[] img = {R.drawable.menu_help, R.drawable.ic_edit, R.drawable.ic_attach_gallery};

    private void openSelectPicture() {
        Intent intent = new Intent(getActivity(), zumma.com.ninegistapp.ui.activities.SelectPicture.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        View view = paramLayoutInflater.inflate(R.layout.settings_list, null);

        SettingsListAdapter adapt = new SettingsListAdapter(getActivity(), list, img);

        listview = (ListView) view.findViewById(R.id.listView);
        listview.setAdapter(adapt);
        listview.setOnItemClickListener(mOnItemClickedListener);

        return view;
    }
}