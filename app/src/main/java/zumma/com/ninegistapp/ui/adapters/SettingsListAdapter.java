package zumma.com.ninegistapp.ui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import zumma.com.ninegistapp.R;


/**
 * Created by Okafor on 07/07/2014.
 */
public class SettingsListAdapter extends ArrayAdapter<String> {

    Activity context;
    String[] list;
    Integer[] img;

    public SettingsListAdapter(Activity context, String[] list, Integer[] img) {
        super(context, R.layout.settings_list_item, list);
        this.context = context;
        this.list = list;
        this.img = img;
    }

    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View row = inflater.inflate(R.layout.settings_list_item, null, true);
        TextView tv = (TextView) row.findViewById(R.id.txt);
        ImageView im = (ImageView) row.findViewById(R.id.img);
        tv.setText(list[position]);
        im.setImageResource(img[position]);
        return row;
    }
}
