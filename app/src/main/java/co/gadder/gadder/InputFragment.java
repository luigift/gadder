package co.gadder.gadder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class InputFragment extends Fragment {

    private static final String TAG = InputFragment.class.getSimpleName();

    private TextView mImage;
    private String mActivity = "unknown";
    private EditText mDescription;

    private Boolean mActivityReady = false;
    private Boolean mLocationReady = false;
    private Friend.Activity mSendActivity = new Friend.Activity();

    private Location mLocation;

    public InputFragment() {
    }

    public static InputFragment newInstance() {
        InputFragment fragment = new InputFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_input, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mImage = (TextView) getActivity().findViewById(R.id.activityImage);
        mDescription = (EditText) getActivity().findViewById(R.id.inputActivity);

        // Activity type pager
        final ViewPager pager = (ViewPager) getActivity().findViewById(R.id.activityTypeViewPager);
        pager.setAdapter(new ActivityTypePagerAdapter(getActivity()));

        // Type activity tab
        final RecyclerView recycler = (RecyclerView) getActivity().findViewById(R.id.activityTypeTab);
        recycler.setAdapter(new ActivityTypeRecyclerAdapter(getContext()));
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                recycler.scrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        recycler.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recycler,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        pager.setCurrentItem(position, true);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                }));

        final TextView confirm = (TextView) getActivity().findViewById(R.id.inputSendActivity);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm.setEnabled(false);
                FirebaseCrash.logcat(Log.DEBUG, TAG, "confirmActivity");
                mSendActivity.type = mActivity;
                mSendActivity.description = mDescription.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
                String time = sdf.format(Calendar.getInstance().getTime());
                mSendActivity.time = time;

                mActivityReady = true;

                sendActivity();

            }
        });
    }


    public void setLocation(Location location) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "setLocation");
        mSendActivity.location.latitude = (float) location.getLatitude();
        mSendActivity.location.longitude = (float) location.getLongitude();

        mLocationReady = true;

        if (mActivityReady) {
            sendActivity();
        }
    }

    private void sendActivity() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "sendActivity");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "Sending Activity");

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(Constants.VERSION + "/" + Constants.USER_ACTIVITIES + "/" + user.getUid(), mSendActivity);
            childUpdates.put(Constants.VERSION + "/" + Constants.USERS + "/" + user.getUid() + "/" + Constants.ACTIVITY, mSendActivity);
            FirebaseDatabase.getInstance().getReference()
                    .updateChildren(childUpdates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
            });
        } else {
            Log.e(TAG, "No user found");
        }
    }

    private class ActivityTypePagerAdapter extends PagerAdapter {

        private Context mContext;

        public ActivityTypePagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.item_activity_type, container, false);

            // Activity recycler
            final RecyclerView recycler = (RecyclerView) layout.findViewById(R.id.activityTypeRecyclerView);
            recycler.setAdapter(new ActivityRecyclerAdapter(position));
            recycler.setLayoutManager(new LinearLayoutManager(mContext));
            recycler.setHasFixedSize(true);
            float offsetPx = 250; //        getResources().getDimension(R.dimen.bottom_offset_dp);
            BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
            recycler.addItemDecoration(bottomOffsetDecoration);
            recycler.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recycler,
                    new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            ActivityRecyclerAdapter adapter = (ActivityRecyclerAdapter) recycler.getAdapter();
                            GadderActivities.GadderActivity item = adapter.getItem(position);
                            mActivity = item.type;
                            mImage.setText(item.emoji);
                            mDescription.setText(getString(item.description));
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {

                        }
                    }));

            container.addView(layout);

            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return GadderActivities.ACTIVITY_TYPES.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public static class ActivityTypeViewHolder extends RecyclerView.ViewHolder {

        TextView activityType;
        TextView activityTypeImage;

        public ActivityTypeViewHolder(View itemView) {
            super(itemView);
            activityType = (TextView) itemView.findViewById(R.id.activityType);
            activityTypeImage = (TextView) itemView.findViewById(R.id.activityTypeImage);
        }
    }

    public class ActivityTypeRecyclerAdapter extends
            RecyclerView.Adapter<ActivityTypeViewHolder> {

        private Context mContext;

        public ActivityTypeRecyclerAdapter(Context context) {
            mContext = context;
        }


        @Override
        public ActivityTypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ActivityTypeViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_activity_type_tab, parent, false));
        }

        @Override
        public void onBindViewHolder(ActivityTypeViewHolder holder, int position) {
            GadderActivities.GadderActivity activity = GadderActivities.ACTIVITY_TYPES.get(position);
            holder.activityType.setText(activity.description);
            holder.activityTypeImage.setText(activity.emoji);
        }


        @Override
        public int getItemCount() {
            return GadderActivities.ACTIVITY_LIST.size();
        }
    }

    static class BottomOffsetDecoration extends RecyclerView.ItemDecoration {
        private int mBottomOffset;

        public BottomOffsetDecoration(int bottomOffset) {
            mBottomOffset = bottomOffset;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int dataSize = state.getItemCount();
            int position = parent.getChildLayoutPosition(view);
            if (dataSize > 0 && position == dataSize - 1) {
                outRect.set(0, 0, 0, mBottomOffset);
            } else {
                outRect.set(0, 0, 0, 0);
            }

        }
    }

}
