package co.gadder.gadder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PermissionFragment extends Fragment {

    private final static String TAG ="PermissionFragment";

    private final static int REQUEST_PHONE_PERMISSIONS = 45;

    public PermissionFragment() {
        // Required empty public constructor
    }

    public static PermissionFragment newInstance() {
        return new PermissionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_permission, container, false);

        // Set button behavior
        Button permission = (Button) layout.findViewById(R.id.permissionButton);
        permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions();
            }
        });

        return layout;
    }

    private void requestPermissions() {
        Log.d(TAG, "requestPermissions");

        String[] permissions
                = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.BATTERY_STATS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat
                .requestPermissions(
                        getActivity(),
                        permissions,
                        REQUEST_PHONE_PERMISSIONS);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PHONE_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission granted");
                    getFragmentManager().beginTransaction()
                            .replace(R.id.activity_main, FriendsActivityFragment.newInstance(), "activities")
                            .commit();
                } else {
                    // TODO: EXPLAIN WHY WE NEED THE PERMISSION
                }
                break;
        }
    }
}
