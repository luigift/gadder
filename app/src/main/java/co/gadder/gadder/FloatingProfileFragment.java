package co.gadder.gadder;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FloatingProfileFragment extends Fragment {

    public FloatingProfileFragment() {
    // Required empty public constructor
    }

    static Friend mFriend;
    static int clickCounting;

    private MainActivity activity;

    public static FloatingProfileFragment newInstance(Friend friend) {
        mFriend = friend;
        clickCounting = 0;
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

        // Set name
        TextView name = (TextView) getActivity().findViewById(R.id.floatingProfileName);
        name.setText(mFriend.name);

        // Set Image
        CircleImageView image = (CircleImageView) getActivity().findViewById(R.id.floatingProfileImage);
        if (mFriend.hasPictureUrl()) {
            Picasso.with(getContext())
                    .load(mFriend.pictureUrl)
                    .into(image);
        }

        // Set unfollow button
        CardView card = (CardView) getActivity().findViewById(R.id.floatingProfileCard);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickCounting > 0) {
                    activity.removeFriendship(mFriend.id);
                    getFragmentManager().beginTransaction()
                            .remove(FloatingProfileFragment.this)
                            .commit();
                } else {
                    Snackbar.make(view, R.string.click_again_to_unfollow, Snackbar.LENGTH_LONG).show();
                }
                clickCounting += 1;
            }
        });
    }
}
