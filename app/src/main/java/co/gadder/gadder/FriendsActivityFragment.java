package co.gadder.gadder;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class FriendsActivityFragment extends Fragment {
    private final static String TAG = "FriendsActivityFragment";

    private MainActivity activity;
    private FriendsRecyclerAdapter adapter;


    public FriendsActivityFragment() {}

    public static FriendsActivityFragment newInstance() {
        return new FriendsActivityFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        activity = (MainActivity) getActivity();

        View layout = inflater.inflate(R.layout.fragment_friends_activity, container, false);

        final RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.friendsRecyclerView);
        adapter = new FriendsRecyclerAdapter((MainActivity) getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(),
                        recyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Log.d(TAG, "position: " + position + "size: " + activity.friends.size());
                                Friend friend = adapter.getItem(position);
                                if (friend.friendship.equals(getString(R.string.friend))){
                                    activity.selectFriend(adapter.getItem(position));
                                } else if (friend.friendship.equals(getString(R.string.contact))) {
                                    activity.requestFriendship(adapter.getItem(position));
                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                getFragmentManager().beginTransaction()
                                        .addToBackStack("click")
                                        .replace(R.id.mainPager, FloatingProfileFragment.newInstance(adapter.getItem(position)))
                                        .commit();
                            }
                        }));

        return layout;
    }

    public void addFriend(Friend friend) {
        adapter.addItem(friend);
    }
}
