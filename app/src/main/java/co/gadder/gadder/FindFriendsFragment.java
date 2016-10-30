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
import android.telephony.PhoneNumberUtils;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static android.telephony.PhoneNumberUtils.formatNumber;

public class FindFriendsFragment extends Fragment {

    private final static String TAG = "FindFriendsFragment";

    private String mSearchString;
    private String[] mSelectionArgs = { mSearchString };

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
        MainActivity activity = (MainActivity) getActivity();

        Cursor phones = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            final Friend friend = new Friend();
            friend.name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String rawPhone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            Log.d(TAG, "name: "+ friend.name + " phone: " + rawPhone);
            Log.d(TAG, "global: " + PhoneNumberUtils.isGlobalPhoneNumber(rawPhone));

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                String util = PhoneNumberUtils.formatNumber(rawPhone, "BR");
                Log.d(TAG, "util: " + util);
            }

            Log.d(TAG, "first: " + rawPhone.charAt(0));
            if (!rawPhone.startsWith("+")) {  // not international format
//                rawPhone.replaceAll("\\D+",""); // remove non number characters
                rawPhone.replaceAll("\\D+",""); // remove non number characters
                Log.d(TAG, "clear: " + rawPhone);
            }

            if (rawPhone.startsWith("0")) {
                String zero = rawPhone.replaceAll("\\^0", "TESTE");
                Log.d(TAG, "zero: " + zero);
            }

//            activity.mDatabase.child("user_phone").child(friend.phone).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.d(TAG, "exists: " + dataSnapshot.exists());
//                    Log.d(TAG, "Snapshot: "+ dataSnapshot.toString());
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
        }
        phones.close();
    }
}
