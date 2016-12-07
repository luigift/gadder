package co.gadder.gadder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class EditFragment extends Fragment {

    private static final String TAG = "EditFragment";
    private static final int PICK_IMAGE_REQUEST_CODE = 1;

    private Friend user;

    public EditFragment() {}

    public static EditFragment newInstance(Friend user) {
        EditFragment fragment = new EditFragment();
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

        user = ((MainActivity) getActivity()).user;

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
            Picasso.with(getActivity())
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
            String photoPath = getPath(imageUri);
            Bitmap userBitmap = BitmapFactory.decodeFile(photoPath);
            if (userBitmap != null) {

                try {
                    ExifInterface ei = new ExifInterface(photoPath);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            userBitmap = Constants.rotateImage(userBitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            userBitmap = Constants.rotateImage(userBitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            userBitmap = Constants.rotateImage(userBitmap, 270);
                            break;
                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            break;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Rotation Exception: " + e);
                }

                final CircleImageView editImage = (CircleImageView) getActivity().findViewById(R.id.editUserImage);
                editImage.setImageBitmap(userBitmap);

                updateUserImage(userBitmap);
            }
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
//        final String timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";

        final StorageReference pictureRef =
                activity.mStorage.child("image/" + user.id + ".jpg");
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
                            .child(user.id)
                            .child("pictureUrl")
                            .setValue(downloadUrl.toString());
                } else {
                    Toast.makeText(activity, "Error uploading image", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
