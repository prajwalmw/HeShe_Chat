package com.circle.chat.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.database.FirebaseDatabase;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private FirebaseDatabase database;

    @Override
    public void onReceive(Context context, Intent intent) {
        database = FirebaseDatabase.getInstance();
        database.getReference()
                .child("chats")
                .removeValue();
    }
}
