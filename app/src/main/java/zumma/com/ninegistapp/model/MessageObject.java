package zumma.com.ninegistapp.model;

import zumma.com.ninegistapp.ui.helpers.GDate;


/**
 * Created by Okafor on 11/03/2015.
 */


public abstract class MessageObject {

    private String fromId;
    private String toId;
    private String date;
    private boolean isSent;
    private boolean is_private;
    private int message_type;
    private int report;
    private long created_at;
    private String uniqkey;

    public MessageObject() {

    }

    public MessageObject(String fromId, String toId, String date, boolean isSent, int message_type) {
        this.fromId = fromId;
        this.toId = toId;
        this.date = date;
        this.isSent = isSent;
        this.message_type = message_type;
        GDate gDate = new GDate();
        created_at = gDate.getTimeStamp();
        uniqkey = null;
        is_private = false;
    }


    public MessageObject(String fromId, String toId, String date, boolean isSent, boolean is_private, int message_type, int report, long created_at, String uniqkey) {
        this.fromId = fromId;
        this.toId = toId;
        this.date = date;
        this.isSent = isSent;
        this.is_private = is_private;
        this.message_type = message_type;
        this.report = report;
        this.created_at = created_at;
        this.uniqkey = uniqkey;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean isSent) {
        this.isSent = isSent;
    }

    public boolean isIs_private() {
        return is_private;
    }

    public void setIs_private(boolean is_private) {
        this.is_private = is_private;
    }

    public int getMessage_type() {
        return message_type;
    }

    public void setMessage_type(int message_type) {
        this.message_type = message_type;
    }

    public int getReport() {
        return report;
    }

    public void setReport(int report) {
        this.report = report;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public String getUniqkey() {
        return uniqkey;
    }

    public void setUniqkey(String uniqkey) {
        this.uniqkey = uniqkey;
    }

    @Override
    public String toString() {
        return "MessageObject{" +
                "fromId='" + fromId + '\'' +
                ", toId='" + toId + '\'' +
                ", date='" + date + '\'' +
                ", isSent=" + isSent +
                ", is_private=" + is_private +
                ", message_type=" + message_type +
                ", report=" + report +
                ", created_at=" + created_at +
                ", uniqkey='" + uniqkey + '\'' +
                '}';
    }
}
