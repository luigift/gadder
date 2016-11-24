package co.gadder.gadder;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.androidanimations.library.attention.RubberBandAnimator;

public class LoginFragment extends Fragment {

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
                        .replace(R.id.activity_login, PhoneLoginFragment.newInstance())
                        .commit();
            }
        });

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        YoYo.with(Techniques.Pulse)
                .duration(700)
                .delay(1000)
                .playOn(signIn);
    }
}