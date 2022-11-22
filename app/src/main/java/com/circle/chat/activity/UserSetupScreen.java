package com.circle.chat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.circle.chat.R;
import com.circle.chat.databinding.ActivityUserSetupScreenBinding;
import com.circle.chat.model.CategoryModel;
import com.circle.chat.model.User;
import com.circle.chat.utilities.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class UserSetupScreen extends AppCompatActivity {
    ActivityUserSetupScreenBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;
    private String category_value;
    private SessionManager sessionManager;
    private List<CategoryModel> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserSetupScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating profile...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        Random random = new Random();
        int value = ThreadLocalRandom.current().nextInt(100, 1000 + 1);
        binding.nameBox.setText("HeShe#User" + value);  // setting random username
        binding.imageViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 45);
            }
        });

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.nameBox.getText().toString();

                if(name.isEmpty()) {
                    binding.nameBox.setError("Please type a name");
                    return;
                }

                dialog.show();
                if(selectedImage != null) {
                    StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();

                                        String uid = auth.getUid();
                                        String phone = auth.getCurrentUser().getPhoneNumber();
                                        String name = binding.nameBox.getText().toString();
                                        String n = sessionManager.getLoggedInUsername();
                                        if (sessionManager.getLoggedInUsername().equalsIgnoreCase("")) // Adding username who logged-in into the session manager.
                                            sessionManager.setLoggedInUsername(name);

                                        User user = new User(uid, name, phone, imageUrl);
                                        sessionManager.setUserModel(user, "loggedIn_UserModel");
                                        database.getReference()
                                                .child("users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        dialog.dismiss();
                                                        Intent intent = new Intent(UserSetupScreen.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });

                                        Intent intent = new Intent(UserSetupScreen.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                } else { // here code for uploading data in firebase database...
                    String uid = auth.getUid();
                    String phone = auth.getCurrentUser().getPhoneNumber();

                    String n = sessionManager.getLoggedInUsername();
                    User user = new User(uid, name, phone, "No Image");
                    sessionManager.setUserModel(user, "loggedIn_UserModel");
                    if (sessionManager.getLoggedInUsername().equalsIgnoreCase(""))
                        sessionManager.setLoggedInUsername(name);   // Adding username who logged-in into the session manager.

                            database.getReference()
                                    .child("users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(UserSetupScreen.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });

                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if(data.getData() != null) {
                Uri uri = data.getData(); // filepath
                FirebaseStorage storage = FirebaseStorage.getInstance();
                long time = new Date().getTime();
                StorageReference reference = storage.getReference().child("Profiles").child(time+"");

                reference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String filePath = uri.toString();
                                    HashMap<String, Object> obj = new HashMap<>();
                                    obj.put("image", filePath);

                                    database.getReference()
                                            .child("users")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .updateChildren(obj).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });

                                }
                            });
                        }
                    }
                });


                binding.imageViewIcon.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}