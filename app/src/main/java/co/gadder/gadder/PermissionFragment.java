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
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

public class PermissionFragment extends Fragment {

    private final static String TAG ="PermissionFragment";

    public final static String LOCATION ="permission_location";
    public final static String CONTACTS ="permission_contacts";


    private final static String ARG_PERMISSION_TYPE = "permission_type";
    private String mPermissionType;

    public PermissionFragment() {
        // Required empty public constructor
    }

    public static PermissionFragment newInstance(String permissionType) {

        Bundle args = new Bundle();
        args.putString(ARG_PERMISSION_TYPE, permissionType);
        PermissionFragment fragment = new PermissionFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPermissionType = getArguments().getString(ARG_PERMISSION_TYPE);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_permission, container, false);

        final TextView title = (TextView) layout.findViewById(R.id.permissionTitle);
        final Button permission = (Button) layout.findViewById(R.id.permissionButton);
        final TextView content = (TextView) layout.findViewById(R.id.permissionContent);

        switch (mPermissionType) {
            case CONTACTS:

                permission.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!PermissionManager.checkContactsPermission(getContext())) {
                            PermissionManager.requestContactsPermission(getActivity());
                        } else {
                            getFragmentManager().beginTransaction()
                                    .remove(PermissionFragment.this)
                                    .commitAllowingStateLoss();
                        }

                        FirebaseCrash.logcat(Log.DEBUG, TAG, "request Contacts");
                    }
                });

                title.setText(R.string.contacts_permission_title);
                content.setText(R.string.contacts_permission_message);

                break;
            case LOCATION:
                permission.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!PermissionManager.checkLocationPermission(getContext())) {
                            PermissionManager.requestLocationPermission(getActivity());
                        } else {
                            getFragmentManager().beginTransaction()
                                    .remove(PermissionFragment.this)
                                    .commitAllowingStateLoss();
                        }
                    }
                });

                title.setText(R.string.location_permission_title);
                content.setText(R.string.location_permission_message);

                break;
            default:
                throw new RuntimeException("Permission don't exist");
        }

        return layout;
    }
}
