package co.gadder.gadder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class SmsCodeFragment extends Fragment {

    public SmsCodeFragment() {

    }

    public static SmsCodeFragment getInstance() {
        SmsCodeFragment fragment = new SmsCodeFragment();

        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }
}
