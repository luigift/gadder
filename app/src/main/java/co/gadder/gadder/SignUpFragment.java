package co.gadder.gadder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpFragment extends Fragment {

    public SignUpFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "SingUpFragment";


    // UI elements
    private ImageButton nextButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText nameEditText;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_sign_up, container, false);
        nextButton = (ImageButton) layout.findViewById(R.id.next);
        emailEditText = (EditText) layout.findViewById(R.id.email);
        nameEditText = (EditText) layout.findViewById(R.id.firstName);
        passwordEditText = (EditText) layout.findViewById(R.id.password);
        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setNextButtonBehavior();
    }

    private void setNextButtonBehavior() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkUserInput()) {
                    createOrSignInUser();
                }
            }
        });
    }

    private static int MIN_NAME = 1;
    private static int MIN_PASSWORD = 6;

    private Boolean checkUserInput() {
        if (nameEditText.getText().length() <= MIN_NAME) {
            Toast.makeText(getActivity(), getString(R.string.short_name), Toast.LENGTH_LONG).show();
            return false;
        } else if (passwordEditText.getText().length() <= MIN_PASSWORD) {
            Toast.makeText(getActivity(), getString(R.string.short_password), Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    private void createOrSignInUser() {
        final String email = emailEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        final String fullName = nameEditText.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (task.isSuccessful()) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User updated.");
                                            }
                                        }
                                    });
                            writeNewUser(user.getUid(), fullName);
                        }
                    }
                }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
                if(e instanceof FirebaseAuthUserCollisionException) {
                    getFragmentManager()
                            .beginTransaction()
                            .addToBackStack("singUp")
                            .replace(R.id.activity_main, SignInFragment.newInstance())
                            .commit();

                    Toast.makeText(getActivity(), "You already have an account", Toast.LENGTH_LONG).show();
                }
            }
        });
        Toast.makeText(getActivity(), "Logging In...", Toast.LENGTH_SHORT).show();
    }

    private void writeNewUser(String userId, String name) {
        mDatabase
                .child(Constants.VERSION)
                .child(Constants.USERS)
                .child(userId)
                .child("name").setValue(name);
    }
}