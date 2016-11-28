package co.gadder.gadder;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ActivityTypeRecyclerAdapter extends
        RecyclerView.Adapter<ActivityTypeRecyclerAdapter.ActivityTypeViewHolder> {

    private Context mContext;

    public ActivityTypeRecyclerAdapter(Context context) {
        mContext = context;
    }

    public static class ActivityTypeViewHolder extends RecyclerView.ViewHolder {

        TextView activityType;
        RecyclerView recycler;

        public ActivityTypeViewHolder(View itemView) {
            super(itemView);
            activityType = (TextView) itemView.findViewById(R.id.activityType);
            recycler = (RecyclerView) itemView.findViewById(R.id.activityTypeRecyclerView);
        }
    }

    @Override
    public ActivityTypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ActivityTypeViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_activity_type, parent, false));
    }

    @Override
    public void onBindViewHolder(ActivityTypeViewHolder holder, int position) {
        holder.activityType.setText("Sports");

        holder.recycler.setAdapter(new ActivityRecyclerAdapter(position));
        holder.recycler.setLayoutManager(new LinearLayoutManager(mContext));
        holder.recycler.setHasFixedSize(true);
    }


    @Override
    public int getItemCount() {
        return 20;
    }
}
