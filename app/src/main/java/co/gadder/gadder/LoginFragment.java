package co.gadder.gadder;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.androidanimations.library.attention.RubberBandAnimator;

import static co.gadder.gadder.R.id.textView;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "LoginFragment";

    // UI elements
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

        final ImageView gadder = (ImageView) getActivity().findViewById(R.id.gadderLogo);

//        RotateAnimation anim = new RotateAnimation(0f, 0f, 0f, 0f);
//        anim.setInterpolator(new LinearInterpolator());
//        anim.setRepeatCount(Animation.INFINITE);
//        anim.setDuration(700);
//        gadder.startAnimation(anim);

        final LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.loginLayout);
//        layout.animate()


//        gadder.animate()
//                .rotation(360f)
//                .setInterpolator(new LinearInterpolator())
//                .translationY(1000f)
//                .scaleX(2f)
//                .scaleY(2f)
//                .x(0f)
//                .y(0f)
//                .setDuration(1000)
//                .setListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animator) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animator) {
//                        gadder.clearAnimation();
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animator) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animator) {
//
//                    }
//                })
//                .start();

        int colorFrom = Color.BLACK;
        int colorMedium = Color.blue(100);
        int colorTo = Color.WHITE;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorMedium, colorTo);
        colorAnimation.setDuration(2000); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int color = (int) animator.getAnimatedValue();
                layout.setBackgroundColor(color);
            }
        });
        colorAnimation.start();


        YoYo.with(Techniques.Pulse)
                .duration(700)
                .delay(1000)
                .playOn(signIn);
    }
}