package co.gadder.gadder;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

public class InputActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "InputActivity";

    GoogleApiClient mGoogleApiClient;

    public Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseCrash.log(TAG + " created");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        buildGoogleApiClient();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_input, InputFragment.newInstance())
                .commit();
    }

    public void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        FirebaseCrash.log(TAG + " onConnected");
        if (PermissionManager.checkLocationPermission(InputActivity.this)) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                InputFragment frag = (InputFragment) getSupportFragmentManager().findFragmentById(R.id.activity_input);
                frag.setLocation(mLastLocation);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
