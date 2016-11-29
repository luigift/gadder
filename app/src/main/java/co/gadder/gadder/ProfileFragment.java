package co.gadder.gadder;

import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.widget.CompoundButton;
import android.support.v7.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static  final String TAG = "ProfileFragment";

    Friend user = new Friend();

    public ProfileFragment() {
        // Required empty public constructor
    }

    private MainActivity activity;

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
        activity = (MainActivity) getActivity();

        final TextView name = (TextView) getActivity().findViewById(R.id.profileName);
        final CardView edit = (CardView) getActivity().findViewById(R.id.profileUserCard);

        final Switch music = (Switch) getActivity().findViewById(R.id.profileMusicSwitch);
        final Switch nearby = (Switch) getActivity().findViewById(R.id.profileNearbySwitch);
        final Switch battery = (Switch) getActivity().findViewById(R.id.profileBatterySwitch);
        final Switch weather = (Switch) getActivity().findViewById(R.id.profileWeatherSwitch);
        final Switch company = (Switch) getActivity().findViewById(R.id.profileCompanySwitch);
        final Switch location = (Switch) getActivity().findViewById(R.id.profileLocationSwitch);
        final Switch activities = (Switch) getActivity().findViewById(R.id.profileActivitySwitch);
        final Switch request = (Switch) getActivity().findViewById(R.id.profileFriendRequestSwitch);

        final CircleImageView image = (CircleImageView) getActivity().findViewById(R.id.profileImage);

        Button logout = (Button) activity.findViewById(R.id.send);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.loginState = null;
                activity.mAuth.signOut();
            }
        });

        final DatabaseReference ref =
                activity.mDatabase
                        .child(Constants.VERSION)
                        .child(Constants.USERS)
                        .child(activity.uid);

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

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "snapshot: "+ dataSnapshot.toString());
                user = dataSnapshot.getValue(Friend.class);
                if (user != null) {
                    Log.d(TAG, "user: " + user.toString());

                    user.id = dataSnapshot.getKey();
                    Log.d("ProfileFragment", "user: " + dataSnapshot.toString());
                    name.setText(user.name);
//                    music.setChecked(user.sharing.musicSharing);
//                    battery.setChecked(user.sharing.batterySharing);
//                    company.setChecked(user.sharing.companySharing);
//                    location.setChecked(user.sharing.locationSharing);
//                    activities.setChecked(user.sharing.activitySharing);

                    if (user.image == null && getContext() != null) {
                        if (user.pictureUrl != null && !user.pictureUrl.isEmpty()) {
                            Glide.with(getContext())
                                    .load(user.pictureUrl)
                                    .into(image);
                        }
                    } else {
                        image.setImageBitmap(user.image);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null) {
                    getFragmentManager().beginTransaction()
                            .addToBackStack("editProfile")
                            .add(R.id.activity_main, EditFragment.newInstance(user))
                            .commit();
                }
            }
        });

    }
}
