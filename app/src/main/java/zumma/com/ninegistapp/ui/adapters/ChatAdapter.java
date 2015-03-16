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
                ImageView status = (ImageView) lView.findViewById(R.id.message_status);

                chat_message = (TextView) lView.findViewById(R.id.msg_text);
                chat_message.setText(messageChat.getMessage());

                int report = messageChat.getReport();
                Log.d(TAG, "report is " + report);
                switch (report) {
                    case 0:
                        status.setImageResource(R.drawable.ic_dot1);
                        break;
                    case 1:
                        status.setImageResource(R.drawable.ic_dot2);
                        break;
                    case 2:
                        status.setImageResource(R.drawable.ic_dot3);
                        break;
                }

            } else {
                lView = fInflater.inflate(R.layout.chat_recieved, parent, false);

                chat_message = (TextView) lView.findViewById(R.id.msg_text);
                chat_message.setText(messageChat.getMessage());
            }
        }

        return lView;
    }


}
