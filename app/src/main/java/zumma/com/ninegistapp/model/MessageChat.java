package zumma.com.ninegistapp.model;

/**
 * Created by Okafor on 11/03/2015.
 */
public class MessageChat extends MessageObject {

    protected String message;

    public MessageChat(){
        super();
    }

    public MessageChat(String fromId, String toId, String date, boolean isSent, int message_type, String message) {
        super(fromId, toId, date, isSent, message_type);
        this.message = message;
    }

    public MessageChat(String fromId, String toId, String date, boolean isSent, boolean is_private, int message_type, int report, long created_at, String uniqkey, String message) {
        super(fromId, toId, date, isSent, is_private, message_type, report, created_at, uniqkey);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageChat{" +
                "message='" + message + '\'' +
                "} " + super.toString();
    }
}
