package com.shrubsink.everylifeismatter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnswerActivity extends AppCompatActivity {

    private EditText answerFieldEt;
    private ImageView postAnswerIv;

    TextView titleView, bodyView, issueLocationView, tagsView;
    ImageView queryPostImageView;
    /*TextView queryPostDate;*/

    TextView queryPostUserName, queryPostUserShortBio;
    CircleImageView queryPostUserImage;

    /*private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;*/

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    String queryPostId;
    String currentUserId;
    String queryPostUserId;
    Date timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid();
        queryPostId = getIntent().getStringExtra("query_post_id");

        titleView = findViewById(R.id.title_tv);
        bodyView = findViewById(R.id.body_tv);
        issueLocationView = findViewById(R.id.issue_location_tv);
        tagsView = findViewById(R.id.tags_tv);
        queryPostImageView = findViewById(R.id.query_image_iv);
        /*queryPostDate = findViewById(R.id.timestamp_tv);*/
        queryPostUserName = findViewById(R.id.username_tv);
        queryPostUserShortBio = findViewById(R.id.short_bio_tv);
        queryPostUserImage = findViewById(R.id.profile_image);

        firebaseFirestore.collection("query_posts").document(queryPostId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @SuppressLint({"CheckResult", "SetTextI18n"})
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String title = task.getResult().getString("title");
                            String body = task.getResult().getString("body");
                            String issueLocation = task.getResult().getString("issue_location");
                            String tags = task.getResult().getString("tags");
                            String imageUrl = task.getResult().getString("image_url");
                            queryPostUserId = task.getResult().getString("user_id");

                            /*long seconds = (long) task.getResult().get("timestamp");
                            String dateString = DateFormat.format("dd MMM yyyy\nhh:mm a", new Date(seconds)).toString();*/

                            titleView.setText(title);
                            bodyView.setText(body);
                            issueLocationView.setText(issueLocation);
                            tagsView.setText(tags);
                            /*queryPostDate.setText("•  " + dateString);*/

                            RequestOptions requestOptions = new RequestOptions();
                            requestOptions.placeholder(R.drawable.ic_baseline_image_24);
                            Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions).load(imageUrl)
                                    .into(queryPostImageView);

                            firebaseFirestore.collection("user_bio").document(queryPostUserId).collection("personal")
                                    .document(queryPostUserId).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                        @SuppressLint({"CheckResult", "SetTextI18n"})
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                String name = task.getResult().getString("name");
                                                String shortBio = task.getResult().getString("short_bio");
                                                String profile = task.getResult().getString("profile");

                                                queryPostUserName.setText(name);
                                                queryPostUserShortBio.setText(shortBio);

                                                RequestOptions requestOptions = new RequestOptions();
                                                requestOptions.placeholder(R.drawable.ic_baseline_image_24);

                                                Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions).load(profile)
                                                        .into(queryPostUserImage);
                                            } else {
                                                Toast.makeText(AnswerActivity.this,
                                                        R.string.check_internet_connection,
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(AnswerActivity.this,
                                    R.string.check_internet_connection,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}