package co.gadder.gadder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationFragment";

    FirebaseRecyclerAdapter mFirebaseAdapter;

    TextView message;

    public int noNotifications = 0;

    public NotificationFragment () {

    }

    public static class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView accept;
        ImageView reject;
        CircleImageView image;

        public FriendRequestViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.friendRequestName);
            image = (CircleImageView) itemView.findViewById(R.id.friendRequestImage);
            accept = (ImageView) itemView.findViewById(R.id.friendRequestAccept);
            reject = (ImageView) itemView.findViewById(R.id.friendRequestReject);
        }
    }

    public static NotificationFragment newInstance() {
        Bundle args = new Bundle();
        NotificationFragment fragment = new NotificationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = (MainActivity) getActivity();

        final FirebaseUser user = activity.mAuth.getCurrentUser();

        message = (TextView) activity.findViewById(R.id.inboxMessage);

        if (user != null ) {

            Log.d("Notification Fragment", "user ok");
            RecyclerView recycler = (RecyclerView) activity.findViewById(R.id.notificationRecyclerView);
            recycler.setHasFixedSize(true);
            recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
            mFirebaseAdapter = new FirebaseRecyclerAdapter<String, FriendRequestViewHolder>(
                    String.class,
                    R.layout.item_friend_request,
                    FriendRequestViewHolder.class,
                    activity.mDatabase
                            .child(Constants.VERSION)
                            .child(Constants.USER_NOTIFICATIONS)
                            .child(user.getUid())
            ) {

                @Override
                protected void populateViewHolder(final FriendRequestViewHolder viewHolder, String model, final int position) {
                    message.setVisibility(View.GONE);
                    noNotifications += 1;
                    Log.d(TAG, "noNotifications: " + noNotifications);
                    activity.setNotificationListener(noNotifications);

                    Log.d("populateViewHolder", "model : " + model);

                    activity.mDatabase
                            .child(Constants.VERSION)
                            .child(Constants.USERS)
                            .child(getRef(position).getKey())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Friend friend = dataSnapshot.getValue(Friend.class);
                                    if (friend != null) {
                                        viewHolder.name.setText(friend.name);
                                        Picasso.with(getActivity())
                                                .load(friend.pictureUrl)
                                                .into(viewHolder.image);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                    viewHolder.accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setFriendship(true, position);
                            removeValue(position);
                        }
                    });

                    viewHolder.reject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setFriendship(false,position);
                            removeValue(position);
                        }
                    });
                }

                private void removeValue(int position) {
                    activity.mDatabase
                            .child(Constants.VERSION)
                            .child(Constants.USER_NOTIFICATIONS)
                            .child(user.getUid())
                            .child(getRef(position).getKey())
                            .removeValue();

                    noNotifications -=1;
                    activity.setNotificationListener(noNotifications);
                    Log.d(TAG, "noNotifications: " + noNotifications);
                }

                private void setFriendship(Boolean value, int position) {
                    activity.mDatabase
                            .child(Constants.VERSION)
                            .child(Constants.USER_FRIENDS)
                            .child(getRef(position).getKey())
                            .child(user.getUid())
                            .setValue(value);
                }
            };
            recycler.setAdapter(mFirebaseAdapter);
        }
    }
}
