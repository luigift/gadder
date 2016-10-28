package co.gadder.gadder;

import android.graphics.Rect;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivityFragment extends Fragment {
    private final static String TAG = "FriendsActivityFragment";

    private static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView batteryText;
        CircleImageView imageView;

        public FriendViewHolder(View itemView) {
            super(itemView);
            nameText = (TextView) itemView.findViewById(R.id.cellName);
            imageView = (CircleImageView) itemView.findViewById(R.id.cellImage);
            batteryText = (TextView) itemView.findViewById(R.id.cellBatteryLevel);
        }
    }

    private MainActivity activity;
    FirebaseRecyclerAdapter<Friend, FriendViewHolder> adapter;

    public FriendsActivityFragment() {
        // Required empty public constructor
    }

    public static FriendsActivityFragment newInstance() {
        return new FriendsActivityFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_friends_activity, container, false);

//        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.friendsRecyclerView);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        adapter = new FirebaseRecyclerAdapter<Friend, FriendViewHolder>(
//                Friend.class,
//                R.layout.item_friend,
//                FriendViewHolder.class,
//                activity.mDatabase.child()
//        ) {
//            @Override
//            protected void populateViewHolder(FriendViewHolder viewHolder, Friend model, int position) {
//                String name = model.firstName + model.lastName;
//                viewHolder.nameText.setText(name);
//                viewHolder.batteryText.setText(model.battery.toString());
//                if (model.pictureUrl != null) {
//                    if (!model.pictureUrl.isEmpty()) {
//                        Picasso.with(getActivity())
//                                .load(model.pictureUrl)
//                                .into(viewHolder.imageView);
//                    }
//                }
//            }
//        };
//        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.fabFriendMap);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .addToBackStack("friendActivity")
                        .replace(R.id.activity_main, FriendsMapFragment.newInstance())
                        .commit();
            }
        });
        return layout;
    }
}
