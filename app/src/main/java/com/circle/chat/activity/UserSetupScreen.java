package com.circle.chat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.circle.chat.R;
import com.circle.chat.databinding.ActivityUserSetupScreenBinding;
import com.circle.chat.model.CategoryModel;
import com.circle.chat.model.User;
import com.circle.chat.utilities.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class UserSetupScreen extends AppCompatActivity {
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    ActivityUserSetupScreenBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;
    private String category_value;
    private SessionManager sessionManager;
    private List<CategoryModel> categoryList;
    private MaterialAlertDialogBuilder builder;
    private AlertDialog alertdialog;
    private String imgpath = "";

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

        loadThisUsersPreviousDetailsAndShow();

        binding.imageViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsGrantedOrRequest();}
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

                                        User user = new User(uid, name, phone);
                                        user.setProfileImage(imageUrl);
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
                    User user = new User(uid, name, phone);
                    if (!imgpath.equalsIgnoreCase(""))
                        user.setProfileImage(imgpath);
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

    @NonNull
    private void generateRandomName() {
        Random random = new Random();
        int value = ThreadLocalRandom.current().nextInt(100, 10000 + 1);
        String name = "HeShe#User" + value;

        // check if name already exists than create new random number.
        checkNameExists(name);
    }

    private void checkNameExists(String name) {
        database.getReference()
                .child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            User user = snapshot1.getValue(User.class);
                            if (user != null) {
                                if (user.getName() != null && user.getName().equalsIgnoreCase(name)) {
                                    generateRandomName();
                                } else {
                                    binding.nameBox.setText(name);  // setting random username
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadThisUsersPreviousDetailsAndShow() {
        showDialog(); // shows the loading dialog
        database.getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dismissDialog();
                        User user = snapshot.getValue(User.class);
                        if (user == null) {
                            if (binding.nameBox.getText().toString().equalsIgnoreCase("")) {
                                generateRandomName();
                            }
                            Toast.makeText(UserSetupScreen.this, "No previous details found. \nWelcome to HEShe Chat!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else {
                            if (user.getName() != null)
                                binding.nameBox.setText(user.getName());
                            if (user.getProfileImage() != null) {
                                    if (user.getImage() != null) {
                                        imgpath = user.getImage();
                                        // if (uri != null) {
                                        Glide.with(UserSetupScreen.this)
                                                .load(Uri.parse(user.getImage()))
                                                .placeholder(R.drawable.avatar_icon)
                                                .skipMemoryCache(false)
                                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                                .into(binding.imageViewIcon);
                                        //  }
                                    }
                                    else {
                                        imgpath = user.getProfileImage();
                                        Glide.with(UserSetupScreen.this)
                                                .load(Uri.parse(user.getProfileImage()))
                                                .placeholder(R.drawable.avatar_icon)
                                                .skipMemoryCache(false)
                                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                                .into(binding.imageViewIcon);
                                    }
                            }


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dismissDialog();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if(data.getData() != null) {
                Uri uri = data.getData(); // filepath
                binding.imageViewIcon.setImageURI(data.getData());
                selectedImage = data.getData();

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



            }
        }
    }

    // permission...
    private boolean checkPermissionsGrantedOrRequest() {
        int readExternalStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (readExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 45);
            return true;
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), GROUP_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GROUP_PERMISSION_REQUEST) {
            boolean allGranted = grantResults.length != 0;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 45);
//                checkPermissionsGrantedOrRequest();
            } else {
                showPermissionDeniedAlert(permissions);
            }
        }
    }

    private void showPermissionDeniedAlert(String[] permissions) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);

        // AlertDialog.Builder alertdialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        alertdialogBuilder.setMessage("If you reject permission,you can not use this service Please turn on permissions at [Setting] > [Permission]");
        alertdialogBuilder.setPositiveButton("Retry Again!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkPermissionsGrantedOrRequest();
            }
        });
        alertdialogBuilder.setNegativeButton("Ok, Close It", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
    }

    // Permission - End


    public void showDialog() {
        builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_layout, null);
        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);
        ImageView imageView = view.findViewById(R.id.icon);

        title.setText("Fetching details...");
        description.setText("Please wait while we are fetching your details.");
        imageView.animate().rotation(3600).setDuration(60000).start();  // icon rotating
        builder.setView(view)
//                .setPositiveButton("Ok", /* listener = */ null)
//                .setNegativeButton("Cancel", /* listener = */ null)
                .setCancelable(false);

        alertdialog = builder.create();
        alertdialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corner_bg); // show rounded corner for the dialog
        alertdialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.

        alertdialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        alertdialog.show();
    }

    public void dismissDialog() {
        alertdialog.dismiss();
    }


}