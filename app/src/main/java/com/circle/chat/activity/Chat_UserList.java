package com.circle.chat.activity;

import static com.circle.chat.AppConstants.clearDbAfter24Hrs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.circle.chat.adapter.UsersAdapter;
import com.circle.chat.databinding.ActivityChatUserListBinding;
import com.circle.chat.model.User;

import com.google.android.gms.ads.AdRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Chat_UserList extends AppCompatActivity {
    FirebaseDatabase database;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    ActivityChatUserListBinding binding;
    static final String currentId = FirebaseAuth.getInstance().getUid();
    public static final String TAG = Chat_UserList.class.getSimpleName();
    private Intent intent;
    private User user;
    private AdRequest adRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatUserListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        //ads - start
        adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
        //ads - end

        intent = getIntent();
        if (intent.getExtras() != null) {
           /* category_value = intent.getStringExtra("category");
            Log.v("Chat", "chatuserlist: " + category_value);
            grpchat_title = "#Circle - " + category_value;
            binding.groupChatRow.username.setText(grpchat_title);*/
        }

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();

        // arrow back click
        binding.arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        usersAdapter = new UsersAdapter(this, users);
        binding.recyclerView.setAdapter(usersAdapter);

        if (users.size() <= 0)
            binding.noData.setVisibility(View.VISIBLE);
        else
            binding.noData.setVisibility(View.GONE);

        // clear lsit after 24 hrs - start
      //  clearDbAfter24Hrs(database);
        usersAdapter.notifyDataSetChanged();
        // clear lsit after 24 hrs - end

        // search view - start
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchOperation(newText);
                return true;
            }
        });
        // search view - end

        Log.v("currentId", "currentId: " + currentId);
        database.getReference()
                        .child("chats")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        users.clear();
                                        HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getValue();
                                        if (map != null) {
                                        List<String> list = new ArrayList<String>(map.keySet());
                                        for (String key : list) {
                                            if (key.contains(currentId)) {
                                            String[] array = key.split(currentId);
                                            if (!array[0].equalsIgnoreCase("")) {
                                                database.getReference()
                                                        .child("users")
                                                        .child(array[0])
                                                        .addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                User user = snapshot.getValue(User.class);
                                                                users.add(user);
                                                                if (users.size() <= 0)
                                                                    binding.noData.setVisibility(View.VISIBLE);
                                                                else
                                                                    binding.noData.setVisibility(View.GONE);

                                                                usersAdapter.notifyDataSetChanged();
                                                                return;
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                            }
                                        }
                                        }
                                    }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
        /**
         * Reading all the users that exists...and here itself checking if the user is blocked by
         * someone than that someone shouldnt show up here...
         */
/*
        database.getReference()
                .child("users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            User user = snapshot1.getValue(User.class);
                            String cuuid = FirebaseAuth.getInstance().getUid();
                            if (!user.getUid().equals(cuuid)) {
                                // now logic of Display only those users that are not blocked and hv not blocked me as well.

                                //2. Other user has blocked me so now he should nt be seen in my lists ie. dont add that user in my list.
                                String otherBlockMe = user.getUid() + currentId; // suffix
                                Log.v(TAG, "otherBlockMe_ID: " + otherBlockMe);

                                // now check in chat branch if this ID is present or not.
                                // read chat branch for the ID is present than read block key for true value based on this make the users list.

                                database.getReference()
                                        .child("chats")
                                        .child(otherBlockMe)
                                        .child("block")
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (!snapshot.exists()) {
                                                    users.add(user);
                                                    if (users.size() <= 0)
                                                        binding.noData.setVisibility(View.VISIBLE);
                                                    else
                                                        binding.noData.setVisibility(View.GONE);

                                                    usersAdapter.notifyDataSetChanged();
                                                    return;
                                                }

                                                Boolean block = snapshot.getValue(Boolean.class);
                                                Log.v(TAG, "other_block_value: " + block);
                                                if (block != null) {
                                                    if (block) {
                                                        user.setIsblocked(true); // ie. this user has blocked me on his end so dont add him.
                                                    } else {
                                                        user.setIsblocked(false);
                                                        users.add(user); // ie. this user has not blocked me on his end.
                                                        if (users.size() <= 0)
                                                            binding.noData.setVisibility(View.VISIBLE);
                                                        else
                                                            binding.noData.setVisibility(View.GONE);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.v(TAG, "Error of Chat is: " + error.getDetails());
                                            }
                                        });
                                //2. end

                                //1. I have blocked some user.
                                String meBlock = currentId + user.getUid(); // prefix
                                Log.v(TAG, "meBlockID: " + meBlock);

                                // now check in chat branch if this ID is present or not.
                                // read chat branch for the ID is present than read block key for true value based on this make the users list.
                                database.getReference()
                                        .child("chats")
                                        .child(meBlock)
                                        .child("block")
                                        .addValueEventListener(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Boolean block = snapshot.getValue(Boolean.class);
                                        Log.v(TAG, "block_value: " + block);
                                        if (block != null) {
                                            if (block) {
                                                user.setIsblocked(true);
                                            } else {
                                                user.setIsblocked(false);
                                            }
                                        }

                                        usersAdapter.notifyDataSetChanged(); // TODO: need to add later.
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.v(TAG, "Error of Chat is: " + error.getDetails());
                                    }
                                });
                                //1. end


//                        users.add(user); // TODO: need to add later.
//                        usersAdapter.notifyDataSetChanged(); // TODO: need to add later.
                            }
                        }
                        // binding.recyclerView.hideShimmerAdapter();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
*/
    }

    private void searchOperation(String newText) {
        ArrayList<User> userList = new ArrayList<>();
        userList.addAll(users);

        if (!newText.isEmpty()) {
            userList.clear();
            for (User userdata : users) {
                if (userdata.getName().toLowerCase().contains(newText.toLowerCase())) {
                    userList.add(userdata);
                    usersAdapter = new UsersAdapter(Chat_UserList.this, userList);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(Chat_UserList.this);
                    layoutManager.setOrientation(RecyclerView.HORIZONTAL);
                    binding.recyclerView.setAdapter(usersAdapter);
                }
                else {
                }
            }

            if (users.size() <= 0) {
                binding.noData.setVisibility(View.VISIBLE);
                binding.noData.setText("No user found with this name.");
            }
            else
                binding.noData.setVisibility(View.GONE);
        }
        else {
            usersAdapter = new UsersAdapter(Chat_UserList.this, users);
            LinearLayoutManager layoutManager = new LinearLayoutManager(Chat_UserList.this);
            layoutManager.setOrientation(RecyclerView.HORIZONTAL);
            binding.recyclerView.setAdapter(usersAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    public void onPause() {
        super.onPause();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}