package com.circle.chat;

import android.content.Context;

import com.circle.chat.activity.MainActivity;
import com.circle.chat.adapter.UsersAdapter;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AppConstants {
    public static final String APP_ID = "ca-app-pub-6656140211699925~1566840953";
    public static final String BANNER = "ca-app-pub-6656140211699925/6924438405";
    public static final String BANNER_CHATLIST = "ca-app-pub-6656140211699925/3601772461";
    public static final String FULLSCREEN = "ca-app-pub-6656140211699925/3974841438";

    public static final String BANNER_Test = "ca-app-pub-3940256099942544/6300978111";   // Testing
    public static final String FULLSCREEN_Test = "ca-app-pub-3940256099942544/1033173712";   // Testing

    public static void clearDbAfter24Hrs(FirebaseDatabase database) {
        long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
        Query oldItems = database.getReference().child("chats").orderByChild("lastMsgTime").endAt(cutoff);
        oldItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot: snapshot.getChildren()) {
                    itemSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

}
