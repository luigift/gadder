package co.gadder.gadder;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsFragment extends Fragment {

    private final static String TAG = "FindFriendsFragment";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
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

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
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
                final ContactViewHolder holder;
                if (convertView == null) {
                    holder = new ContactViewHolder();
                    final LayoutInflater inflater = LayoutInflater.from(getActivity());
                    convertView = inflater.inflate(R.layout.item_contact, parent, false);
                    holder.add = (ImageView) convertView.findViewById(R.id.contactAdd);
                    holder.name = (TextView) convertView.findViewById(R.id.contactName);
                    holder.layout = (LinearLayout) convertView.findViewById(R.id.contactLayout);
                    holder.image = (CircleImageView) convertView.findViewById(R.id.contactImage);
                    convertView.setTag(holder);
                } else {
                    holder = (ContactViewHolder) convertView.getTag();
                }

                holder.name.setText(friend.name);
                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            holder.add.setVisibility(View.GONE);
                            mDatabase.child(Constants.VERSION)
                                    .child(Constants.USER_FRIENDS)
                                    .child(user.getUid())
                                    .child(friend.id)
                                    .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    friendsAdapter.removeItem(friend.id);
                                    holder.add.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                });

                Log.d(TAG, "set holder: "+ friend.name);
                if (friend.pictureUrl != null && !friend.pictureUrl.isEmpty()) {
                    Picasso.with(getActivity())
                            .load(friend.pictureUrl)
                            .into(holder.image);
                }

                return convertView;
            }
        };

        final LoginActivity activity = (LoginActivity) getActivity();

        ListView friendsList = (ListView) activity.findViewById(R.id.friendsList);
//        friendsList.addHeaderView(LayoutInflater.from(getActivity()).inflate(R.layout.));
//        friendsList.setDivider(null);
        friendsList.setAdapter(friendsAdapter);

        getFriendsFromContacts();

//        Cursor phones = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
//        while (phones.moveToNext())  {
//            final Friend friend = new Friend();
//            friend.name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//            String rawPhone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//            friend.phone = rawPhone.replaceAll("\\D+",""); // remove non number characters
//
//            mDatabase
//                    .child(Constants.VERSION)
//                    .child(Constants.USER_PHONE)
//                    .child(friend.phone)
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            if(dataSnapshot.exists()) {
//                                Log.d(TAG, "name" +friend.name + " phone: " + friend.phone);
//                                String uid = dataSnapshot.getValue(String.class);
//                                friendsAdapter.addItem(uid, friend);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
//        }
//        phones.close();


        FloatingActionButton cancel = (FloatingActionButton) getActivity().findViewById(R.id.cancelFindFriends);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "Cancel", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
    }

    private void getFriendsFromContacts() {
        Log.d(TAG, "getFriendsFromContacts");
        Log.d(TAG, "got Contacts permission");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor phones = getActivity()
                        .getContentResolver()
                        .query(ContactsContract
                                .CommonDataKinds
                                .Phone.CONTENT_URI, null, null, null, null);
                while (phones.moveToNext()) {
                    final Friend friend = new Friend();
                    friend.name = phones
                            .getString(phones
                                    .getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String rawPhone = phones
                            .getString(phones
                                    .getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    friend.phone = rawPhone.replaceAll("\\D+", ""); // remove non number characters

                    mDatabase
                            .child(Constants.VERSION)
                            .child(Constants.USER_PHONE)
                            .child(friend.phone)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Log.d(TAG, "name" + friend.name + " phone: " + friend.phone);
                                        Log.d(TAG, "snapshot: " + dataSnapshot.toString());
                                        friend.id = dataSnapshot.getValue(String.class);
                                        friendsAdapter.addItem(friend.id, friend);

                                        mDatabase.
                                                child(Constants.VERSION)
                                                .child(Constants.USERS)
                                                .child(friend.id)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Friend newFriend = dataSnapshot.getValue(Friend.class);
                                                Log.d(TAG, "contact info downloaded:  " + newFriend.name);
                                                newFriend.id = dataSnapshot.getKey();
                                                friendsAdapter.addItem(newFriend.id, newFriend);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
                phones.close();
            }
        }).start();
    }

}
