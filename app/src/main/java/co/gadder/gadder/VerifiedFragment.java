package co.gadder.gadder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


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
//        Uri uri = Uri.parse("content://sms/inbox/8549");
//        Cursor c = getActivity().getContentResolver().query(uri, null, null, null, null);
//        while(c.moveToNext()) {
//            String pid = c.getString(0);
//            String body = c.getString(c.getColumnIndex("body"));
//            String u = "content://sms/" + pid;
//            Log.d(TAG, "PID: " + pid + " body: " + body);
//        }
//        c.close();
    }

    private void findFriends() {
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_main, FindFriendsFragment.newInstance())
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_CONTACTS_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission granted");
                    findFriends();
                } else {
                    // TODO: EXPLAIN WHY WE NEED THE PERMISSION
                }
                break;
        }

    }
}
