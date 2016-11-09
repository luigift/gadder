package co.gadder.gadder;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class FindFriendsFragment extends Fragment {

    private final static String TAG = "FindFriendsFragment";

    private ContactAdapter friendsAdapter;

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

        friendsAdapter = new ContactAdapter() {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final Friend friend = friends.get(friendsKey.get(position));
                ContactViewHolder viewHolder;
                if (convertView == null) {
                    viewHolder = new ContactViewHolder();
                    final LayoutInflater inflater = LayoutInflater.from(getActivity());
                    convertView = inflater.inflate(R.layout.item_contact, parent, false);
                    viewHolder.add = (Button) convertView.findViewById(R.id.contactAdd);
                    viewHolder.name = (TextView) convertView.findViewById(R.id.contactName);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ContactViewHolder) convertView.getTag();
                }

                viewHolder.name.setText(friend.name);
                viewHolder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getFragmentManager().beginTransaction()
                                .addToBackStack("profile")
                                .add(R.id.activity_main, FloatingProfileFragment.newInstance(friend))
                                .commit();
                    }
                });

                return convertView;
            }
        };

        final MainActivity activity = (MainActivity) getActivity();

        ListView friendsList = (ListView) activity.findViewById(R.id.friendsList);
        friendsList.setAdapter(friendsAdapter);

        Cursor phones = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())  {
            final Friend friend = new Friend();
            friend.name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String rawPhone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            friend.phone = rawPhone.replaceAll("\\D+",""); // remove non number characters

            activity.mDatabase.child("user_phone").child(friend.phone).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        Log.d(TAG, "name" +friend.name + " phone: " + friend.phone);
                        String uid = dataSnapshot.getValue(String.class);
                        friendsAdapter.addItem(uid, friend);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        phones.close();
    }
}
