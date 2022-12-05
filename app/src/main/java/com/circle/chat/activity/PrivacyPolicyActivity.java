package com.circle.chat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;

import com.circle.chat.R;
import com.circle.chat.databinding.ActivityPrivacyPolicyBinding;
import com.circle.chat.utilities.SessionManager;

public class PrivacyPolicyActivity extends AppCompatActivity {
    private ActivityPrivacyPolicyBinding binding;
    private SessionManager sessionManager;
    private boolean isPrivacyChecked = false, isTermsChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.purple_500));
        }

        binding.privacyCheckbox.setMovementMethod(LinkMovementMethod.getInstance());  // When you need to show custom text rather than link to user.
        binding.termsCheckbox.setMovementMethod(LinkMovementMethod.getInstance());  // When you need to show custom text rather than link to user.

        if (sessionManager.isAccepted()) {
            Intent intent = new Intent(this, ProfileOTP_Login.class);
            startActivity(intent);
            finish();
        }

        binding.startBtn.setOnClickListener(v -> {
            if (binding.privacyCheckbox.isChecked() && binding.termsCheckbox.isChecked()) {
                sessionManager.setAccepted(true);
                Intent intent = new Intent(PrivacyPolicyActivity.this, ProfileOTP_Login.class);
                startActivity(intent);
                finish();
            }
            else {
                binding.startBtn.setBackground(getResources().getDrawable(R.drawable.disabled_button));
             //   binding.startBtn.setEnabled(false);
            }
        });

        binding.privacyCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked)
                    isPrivacyChecked = true;
                else
                    isPrivacyChecked = false;

                if (isPrivacyChecked && isTermsChecked) {
                    binding.startBtn.setBackground(getResources().getDrawable(R.drawable.send_otp_svg));
                }
                else {
                    binding.startBtn.setBackground(getResources().getDrawable(R.drawable.disabled_button));
                }

            }
        });

        binding.termsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

                if (checked)
                    isTermsChecked = true;
                else
                    isTermsChecked = false;

                if (isPrivacyChecked && isTermsChecked) {
                    binding.startBtn.setBackground(getResources().getDrawable(R.drawable.send_otp_svg));
                }
                else {
                    binding.startBtn.setBackground(getResources().getDrawable(R.drawable.disabled_button));
                }
            }
        });


    }
}