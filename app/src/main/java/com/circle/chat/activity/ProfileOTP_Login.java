package com.circle.chat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.circle.chat.R;
import com.circle.chat.model.CategoryModel;
import com.circle.chat.utilities.SessionManager;
import com.circle.chat.databinding.ActivityProfileOtpLoginBinding;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProfileOTP_Login extends AppCompatActivity {
    private ActivityProfileOtpLoginBinding binding;
    private FirebaseAuth mauth;
    private String mVerificationId;
    private Intent intent;
    private List<CategoryModel> categoryList;
    private MaterialAlertDialogBuilder builder;
    private AlertDialog dialog;
    FirebaseDatabase database;
    private SessionManager sessionManager;
    private boolean update = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileOtpLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        intent = getIntent();
        if (intent.getExtras() != null) {
            Bundle args = intent.getBundleExtra("BUNDLE");
            categoryList = (List<CategoryModel>) args.getSerializable("category_list");
            Log.v("Category", "checkedvalues: " + categoryList.size());
        }

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        // loggin - checking - start
        sessionManager = new SessionManager(this);
        database = FirebaseDatabase.getInstance();
        // Checks if user is already logged in or not.
        mauth = FirebaseAuth.getInstance();
        FirebaseUser user = mauth.getCurrentUser();

        Log.v("user", "user_: " + user);
        if (user != null) { // TODO: user != null
            Intent i = getIntent();
            if (i.getExtras() == null) { // ie. user is already logged in and just opening up the app.
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("app_open", "app_open");

                startActivity(intent);
                finish();
            }
            else {  // ie. user is already logged in and wants to update the list of category again.
               /* update = i.getBooleanExtra("screen", false);
                Bundle args = i.getBundleExtra("BUNDLE");
                checkedValues = (List<CategoryModel>) args.getSerializable("category_list");
                Log.v("Category", "checkedvalues: " + checkedValues.size());*/
            }
        }

        //loggedin - check end
/*
        if (user != null) { // TODO: user != null
            Intent i = getIntent();
            if (i.getExtras() == null) { // ie. user is already logged in and just opening up the app.
                Intent intent = new Intent(this, MyCommunity.class);
                Bundle args = new Bundle();
                args.putSerializable("category_list", (Serializable) sessionManager.getArrayList("my_community"));
                intent.putExtra("BUNDLE", args);

                Log.v("Chat", "categoryactv: " + sessionManager.getCategorySelected());
                startActivity(intent);
                finish();
            }
            else {  // ie. user is already logged in and wants to update the list of category again.
                update = i.getBooleanExtra("screen", false);
                Bundle args = i.getBundleExtra("BUNDLE");
                checkedValues = (List<CategoryModel>) args.getSerializable("category_list");
                Log.v("Category", "checkedvalues: " + checkedValues.size());
            }
        }
*/

        // loggin checking - end



        // OTP Login support is added.
        mauth = FirebaseAuth.getInstance();

        /*// Checks if user is already logged in or not.
        FirebaseUser user = mauth.getCurrentUser();
        Log.v("user", "user_: " + user);
        if (user != null) { // TODO: user != null
            startActivity(new Intent(ProfileOTP_Login.this, CategoryActivity.class));
        }
        else {
            //  startActivity(new Intent(ProfileOTP_Login.this, UserSetupScreen.class));
        }*/

        binding.countrycodeSpinner.registerCarrierNumberEditText(binding.mobileNoBox); // attaches the ccp spinner with the edittext

        binding.sendOtpBtn.setOnClickListener(v -> {
            String mobileString = binding.mobileNoBox.getText().toString().trim();
            if(mobileString.isEmpty() /*|| mobileString.length() < 10*/) {
                binding.mobileNoBox.setError("Enter a valid mobile number");
                binding.mobileNoBox.requestFocus();
                return;
            }
            else {
                binding.mobileNoBox.setError(null);
                //getting mobile number from user entered flow and passing it to verification
                showDialog(); // shows the loading dialog
                phone_verification(mobileString);
            }

        });

/*
        binding.continueBtn.setOnClickListener(v -> {
            String edit_otp = binding.otpBox.getText().toString();
            if(edit_otp.isEmpty() || edit_otp.length() < 6)
            {
                binding.otpBox.setError("Enter valid code");
                binding.otpBox.requestFocus();
            }
            verify_VerificationCode(edit_otp); // use the otp to verify and login.
        });
*/
    }


    /**This function is used to verify the mobile number
     * of the user entered.
     * @param mobile
     * @return void
     */
    private void phone_verification(String mobile) {
        Log.v("mobile", "mobile: " + binding.countrycodeSpinner.getFullNumberWithPlus());
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                binding.countrycodeSpinner.getFullNumberWithPlus(),
                60,
                TimeUnit.SECONDS,
                ProfileOTP_Login.this,
                callbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    //Getting the code sent by SMS
                    String code = phoneAuthCredential.getSmsCode();

                    //sometime the code is not detected automatically
                    //in this case the code will be null
                    //so user has to manually enter the code
                    if (code != null) {
                        binding.otpBox.setText(code);

                        //verifying the code
                        verify_VerificationCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    // here in onsuccess -> dismiss the Dialog box.
                    dismissDialog();
                    Toast.makeText(ProfileOTP_Login.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    mVerificationId = verificationId; // type: String
                    verify_VerificationCode("123456");  // todo: remove this line later as its just for testing...

                }

            };

    /**This function takes the otp code as parameter and verifies it.
     * @param code
     */
    private void verify_VerificationCode(String code) {
        //credenials created
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        //user signing in
        signInWithPhoneAuthCredential(credential);
    }

    /**
     * Verifying otp and if success moving the user to next screen.
     * @param mcredential
     */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential mcredential) {
        mauth.signInWithCredential(mcredential).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // here in onsuccess -> dismiss the Dialog box.
                            dismissDialog();

                            // get id here and send that to main activity.
                            Intent intent = new Intent(ProfileOTP_Login.this, UserSetupScreen.class);
                            startActivity(intent);
                        }
                        else if (task.isCanceled()){
                            // here in onsuccess -> dismiss the Dialog box.
                            dismissDialog();
                            Toast.makeText(ProfileOTP_Login.this, "Something went wrong: ", Toast.LENGTH_SHORT).show();
                        }
                    }});
    }


    public void showDialog() {
        builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View customLayout = inflater.inflate(R.layout.dialog_layout, null);
        ImageView imageView = customLayout.findViewById(R.id.icon);
        imageView.animate().rotation(3600).setDuration(60000).start();  // icon rotating
        builder.setView(customLayout)
//                .setPositiveButton("Ok", /* listener = */ null)
//                .setNegativeButton("Cancel", /* listener = */ null)
                .setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corner_bg); // show rounded corner for the dialog
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.

        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }





}