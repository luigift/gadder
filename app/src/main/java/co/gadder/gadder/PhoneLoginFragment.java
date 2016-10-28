package co.gadder.gadder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.hbb20.CountryCodePicker;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

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

    private String mUserPhone;
    private String mMessage;
    private EditText mPhoneText;
    private ProgressBar mProgressBar;
    private CountryCodePicker mCountryCodePicker;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        BroadcastReceiver receiver = new BroadcastReceiver() {
            @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive");
                if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
                    for (SmsMessage sms : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                        Log.d(TAG, "body: " + sms.getMessageBody());
                        Log.d(TAG, "Sender: " + sms.getOriginatingAddress());
                        Log.d(TAG, "pid: " + sms.getIndexOnIcc());
                        Log.d(TAG, "protocol: " + sms.getProtocolIdentifier());

//                        Uri uri = Uri.parse("content://sms/inbox/");
//                        Cursor c = getActivity().getContentResolver().query(uri, null, null, null, null);
//                        while(c.moveToNext()) {
//                            String pid = c.getString(0);
//                            String body = c.getString(c.getColumnIndex("body"));
//                            if (body.equals(mMessage)) {
//                                getActivity().getContentResolver().delete(Uri.parse(uri+pid),null, null);
//                                return;
//                            }
//                            Log.d(TAG, "PID: " + pid + " body: " + body);
//                        }
//                        c.close();

                        if(mMessage.equals(sms.getMessageBody()) && mUserPhone.equals(sms.getOriginatingAddress())) {
                            Log.d(TAG, "Phone Verified");
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.activity_main, VerifiedFragment.newInstance())
                                    .commit();
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        intentFilter.setPriority(1000);
        getActivity().registerReceiver(receiver, intentFilter);

        mPhoneText = (EditText) getActivity().findViewById(R.id.phoneNumberInput);
        mCountryCodePicker = (CountryCodePicker) getActivity().findViewById(R.id.ccp);
        mCountryCodePicker.setKeyboardAutoPopOnSearch(false);

        mProgressBar = (ProgressBar) getActivity().findViewById(R.id.verificationProgress);
        final Button verify = (Button) getActivity().findViewById(R.id.verifyPhoneButton);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED||
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, REQUEST_SMS_PERMISSION);
                } else {
                    verifyPhone();
                }
            }
        });
    }

    private void animateBar() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            public void run() {
                final int[] progressStatus = new int[]{0};
                while (progressStatus[0] < 100) {
//                    mProgressStatus = doWork();
                    // Update the progress bar
                    handler.post(new Runnable() {
                        public void run() {
                            mProgressBar.setProgress(progressStatus[0]);
                            progressStatus[0] += 1;
                        }
                    });
                    try {
                        Thread.sleep(80);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void verifyPhone() {
        mUserPhone = "+" + mCountryCodePicker.getSelectedCountryCode() + mPhoneText.getText();
        Random r = new Random();
        int code = r.nextInt(8999) + 1000;
        SmsManager sm = SmsManager.getDefault();
        mMessage = "Your gadder verification code: " + code;
        sm.sendTextMessage(mUserPhone, null, mMessage, null, null);
        Log.d("Phone Verifications", "SMS sent to " + mUserPhone + ": " + code);
        animateBar();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_SMS_PERMISSION: {
                verifyPhone();
                break;
            }
        }

    }
}
