package co.gadder.gadder;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.hbb20.CountryCodePicker;

public class PhoneLoginFragment extends Fragment {
    private final static String TAG = "PhoneLoginFragment";

    private final int REQUEST_SMS_PERMISSION = 1;

    public PhoneLoginFragment() {
        // Required empty public constructor
    }

    public static PhoneLoginFragment newInstance() {
        return new PhoneLoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TODO: CHECK CARRIER CONNECTION

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phone_login, container, false);
    }

    private EditText mPhoneText;
    private CountryCodePicker mCountryCodePicker;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPhoneText = (EditText) getActivity().findViewById(R.id.phoneNumberInput);
        mCountryCodePicker = (CountryCodePicker) getActivity().findViewById(R.id.ccp);
        mCountryCodePicker.setKeyboardAutoPopOnSearch(false);

        // Open keyboard
        mPhoneText.requestFocus();
        InputMethodManager imgr =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        final Button verify = (Button) getActivity().findViewById(R.id.sendSmsButton);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED||
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, REQUEST_SMS_PERMISSION);
                } else {
                    startVerification();
                }
            }
        });
    }

    private void startVerification() {
        String country_code = mCountryCodePicker.getSelectedCountryCode();
        final String phone = "+" + country_code + mPhoneText.getText();

        SharedPreferences pref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(getString(R.string.phone), phone);
        editor.putString(getString(R.string.country_code), country_code);
        editor.commit();

        getFragmentManager().beginTransaction()
                .addToBackStack("SmsCode")
                .replace(R.id.activity_login, SmsCodeFragment.getInstance(phone))
                .commit();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_SMS_PERMISSION: {
                startVerification();
                break;
            }
        }

    }
}
