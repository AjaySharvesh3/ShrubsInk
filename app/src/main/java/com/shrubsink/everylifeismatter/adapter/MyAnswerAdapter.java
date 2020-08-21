package com.shrubsink.everylifeismatter.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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
import com.shrubsink.everylifeismatter.MyActivitiesActivity;
import com.shrubsink.everylifeismatter.R;
import com.shrubsink.everylifeismatter.model.QueryAnswer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MyAnswerAdapter extends RecyclerView.Adapter<MyAnswerAdapter.ViewHolder> {

    public MyActivitiesActivity myActivitiesActivity;
    public List<QueryAnswer> answerList;
    public Context context;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    public MyAnswerAdapter(List<QueryAnswer> answerList) {
        this.answerList = answerList;
    }

    @Override
    public MyAnswerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_my_answer_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new MyAnswerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyAnswerAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        final String queryAnswerId = answerList.get(position).QueryAnswerId;
        /*final String queryPostId = myActivitiesActivity.getIntent().getStringExtra("query_post_id");
        final String queryPostUserId = myActivitiesActivity.getIntent().getStringExtra("user_id");*/
        /*final String title = myActivitiesActivity.getIntent().getStringExtra("title");
        final String body = myActivitiesActivity.getIntent().getStringExtra("body");
        final String tags = myActivitiesActivity.getIntent().getStringExtra("tags");
        final String image_url = myActivitiesActivity.getIntent().getStringExtra("image_url");
        final String image_thumb = myActivitiesActivity.getIntent().getStringExtra("image_thumb");
        final String credits = myActivitiesActivity.getIntent().getStringExtra("credits");
        final String is_solved = myActivitiesActivity.getIntent().getStringExtra("is_solved");
        final String issue_location = myActivitiesActivity.getIntent().getStringExtra("issue_location");*/
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String answerMessage = answerList.get(position).getAnswer();
        final String user_id = answerList.get(position).getUser_id();
        holder.setComment_message(answerMessage);

        try {
            //User Data will be retrieved here...
            firebaseFirestore.collection("user_bio").document(user_id)
                    .collection("personal").document(user_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                String userName = task.getResult().getString("name");
                                String userImage = task.getResult().getString("profile");
                                /*String shortBio = task.getResult().getString("short_bio");*/
                                holder.setUserData(userName, userImage);
                            } else {
                                Toast.makeText(myActivitiesActivity,
                                        context.getString(R.string.check_internet_connection),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            long millisecond = answerList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("dd MMM yyyy • hh:mm a", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.editAnswerIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(myActivitiesActivity, AnswerActivity.class);
                view.getContext().startActivity(i);
            }
        });
    }


    @Override
    public int getItemCount() {
        if (answerList != null) {
            return answerList.size();
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        public TextView answerContent;
        public TextView username, answerDate;
        public ImageView userImage;
        public ImageView editAnswerIv;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            editAnswerIv = itemView.findViewById(R.id.edit_answer_iv);
        }

        public void setComment_message(String answer) {
            answerContent = mView.findViewById(R.id.answer_content_tv);
            answerContent.setText(answer);
        }

        public void setTime(String date) {
            answerDate = mView.findViewById(R.id.answer_timestamp_tv);
            answerDate.setText(date);
        }

        @SuppressLint("CheckResult")
        public void setUserData(String name, String image) {
            username = mView.findViewById(R.id.answer_username_tv);
            userImage = mView.findViewById(R.id.profile_image);
            username.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(userImage);
        }
    }
}
