package com.shrubsink.everylifeismatter.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.shrubsink.everylifeismatter.AnswerActivity;
import com.shrubsink.everylifeismatter.EmergencyReportActivity;
import com.shrubsink.everylifeismatter.MainActivity;
import com.shrubsink.everylifeismatter.NotificationFragment;
import com.shrubsink.everylifeismatter.QueryFragment;
import com.shrubsink.everylifeismatter.R;
import com.shrubsink.everylifeismatter.model.Notification;
import com.shrubsink.everylifeismatter.model.QueryAnswer;
import com.shrubsink.everylifeismatter.model.QueryPost;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    NotificationFragment notificationFragment;
    public List<Notification> notificationList;
    public Context context;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    public NotificationAdapter(List<Notification> notificationList, NotificationFragment notificationFragment) {
        this.notificationFragment = notificationFragment;
        this.notificationList = notificationList;
    }

    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notifications_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NotificationAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        String notificationMessage = notificationList.get(position).getMessage();
        final String user_id = notificationList.get(position).getFrom();
        final String user_name = notificationList.get(position).getUser_name();
        holder.setComment_message(user_name + notificationMessage);

        try {
            //User Data will be retrieved here...
            firebaseFirestore.collection("user_bio").document(user_id)
                    .collection("personal").document(user_id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            try {
                                if (task.isSuccessful()) {
                                    String userImage = task.getResult().getString("profile");
                                    holder.setUserProfile(userImage);
                                } else {
                                    Toast.makeText(context,
                                            context.getString(R.string.check_internet_connection),
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            long millisecond = notificationList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("dd MMM yyy • hh:mm a", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        if (notificationList != null) {
            return notificationList.size();
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        public TextView notificationMessage;
        public TextView notificationDate;
        public ImageView userImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment_message(String notification) {
            notificationMessage = mView.findViewById(R.id.notification_message);
            notificationMessage.setText(notification);
        }

        public void setTime(String date) {
            notificationDate = mView.findViewById(R.id.notification_timestamp);
            notificationDate.setText(date);
        }

        @SuppressLint("CheckResult")
        public void setUserProfile(String image) {
            try {
                userImage = mView.findViewById(R.id.notification_profile);
                RequestOptions placeholderOption = new RequestOptions();
                placeholderOption.placeholder(R.drawable.profile_placeholder);
                Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(userImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}