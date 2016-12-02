package co.gadder.gadder;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

public class PermissionManager {

    public static final int REQUEST_CAMERA_PERMISSION = 11;
    public static final int REQUEST_STORAGE_PERMISSION = 12;
    public static final int REQUEST_CONTACTS_PERMISSION = 13;
    public static final int REQUEST_LOCATION_PERMISSION = 14;

    /*
     *  Check methods
     */
    @NonNull
    public static Boolean checkCameraPermission(Context context) {
        return ActivityCompat
                .checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    public static Boolean checkStoragePermission(Context context) {
        return ActivityCompat
                .checkSelfPermission(
                        context,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    public static Boolean checkContactsPermission(Context context) {
        return ActivityCompat
                .checkSelfPermission(
                        context,
                        android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    public static Boolean checkLocationPermission(Context context) {
        return ActivityCompat
                .checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    /*
     *  Request methods
     */
    @NonNull
    public static void requestCameraPermission (Activity activity) {
        ActivityCompat
                .requestPermissions(
                        activity,
                        new String[] {Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
    }

    @NonNull
    public static void requestStoragePermission (Activity activity) {
        ActivityCompat
                .requestPermissions(
                        activity,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
    }

    @NonNull
    public static void requestLocationPermission (Activity activity) {
        ActivityCompat
                .requestPermissions(
                        activity,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
    }

    @NonNull
    public static void requestContactsPermission (Activity activity) {
        ActivityCompat
                .requestPermissions(
                        activity,
                        new String[] {Manifest.permission.WRITE_CONTACTS},
                        REQUEST_CONTACTS_PERMISSION);
    }


}
