package co.gadder.gadder;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ActivityRecyclerAdapter
        extends RecyclerView.Adapter<ActivityRecyclerAdapter.ActivityViewHolder> {

    Integer mPosition;

    public ActivityRecyclerAdapter(Integer position) {
        mPosition = position;
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
        holder.description.setText("Running");
        holder.image.setText(new String(Character.toChars(0x1F60A)));
    }

    @Override
    public int getItemCount() {
        return 50;
    }
}
