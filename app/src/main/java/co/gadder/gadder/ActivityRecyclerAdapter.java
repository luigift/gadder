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
import java.util.List;
import java.util.Map;

public class ActivityRecyclerAdapter
        extends RecyclerView.Adapter<ActivityRecyclerAdapter.ActivityViewHolder> {

    List<GadderActivities.GadderActivity> mActivities;

    public ActivityRecyclerAdapter(Integer position) {
        mActivities = GadderActivities.ACTIVITY_LIST.get(position);
    }


    public GadderActivities.GadderActivity getItem(int position) {
        return mActivities.get(position);
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

        holder.description.setText(mActivities.get(position).description);
        holder.image.setText(mActivities.get(position).emoji);
    }

    @Override
    public int getItemCount() {
        return mActivities.size();
    }
}
