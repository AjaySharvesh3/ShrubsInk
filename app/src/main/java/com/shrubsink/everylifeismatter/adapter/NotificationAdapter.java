package com.shrubsink.everylifeismatter.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shrubsink.everylifeismatter.MainActivity;
import com.shrubsink.everylifeismatter.NotificationFragment;
import com.shrubsink.everylifeismatter.QueryFragment;
import com.shrubsink.everylifeismatter.R;
import com.shrubsink.everylifeismatter.model.Notification;
import com.shrubsink.everylifeismatter.model.QueryPost;

import java.util.Date;
import java.util.List;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public List<Notification> notificationList;
    public Context context;
    NotificationFragment thisFragment;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    MainActivity thisActivity;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notifications_list_item, parent, false);
        thisActivity = new MainActivity();
        thisFragment = new NotificationFragment();
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        final String notificationId = notificationList.get(position).NotificationId;
        final String user_id = notificationList.get(position).getFrom();
        final String message = notificationList.get(position).getMessage();
        holder.setNotificationMessage(message);

        //User Data will be retrieved here...
        firebaseFirestore.collection("user_bio").document(user_id)
                .collection("personal").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try {
                            if (task.isSuccessful()) {
                                String userImage = task.getResult().getString("profile");
                                holder.setUserProfileImage(userImage);
                            } else {
                                Toast.makeText(thisActivity,
                                        context.getString(R.string.check_internet_connection),
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception er) {
                            er.printStackTrace();
                        }
                    }
                });

        try {
            long millisecond = notificationList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("dd MMM yyyy\nhh:mm a", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView notificationMessage, notificationTimeStamp;
        private ImageView notificationUserProfileImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setNotificationMessage(String message) {
            notificationMessage = mView.findViewById(R.id.notification_message);
            notificationMessage.setText(message);
        }

        public void setTime(String date) {
            notificationTimeStamp = mView.findViewById(R.id.notification_timestamp);
            notificationTimeStamp.setText(date);
        }

        @SuppressLint("CheckResult")
        public void setUserProfileImage(String image) {
            notificationUserProfileImage = mView.findViewById(R.id.notification_profile);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(notificationUserProfileImage);
        }
    }
}
