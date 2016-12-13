package co.gadder.gadder;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    private class FriendViewHolder extends GenericViewHolder {
        TextView friendName;
        TextView distanceText;
        ImageView friendImage;
        ImageView batteryImage;
        ImageView friendActivity;

        FriendViewHolder(View itemView) {
            super(itemView);
            friendName = (TextView) itemView.findViewById(R.id.friendName);
            friendImage = (ImageView) itemView.findViewById(R.id.friendImage);
            distanceText = (TextView) itemView.findViewById(R.id.friendDistance);
            batteryImage = (ImageView) itemView.findViewById(R.id.friendBattery);
            friendActivity = (ImageView) itemView.findViewById(R.id.friendActivity);
        }

        public void updateView(int position) {
            FirebaseCrash.log("updateView");
            final Friend friend = getItem(position);
            Log.d(TAG, "updateView: " + friend.name);

            // Set name
            friendName.setText(friend.name);

            // Set user battery
            if (friend.sharing.batterySharing != null && friend.sharing.batterySharing && friend.battery != null) {
                batteryImage.setImageResource(getBatteryResource(friend.battery));
            }

            // Set user distance
            if (activity.user != null && friend.isSharingLocation()) {
                Float dist = activity.user.getLocation().distanceTo(friend.getLocation());
                Integer m = Math.round(dist/100);
                Integer km = Math.round( dist / 1000);

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

            // set emoji
            if (friend.activity.type != null && !friend.activity.type.isEmpty()) {
                GadderActivities.GadderActivity act = GadderActivities.ACTIVITY_MAP.get(friend.activity.type);
                if (act != null) {
                    String emoji = act.emoji;
                    friendActivity.setImageBitmap(Constants.textAsBitmap(emoji, EMOJI_SIZE, Color.WHITE));
                }
            } else {
                friendActivity.setImageBitmap(null);
            }

            // Set picture
            if (friend.image == null) {
                Log.d(TAG, "No image" + friend.name);
                FirebaseCrash.log("No image");

                friendImage.setImageResource(R.drawable.ic_face_black_24dp);

                if (friend.pictureUrl != null && !friend.pictureUrl.isEmpty()) {
                    final Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            FirebaseCrash.log("onBitmapLoaded");
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
                            .into(target);
                }

            } else {
                Log.d(TAG, "Has image " + friend.name);
                FirebaseCrash.log("has image");
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
                Picasso.with(activity)
                        .load(friend.pictureUrl)
                        .into(friendImage);
            } else {
                Picasso.with(activity)
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
        Friend friend = getItem(position);
        Log.d(TAG, "getItemViewType: " + friend.name);
        int itemViewType;
        switch (friend.friendship) {
            case "friend" :
                itemViewType = FRIEND_VIEWHOLDER;
                break;
            case "contact" :
                itemViewType = NEW_FRIENDS_VIEWHOLDER;
                break;
            default:
                itemViewType = FRIEND_VIEWHOLDER;
                break;
        }
        return itemViewType;
    }

    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
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

    @Override
    public void onBindViewHolder(final GenericViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder:  " + position + "type: " + holder.getClass());

        holder.updateView(position);
    }

    @Override
    public int getItemCount() {
        return activity.friends.size();
    }



    public Friend getItem(int position) {
        ArrayList<Friend> array = new ArrayList<>(activity.friends.values());
        return array.get(position);
    }

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

    public void removeItem(int position) {
        activity.friends.remove(getItem(position).id);
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
