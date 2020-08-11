package com.shrubsink.everylifeismatter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.shrubsink.everylifeismatter.adapter.QueryAnswerRecyclerAdapter;
import com.shrubsink.everylifeismatter.model.QueryAnswer;
import com.shrubsink.everylifeismatter.model.QueryPost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnswerActivity extends AppCompatActivity {

    EditText answerFieldEt;
    ImageView postAnswerIv;

    TextView titleView, bodyView, issueLocationView, tagsView;
    ImageView queryPostImageView;
    /*TextView queryPostDate;*/

    TextView queryPostUserName;
    /*TextView queryPostUserShortBio;*/
    CircleImageView queryPostUserImage;

    RecyclerView answerRecyclerView;
    QueryAnswerRecyclerAdapter queryAnswerRecyclerAdapter;
    List<QueryAnswer> queryAnswerList;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    String queryPostId;
    String currentUserId;
    String queryPostUserId;

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
        /*queryPostUserShortBio = findViewById(R.id.short_bio_tv);*/
        queryPostUserImage = findViewById(R.id.profile_image);

        answerRecyclerView = findViewById(R.id.answer_list);
        postAnswerIv = findViewById(R.id.answer_post_btn);
        answerFieldEt = findViewById(R.id.answer_field);

        fetchQueryPost();

        //RecyclerView Firebase List
        queryAnswerList = new ArrayList<>();
        queryAnswerRecyclerAdapter = new QueryAnswerRecyclerAdapter(queryAnswerList, this);
        answerRecyclerView.setHasFixedSize(true);
        answerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        answerRecyclerView.setAdapter(queryAnswerRecyclerAdapter);

        firebaseFirestore.collection("query_posts/" + queryPostId + "/answers")
                .addSnapshotListener(AnswerActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (!documentSnapshots.isEmpty()) {
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String answerId = doc.getDocument().getId();
                                    QueryAnswer queryAnswer = doc.getDocument().toObject(QueryAnswer.class).withId(answerId);
                                    queryAnswerList.add(queryAnswer);
                                    queryAnswerRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });

        postAnswerIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answer = answerFieldEt.getText().toString();
                if (!TextUtils.isEmpty(answer)) {
                    Map<String, Object> answerMap = new HashMap<>();
                    answerMap.put("answer", answer);
                    answerMap.put("user_id", currentUserId);
                    answerMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("query_posts/" + queryPostId + "/answers")
                            .add(answerMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                answerFieldEt.setText("");
                                return;
                            }

                            if (!task.isSuccessful()) {
                                Toast.makeText(AnswerActivity.this,
                                        "Error Posting Comment : " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
                }
            }
        });

    }

    public void fetchQueryPost() {
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
                            tagsView.setText("TAGS: " + tags);
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
                                                /*String shortBio = task.getResult().getString("short_bio");*/
                                                String profile = task.getResult().getString("profile");

                                                queryPostUserName.setText(name);
                                                /*queryPostUserShortBio.setText(shortBio);*/

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