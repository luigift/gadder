package co.gadder.gadder;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class ActivityRecyclerAdapter
        extends RecyclerView.Adapter<ActivityRecyclerAdapter.ActivityViewHolder> {

    int[] mEmojis;
    Context mContext;
    Integer mPosition;
    String[] mDescription;

    public ActivityRecyclerAdapter(Context context, Integer position) {
        mContext = context;
        mPosition = position;

        switch (position) {
            default:
                mDescription = context.getResources().getStringArray(R.array.activity_sports);
                mEmojis = context.getResources().getIntArray(R.array.activity_sports_emojis);
                break;
            case 0:
                mDescription = context.getResources().getStringArray(R.array.activity_sports);
                mEmojis = context.getResources().getIntArray(R.array.activity_sports_emojis);
                break;
            case 1:
                mDescription = context.getResources().getStringArray(R.array.activity_music);
                mEmojis = context.getResources().getIntArray(R.array.activity_sports_emojis);
                break;
            case 2:
                mDescription = context.getResources().getStringArray(R.array.activity_places);
                mEmojis = context.getResources().getIntArray(R.array.activity_sports_emojis);
                break;
            case 3:
                mDescription = context.getResources().getStringArray(R.array.activity_nightlife);
                mEmojis = context.getResources().getIntArray(R.array.activity_sports_emojis);
                break;
//            case 4:
//                break;
//            case 5:
//                break;
//            case 6:
//                break;

        }
    }


    public Map<String, Object> getItem(int position) {
        Map<String, Object> item = new HashMap<>();
        item.put("emoji", mEmojis[position]);
        item.put("activity", mDescription[position]);
        return item;
    }

    public class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView image;
        TextView description;

        public ActivityViewHolder(View itemView) {
            super(itemView);

            image = (TextView) itemView.findViewById(R.id.activityImage);
            description = (TextView) itemView.findViewById(R.id.activityDescription);
        }
    }


    @Override
    public ActivityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ActivityViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false));
    }

    @Override
    public void onBindViewHolder(ActivityViewHolder holder, int position) {
        holder.description.setText(mDescription[position]);
        holder.image.setText(new String(Character.toChars(mEmojis[position])));
    }

    @Override
    public int getItemCount() {
        return mDescription.length;
    }
}
