package co.gadder.gadder;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.app.Fragment;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class FindFriendsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private final static String TAG = "FindFriendsFragment";

    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private final static int[] TO_IDS = {
            R.id.text1,
            R.id.text2
    };

    private static final int CONTACT_ID_INDEX = 0;
    private static final int CONTACT_KEY_INDEX = 1;
    private static final int PHONE_INDEX = 2;

    @SuppressLint("InlinedApi")
    private static final String SELECTION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" :
                    ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION =
            {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    Build.VERSION.SDK_INT
                            >= Build.VERSION_CODES.HONEYCOMB ?
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                            ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };

    private String mSearchString;
    private String[] mSelectionArgs = { mSearchString };

    long mContactId;
    Uri mContactUri;
    String mContactKey;
    ListView mContactsList;
    private SimpleCursorAdapter mCursorAdapter;

    public FindFriendsFragment() {
        // Required empty public constructor
    }

    public static FindFriendsFragment newInstance() {
        return new FindFriendsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_friends, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        mContactsList = (ListView) getActivity().findViewById(R.id.friendsList);
        mCursorAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.item_contact,
                null,
                FROM_COLUMNS, TO_IDS,
                0);
        mContactsList.setAdapter(mCursorAdapter);
        mContactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), "TESTE", Toast.LENGTH_SHORT).show();
                Log.d(TAG,"TEST");
            }
        });

        getLoaderManager().initLoader(0, null, this);

        Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String contactName=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String contactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Log.d(TAG, "name: " + contactName + " phone: " + contactNumber);

        }
        phones.close();

        Log.d(TAG, "Loader set");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View item, int position, long rowID) {
        final Cursor cursor = mCursorAdapter.getCursor();
        cursor.moveToPosition(position);
        mContactId = cursor.getLong(CONTACT_ID_INDEX);
        mContactKey = cursor.getString(CONTACT_KEY_INDEX);
        mContactUri = ContactsContract.Contacts.getLookupUri(mContactId, mContactKey);
        Toast.makeText(getActivity(), "Contact: " + mContactKey, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        /*
         * Makes search string into pattern and
         * stores it in the selection array
         */
//        mSelectionArgs[0] = "%" + mSearchString + "%";
        mSearchString = "Daniel";
//        mSelectionArgs[0] = "%" + mSearchString + "%";
        mSelectionArgs[0] = "%Daniel%";
        // Starts the query
        Log.d(TAG, "Uri: "+ ContactsContract.CommonDataKinds.Phone.CONTENT_URI);//Contacts.CONTENT_URI);
        return new CursorLoader(
                getActivity(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, //ContactsContract.Contacts.CONTENT_URI,
                PROJECTION,
                SELECTION,
                mSelectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Put the result Cursor in the adapter for the ListView
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Delete the reference to the existing Cursor
        mCursorAdapter.swapCursor(null);

    }
}
