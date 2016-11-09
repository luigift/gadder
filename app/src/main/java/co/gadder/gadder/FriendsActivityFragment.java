package co.gadder.gadder;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends_activity, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = (MainActivity) getActivity();

        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.friendsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        activity.adapter.setRecyclerView(recyclerView);
        recyclerView.setAdapter(activity.adapter);
//        recyclerView.addOnItemTouchListener(
//                new RecyclerItemClickListener(getActivity(),
//                        recyclerView,
//                        new RecyclerItemClickListener.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(View view, int position) {
//
//                            }
//
//                            @Override
//                            public void onLongItemClick(View view, int position) {
//                                getFragmentManager().beginTransaction()
//                                        .addToBackStack("profile")
//                                        .add(R.id.activity_main,
//                                                ProfileFragment.newInstance(activity.friends.get(activity.friendsId.get(position))))
//                                        .commit();
//                            }
//                        }));

////        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
////            @Override
////            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
////                super.onScrollStateChanged(recyclerView, newState);
////                Log.d(TAG, "newState: " + newState);
////                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
////                    Log.d(TAG, "state:" + newState + " down: " + scrollingDown + " pos: " + recyclerPosition);
////                    if (scrollingDown == null) {
////
////                    } else if(scrollingDown) {
////                        recyclerPosition += 1;
////                    } else {
////                        if (recyclerPosition != 0) {
////                            recyclerPosition -= 1;
////                        }
////                    }
////                    recyclerView.smoothScrollToPosition(recyclerPosition);
////                    recyclerPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
////                }
////            }
////
////            @Override
////            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
////                super.onScrolled(recyclerView, dx, dy);
////                if (dy > 0 ) {
////                    scrollingDown = true;
////                } else {
////                    scrollingDown = false;
////                }
//////                Log.d(TAG, "dx: " + dx + " dy: " + dy);
////            }
////        });
//
////        FloatingActionButton fab = (FloatingActionButton) activity.findViewById(R.id.fabFriendMap);
////        fab.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                getFragmentManager().beginTransaction()
////                        .addToBackStack("friendActivity")
////                        .replace(R.id.activity_main, FriendsMapFragment.newInstance())
////                        .commit();
////            }
////        });
    }

}
