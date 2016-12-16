package co.gadder.gadder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.crash.FirebaseCrash;


public class VerifiedFragment extends Fragment {

    private final static String TAG = "VerifiedFragment";
    private final static int REQUEST_CONTACTS_PERMISSION = 2;

    public VerifiedFragment() {
        // Required empty public constructor
    }

    public static VerifiedFragment newInstance() {
        return new VerifiedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verified, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        final Button find = (Button) getActivity().findViewById(R.id.buttonFindFriendsFromContacts);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACTS_PERMISSION);
                } else {
                    findFriends();
                }
            }
        });

        final Button skip = (Button) getActivity().findViewById(R.id.buttonSkipFindFriends);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    private void findFriends() {
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_login, FindFriendsFragment.newInstance())
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        FirebaseCrash.logcat(Log.DEBUG, TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_CONTACTS_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "permission granted");
                    findFriends();
                } else {
                    // TODO: EXPLAIN WHY WE NEED THE PERMISSION
                }
                break;
        }

    }
}
