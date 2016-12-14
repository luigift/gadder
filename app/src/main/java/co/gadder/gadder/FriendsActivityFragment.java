package co.gadder.gadder;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

public class FriendsActivityFragment extends Fragment {
    private final static String TAG = "FriendsActivityFragment";

    private MainActivity activity;

    public FriendsRecyclerAdapter adapter;

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

                                FirebaseCrash.logcat(Log.DEBUG, TAG, "position: " + position + "size: " + activity.friends.size());

                                Friend friend = adapter.getItem(position);
                                if (friend.friendship.equals(Friend.FRIEND)){
                                    activity.selectFriend(adapter.getItem(position));
                                } else if (friend.friendship.equals(Friend.CONTACT)) {
                                    Snackbar.make(view, getString(R.string.follow_request), Snackbar.LENGTH_LONG).show();
                                    activity.requestFriendship(adapter.getItem(position).id);
                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                getFragmentManager().beginTransaction()
                                        .addToBackStack("click")
                                        .replace(R.id.mainLayout, FloatingProfileFragment.newInstance(adapter.getItem(position)))
                                        .commit();
                            }
                        }));

        return layout;
    }

    public void hideProgressBar() {
        if (getActivity() != null) {
            final ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.activityLoadingFriends);
            progressBar.setVisibility(View.GONE);
        }
    }

    public void updateRecycler() {
        adapter.notifyDataSetChanged();
    }
}
