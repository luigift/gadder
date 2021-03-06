package co.gadder.gadder;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.hbb20.CountryCodePicker;

import static co.gadder.gadder.Config.FORMAT_WHILE_TYPING;
import static co.gadder.gadder.Config.OPEN_KEYBOARD;

public class PhoneLoginFragment extends Fragment {
    private final static String TAG = "PhoneLoginFragment";

    private final int REQUEST_SMS_PERMISSION = 1;

    private final static String PHONE_HINT = "1123546532";

    // Release Switches

    private PhoneNumberFormattingTextWatcher phoneNumberWatcher;

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

        SharedPreferences pref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // Set saved country code if there is
        String savedCountryCode = pref.getString(getString(R.string.country_code), null);
        if (savedCountryCode != null) {
            mCountryCodePicker.setCountryForPhoneCode(Integer.valueOf(savedCountryCode));
        }

        // Set saved number if there is
        String savedPhone = pref.getString(getString(R.string.phone), null);
        if (savedPhone != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mPhoneText.setText(PhoneNumberUtils.formatNumber(savedPhone, mCountryCodePicker.getSelectedCountryNameCode()));
            } else {
                mPhoneText.setText(savedPhone);
            }
        }

        // Format phone as it is being typed
        if (FORMAT_WHILE_TYPING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                phoneNumberWatcher = new PhoneNumberFormattingTextWatcher(mCountryCodePicker.getSelectedCountryNameCode());
                mPhoneText.addTextChangedListener(phoneNumberWatcher);

                mCountryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
                    @Override
                    public void onCountrySelected() {
                        mPhoneText.removeTextChangedListener(phoneNumberWatcher);
                        phoneNumberWatcher = new PhoneNumberFormattingTextWatcher(mCountryCodePicker.getSelectedCountryNameCode());
                        mPhoneText.addTextChangedListener(phoneNumberWatcher);
                        mPhoneText.setHint(PhoneNumberUtils.formatNumber(PHONE_HINT, mCountryCodePicker.getSelectedCountryNameCode()));
                    }
                });
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String hintPhone = PhoneNumberUtils.formatNumber(PHONE_HINT, mCountryCodePicker.getSelectedCountryNameCode());
            mPhoneText.setHint(hintPhone);
        }

        // Open keyboard
        if (OPEN_KEYBOARD) {
            mPhoneText.requestFocus();
            InputMethodManager imgr =
                    (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
            mPhoneText.clearFocus();

        // Verify Buttons
        final Button verify = (Button) getActivity().findViewById(R.id.sendSmsButton);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionAndVerify();
            }
        });
        // Enter button behave as send sms button
        mPhoneText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)) {
                    checkPermissionAndVerify();
                }
                return false;
            }
        });
    }

    private void checkPermissionAndVerify() {
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, REQUEST_SMS_PERMISSION);
        } else {
            startVerification();
        }
    }

    private void startVerification() {
        String country_code = mCountryCodePicker.getSelectedCountryCode();
        final String phone = "+" + country_code + mPhoneText.getText();

        SharedPreferences pref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(getString(R.string.phone), mPhoneText.getText().toString());
        editor.putString(getString(R.string.country_code), country_code);
        editor.apply();

        getFragmentManager().beginTransaction()
                .addToBackStack("SmsCode")
                .replace(R.id.activity_login, SmsCodeFragment.getInstance(phone))
                .commitAllowingStateLoss();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_SMS_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    FirebaseCrash.logcat(Log.DEBUG, TAG, "Got SMS permission");
                    FirebaseCrash.log("Got SMS permission");
                    startVerification();

                } else {
                    // TODO: EXPLAIN WHY WE NEED THE PERMISSION
                }
                break;
            }
        }

    }
}
