package co.gadder.gadder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SmallFriendRecyclerAdapter
        extends RecyclerView.Adapter<SmallFriendRecyclerAdapter.SmallFriendViewHolder> {

    protected static final String TAG = "FriendsRecyclerAdapter";

    static class SmallFriendViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        CircleImageView imageView;

        SmallFriendViewHolder(View itemView) {
            super(itemView);
            nameText = (TextView) itemView.findViewById(R.id.smallFriendName);
            imageView = (CircleImageView) itemView.findViewById(R.id.smallFriendImage);
        }
    }

    private MainActivity activity;
//    private static RecyclerViewClickListener itemListener;

    public SmallFriendRecyclerAdapter(MainActivity activity) {//, RecyclerViewClickListener itemListener) {
        this.activity = activity;
//        this.itemListener = itemListener;
    }

    @Override
    public SmallFriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_small_friend, parent, false);
        return new SmallFriendViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SmallFriendViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        Friend friend = getItem(position);

        holder.nameText.setText(friend.name);

        if(friend.image == null) {

            if (friend.pictureUrl != null) {
                if (!friend.pictureUrl.isEmpty()) {
                    Picasso.with(activity)
                            .load(friend.pictureUrl)
                            .into(holder.imageView);
                }
            }
        } else {
            holder.imageView.setImageBitmap(friend.image);
        }
    }

    @Override
    public int getItemCount() {
        return activity.friends.size();
    }

    public Friend getItem(int position) {
        return activity.friends.get(activity.friendsId.get(position));
    }

    public void addItem(Friend friend) {
        activity.friendsId.add(friend.id);
        activity.friends.put(friend.id, friend);
        notifyItemInserted(activity.friends.size());
    }

    public void removeItem(int position) {
        activity.friends.remove(activity.friendsId.remove(position));
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, activity.friends.size());
    }
}
