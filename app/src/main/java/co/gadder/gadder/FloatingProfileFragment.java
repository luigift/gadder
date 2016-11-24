package co.gadder.gadder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FloatingProfileFragment extends Fragment {

    public FloatingProfileFragment() {
    // Required empty public constructor
    }

    private MainActivity activity;

    static Friend mFriend;

    public static FloatingProfileFragment newInstance(Friend friend) {
        mFriend = friend;
        return new FloatingProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_floating_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (MainActivity) getActivity();
        RelativeLayout layout = (RelativeLayout) activity.findViewById(R.id.floatingProfileLayout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .remove(FloatingProfileFragment.this)
                        .commit();
            }
        });

        TextView name = (TextView) getActivity().findViewById(R.id.floatingProfileName);
        name.setText(mFriend.name);
    }
}
