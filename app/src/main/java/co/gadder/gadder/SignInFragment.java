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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.crash.FirebaseCrash;

public class SignInFragment extends Fragment {

    public SignInFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "SingUpFragment";

    private FirebaseAuth mAuth;

    // UI elements
    private ImageButton nextButton;
    private EditText emailEditText;
    private EditText passwordEditText;

    private LoginActivity activity;

    public static SignInFragment newInstance() {
        SignInFragment fragment = new SignInFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCreate");
        super.onCreate(savedInstanceState);
        activity = (LoginActivity) getActivity();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCreateView");
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_sign_in, container, false);
        nextButton = (ImageButton) layout.findViewById(R.id.next);
        emailEditText = (EditText) layout.findViewById(R.id.email);
        passwordEditText = (EditText) layout.findViewById(R.id.password);
        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        setNextButtonBehavior();
    }

    private void setNextButtonBehavior() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser();
//                if (checkUserInput()) {
//                    signInUser();
//                }
            }
        });
    }

    private static int MIN_PASSWORD = 6;

    private Boolean checkUserInput() {
        if (passwordEditText.getText().length() <= MIN_PASSWORD) {
            Toast.makeText(getActivity(), getString(R.string.short_password), Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    private void signInUser() {
        final String email = emailEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirebaseCrash.logcat(Log.DEBUG, TAG, e.toString());
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(getActivity(), "Wrong Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}