package co.gadder.gadder;

import android.os.Bundle;
import android.util.TimingLogger;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.widget.CompoundButton;
import android.support.v7.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static  final String TAG = "ProfileFragment";

    TimingLogger timings = new TimingLogger(TAG, "Create");

    CardView edit;
    TextView name;
    CircleImageView image;

    Switch music;
    Switch nearby;
    Switch battery;
    Switch weather;
    Switch company;
    Switch location;
    Switch activities;
    Switch request;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
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

        name = (TextView) getActivity().findViewById(R.id.profileName);
        edit = (CardView) getActivity().findViewById(R.id.profileUserCard);

        music = (Switch) getActivity().findViewById(R.id.profileMusicSwitch);
        nearby = (Switch) getActivity().findViewById(R.id.profileNearbySwitch);
        battery = (Switch) getActivity().findViewById(R.id.profileBatterySwitch);
        weather = (Switch) getActivity().findViewById(R.id.profileWeatherSwitch);
        company = (Switch) getActivity().findViewById(R.id.profileCompanySwitch);
        location = (Switch) getActivity().findViewById(R.id.profileLocationSwitch);
        activities = (Switch) getActivity().findViewById(R.id.profileActivitySwitch);
        request = (Switch) getActivity().findViewById(R.id.profileFriendRequestSwitch);

        image = (CircleImageView) getActivity().findViewById(R.id.profileImage);

        Button logout = (Button) getActivity().findViewById(R.id.send);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
            }
        });

        Friend user = ((MainActivity) getActivity()).user;
        if (user != null) {
            setUser(user);
        }
    }

    public void setUser(final Friend user) {
        name.setText(user.name);
        music.setChecked(user.sharing.musicSharing);
        battery.setChecked(user.sharing.batterySharing);
        company.setChecked(user.sharing.companySharing);
        location.setChecked(user.sharing.locationSharing);
        activities.setChecked(user.sharing.activitySharing);

        nearby.setChecked(user.notification.nearbyNotification);
        request.setChecked(user.notification.requestNotification);

        if (user.image == null) {
            if (user.pictureUrl != null && !user.pictureUrl.isEmpty()) {
                Glide.with(getContext())
                        .load(user.pictureUrl)
                        .into(image);
            }
        } else {
            image.setImageBitmap(user.image);
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .addToBackStack("editProfile")
                        .add(R.id.activity_main, EditFragment.newInstance(user))
                        .commit();
            }
        });

        final DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference()
                        .child(Constants.VERSION)
                        .child(Constants.USERS)
                        .child(user.id);

        // Sharing callbacks
        music.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ref.child("sharing").child("musicSharing").setValue(b);
            }
        });
        battery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ref.child("sharing").child("batterySharing").setValue(b);
            }
        });
        company.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ref.child("sharing").child("companySharing").setValue(b);
            }
        });
        location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ref.child("sharing").child("locationSharing").setValue(b);
            }
        });
        activities.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ref.child("sharing").child("activitySharing").setValue(b);
            }
        });
        weather.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ref.child("sharing").child("weatherSharing").setValue(b);
            }
        });

        // Notification callbacks
        nearby.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ref.child("notification").child("nearbyNotification").setValue(b);
            }
        });
        request.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ref.child("notification").child("requestNotification").setValue(b);
            }
        });
    }
}
