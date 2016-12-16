package co.gadder.gadder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SmsCodeFragment extends Fragment {

    public static final String TAG = "SmsCodeFragment";
    private static final String ARG_PHONE = "arg_phone";

    public SmsCodeFragment() {

    }

    private BroadcastReceiver receiver;

    private int mCode;
    private String mPhone;
    private String mEmail;
    private String mMessage;
    private String mPassword;
    private String mPhoneParsed;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public static SmsCodeFragment getInstance(String phone) {
        SmsCodeFragment fragment = new SmsCodeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PHONE, phone);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mPhone = getArguments().getString(ARG_PHONE);
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set user login info
        mPhoneParsed = mPhone.replaceAll("\\D+", "");
        mEmail = mPhoneParsed + "@gadder.co";
        mPassword = mPhone;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sms_code, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setSmsReceiver();
        }

        final EditText code1 = (EditText) getActivity().findViewById(R.id.code1);
        final EditText code2 = (EditText) getActivity().findViewById(R.id.code2);
        final EditText code3 = (EditText) getActivity().findViewById(R.id.code3);
        final EditText code4 = (EditText) getActivity().findViewById(R.id.code4);

        // Open keyboard
        code1.requestFocus();
        InputMethodManager imgr =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        // Set EditText jump behavior
        code1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    code2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        code2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    code3.requestFocus();
                } else {
                    code1.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        code3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    code4.requestFocus();
                } else {
                    code2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        code4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    code3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        // set enter button to behave as verify
        code4.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)) {
                    String enteredCode
                            = code1.getText().toString()
                            + code2.getText().toString()
                            + code3.getText().toString()
                            + code4.getText().toString();

                    if (enteredCode.equals(String.valueOf(mCode))) {
                        createUserOrSignIn();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                                .setPositiveButton(R.string.try_again, null)
                                .setTitle(R.string.wrong_code_title)
                                .setMessage(R.string.wrong_code_message);
                        builder.create().show();
                    }
                }

                return false;
            }
        });

        TextView phoneBeingVerified = (TextView) getActivity().findViewById(R.id.phoneBeingVerified);
        phoneBeingVerified.setText(mPhone);

        final Button verify = (Button) getActivity().findViewById(R.id.verifyPhoneButton);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredCode
                        = code1.getText().toString()
                        + code2.getText().toString()
                        + code3.getText().toString()
                        + code4.getText().toString();

                if (enteredCode.equals(String.valueOf(mCode))) {
                    createUserOrSignIn();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setPositiveButton(R.string.try_again, null)
                            .setTitle(R.string.wrong_code_title)
                            .setMessage(R.string.wrong_code_message);
                    builder.create().show();
                }
            }
        });

        verifyPhone();
        animateBar();

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setSmsReceiver() {

        receiver = new BroadcastReceiver() {
            @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onReceive(Context context, Intent intent) {
                FirebaseCrash.logcat(Log.DEBUG, TAG, "onReceive");
                if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
                    for (SmsMessage sms : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "body: " + sms.getMessageBody());
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "Sender: " + sms.getOriginatingAddress());
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "pid: " + sms.getIndexOnIcc());
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "protocol: " + sms.getProtocolIdentifier());

                        if(mMessage.equals(sms.getMessageBody()) && mPhone.equals(sms.getOriginatingAddress())) {
                            createUserOrSignIn();
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        intentFilter.setPriority(1000);
        getActivity().registerReceiver(receiver, intentFilter);
    }

    private void animateBar() {
        final ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.verificationProgress);
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            public void run() {
                final int[] progressStatus = new int[]{0};
                while (progressStatus[0] < 100) {
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus[0]);
                            progressStatus[0] += 1;
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void verifyPhone() {
        Random r = new Random();
        mCode = r.nextInt(8999) + 1000;
//        Toast.makeText(getActivity(), "code: " + mCode, Toast.LENGTH_SHORT).show(); // TODO remove
        mMessage = "Your gadder verification code: " + mCode;
        SmsManager sm = SmsManager.getDefault();
        sm.sendTextMessage(mPhone, null, mMessage, null, null); //TODO remove comment
        animateBar();
    }

    private void createUserOrSignIn() {
        getActivity().unregisterReceiver(receiver);

        mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "signInWithCustomToken:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            FirebaseCrash.logcat(Log.DEBUG, TAG, "SignIn failed");
                            FirebaseCrash.report(task.getException());
                        } else {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {

                                FirebaseCrash.logcat(Log.DEBUG, TAG, "User existent");

                                Map<String, Object> childUpdates = new HashMap<>();

                                // Register user number
                                childUpdates.put(Constants.USER_PHONE + "/" + mPhoneParsed + "/", user.getUid());

                                // Register user token
                                String token = FirebaseInstanceId.getInstance().getToken();
                                if (token == null || token.isEmpty()) {
                                    SharedPreferences pref =
                                            getActivity().getSharedPreferences(
                                                    getString(R.string.preference_file_key),
                                                    Context.MODE_PRIVATE);
                                    token = pref.getString(getString(R.string.token), null);
                                }
                                if (token != null) {
                                    childUpdates.put(Constants.USER_TOKEN + "/" + user.getUid() + "/", token);
                                }

                                // Create user profile
                                Friend friend = new Friend();
                                childUpdates.put(Constants.USERS + "/" + user.getUid() + "/", friend);

                                // Add founders
                                childUpdates.put(Constants.USER_FRIENDS + "/" + user.getUid() + "/" + user.getUid() + "/", true); // self
                                childUpdates.put(Constants.USER_FRIENDS + "/" + user.getUid() + "/" + "nJnFV13qbrZ7LdB51nMC08LcTM23" + "/", true); // luigi
                                childUpdates.put(Constants.USER_FRIENDS + "/" + user.getUid() + "/" + "31wCZvy0RfYWAsz18PoyWLOyBAE2" + "/", true); // lucas

                                FirebaseCrash.logcat(Log.DEBUG, TAG, "User set up");

                                mDatabase
                                        .child(Constants.VERSION)
                                        .updateChildren(childUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseCrash.logcat(Log.DEBUG, TAG, "User successfully created");
                                                goToMainActivity();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                FirebaseCrash.logcat(Log.DEBUG, TAG, "Error creating user");
                                                FirebaseCrash.report(e);
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            mAuth.signInWithEmailAndPassword(mEmail, mPassword)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            goToMainActivity();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            FirebaseCrash.logcat(Log.DEBUG, TAG, "exception: " + e);
                                        }
                                    });
                        }
                    }
                });
    }

    private void goToMainActivity() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
