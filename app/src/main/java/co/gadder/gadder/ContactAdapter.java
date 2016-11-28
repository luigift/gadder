package co.gadder.gadder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class ContactAdapter extends BaseAdapter {

    List<String> friendsKey;
    Map<String, Friend> friends;

    static class ContactViewHolder {
        ImageView add;
        TextView name;
        LinearLayout layout;
        CircleImageView image;
    }


    ContactAdapter() {
        this.friends = new HashMap<>();
        this.friendsKey = new ArrayList<>();
    }

    void addItem(String uid, Friend friend) {
        if (!friends.containsKey(uid)) {
            friendsKey.add(uid);
            friends.put(uid, friend);
            notifyDataSetChanged();
        } else {
            friends.put(uid, friend);
            notifyDataSetChanged();
        }
    }

    void removeItem(String uid) {
        friends.remove(uid);
        friendsKey.remove(uid);
        notifyDataSetChanged();

    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int i) {
        return friends.get(friendsKey.get(i));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
}
