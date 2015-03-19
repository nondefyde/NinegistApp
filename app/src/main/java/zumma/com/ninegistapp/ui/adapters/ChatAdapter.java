package zumma.com.ninegistapp.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.ArrayList;

import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.model.MessageChat;
import zumma.com.ninegistapp.model.MessageObject;

/**
 * Created by Okafor on 09/01/2015.
 */
public class ChatAdapter extends BaseAdapter {

    private static final String TAG = ChatAdapter.class.getSimpleName();
    private Context context;
    private LayoutInflater fInflater;
    private String user_id;

    private ArrayList<MessageObject> messages;

    public ChatAdapter(Context aContext, ArrayList<MessageObject> messages) {
        context = aContext;
        this.messages = messages;
        fInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        user_id = ParseUser.getCurrentUser().getObjectId();
    }

    public int getCount() {
        return messages.size();
    }

    public MessageObject getItem(int i) {
        return messages.get(i);
    }

    public long getItemId(int i) {
        return (long) messages.get(i).getCreated_at();
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View lView = convertView;

        final MessageObject messageObject = messages.get(position);

        if (messageObject instanceof MessageChat){
            MessageChat messageChat = (MessageChat) messageObject;
            TextView chat_message;
            if (messageObject.getFromId().equals(user_id)) {

                lView = fInflater.inflate(R.layout.chat_sent, parent, false);
                ImageView status = (ImageView) lView.findViewById(R.id.user_reply_status);

                chat_message = (TextView) lView.findViewById(R.id.chat_user_reply);
                chat_message.setText(messageChat.getMessage());

                TextView time = (TextView) lView.findViewById(R.id.user_reply_timing);

                ImageView imageView1 = (ImageView) lView.findViewById(R.id.user_reply_sent);
                ImageView imageView2 = (ImageView) lView.findViewById(R.id.user_reply_status);
                ImageView imageView3 = (ImageView) lView.findViewById(R.id.user_reply_status2);

                int report = messageChat.getReport();
                Log.d(TAG, "report is " + report);
                switch (report) {
                    case 0:
                        imageView1.setVisibility(View.VISIBLE);
                        imageView2.setVisibility(View.INVISIBLE);
                        imageView3.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        imageView1.setVisibility(View.INVISIBLE);
                        imageView2.setVisibility(View.VISIBLE);
                        imageView3.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        imageView1.setVisibility(View.INVISIBLE);
                        imageView2.setVisibility(View.VISIBLE);
                        imageView3.setVisibility(View.VISIBLE);
                        break;
                    default:
                        imageView1.setVisibility(View.INVISIBLE);
                        imageView2.setVisibility(View.INVISIBLE);
                        imageView3.setVisibility(View.INVISIBLE);
                        break;

                }

                time.setText(messageChat.getDate());

            } else {

                lView = fInflater.inflate(R.layout.chat_recieved, parent, false);
                TextView time = (TextView) lView.findViewById(R.id.user_reply_timing);

                chat_message = (TextView) lView.findViewById(R.id.chat_user_reply);
                chat_message.setText(messageChat.getMessage());

                time.setText(messageChat.getDate());
            }
        }

        return lView;
    }


}
