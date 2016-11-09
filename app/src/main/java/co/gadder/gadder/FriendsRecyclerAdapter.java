package co.gadder.gadder;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsRecyclerAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static final String TAG = "FriendsRecyclerAdapter";

    static class AllFriendsViewHolder extends RecyclerView.ViewHolder {
        GridLayout gridLayout;

        AllFriendsViewHolder(View itemView) {
            super(itemView);
            gridLayout = (GridLayout) itemView.findViewById(R.id.friendsGrid);
        }
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView musicText;
        TextView batteryText;
        TextView distanceText;
        CircleImageView imageView;

        FriendViewHolder(View itemView) {
            super(itemView);
            nameText = (TextView) itemView.findViewById(R.id.smallFriendName);
            musicText = (TextView) itemView.findViewById(R.id.friendMusic);
            batteryText = (TextView) itemView.findViewById(R.id.friendBattery);
            distanceText = (TextView) itemView.findViewById(R.id.friendDistance);
            imageView = (CircleImageView) itemView.findViewById(R.id.smallFriendImage);
        }

        public void update() {

        }
    }

    private MainActivity activity;

    public FriendsRecyclerAdapter(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return 0;
            default:
                return position;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new AllFriendsViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_all_friends, parent, false));
            default:
                return new FriendViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_friend, parent, false));
        }
    }

    private RecyclerView mRecyclerView;

    void setRecyclerView(RecyclerView rv) {
        mRecyclerView = rv;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder:  " + position);

        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        holder.itemView.setBackgroundColor(color);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
            activity.getWindow().setNavigationBarColor(color);
            activity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
//            activity.getActionBar().setBackgroundDrawable(new ColorDrawable(color));
        }

        if (position == 0) {
            for (final Friend friend : activity.friends.values()) {
                View v = View.inflate(activity, R.layout.item_small_friend, null);
                ((TextView)v.findViewById(R.id.smallFriendName)).setText(null);//friend.name);
                if (friend.pictureUrl != null) {
                    if (!friend.pictureUrl.isEmpty()) {
                        final CircleImageView circleImageView =
                                (CircleImageView) v.findViewById(R.id.smallFriendImage);
                        Picasso.with(activity)
                                .load(friend.pictureUrl)
                                .into(circleImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        friend.image = ((BitmapDrawable)circleImageView.getDrawable()).getBitmap();
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                    }
                }
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRecyclerView.smoothScrollToPosition(friend.position);
                    }
                });
                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        activity.getSupportFragmentManager().beginTransaction()
                                .addToBackStack("profile")
                                .add(R.id.activity_main, FloatingProfileFragment.newInstance(friend))
                                .commit();
                        return true;
                    }
                });
                ((AllFriendsViewHolder)holder).gridLayout.addView(v);
            }
        } else {

            final Friend friend = getItem(position - 1);

            ((FriendViewHolder)holder).nameText.setText(friend.name);
            String battery = friend.battery + "%";
            ((FriendViewHolder)holder).batteryText.setText(battery);
            ((FriendViewHolder)holder).musicText.setText(friend.music);
            String distance =
                    Math.round(activity.mLocation.distanceTo(friend.getLocation()) / 1000) + "km";
            ((FriendViewHolder)holder).distanceText.setText(distance);
            if(friend.image == null) {
                if (friend.pictureUrl != null) {
                    if (!friend.pictureUrl.isEmpty()) {
                        Picasso.with(activity)
                                .load(friend.pictureUrl)
                                .into(((FriendViewHolder) holder).imageView);
                    }
                }
            } else {
                ((FriendViewHolder) holder).imageView.setImageBitmap(friend.image);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 1 + activity.friends.size();
    }

    public Friend getItem(int position) {
        return activity.friends.get(activity.friendsId.get(position));
    }

    public void updateItem(Friend friend) {
        if(activity.friends.containsKey(friend.id)) {
            int position = activity.friendsId.indexOf(friend.id);
            activity.friends.put(friend.id, friend);
            notifyItemChanged(position);
        } else {
            addItem(friend);
        }
    }

    public void addItem(Friend friend) {
        activity.friendsId.add(friend.id);
        activity.friends.put(friend.id, friend);
        friend.position = activity.friends.size();
        notifyItemInserted(activity.friends.size());
        notifyItemChanged(0);
    }

    public void removeItem(int position) {
        activity.friends.remove(activity.friendsId.remove(position));
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, activity.friends.size());
    }
}
