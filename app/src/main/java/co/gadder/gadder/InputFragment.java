package co.gadder.gadder;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import co.gadder.gadder.emoji.Activity;

public class InputFragment extends Fragment {

    private static final String TAG = InputFragment.class.getSimpleName();

    private String[] mActivityType;
    private int[] mActivityTypeEmoji;

    private TextView mImage;
    private String mActivity;
    private EditText mDescription;


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

        mActivityType = getActivity().getResources().getStringArray(R.array.activity_types);
        mActivityTypeEmoji = getActivity().getResources().getIntArray(R.array.activity_types_emojis);

        mImage = (TextView) getActivity().findViewById(R.id.activityImage);
        mDescription = (EditText) getActivity().findViewById(R.id.inputActivity);

        // Activity pager
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

        final TextView confirm = (TextView) getActivity().findViewById(R.id.confirmActivity);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("description", mDescription.getText().toString());
                    childUpdates.put("activity", mActivity);
//                childUpdates.put("location", mLocation);
//                childUpdates.put("start", mDateTime);
//                childUpdates.put("end", mDateTime);
                    FirebaseDatabase.getInstance().getReference()
                            .child(Constants.VERSION)
                            .child(Constants.USER_ACTIVITIES)
                            .child(user.getUid())
                            .updateChildren(childUpdates);
                } else {
                    Log.e(TAG, "No user found");
                }
            }
        });
    }

    private class ActivityTypePagerAdapter extends PagerAdapter {

        private Context mContext;

        public ActivityTypePagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.item_activity_type, container, false);

            // Activity recycler
            final RecyclerView recycler = (RecyclerView) layout.findViewById(R.id.activityTypeRecyclerView);
            recycler.setAdapter(new ActivityRecyclerAdapter(getContext(), position));
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
                            Map<String, Object> item = adapter.getItem(position);
                            mImage.setText(new String(Character.toChars((int) item.get("emoji"))));
                            mActivity = (String) item.get("activity");
                            mDescription.setText(mActivity);
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
            return mActivityType.length;
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
            holder.activityType.setText(mActivityType[position]);
            holder.activityTypeImage.setText(new String(Character.toChars(mActivityTypeEmoji[position])));
        }


        @Override
        public int getItemCount() {
            return mActivityType.length;
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
