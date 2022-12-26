package com.circle.chat.utilities;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.circle.chat.R;
import com.circle.chat.activity.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MyFirstbaseService extends FirebaseMessagingService {
    public static final String FCM_FALLBACK_NOTIFICATION_CHANNEL =
            "fcm_fallback_notification_channel";
    public static final String FCM_FALLBACK_NOTIFICATION_CHANNEL_LABEL =
            "fcm_fallback_notification_channel_label";
    Bitmap bitmap = null;
    public String NOTIFICATION_CHANNEL_ID = "1";
    public String NOTIFICATION_CHANNEL_NAME = "Chat Notification";


    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

//        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("name");
        String body = data.get("body");
        String token = data.get("token");
        String image = data.get("image");
        String uid = data.get("uid");
     //   String category = data.get("category");
        String activity = data.get("activity");
        sendNotification(title, body, token, image, uid, activity);
        Log.v("fcm", "fcm_value: Title: "+ title + "\n, " +
                "Message: " + body + "\n, Toke: " + token + "\n, Image: " + image);

    }

    private void sendNotification(String title, String messageBody, String token,
                                  String image, String uid, String activity) {
        Intent intent = null;
        if(activity.equalsIgnoreCase("ChatActivity")) {
            intent = new Intent(this, MainActivity.class);
        }
        else {
        //    intent = new Intent(this, Chat_UserList.class);
        }
        intent.putExtra("name",title);
        intent.putExtra("token",token);
        intent.putExtra("image",image);
        intent.putExtra("uid",uid);
      //  intent.putExtra("category",category);
        intent.putExtra("notification", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            bitmap = Glide.with(this)
                    .asBitmap()
                    .load(image)
                    .placeholder(R.drawable.avatar)
                    .submit()
                    .get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this,
                    0, intent, PendingIntent.FLAG_ONE_SHOT |
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        }else {
            pendingIntent = PendingIntent.getActivity(this,
                    0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
        }

        if(activity.equalsIgnoreCase("ChatActivity")) {
            NOTIFICATION_CHANNEL_ID = "1";
            NOTIFICATION_CHANNEL_NAME = "Chat Channel";
        }
        else {
            NOTIFICATION_CHANNEL_ID = "2";
            NOTIFICATION_CHANNEL_NAME = "Food Channel";
        }

        if(bitmap != null) {
            bitmap = getCircularBitmap(bitmap);
        }
        else {
            bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.avatar);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                      //  .setSubText(category)
//                        .setStyle(
//                                new NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                        .setLargeIcon(getCircularBitmap(bitmap)) // Todo: #crash: MyFirstbaseService.java line 127 bitmap null
                        .setContentTitle(title)
                        .setContentText(messageBody)
//                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setSound(defaultSoundUri)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setAutoCancel(true) // so that msg disappers from statusbar when clicked upon
//                        .setFullScreenIntent(pendingIntent, true);
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify((int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE) /* ID of notification */, notificationBuilder.build());
    }

    protected Bitmap getCircularBitmap(Bitmap srcBitmap) {
        // Calculate the circular bitmap width with border
        int squareBitmapWidth = Math.min(srcBitmap.getWidth(), srcBitmap.getHeight());
        // Initialize a new instance of Bitmap
        Bitmap dstBitmap = Bitmap.createBitmap (
                squareBitmapWidth, // Width
                squareBitmapWidth, // Height
                Bitmap.Config.ARGB_8888 // Config
        );
        Canvas canvas = new Canvas(dstBitmap);
        // Initialize a new Paint instance
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, squareBitmapWidth, squareBitmapWidth);
        RectF rectF = new RectF(rect);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // Calculate the left and top of copied bitmap
        float left = (squareBitmapWidth-srcBitmap.getWidth())/2;
        float top = (squareBitmapWidth-srcBitmap.getHeight())/2;
        canvas.drawBitmap(srcBitmap, left, top, paint);
        // Free the native object associated with this bitmap.
        //  srcBitmap.recycle();
        // Return the circular bitmap
        return dstBitmap;
    }

}

