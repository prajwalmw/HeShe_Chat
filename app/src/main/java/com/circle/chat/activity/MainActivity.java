package com.circle.chat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.circle.chat.R;
import com.circle.chat.adapter.MessagesAdapter;
import com.circle.chat.databinding.ActivityMainBinding;
import com.circle.chat.model.Message;
import com.circle.chat.model.User;
import com.circle.chat.utilities.MyBroadcastReceiver;
import com.circle.chat.utilities.SessionManager;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
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
import java.util.concurrent.ThreadLocalRandom;

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
    String receivertoken;
    String profile;
    String name;
    URL serverURL;
    User sender_user;
    String s_name, s_token, s_image, s_uid, category_value;
    boolean block = false;
    SessionManager sessionManager;
    ArrayList<User> userArrayList = new ArrayList<>();
    Handler hand;
    private InterstitialAd mInterstitialAd;
    private AdRequest adRequest;
    private int counter = 0;
    String nameN = "";
    private Intent intent;
    boolean fromNotification = false;
    private String n_name, n_profile, n_uid;

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

        profile = getIntent().getStringExtra("image");
        receivertoken = getIntent().getStringExtra("token");
        receiverUid = getIntent().getStringExtra("uid"); // this id will be of the one to whom you are sending the msg.

        senderGeneratingToken(); // generating token for notifin.

        // Setting up Alarm Manager
        myAlarm();

        // Admob - Start
        final Handler handelay = new Handler();
        handelay.postDelayed(new Runnable() {
            @Override
            public void run() {
                initAds();

                adRequest = new AdRequest.Builder().build();
                binding.adView.loadAd(adRequest);
                binding.adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        binding.adView.loadAd(adRequest);
                    }
                });

                loadFullScreenAd();
            }
        }, 5000);

        // Admob - End

        intent = getIntent();
        if (intent.getExtras() != null) {
            fromNotification = intent.getBooleanExtra("notification", false);
            n_name = intent.getStringExtra("name");
            n_profile = intent.getStringExtra("image");
            n_uid = intent.getStringExtra("uid");
            Log.v("Chat", "fromNotification: " + fromNotification);
        }

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
            }
        });

        if (!fromNotification)
            fetchAllUsers();
        else {
            fetchAllUsers_fromNotification();
        }

      /*  if (block)
            binding.blockBtn.setText("Unblock");
        else
            binding.blockBtn.setText("Block");

        binding.blockBtn.setOnClickListener(v -> {
            blockUser();
        });*/

        binding.chatListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Chat_UserList.class);
            startActivity(intent);
        });


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
                    counter++;  // incrementing counter here...
                    if (counter == 10) {
                        counter = 0;    // resetting counter...
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(MainActivity.this);
                    } else {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.");
                    }}

                 //   deleteCurrentChatWithUser(senderRoom, receiverRoom);
                    fetchRandomUser(userArrayList);
                    binding.cvNewbtn.setCardBackgroundColor(getResources().getColor(R.color.purple_700));
                }


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

    // only once...
    private void initAds() {
        // Ads initialize only once.
        MobileAds.initialize(MainActivity.this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }

    public void loadFullScreenAd() {
        // Fullscreen ads.
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        mInterstitialAd = null;
                        loadFullScreenAd();
                    }

                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                             //   mInterstitialAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                mInterstitialAd = null;
                                loadFullScreenAd();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                            }
                        });
                    }
                });
    }

    private void deleteCurrentChatWithUser(String sRoom, String rRoom) {
        if (hand != null)
            hand.removeCallbacksAndMessages(null);

        if (sRoom != null) {
            database.getReference()
                    .child("chats")
                    .child(sRoom)
                    .removeValue();
        }

        if (rRoom != null) {
            database.getReference()
                    .child("chats")
                    .child(rRoom)
                    .removeValue();
        }
    }

    private void fetchMessages(String sRoom, String rRoom, String userName) {
        // Fetching name of the current Receiver ie. Other User.
        database.getReference()
                .child("users")
                .child(receiverUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        nameN = user.getName();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        // end

        adapter = new MessagesAdapter(this, messages, sRoom, rRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        scrollToLatestItem(); // scroll recyclerview to latest item


        database.getReference()
                .child("chats")
                .child(sRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (userName.equalsIgnoreCase(binding.name.getText().toString())) {
                            // ie. If the user that is sending the messages if actually the same users with whom I am talking...
                            messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            message.setTimestamp(message.getTimestamp());
                            messages.add(message);
                        }

                            scrollToLatestItem(); // scroll recyclerview to latest item
                            binding.progress.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        }

//                        if (sRoom.contains(FirebaseAuth.getInstance().getUid())) {
//                            scrollToLatestItem(); // scroll recyclerview to latest item
//                            adapter = new MessagesAdapter(MainActivity.this, messages, sRoom, rRoom/*, category_value*/);
//                            binding.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//                            binding.recyclerView.setAdapter(adapter);
//                            binding.progress.setVisibility(View.GONE);
//                            adapter.notifyDataSetChanged();
//                        }
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
                            User user = snapshot1.getValue(User.class);
                            String userID = user.getUid();
                            if (!userID.equalsIgnoreCase(currentID)) {
                                userArrayList.add(user);
                            }
                        }
                        fetchRandomUser(userArrayList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private void fetchAllUsers_fromNotification() {
        String currentID = FirebaseAuth.getInstance().getUid();

        database.getReference()
                .child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            User user = snapshot1.getValue(User.class);
                            String userID = user.getUid();
                            if (!userID.equalsIgnoreCase(currentID)) {
                                userArrayList.add(user);
                            }
                        }
                      //  fetchRandomUser(userArrayList);
                        User user = new User();
                        user.setName(n_name);
                        user.setProfileImage(n_profile);
                        user.setUid(n_uid);
                        sessionManager.setCurrentChattingUser(n_name); // setting user name of new user...
                        fetchNotifiUser(user);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void fetchNotifiUser(User user) {
            binding.progress.setVisibility(View.VISIBLE);
            name = user.getName();
            profile = user.getProfileImage();
            //  token = user.getToken();  // TODO: check later...

            receiverUid = user.getUid(); // this id will be of the one to whom you are sending the msg.
            senderUid = FirebaseAuth.getInstance().getUid();

            receiverGeneratingToken(receiverUid);

            block = user.isIsblocked();

            senderRoom = senderUid + receiverUid;
            receiverRoom = receiverUid + senderUid;

            // Adding this Delete so that other user sending message to new when I am not available/talking with
            // other user should not be shown ie. in that case be deleted.
            //    deleteCurrentChatWithUser(senderRoom, receiverRoom);

            // typing....
            database.getReference().child("presence").child(receiverUid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String online = snapshot.getValue(String.class);
                                if (!online.isEmpty()) {
                                    if (online.equals("Offline")) {
                                        //   binding.status.setVisibility(View.GONE);
                                    } else {
                                        binding.onlinetxtview.setText(online);
                                        //   binding.status.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            // typinf end....
            binding.name.setText(name);
            Glide.with(MainActivity.this).load(profile)
                    .placeholder(R.drawable.avatar_icon)
                    .into(binding.profile);
            binding.newBtn.setText("New");

            fetchMessages(senderRoom, receiverRoom, name);
            // Receiver is sendig message 'Hi'
        //    receiverSendingMessage(senderRoom, receiverRoom);
    }

    private void fetchRandomUser(ArrayList<User> userList) {

        if (userList.size() > 0) {
            binding.progress.setVisibility(View.VISIBLE);
            Random random = new Random();
           // int index = ThreadLocalRandom.current().nextInt(0, userArrayList.size());
            int index = random.nextInt(userList.size());
            User user = userList.get(index);
            name = user.getName();
            profile = user.getProfileImage();
          //  token = user.getToken();  // TODO: check later...

            receiverUid = user.getUid(); // this id will be of the one to whom you are sending the msg.
            senderUid = FirebaseAuth.getInstance().getUid();

            receiverGeneratingToken(receiverUid);

            block = user.isIsblocked();

            senderRoom = senderUid + receiverUid;
            receiverRoom = receiverUid + senderUid;

            sessionManager.setCurrentChattingUser(name); // setting user name of new user...

            // Adding this Delete so that other user sending message to new when I am not available/talking with
            // other user should not be shown ie. in that case be deleted.
        //    deleteCurrentChatWithUser(senderRoom, receiverRoom);

            // typing....
            database.getReference().child("presence").child(receiverUid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String online = snapshot.getValue(String.class);
                                if (!online.isEmpty()) {
                                    if (online.equals("Offline")) {
                                     //   binding.status.setVisibility(View.GONE);
                                    } else {
                                        binding.onlinetxtview.setText(online);
                                     //   binding.status.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            // typinf end....
            binding.name.setText(name);
            Glide.with(MainActivity.this).load(profile)
                    .placeholder(R.drawable.avatar_icon)
                    .into(binding.profile);
            binding.newBtn.setText("New");

            fetchMessages(senderRoom, receiverRoom, name);
            // Receiver is sendig message 'Hi'
          //  receiverSendingMessage(senderRoom, receiverRoom);
        }
    }

    /**
     * Receiver sending Hi message for a chat kickstarter...
     */
    private void receiverSendingMessage(String sRoom, String rRoom) {

        Date date = new Date();
        Runnable userStoppedTyping = new Runnable() {
            @Override
            public void run() {
                database.getReference().child("presence").child(senderUid).setValue("Online");
                binding.onlinetxtview.setText("Online");
                // start

                String hi = "Hi";
                int no = new Random().nextInt(100);
                if (no > 1 && no < 10)
                    hi = "Hey";
                else if (no > 10 && no < 20)
                    hi = "Hello";
                else if (no > 20 && no < 30)
                    hi = "hii";
                else if (no > 30 && no < 40)
                    hi = "hey";
                else if (no > 40 && no < 50)
                    hi = "Hie";
                else if (no > 60 && no < 70)
                    hi = "hello";

                Message message = new Message(hi, receiverUid, date.getTime());
                String randomKey = database.getReference().push().getKey();

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());
                lastMsgObj.put("block", false);

                database.getReference()
                        .child("chats")
                        .child(sRoom).updateChildren(lastMsgObj); // Updating the values...

                database.getReference()
                        .child("chats")
                        .child(rRoom).updateChildren(lastMsgObj);

                database.getReference()
                        .child("chats")
                        .child(sRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.getReference()
                                        .child("chats")
                                        .child(rRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                            }
                                        });
                            }
                        });

              //  fetchMessages();
                // end
            }
        };

        hand = new Handler(Looper.getMainLooper());
        hand.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                binding.onlinetxtview.setText("Typing...");
                hand.removeCallbacksAndMessages(null);
                hand.postDelayed(userStoppedTyping, 4000);
            }
        }, 1000);

    }

    private void sendChatMessage() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {

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
                                        sendNotification(name, message.getMessage(), receivertoken, profile); // this calls fcm by hitting fcm api.
                                    }
                                });
                    }
                });
    }

    void sendNotification(String name, String message, String receivertoken, String profile) {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject(); // here the one who is sending the msg his details must come here.
            data.put("name", sender_user.getName()); // user-name
            data.put("body", message); // message
            data.put("token", sender_user.getToken());  // here comes sender's token...
            data.put("image", sender_user.getProfileImage());
            data.put("uid", sender_user.getUid());
        //    data.put("category", category_value);
            data.put("activity", "ChatActivity");

            JSONObject notificationData = new JSONObject();
            notificationData.put("data", data); // sending value to "data" is very imp to trigger notifi in both fore and background.
            notificationData.put("to", receivertoken);  // here comes receiver's token...

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
                    , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                     //  Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("volley", "error: " + error + ". : " + error.networkResponse);
                    if(error.getMessage() != null) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Error in Volley", Toast.LENGTH_SHORT).show();

                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    /** ..... IMPORTANT .....
                     * Firebase - setting- project settings - cloud messaging - cm api (legacy)
                     * - three dots - redirect - enable - done - copy and paste here the key.
                     */
                    HashMap<String, String> map = new HashMap<>();
                    String key = "Key=AAAAPSQ4b6E:APA91bHF8Rxt24vHjsd7bY9ns47YmH2X4Qeju_EYbrJoscrHqkVXxXTYDNsJarXMIA4xv6duv8mI3JEQzt6TvBhLkbqNGz-tu3VXVOIuhhb_adDYTxZQwIc3HzISeOHELVXwoEC52ToT";
                    map.put("Content-Type", "application/json");
                    map.put("Authorization", key);

                    return map;
                }
            };

            queue.add(request);
        }
        catch (Exception ex) {
            Log.v("hi", "hii: " + ex);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
        sessionManager.setInForeground(true);
        sessionManager.setCurrentChattingUser(binding.name.getText().toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
        sessionManager.setInForeground(false);
        sessionManager.setCurrentChattingUser(binding.name.getText().toString());
    }

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
        if (fromNotification) {
            Intent intent = new Intent(this, Chat_UserList.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        // So that when app is forced closed so than also the current chat is deleted.
        super.onDestroy();
      //  deleteCurrentChatWithUser(senderRoom, receiverRoom);
    }

    private void senderGeneratingToken() {
        /**
         * GEnerating token and than need to upate token else notific wont show up as it requires token.
         */
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String senderToken) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("token", senderToken);
                        database.getReference()
                                .child("users")
                                .child(FirebaseAuth.getInstance().getUid())
                                .updateChildren(map);
                    }
                });
    }

    private void receiverGeneratingToken(String receiverUid) {
        // Reading token of receiver...
        database.getReference()
                .child("users")
                .child(receiverUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        receivertoken = user.getToken();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void myAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12); // Afternoon 12:00 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTime().compareTo(new Date()) < 0)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(getApplicationContext(), MyBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }




}
