package zumma.com.ninegistapp.ui.fragments;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.parse.ParseUser;

import java.util.Date;
import java.util.Locale;

import zumma.com.ninegistapp.MainActivity;
import zumma.com.ninegistapp.ParseConstants;
import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.TextDrawable;
import zumma.com.ninegistapp.custom.CustomFragment;
import zumma.com.ninegistapp.database.table.FriendTable;
import zumma.com.ninegistapp.ui.helpers.FriendsUtilHelper;
import zumma.com.ninegistapp.utils.ColorGenerator;
import zumma.com.ninegistapp.utils.DrawableProvider;

public class FriendsFragment extends CustomFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = FriendsFragment.class.getSimpleName();
    // Bundle key for saving previously selected search result item
    private static final String STATE_PREVIOUSLY_SELECTED_KEY =
            "com.zumacomm.ngapp.ui.SELECTED_ITEM";

    private static final int HIGHLIGHT_COLOR = 0x999be6ff;

    private AdapterView.OnItemClickListener mOnItemClickedListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            view.setSelected(true);
            final Cursor cursor = mAdapter.getCursor();

            // Moves to the Cursor row corresponding to the ListView item that was clicked
            cursor.moveToPosition(position);

            final String objectId = cursor.getString(FriendQuery.COLUMN_ID);
            final String username = cursor.getString(FriendQuery.COLUMN_USERNAME);
            final String phone_number = cursor.getString(FriendQuery.COLUMN_PHONE_NUMBER);

            MainActivity mainActivity = (MainActivity) getActivity();

            Bundle bundle = new Bundle();
            bundle.putString(ParseConstants.KEY_USER_ID, objectId);
            bundle.putString(ParseConstants.ACTION_BAR_TITLE, /** "Chat with " + */username);
            mainActivity.launchFragment(1, bundle);
        }
    };
    private ProgressBar progressBar;
    private ParseUser mCurrentUser;
    private ContactsAdapter mAdapter; // The main query adapter
    private int mPreviouslySelectedSearchItem = 0;
    private SharedPreferences preferences;
    private SearchView mSearchView;
    private String mCurFilter;
    private MenuItem sendMenu;
    private ListView listView;
    private String userId;

    // declare the color generator and drawable builder
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        listView = (ListView) rootView.findViewById(R.id.list);
        TextView empty = (TextView) rootView.findViewById(R.id.empty);
        listView.setEmptyView(empty);

        Intent intent = getActivity().getIntent();
        int type = intent.getIntExtra("TYPE", DrawableProvider.SAMPLE_RECT);

        // initialize the builder based on the "TYPE"
        switch (type) {
            case DrawableProvider.SAMPLE_RECT:
                mDrawableBuilder = TextDrawable.builder()
                        .rect();
                break;
            case DrawableProvider.SAMPLE_ROUND_RECT:
                mDrawableBuilder = TextDrawable.builder()
                        .roundRect(10);
                break;
            case DrawableProvider.SAMPLE_ROUND:
                mDrawableBuilder = TextDrawable.builder()
                        .round();
                break;
            case DrawableProvider.SAMPLE_RECT_BORDER:
                mDrawableBuilder = TextDrawable.builder()
                        .beginConfig()
                        .withBorder(4)
                        .endConfig()
                        .rect();
                break;
            case DrawableProvider.SAMPLE_ROUND_RECT_BORDER:
                mDrawableBuilder = TextDrawable.builder()
                        .beginConfig()
                        .withBorder(4)
                        .endConfig()
                        .roundRect(10);
                break;
            case DrawableProvider.SAMPLE_ROUND_BORDER:
                mDrawableBuilder = TextDrawable.builder()
                        .beginConfig()
                        .withBorder(4)
                        .endConfig()
                        .round();
                break;
        }

        listView.setOnItemClickListener(mOnItemClickedListener);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAdapter = new ContactsAdapter(getActivity());

        if (savedInstanceState != null) {
            // If we're restoring state after this fragment was recreated then
            // retrieve previous search term and previously selected search
            // result.
            mCurFilter = savedInstanceState.getString(SearchManager.QUERY);
            mPreviouslySelectedSearchItem =
                    savedInstanceState.getInt(STATE_PREVIOUSLY_SELECTED_KEY, 0);
        }

        userId = ParseUser.getCurrentUser().getObjectId();


        Firebase mFirebaseRef = new Firebase(ParseConstants.FIREBASE_URL).child("9Gist/Users").child(userId);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set up ListView, assign adapter and set some listeners. The adapter was previously
        // created in onCreate().

        setHasOptionsMenu(true);

        listView.setAdapter(mAdapter);

        // If there's a previously selected search item from a saved state then don't bother
        // initializing the loader as it will be restarted later when the query is populated into
        // the action bar search view (see onQueryTextChange() in onCreateOptionsMenu()).
        if (mPreviouslySelectedSearchItem == 0) {
            getLoaderManager().initLoader(FriendQuery.QUERY_ID, null, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");


    }


    @Override
    public boolean onClose() {
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
        // Don't do anything if the filter hasn't actually changed.
        // Prevents restarting the loader when restoring state.
        if (mCurFilter == null && newFilter == null) {
            return true;
        }
        if (mCurFilter != null && mCurFilter.equals(newFilter)) {
            return true;
        }
        mCurFilter = newFilter;
        getLoaderManager().restartLoader(FriendQuery.QUERY_ID, null, this);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.

        MenuItem item = menu.findItem(R.id.menu_search);

        mSearchView = new MySearchView(getActivity());
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setQueryHint("Type something...");
        int searchPlateId = mSearchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = mSearchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText != null) {
                searchText.setTextColor(Color.WHITE);
                searchText.setHintTextColor(Color.GREEN);
            }
        }

        item.setActionView(mSearchView);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // If this is the loader for finding contacts in the Contacts Provider
        // (the only one supported)
        if (id == FriendQuery.QUERY_ID) {
            Uri contentUri;

            if (mCurFilter == null) {
                // Since there's no search string, use the content URI that searches the entire
                // Contacts tableg
                contentUri = FriendQuery.CONTENT_URI;
            } else {
                // Since there's a search string, use the special content Uri that searches the
                // Contacts table. The URI consists of a base Uri and the search string.
                contentUri =
                        Uri.withAppendedPath(FriendQuery.CONTENT_URI, Uri.encode(mCurFilter));
            }
            Log.d(TAG, " content uri " + contentUri.toString() + "   fileter " + mCurFilter);

            mCurrentUser = ParseUser.getCurrentUser();
            String userId = mCurrentUser.getObjectId();
            String[] values = {userId};

            return new CursorLoader(getActivity(),
                    contentUri,
                    FriendQuery.PROJECTION,
                    FriendQuery.SELECTION,
                    values,
                    null);
        }

        Log.e(TAG, "onCreateLoader - incorrect ID provided (" + id + ")");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == FriendQuery.QUERY_ID) {
            Log.d(TAG, "data = " + data);
            if (data != null) {
                mAdapter.swapCursor(data);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == FriendQuery.QUERY_ID) {
            // When the loader is being reset, clear the cursor from the adapter. This allows the
            // cursor resources to be freed.
            mAdapter.swapCursor(null);
        }
    }

    public interface FriendQuery {
        // An identifier for the loader
        final static int QUERY_ID = ParseConstants.FRIEND_QUERY_ID;
        final static Uri CONTENT_URI = FriendTable.CONTENT_URI;
        final static String SELECTION = FriendTable.COLUMN_USER_ID + "=?";

        final static String[] PROJECTION = {
                FriendTable.COLUMN_ID,
                FriendTable.COLUMN_USERNAME,
                FriendTable.COLUMN_PHONE_NUMBER,
                FriendTable.COLUMN_MSG_COUNT,
                FriendTable.COLUMN_STATUS,
                FriendTable.COLUMN_STATUS_ICON,
                FriendTable.COLUMN_PROFILE_PICTURE,
                FriendTable.COLUMN_LAST_CHAT_TIME,
                FriendTable.COLUMN_UPDATED_AT,
        };

        // The query column numbers which map to each value in the projection
        final static int COLUMN_ID = 0;
        final static int COLUMN_USERNAME = 1;
        final static int COLUMN_PHONE_NUMBER = 2;
        final static int COLUMN_MSG_COUNT = 3;
        final static int COLUMN_STATUS = 4;
        final static int COLUMN_STATUS_ICON = 5;
        final static int COLUMN_PROFILE_PICTURE = 6;
        final static int COLUMN_LAST_CHAT_TIME = 7;
        final static int COLUMN_UPDATED_AT = 8;
    }

    public static class MySearchView extends SearchView {
        public MySearchView(Context context) {
            super(context);
        }

        // The normal SearchView doesn't clear its search text when
        // collapsed, so we will do this for it.
        @Override
        public void onActionViewCollapsed() {
            setQuery("", false);
            super.onActionViewCollapsed();
        }
    }

    private class ContactsAdapter extends CursorAdapter implements SectionIndexer {
        private LayoutInflater mInflater; // Stores the layout inflater
        private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance
        private TextAppearanceSpan highlightTextSpan; // Stores the highlight text appearance style
        private FriendsUtilHelper friendsUtilHelper;

        /**
         * Instantiates a new Contacts Adapter.
         *
         * @param context A context that has access to the app's layout.
         */
        public ContactsAdapter(Context context) {
            super(context, null, 0);

            // Stores inflater for use later
            mInflater = LayoutInflater.from(context);

            // Loads a string containing the English alphabet. To fully localize the app, provide a
            // strings.xml file in res/values-<x> directories, where <x> is a locale. In the file,
            // define a string with android:name="alphabet" and contents set to all of the
            // alphabetic characters in the language in their proper sort order, in upper case if
            // applicable.
            final String alphabet = context.getString(R.string.alphabet);

            // Instantiates a new AlphabetIndexer bound to the column used to sort contact names.
            // The cursor is left null, because it has not yet been retrieved.
            mAlphabetIndexer = new AlphabetIndexer(null, FriendQuery.COLUMN_USERNAME, alphabet);

            // Defines a span for highlighting the part of a display name that matches the search
            // string
            highlightTextSpan = new TextAppearanceSpan(getActivity(), R.style.searchTextHiglight);

            friendsUtilHelper = new FriendsUtilHelper();
        }

        /**
         * Identifies the start of the search string in the display name column of a Cursor row.
         * E.g. If displayName was "Adam" and search query (mSearchTerm) was "da" this would
         * return 1.
         *
         * @param displayName The contact display name.
         * @return The starting position of the search string in the display name, 0-based. The
         * method returns -1 if the string is not found in the display name, or if the search
         * string is empty or null.
         */
        private int indexOfSearchQuery(String displayName) {
            if (!TextUtils.isEmpty(mCurFilter)) {
                return displayName.toLowerCase(Locale.getDefault()).indexOf(
                        mCurFilter.toLowerCase(Locale.getDefault()));
            }
            return -1;
        }


        /**
         * Overrides newView() to inflate the list item views.
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View itemLayout =
                    mInflater.inflate(R.layout.chatuser_list_item, parent, false);

            final ViewHolder viewHolder = new ViewHolder();

            viewHolder.view = itemLayout;
            viewHolder.objectId = (TextView) itemLayout.findViewById(R.id.objectId);
            viewHolder.nameIcon = (ImageView) itemLayout.findViewById(R.id.nameIcon);
            viewHolder.nameLabel = (TextView) itemLayout.findViewById(R.id.nameLabel);
            viewHolder.nameLabel2 = (TextView) itemLayout.findViewById(R.id.nameLabel2);
            viewHolder.timeLabel = (TextView) itemLayout.findViewById(R.id.timeLabel);
            viewHolder.countLabel = (TextView) itemLayout.findViewById(R.id.countField);
            viewHolder.statusLabel = (TextView) itemLayout.findViewById(R.id.message_status);
            viewHolder.statusIcon = (ImageView) itemLayout.findViewById(R.id.statusIcon);
            viewHolder.check_icon = (ImageView) itemLayout.findViewById(R.id.check_icon);

            itemLayout.setTag(viewHolder);


            return itemLayout;
        }


        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder viewHolder = (ViewHolder) view.getTag();

            final String id = cursor.getString(FriendQuery.COLUMN_ID);
            final String username = cursor.getString(FriendQuery.COLUMN_USERNAME);
            final String status = cursor.getString(FriendQuery.COLUMN_STATUS);
            final int status_icon = cursor.getInt(FriendQuery.COLUMN_STATUS_ICON);
            final int msg_count = cursor.getInt(FriendQuery.COLUMN_MSG_COUNT);
            final byte[] profile_pics = cursor.getBlob(FriendQuery.COLUMN_PROFILE_PICTURE);
            final String updated_at = cursor.getString(FriendQuery.COLUMN_UPDATED_AT);

            final int startIndex = indexOfSearchQuery(username);

            String name = friendsUtilHelper.capitaliseFirst(username);

            if (msg_count > 0) {
                viewHolder.countLabel.setVisibility(View.VISIBLE);
                viewHolder.countLabel.setText(msg_count + "");
                viewHolder.statusLabel.setText(status);
            } else {
                viewHolder.countLabel.setVisibility(View.INVISIBLE);
            }

            switch (status_icon) {
                case 0:
                    viewHolder.statusIcon.setImageResource(R.drawable.ic_chat_icon_read);
                    break;
                case 1:
                    viewHolder.statusIcon.setImageResource(R.drawable.ic_chat_icon);
                    break;
                case 2:
                    viewHolder.statusIcon.setImageResource(R.drawable.ic_dot1);
                    break;
                case 3:
                    viewHolder.statusIcon.setImageResource(R.drawable.ic_dot2);
                    break;
                case 4:
                    viewHolder.statusIcon.setImageResource(R.drawable.ic_dot3);
                    break;
                default:
                    viewHolder.statusIcon.setImageResource(R.drawable.ic_dot1);
            }

            Long updateAt = Long.parseLong(updated_at);
            long now = new Date().getTime();
            String convertedDate = DateUtils.getRelativeTimeSpanString(
                    updateAt,
                    now + 1000,
                    DateUtils.SECOND_IN_MILLIS
            ).toString();

            viewHolder.timeLabel.setText(convertedDate);

            if (startIndex == -1) {
                // If the user didn't do a search, or the search string didn't match a display
                // name, show the display name without highlighting
                viewHolder.nameLabel.setText(name);
                if (TextUtils.isEmpty(mCurFilter)) {
                    // If the search search is empty, hide the second line of text
                    viewHolder.nameLabel2.setVisibility(View.GONE);
                } else {
                    // Shows a second line of text that indicates the search string matched
                    // something other than the display name
                    viewHolder.nameLabel2.setVisibility(View.VISIBLE);
                }
            } else {
                // If the search string matched the display name, applies a SpannableString to
                // highlight the search string with the displayed display name

                // Wraps the display name in the SpannableString
                final SpannableString highlightedName = new SpannableString(name);

                // Sets the span to start at the starting point of the match and end at "length"
                // characters beyond the starting point
                highlightedName.setSpan(highlightTextSpan, startIndex,
                        startIndex + mCurFilter.length(), 0);

                // Binds the SpannableString to the display name View object
                viewHolder.nameLabel.setText(highlightedName);

                // Since the search string matched the name, this hides the secondary message
                viewHolder.nameLabel2.setVisibility(View.GONE);
            }


            viewHolder.statusLabel.setText(status);


            ListData data = new ListData(id);
            // provide support for selected state
            updateCheckedState(viewHolder, data, profile_pics);
//            viewHolder.nameIcon.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // when the image is clicked, update the selected state
//                    ListData data = new ListData(id);
//                    data.setChecked(!data.isChecked);
//                    updateCheckedState(viewHolder, data);
//                }
//            });


        }

        private void updateCheckedState(ViewHolder holder, ListData item, byte[] image) {
            if (item.isChecked) {
                holder.nameIcon.setImageDrawable(mDrawableBuilder.build(" ", 0xff616161));
                holder.view.setBackgroundColor(HIGHLIGHT_COLOR);
                holder.check_icon.setVisibility(View.VISIBLE);
            } else {
                if (image == null){
                    TextDrawable drawable = mDrawableBuilder.build(String.valueOf(item.data.toUpperCase().charAt(0)), mColorGenerator.getColor(item.data));
                    holder.nameIcon.setImageDrawable(drawable);
                    holder.view.setBackgroundColor(Color.TRANSPARENT);
                }else{
                    Bitmap byteImage = BitmapFactory.decodeByteArray(image, 0, image.length);
                    holder.nameIcon.setImageBitmap(byteImage);
                }
                holder.check_icon.setVisibility(View.GONE);
            }
        }

        /**
         * Overrides swapCursor to move the new Cursor into the AlphabetIndex as well as the
         * CursorAdapter.
         */
        @Override
        public Cursor swapCursor(Cursor newCursor) {
            // Update the AlphabetIndexer with new cursor as well
            mAlphabetIndexer.setCursor(newCursor);
            return super.swapCursor(newCursor);
        }

        /**
         * An override of getCount that simplifies accessing the Cursor. If the Cursor is null,
         * getCount returns zero. As a result, no test for Cursor == null is needed.
         */
        @Override
        public int getCount() {
            if (getCursor() == null) {
                return 0;
            }
            return super.getCount();
        }

        /**
         * Defines the SectionIndexer.getSections() interface.
         */
        @Override
        public Object[] getSections() {
            return mAlphabetIndexer.getSections();
        }

        /**
         * Defines the SectionIndexer.getPositionForSection() interface.
         */
        @Override
        public int getPositionForSection(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getPositionForSection(i);
        }

        /**
         * Defines the SectionIndexer.getSectionForPosition() interface.
         */
        @Override
        public int getSectionForPosition(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getSectionForPosition(i);
        }


        /**
         * A class that defines fields for each resource ID in the list item layout. This allows
         * ContactsAdapter.newView() to store the IDs once, when it inflates the layout, instead of
         * calling findViewById in each iteration of bindView.
         */
        private class ViewHolder {
            View view;
            TextView objectId;
            ImageView nameIcon;
            TextView nameLabel;
            TextView nameLabel2;
            TextView timeLabel;
            TextView statusLabel;
            TextView countLabel;
            ImageView statusIcon;
            ImageView check_icon;
        }


    }

    private static class ListData {

        private String data;

        private boolean isChecked;

        public ListData(String data) {
            this.data = data;
        }

        public void setChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }
    }
}
