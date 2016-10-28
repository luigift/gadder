package co.gadder.gadder;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class LoginFragment extends android.app.Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "LoginFragment";

    // UI elements
    private Button signUp;
    private Button signIn;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_login, container, false);
        signIn = (Button) layout.findViewById(R.id.signIn);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager()
                        .beginTransaction()
                        .addToBackStack("signIn")
                        .replace(R.id.activity_main, SignInFragment.newInstance())
                        .commit();
            }
        });

        signUp = (Button) layout.findViewById(R.id.signUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager()
                        .beginTransaction()
                        .addToBackStack("signIn")
                        .replace(R.id.activity_main, SignUpFragment.newInstance())
                        .commit();
            }
        });
        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }
}