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

    private static int recyclerPosition = 0;
    private static Boolean scrollingDown = null;


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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        activity = (MainActivity) getActivity();

        View layout = inflater.inflate(R.layout.fragment_friends_activity, container, false);

        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.friendsRecyclerView);
        recyclerView.setAdapter(activity.adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
//        recyclerView
//                .setLayoutManager(
//                    new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(),
                        recyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }
                        }));
        activity.adapter.setRecyclerView(recyclerView);

//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                Log.d(TAG, "newState: " + newState);
//                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    Log.d(TAG, "state:" + newState + " down: " + scrollingDown + " pos: " + recyclerPosition);
//                    if (scrollingDown == null) {
//
//                    } else if(scrollingDown) {
//                        recyclerPosition += 1;
//                    } else {
//                        if (recyclerPosition != 0) {
//                            recyclerPosition -= 1;
//                        }
//                    }
//                    recyclerView.smoothScrollToPosition(recyclerPosition);
//                    recyclerPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (dy > 0 ) {
//                    scrollingDown = true;
//                } else {
//                    scrollingDown = false;
//                }
//                Log.d(TAG, "dx: " + dx + " dy: " + dy);
//            }
//        });

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}
