package co.gadder.gadder;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.media.MediaPlayer;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

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

        final TextView terms = (TextView) layout.findViewById(R.id.termsOfService);
        final TextView policy = (TextView) layout.findViewById(R.id.privacyPolicy);

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .addToBackStack("termsOfService")
                        .add(R.id.activity_login, TextFragment.newInstance(TextFragment.TERMS_OF_SERVICE))
                        .commit();
            }
        });

        policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .addToBackStack("termsOfService")
                        .add(R.id.activity_login, TextFragment.newInstance(TextFragment.PRIVACY_POLICY))
                        .commit();
            }
        });

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        final ImageView gadder = (ImageView) getActivity().findViewById(R.id.gadderLogo);
        gadder.setAlpha(0f);
        final VideoView gadderVideo = (VideoView) getActivity().findViewById(R.id.videoIntro);

        final ObjectAnimator fadeOut = ObjectAnimator.ofFloat(gadderVideo, "alpha",  1f, 0f);
        fadeOut.setDuration(2000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(gadder, "alpha", 0f, 1f);
        fadeIn.setDuration(2000);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeOut).after(fadeIn);

        String path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.intro;
        gadderVideo.setVideoURI(Uri.parse(path));
        gadderVideo.start();
        gadderVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mAnimationSet.start();
            }
        });


//        RotateAnimation anim = new RotateAnimation(0f, 0f, 0f, 0f);
//        anim.setInterpolator(new LinearInterpolator());
//        anim.setRepeatCount(Animation.INFINITE);
//        anim.setDuration(700);
//        gadder.startAnimation(anim);

//       RotateAnimation anim = new RotateAnimation(0f, 0f, 0f, 0f);
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

        int colorFrom = Color.WHITE;
        int colorTo = Color.parseColor("#935aa4"); //Color.parseColor("#8e67a4");
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);//colorMedium, colorTo);
        colorAnimation.setDuration(5000); // milliseconds
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
                .delay(7000)
                .playOn(signIn);
    }
}