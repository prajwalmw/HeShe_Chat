package com.circle.chat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.circle.chat.R;
import com.circle.chat.adapter.MessagesAdapter;
import com.circle.chat.databinding.ActivityMainBinding;
import com.circle.chat.model.Message;
import com.circle.chat.model.User;
import com.circle.chat.utilities.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String senderUid;
    String receiverUid;
    String token;
    String profile;
    String name;
    URL serverURL;
    User sender_user;
    String s_name, s_token, s_image, s_uid, category_value;
    boolean block = false;
    SessionManager sessionManager;
    ArrayList<User> userArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#005005"));

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        sessionManager = new SessionManager(this);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        fetchAllUsers();
        binding.progress.setVisibility(View.VISIBLE);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);
        messages = new ArrayList<>();

        senderUid = FirebaseAuth.getInstance().getUid();    // this is your uid.

        database.getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sender_user = snapshot.getValue(User.class);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        // arrow back click
        binding.arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
              /*  boolean notification = getIntent().getBooleanExtra("notification", false);
                if (notification) {
                    Intent intent = new Intent(MainActivity.this, Chat_UserList.class);
                    intent.putExtra("category",category_value);
                    startActivity(intent);
                }
                else {
                    finish();
                }*/
            }
        });

        if (block)
            binding.blockBtn.setText("Unblock");
        else
            binding.blockBtn.setText("Block");

        binding.blockBtn.setOnClickListener(v -> {
            blockUser();
        });

/*
        database.getReference().child("presence").child(receiverUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String status = snapshot.getValue(String.class);
                            if (!status.isEmpty()) {
                                if (status.equals("Offline")) {
                                    binding.status.setVisibility(View.GONE);
                                } else {
                                    binding.status.setText(status);
                                    binding.status.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
*/


        binding.messageBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView message, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    sendChatMessage();
                }
                return false;
            }
        });

        binding.sendBtn.setOnClickListener(v -> {
            sendChatMessage();
        });

/*
        database.getReference()
                .child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            message.setTimestamp(message.getTimestamp());
                            messages.add(message);
                        }

                        scrollToLatestItem(); // scroll recyclerview to latest item
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
*/

      /*  if (block) {
            binding.messageBox.setEnabled(false);
            binding.messageBox.setHint("You have blocked this user");
        }
        else {
            binding.messageBox.setEnabled(true);
            binding.messageBox.setHint("Type a message...");
        }
*/
        binding.messageBox.setEnabled(true);
        binding.messageBox.setHint("Type a message...");

        binding.cvNewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.newBtn.getText().toString().equalsIgnoreCase("New")) {
                    binding.newBtn.setText("Really?");
                    binding.cvNewbtn.setCardBackgroundColor(getResources().getColor(R.color.theme_red_sports));
                    return;
                }

                if (binding.newBtn.getText().toString().equalsIgnoreCase("Really?")) {
                    deleteCurrentChatWithUser();
                    fetchRandomUser();
                    binding.cvNewbtn.setCardBackgroundColor(getResources().getColor(R.color.purple_700));
                }


            }
        });

        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            }
        });

        final Handler handler = new Handler();
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equalsIgnoreCase(""))
                    binding.sendBtn.setVisibility(View.VISIBLE);
                else
                    binding.sendBtn.setVisibility(View.GONE);

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });

    }

    private void deleteCurrentChatWithUser() {
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        database.getReference()
                .child("chats")
                .child(senderRoom)
                .removeValue();

        database.getReference()
                .child("chats")
                .child(receiverRoom)
                .removeValue();
    }

    private void fetchMessages() {
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        database.getReference()
                .child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            message.setTimestamp(message.getTimestamp());
                            messages.add(message);
                        }

                        if (senderRoom.contains(FirebaseAuth.getInstance().getUid())) {
                            scrollToLatestItem(); // scroll recyclerview to latest item
                            adapter = new MessagesAdapter(MainActivity.this, messages, senderRoom, receiverRoom, category_value);
                            binding.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            binding.recyclerView.setAdapter(adapter);
                            binding.progress.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void fetchAllUsers() {
        String currentID = FirebaseAuth.getInstance().getUid();

        database.getReference()
                .child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            User randomUser = snapshot1.getValue(User.class);
                            String randomID = randomUser.getUid();
                            if (!randomID.equalsIgnoreCase(currentID)) {
                                userArrayList.add(randomUser);
                            }
                        }
                        fetchRandomUser();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }

    private void fetchRandomUser() {

        if (userArrayList.size() > 0) {
            binding.progress.setVisibility(View.VISIBLE);
            Random random = new Random();
            int index = random.nextInt(userArrayList.size());
            User user = userArrayList.get(index);
            name = user.getName();
            profile = user.getProfileImage();
            token = user.getToken();
            receiverUid = user.getUid(); // this id will be of the one to whom you are sending the msg.
            block = user.isIsblocked();

            binding.name.setText(name);
            Glide.with(MainActivity.this).load(profile)
                    .placeholder(R.drawable.avatar_icon)
                    .into(binding.profile);



            fetchMessages();
            binding.newBtn.setText("New");
        }
    }

    private void sendChatMessage() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        String messageTxt = binding.messageBox.getText().toString();

        if(messageTxt.trim().equalsIgnoreCase("")) {
            Toast.makeText(MainActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        scrollToLatestItem(); // scroll recyclerview to latest item

        Date date = new Date();
        Message message = new Message(messageTxt, senderUid, date.getTime());
        binding.messageBox.setText("");

        String randomKey = database.getReference().push().getKey();

        HashMap<String, Object> lastMsgObj = new HashMap<>();
        lastMsgObj.put("lastMsg", message.getMessage());
        lastMsgObj.put("lastMsgTime", date.getTime());
        lastMsgObj.put("block", false);

        database.getReference()
                .child("chats")
                .child(senderRoom).updateChildren(lastMsgObj); // Updating the values...
        database.getReference()
                .child("chats")
                .child(receiverRoom).updateChildren(lastMsgObj);

        database.getReference()
                .child("chats")
                .child(senderRoom)
                .child("messages")
                .child(randomKey)
                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference()
                                .child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // TODO: Handle later.
                                        //   sendNotification(name, message.getMessage(), token, profile); // this calls fcm by hitting fcm api.
                                    }
                                });
                    }
                });
    }

    // todo: handle this block later for heshe
//    void sendNotification(String name, String message, String token, String profile) {
//        try {
//            RequestQueue queue = Volley.newRequestQueue(this);
//
//            String url = "https://fcm.googleapis.com/fcm/send";
//
//            JSONObject data = new JSONObject(); // here the one who is sending the msg his details must come here.
//            data.put("name", sender_user.getName()); // user-name
//            data.put("body", message); // message
//            data.put("token", sender_user.getToken());
//            data.put("image", sender_user.getProfileImage());
//            data.put("uid", sender_user.getUid());
//            data.put("category", category_value);
//            data.put("activity", "ChatActivity");
//
//            JSONObject notificationData = new JSONObject();
//            notificationData.put("data", data); // sending value to "data" is very imp to trigger notifi in both fore and background.
//            notificationData.put("to", token);
//
//            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
//                    , new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject response) {
//                    //   Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.v("volley", "error: " + error + ". : " + error.networkResponse);
//                    if(error.getMessage() != null) {
//                        Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                    else {
//                        Toast.makeText(ChatActivity.this, "Error in Volley", Toast.LENGTH_SHORT).show();
//
//                    }
//                }
//            }) {
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                    /**
//                     * Firebase - setting- project settings - cloud messaging - cm api (legacy) - three dots - redirect - enable - done - copy and paste here the key.
//                     */
//                    HashMap<String, String> map = new HashMap<>();
//                    String key = "Key=AAAAWWFIPG0:APA91bHPg7uiQHaU3NTX2SZLyRdXYTCdveog5vYECpCdZixulrWo_A6LmojdJ_z88K8DYuqlDapzqwPGVha5Fq-8OCHptSaUI3gRmCO_ILiMEeJ0Z_YZGtvi9v4ookji-OokBgHeo0U1";
//                    map.put("Content-Type", "application/json");
//                    map.put("Authorization", key);
//
//                    return map;
//                }
//            };
//
//            queue.add(request);
//
//
//        } catch (Exception ex) {
//            Log.v("hi", "hii: " + ex);
//
//        }
//
//
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 25) {
            if (data != null) {
                if (data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference()
                            .child("chats")
                            .child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();

                                        String messageTxt = binding.messageBox.getText().toString();

                                        Date date = new Date();
                                        Message message = new Message(messageTxt, senderUid, date.getTime());
                                        message.setMessage("photo");
                                        message.setImageUrl(filePath);
                                        binding.messageBox.setText("");

                                        String randomKey = database.getReference().push().getKey();

                                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                                        lastMsgObj.put("lastMsg", message.getMessage());
                                        lastMsgObj.put("lastMsgTime", date.getTime());
                                        lastMsgObj.put("block", false);

                                        database.getReference()
                                                .child("chats")
                                                .child(senderRoom).updateChildren(lastMsgObj);
                                        database.getReference()
                                                .child("chats")
                                                .child(receiverRoom).updateChildren(lastMsgObj);

                                        database.getReference()
                                                .child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        database.getReference()
                                                                .child("chats")
                                                                .child(receiverRoom)
                                                                .child("messages")
                                                                .child(randomKey)
                                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                    }
                                                                });
                                                    }
                                                });

                                        //Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }

    /*   @Override
       public boolean onCreateOptionsMenu(Menu menu) {
           getMenuInflater().inflate(R.menu.chat_menu, menu);
           if (block)
               menu.findItem(R.id.block).setTitle("Unblock");
           else
               menu.findItem(R.id.block).setTitle("Block");

           return true;
       }

       @Override
       public boolean onOptionsItemSelected(@NonNull MenuItem item) {
           switch (item.getItemId()) {
               case R.id.chhat:
                   connectVideoCall();
                   return true;

               case R.id.block:
                   blockUser();
                   return true;

               default:
                   return super.onOptionsItemSelected(item);
           }
       }
   */
    private void blockUser() {
        if (messages.size() > 0) {
            HashMap<String, Object> block_key = new HashMap<>();
            if (block) {
                block_key.put("block" , false);
                database.getReference()
                        .child("chats")
                        .child(senderRoom).updateChildren(block_key); // Updating the values...
                binding.messageBox.setEnabled(false);
                binding.messageBox.setHint("You have blocked this user");
                Toast.makeText(this, "User is unblocked successfully!", Toast.LENGTH_SHORT).show();
            }
            else {
                block_key.put("block" , true);
                database.getReference()
                        .child("chats")
                        .child(senderRoom).updateChildren(block_key); // Updating the values...
                binding.messageBox.setEnabled(true);
                binding.messageBox.setHint("Type a message...");
                Toast.makeText(this, "User is blocked successfully!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "You cannot block before sending any message to each other", Toast.LENGTH_SHORT).show();
        }
    }

    private void connectVideoCall() {
//        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
//                .setRoom("Prajwal456")
//                // .setWelcomePageEnabled(false)
//                .build();
//
//        JitsiMeetActivity.launch(ChatActivity.this, options);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void scrollToLatestItem() {
        binding.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                binding.recyclerView.scrollToPosition(adapter.getItemCount()-1);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        boolean notification = getIntent().getBooleanExtra("notification", false);
      /*  if (notification) {
            Intent intent = new Intent(this, Chat_UserList.class);
            intent.putExtra("category",category_value);
            intent.putExtra("notification",notification);
            startActivity(intent);
        }
        else {
            finish();
        }*/
    }

    @Override
    protected void onDestroy() {
        // So that when app is forced closed so than also the current chat is deleted.
        super.onDestroy();
        deleteCurrentChatWithUser();
    }
}
