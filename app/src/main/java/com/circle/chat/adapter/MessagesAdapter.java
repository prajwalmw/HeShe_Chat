package com.circle.chat.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.circle.chat.R;
import com.circle.chat.databinding.DeleteDialogBinding;
import com.circle.chat.databinding.ItemReceive1Binding;
import com.circle.chat.databinding.ItemSent1Binding;
import com.circle.chat.model.Message;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;
    String senderRoom;
    String receiverRoom;
    FirebaseRemoteConfig remoteConfig;
  //  String category_value;

    public MessagesAdapter(Context context, ArrayList<Message> messages,
                           String senderRoom, String receiverRoom/*, String category_value*/) {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
      //  this.category_value = category_value;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == ITEM_SENT) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_sent_1, parent, false);
                return new SentViewHolder(view);
            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.item_receive_1, parent, false);
                return new ReceiverViewHolder(view);
            }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

            if (holder.getClass() == SentViewHolder.class) {
                SentViewHolder viewHolder = (SentViewHolder) holder;
                viewHolder.binding.message.setText(message.getMessage());

                //show time in message item
                long time = message.getTimestamp();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm"); // HH for 24 and hh for 12
                viewHolder.binding.timeTxtview.setText(dateFormat.format(new Date(time)));
                //end
            } else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.message.setText(message.getMessage());

                //show time in message item
                long time = message.getTimestamp();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm"); // HH for 24 and hh for 12
                viewHolder.binding.timeTxtview.setText(dateFormat.format(new Date(time)));
                //end
            }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {

        ItemSent1Binding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSent1Binding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        ItemReceive1Binding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceive1Binding.bind(itemView);
        }
    }

}
