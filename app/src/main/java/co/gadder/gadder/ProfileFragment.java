package co.gadder.gadder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    private MainActivity activity;

    static Friend mFriend;

    public static ProfileFragment newInstance(Friend friend) {
        mFriend = friend;
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (MainActivity) getActivity();

        TextView name = (TextView) getActivity().findViewById(R.id.floatingProfileName);
        name.setText(mFriend.name);

        Button token = (Button) activity.findViewById(R.id.token);
        token.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.sendUpdateRequest();
            }
        });

        Button logout = (Button) activity.findViewById(R.id.send);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.loginState = null;
                activity.mAuth.signOut();
            }
        });

    }
}
