package co.gadder.gadder;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TimingLogger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static co.gadder.gadder.Constants.EMOJI_SIZE;

public class FriendsRecyclerAdapter
        extends RecyclerView.Adapter<FriendsRecyclerAdapter.GenericViewHolder> {

    protected static final String TAG = "FriendsRecyclerAdapter";

    private static final int FRIEND_VIEWHOLDER = 0;
    private static final int ALL_FRIENDS_VIEWHOLDER = 1;
    private static final int NEW_FRIENDS_VIEWHOLDER = 2;
    private static final int ADD_FRIEND_VIEWHOLDER = 3;
    private static final int CONTACTS_REQUEST_VIEWHOLDER = 4;

    private MainActivity activity;

    /////////  ViewHolders  /////////
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
//                        smoothScrollToPosition(friend.position);
                    }
                });
                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        activity.getSupportFragmentManager().beginTransaction()
                                .addToBackStack("profile")
                                .add(R.id.coordinatorLayout, FloatingProfileFragment.newInstance(friend))
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
                    .resize(400,200)
                    .onlyScaleDown()
                    .into(requestContactsImage);
            requestContactsPermissionButton = (Button) itemView.findViewById(R.id.buttonRequestContacts);
            requestContactsPermissionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PermissionManager.requestContactsPermission(activity);
                }
            });
        }

        @Override
        public void updateView(int position) {

        }
    }

    private class AddFriendViewHolder extends GenericViewHolder {

        AddFriendViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void updateView(int position) {

        }
    }

    private class FriendViewHolder extends GenericViewHolder {
        TextView noFriends;
        TextView friendName;
        TextView lastUpdate;
        TextView distanceText;
        ImageView friendImage;
        ImageView batteryImage;
        ImageView friendActivity;

        FriendViewHolder(View itemView) {
            super(itemView);
            friendName = (TextView) itemView.findViewById(R.id.friendName);
            friendImage = (ImageView) itemView.findViewById(R.id.friendImage);
            noFriends = (TextView) itemView.findViewById(R.id.numberOfFriends);
            lastUpdate = (TextView) itemView.findViewById(R.id.friendLastUpdate);
            distanceText = (TextView) itemView.findViewById(R.id.friendDistance);
            batteryImage = (ImageView) itemView.findViewById(R.id.friendBattery);
            friendActivity = (ImageView) itemView.findViewById(R.id.friendActivity);
        }

        public void updateView(int position) {
            final TimingLogger timings = new TimingLogger(TAG, "FriendViewHolder");
            FirebaseCrash.log("updateView");
            timings.addSplit("start update");
            final Friend friend = getItem(position);
            timings.addSplit("got friend: " + friend.name);

            if (friend != null) {
                FirebaseCrash.logcat(Log.DEBUG, TAG, "updateView: " + friend.name);

                // Set name
                friendName.setText(friend.name);
                timings.addSplit("set name");

                // Set number of friends
                if (friend.noFriends != null) {
                    noFriends.setText(String.valueOf(friend.noFriends));
                } else {
                    noFriends.setText("0");
                }

                // Set user battery
                if (friend.sharing.batterySharing != null && friend.sharing.batterySharing && friend.battery != null) {
                    batteryImage.setImageResource(getBatteryResource(friend.battery));
                }
                timings.addSplit("set battery");

                // Set user distance
                if (activity.user != null && friend.isSharingLocation()) {

                    Float dist = activity.user.getLocation().distanceTo(friend.getLocation());
                    Integer m = Math.round(dist / 100);
                    Integer km = Math.round(dist / 1000);

                    String distance;

                    if (km > 99) {
                        distance = "99+";
                    } else if (km > 0) {
                        distance = String.valueOf(km);
                    } else if (m > 1) {
                        distance = "0." + m;
                    } else {
                        distance = "0";
                    }
                    distanceText.setText(distance);
                } else {
                    distanceText.setText("?");
                }
                timings.addSplit("set distance");

                // set emoji
                if (friend.displayActivity()) {
                    friendActivity.setImageBitmap(null);
                    if (friend.activity.type != null && !friend.activity.type.isEmpty()) {
                        GadderActivities.GadderActivity act = GadderActivities.ACTIVITY_MAP.get(friend.activity.type);
                        if (act != null) {
                            FirebaseCrash.logcat(Log.DEBUG, TAG, "activity set");
                            String emoji = act.emoji;
                            friendActivity.setImageBitmap(Constants.textAsBitmap(emoji, EMOJI_SIZE, Color.WHITE));
                        }
                    }
                    timings.addSplit("set emoji");
                }

                String timeLapse = friend.getTimeLapse(activity.getResources());
                if (timeLapse != null && !timeLapse.isEmpty()) {
                    lastUpdate.setText(timeLapse);
                }

                // Set picture
                if (friend.image == null) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "No image");

                    friendImage.setImageResource(R.drawable.ic_face_black_24dp);

                    if (friend.pictureUrl != null && !friend.pictureUrl.isEmpty()) {
                        final Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                FirebaseCrash.logcat(Log.DEBUG, TAG, "onBitmapLoaded");
                                friend.image = bitmap;
                                friendImage.setImageBitmap(bitmap);
                                timings.addSplit("image downloaded");

                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                FirebaseCrash.log("onBitmapFailed");
//                            friendImage.setImageResource(R.drawable.ic_face_black_24dp);
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                FirebaseCrash.log("onPrepareLoad");
//                            friendImage.setImageResource(R.drawable.ic_face_black_24dp);
                            }
                        };

                        Picasso.with(activity)
                                .load(friend.pictureUrl)
                                .resize(80, 80)
                                .centerCrop()
                                .onlyScaleDown()
                                .into(target);
                    }

                } else {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "Has image " + friend.name);
                    FirebaseCrash.log("has image");
                    friendImage.setImageBitmap(friend.image);
                    timings.addSplit("set image");
                }
                timings.dumpToLog();
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
            final Friend friend = getItem(position);

            if (friend != null) {
                friendName.setText(friend.name);

                // Set image
                if (friend.image == null) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "No image");

                    friendImage.setImageResource(R.drawable.ic_face_black_24dp);

                    if (friend.pictureUrl != null && !friend.pictureUrl.isEmpty()) {
                        final Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                FirebaseCrash.logcat(Log.DEBUG, TAG, "onBitmapLoaded");
                                friend.image = bitmap;
                                friendImage.setImageBitmap(bitmap);

                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                FirebaseCrash.log("onBitmapFailed");
//                            friendImage.setImageResource(R.drawable.ic_face_black_24dp);
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                FirebaseCrash.log("onPrepareLoad");
//                            friendImage.setImageResource(R.drawable.ic_face_black_24dp);
                            }
                        };

                        Picasso.with(activity)
                                .load(friend.pictureUrl)
                                .resize(85, 85)
                                .centerCrop()
                                .onlyScaleDown()
                                .into(target);
                    }

                } else {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "Has image " + friend.name);
                    friendImage.setImageBitmap(friend.image);
                }
            }
        }
    }

    /////////  Recycler Methods  ////////
    public FriendsRecyclerAdapter(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        int itemViewType;

        if (position < activity.friends.size()) {
            Friend friend = getItem(position);

            if (friend == null) {
                return FRIEND_VIEWHOLDER;
            }

            switch (friend.friendship) {
                case Friend.FRIEND:
                    itemViewType = FRIEND_VIEWHOLDER;
                    break;
                case Friend.CONTACT:
                    itemViewType = NEW_FRIENDS_VIEWHOLDER;
                    break;
                default:
                    itemViewType = FRIEND_VIEWHOLDER;
                    break;
            }
        } else {
            itemViewType = ADD_FRIEND_VIEWHOLDER;
        }
        return itemViewType;
    }

    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCreateViewHolder");
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

            case ADD_FRIEND_VIEWHOLDER:
                return new AddFriendViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_add_friends, parent, false));

            default:
                return new FriendViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.item_friend, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final GenericViewHolder holder, int position) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onBindViewHolder:  " + position + "type: " + holder.getClass());

        holder.updateView(position);
    }

    @Override
    public int getItemCount() {
        return activity.friends.size() + 1;
    }

    public Friend getItem(int position) {
        ArrayList<Friend> array = new ArrayList<>(activity.friends.values());
        if (position > array.size() - 1) {
            return null;
        }
        return array.get(position);
    }

    ////////  Update Methods  ////////
    public void addItem(String uid) {
        int position = getPositionById(uid);
        if (position > 0) {
            notifyItemInserted(position);
        }
    }

    public void updateItem(String uid) {
        int position = getPositionById(uid);
        if (position > 0) {
            notifyItemChanged(position);
        }
    }

    public void removeItem(int position) {
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, activity.friends.size());
    }

    /////// Auxiliary Methods  ////////
    public int getPositionById(String uid) {
        ArrayList<Friend> array = new ArrayList<>(activity.friends.values());
        int position = 0;
        for (Friend f : array) {
            if (f.id.equals(uid)){
                return position;
            }
            position += 1;
        }
        return -1;
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
