package co.gadder.gadder;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class EditFragment extends Fragment {

    private static final String TAG = "EditFragment";
    private static final int PICK_IMAGE_REQUEST_CODE = 1;

    private Friend user;

    public EditFragment(){}

    public EditFragment(Friend user) {
        this.user = user;
    }

    public static EditFragment newInstance(Friend user) {
        EditFragment fragment = new EditFragment(user);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final TextView editName = (TextView) getActivity().findViewById(R.id.editUserName);
        final CircleImageView editImage = (CircleImageView) getActivity().findViewById(R.id.editUserImage);

        FrameLayout layout = (FrameLayout) getActivity().findViewById(R.id.editLayout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .remove(EditFragment.this)
                        .commit();
            }
        });

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                } else {
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                    startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST_CODE);

                }
            }
        });

        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ((MainActivity)getActivity()).mDatabase
                        .child(Constants.VERSION)
                        .child(Constants.USERS)
                        .child(user.id)
                        .child("name")
                        .setValue(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editName.setText(user.name);
        if (user.pictureUrl != null && !user.pictureUrl.isEmpty()) {
            Glide.with(getActivity())
                    .load(user.pictureUrl)
                    .into(editImage);
        } else {
            ImageView cameraButton = (ImageView) getActivity().findViewById(R.id.editCameraButton);
            cameraButton.setColorFilter(R.color.colorAccent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Toast.makeText(getActivity(), "onActivityResult", Toast.LENGTH_SHORT).show();
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            Uri imageUri = data.getData();
            Log.d(TAG, "data: " + imageUri.toString());
            String filePath = getPath(imageUri);
            Bitmap userImage = BitmapFactory.decodeFile(filePath);
            final CircleImageView editImage = (CircleImageView) getActivity().findViewById(R.id.editUserImage);
            editImage.setImageBitmap(userImage);

            if (userImage != null)
                updateUserImage(userImage);
        }
    }

    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity()
                .getContentResolver()
                .query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }

    private void updateUserImage(Bitmap bitmap) {
        final MainActivity activity = (MainActivity) getActivity();
        final String uid = activity.mAuth.getCurrentUser().getUid();
//        final String timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";

        final StorageReference pictureRef =
                activity.mStorage.child("image/" + uid + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        final byte[] thumbData = baos.toByteArray();
        final UploadTask uploadThumb = pictureRef.putBytes(thumbData);
        uploadThumb.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if (downloadUrl != null) {
                    activity.mDatabase
                            .child(Constants.VERSION)
                            .child(Constants.USERS)
                            .child(uid)
                            .child("pictureUrl")
                            .setValue(downloadUrl.toString());
                } else {
                    Toast.makeText(activity, "Error uploading image", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
