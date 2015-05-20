package zumma.com.ninegistapp.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.model.Data;

public class RightNavAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Data> items;

    public RightNavAdapter(Context paramContext, ArrayList<Data> paramArrayList) {
        this.context = paramContext;
        this.items = paramArrayList;
    }

    public int getCount() {
        return this.items.size();
    }

    public Data getItem(int paramInt) {
        return (Data) this.items.get(paramInt);
    }

    public long getItemId(int paramInt) {
        return paramInt;
    }

    public View getView(int paramInt, View view, ViewGroup paramViewGroup) {
        if (view == null)
            view = LayoutInflater.from(this.context).inflate(R.layout.right_nav_item, null);
        Data localData = getItem(paramInt);

        TextView name = (TextView) view.findViewById(R.id.lbl1);
        ImageView picture = (ImageView) view.findViewById(R.id.img);
        TextView msg_count = (TextView) view.findViewById(R.id.lbl2);

        if(localData.getMes_count() > 0){
            msg_count.setVisibility(View.VISIBLE);
            msg_count.setText(localData.getMes_count());
        }

        Bitmap byteImage = BitmapFactory.decodeByteArray(localData.getImage(), 0, localData.getImage().length);
        picture.setImageBitmap(byteImage);

        name.setText(localData.getTitle());

        return view;
    }
}
