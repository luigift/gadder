package co.gadder.gadder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsRecyclerAdapter
        extends RecyclerView.Adapter<FriendsRecyclerAdapter.GenericViewHolder> {

    protected static final String TAG = "FriendsRecyclerAdapter";

    private static final int FRIEND_VIEWHOLDER = 0;
    private static final int ALL_FRIENDS_VIEWHOLDER = 1;
    private static final int NEW_FRIENDS_VIEWHOLDER = 2;
    private static final int CONTACTS_REQUEST_VIEWHOLDER = 3;

    private MainActivity activity;

    abstract class GenericViewHolder extends RecyclerView.ViewHolder {
        GenericViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void updateView(int position);
    }

    private class AllFriendsViewHolder extends GenericViewHolder {
        GridLayout gridLayout;

        AllFriendsViewHolder(View itemView) {
            super(itemView);
            gridLayout = (GridLayout) itemView.findViewById(R.id.friendsGrid);
        }

        @Override
        public void updateView(int position) {
            for (final Friend friend : activity.friends.values()) {
                View v = View.inflate(activity, R.layout.item_small_friend, null);
                ((TextView)v.findViewById(R.id.smallFriendName)).setText(null);
                if (friend.pictureUrl != null) {
                    if (!friend.pictureUrl.isEmpty()) {
                        final CircleImageView circleImageView =
                                (CircleImageView) v.findViewById(R.id.smallFriendImage);
                        Picasso.with(activity)
                                .load(friend.pictureUrl)
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        circleImageView.setImageBitmap(bitmap);

                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

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
                gridLayout.addView(v);
            }
        }
    }

    private class ContactsRequestViewHolder extends GenericViewHolder {
        Button requestContactsPermissionButton;
        ImageView requestContactsImage;

        ContactsRequestViewHolder(View itemView) {
            super(itemView);
            requestContactsImage = (ImageView) itemView.findViewById(R.id.requestContactsImage);
            Picasso.with(activity)
                    .load("http://cdn.tinybuddha.com/wp-content/uploads/2014/09/Friends-Having-Fun.png")
                    .into(requestContactsImage);
            requestContactsPermissionButton = (Button) itemView.findViewById(R.id.buttonRequestContacts);
            requestContactsPermissionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, activity.REQUEST_READ_CONTACTS_PERMISSION);
                }
            });
        }

        @Override
        public void updateView(int position) {

        }
    }

    private class FriendViewHolder extends GenericViewHolder {
        CardView friendCard;
        ImageView friendImage;
        TextView distanceText;
        ImageView batteryImage;

        FriendViewHolder(View itemView) {
            super(itemView);
            friendCard = (CardView) itemView.findViewById(R.id.friendCard);
            friendImage = (ImageView) itemView.findViewById(R.id.friendImage);
            batteryImage = (ImageView) itemView.findViewById(R.id.friendBattery);
            distanceText = (TextView) itemView.findViewById(R.id.friendDistance);
        }

        public void updateView(int position) {
            final Friend friend = getItem(position);

            this.batteryImage.setImageResource(getBatteryResource(friend.battery));

            String distance =
                    Math.round(activity.mLocation.distanceTo(friend.getLocation()) / 1000) + " km";
            distanceText.setText(distance);
            if(friend.image == null) {
                if (friend.pictureUrl != null) {
                    if (!friend.pictureUrl.isEmpty()) {
                        Picasso.with(activity)
                                .load(friend.pictureUrl)
                                .into(friendImage);
                    }
                }
            } else {
                friendImage.setImageBitmap(friend.image);
            }
        }
    }

    private class NewFriendViewHolder extends GenericViewHolder {
        TextView friendName;
        ImageView friendImage;

        NewFriendViewHolder(View itemView) {
            super(itemView);
            friendName = (TextView) itemView.findViewById(R.id.newFriendName);
            friendImage = (ImageView) itemView.findViewById(R.id.newFriendImage);
        }

        @Override
        public void updateView(int position) {
            Friend friend = getItem(position);
            friendName.setText(friend.name);
            if (friend.pictureUrl != null && !friend.pictureUrl.isEmpty()) {
                Glide.with(activity)
                        .load(friend.pictureUrl)
                        .into(friendImage);
            }
        }
    }

    public FriendsRecyclerAdapter(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        int itemViewType = FRIEND_VIEWHOLDER;
        if (position >= activity.friends.size()) {
            if (activity.checkContactsPermission()) {
                itemViewType = NEW_FRIENDS_VIEWHOLDER;
            } else {
                itemViewType = CONTACTS_REQUEST_VIEWHOLDER;
            }
        }
        return itemViewType;
    }

    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case CONTACTS_REQUEST_VIEWHOLDER:
                return new ContactsRequestViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_contact_request, parent, false));

            case ALL_FRIENDS_VIEWHOLDER:
                return new AllFriendsViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_all_friends, parent, false));

            case NEW_FRIENDS_VIEWHOLDER:
                return new NewFriendViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_new_friend, parent, false));
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
    public void onBindViewHolder(final GenericViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder:  " + position + "type: " + holder.getClass());

//        Random rnd = new Random();
//        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//        mRecyclerView.setBackgroundColor(color);
//        holder.itemView.setBackgroundColor(color);

//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            activity.getWindow().setStatusBarColor(color);
//            activity.getWindow().setNavigationBarColor(color);
//        }

        holder.updateView(position);
    }


    @Override
    public int getItemCount() {
        if(activity.friends.size() == 0) {
            return 0;
        } else {
            if (activity.checkContactsPermission()) {
                return activity.friends.size() + activity.contacts.size();
            } else {
                return activity.friends.size() + 1;
            }
        }
    }

    public Friend getItem(int position) {
        if (position < activity.friends.size()) {
            return activity.friends.get(activity.friendsId.get(position));
        } else {
            return activity.contacts.get(position - activity.friends.size());
        }
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

    private static int getBatteryResource(int battery) {
        if(battery <= 10) {
            return R.drawable.ic_battery_10_white_24dp;
        } else if (battery <= 20) {
            return R.drawable.ic_battery_20_white_24dp;
        } else if (battery <= 30) {
            return R.drawable.ic_battery_30_white_24dp;
        } else if (battery <= 40) {
            return R.drawable.ic_battery_40_white_24dp;
        } else if (battery <= 50) {
            return R.drawable.ic_battery_50_white_24dp;
        } else if (battery <= 60) {
            return R.drawable.ic_battery_60_white_24dp;
        } else if (battery <= 70) {
            return R.drawable.ic_battery_70_white_24dp;
        } else if (battery <= 80) {
            return R.drawable.ic_battery_80_white_24dp;
        } else if (battery <= 90) {
            return R.drawable.ic_battery_90_white_24dp;
        } else {
            return R.drawable.ic_battery_std_white_24dp;
        }

    }
}
