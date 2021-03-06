package zumma.com.ninegistapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.database.table.MessageTable;


/**
 * Created by Okafor on 31/10/2014.
 */
public class ChatSqlLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "qr.db";
    private static final int DATABASE_VERSION = 1;

    public ChatSqlLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FriendTable.CREATE_TABLE);
        db.execSQL(MessageTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}