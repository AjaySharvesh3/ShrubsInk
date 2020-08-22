package com.shrubsink.everylifeismatter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
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
    ImageView isQuerySolvedIv;
    ImageView reportQuestionIv;

    RecyclerView answerRecyclerView;
    QueryAnswerRecyclerAdapter queryAnswerRecyclerAdapter;
    List<QueryAnswer> queryAnswerList;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    String queryPostId;
    String postUserid;
    String currentUserId;
    String queryPostUserId;

    ImageView noAnswerIv;
    LinearLayout postAnswerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid();
        queryPostId = getIntent().getStringExtra("query_post_id");
        postUserid = getIntent().getStringExtra("user_id");

        titleView = findViewById(R.id.title_tv);
        bodyView = findViewById(R.id.body_tv);
        issueLocationView = findViewById(R.id.issue_location_tv);
        tagsView = findViewById(R.id.tags_tv);
        queryPostImageView = findViewById(R.id.query_image_iv);
        postAnswerLayout = findViewById(R.id.post_answer_layout);
        isQuerySolvedIv = findViewById(R.id.is_query_solved_iv);
        reportQuestionIv = findViewById(R.id.report_question_iv);

        answerRecyclerView = findViewById(R.id.answer_list);
        postAnswerIv = findViewById(R.id.answer_post_btn);
        answerFieldEt = findViewById(R.id.answer_field);

        noAnswerIv = findViewById(R.id.no_answer_iv);

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
                            noAnswerIv.setVisibility(View.GONE);
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String answerId = doc.getDocument().getId();
                                    QueryAnswer queryAnswer = doc.getDocument().toObject(QueryAnswer.class).withId(answerId);
                                    queryAnswerList.add(queryAnswer);
                                    queryAnswerRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            noAnswerIv.setVisibility(View.VISIBLE);
                        }
                    }
                });

        postAnswerIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postAnswerIv.setEnabled(false);
                String answer = answerFieldEt.getText().toString();
                if (!TextUtils.isEmpty(answer)) {
                    final Map<String, Object> answerMap = new HashMap<>();
                    answerMap.put("answer", answer);
                    answerMap.put("user_id", currentUserId);
                    answerMap.put("credits", 20);
                    answerMap.put("is_solved", false);
                    answerMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("query_posts/" + queryPostId + "/answers")
                            .add(answerMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            FirebaseUser acct = firebaseAuth.getCurrentUser();
                            Map<String, Object> notificationMessage = new HashMap<>();
                            notificationMessage.put("timestamp", FieldValue.serverTimestamp());
                            notificationMessage.put("message", "Answered your question");
                            notificationMessage.put("user_name", acct.getDisplayName());
                            notificationMessage.put("from", currentUserId);

                            firebaseFirestore.collection("user_bio/" + postUserid + "/notifications/")
                                    .add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    try {
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            firebaseFirestore.collection("user_bio/" + currentUserId + "/answer_list/")
                                    .add(answerMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    try {
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            if (task.isSuccessful()) {
                                answerFieldEt.setText("");
                                postAnswerIv.setEnabled(true);
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

        if (postUserid.equals(currentUserId)) {
            postAnswerLayout.setVisibility(View.GONE);
        }

        try {
            reportQuestionIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                            AnswerActivity.this, R.style.BottomSheetDialogTheme
                    );
                    final View bottomSheetView = LayoutInflater.from(getApplicationContext())
                            .inflate(
                                    R.layout.report_query_bottom_sheet,
                                    (LinearLayout) view.findViewById(R.id.bottom_sheet_container)
                            );
                    bottomSheetView.findViewById(R.id.sexual_activity_layout).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            reportAnswer(bottomSheetDialog, queryPostId, postUserid, "Sexual activity");
                        }
                    });
                    bottomSheetView.findViewById(R.id.scam_layout).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            reportAnswer(bottomSheetDialog, queryPostId, postUserid, "Scam or fraud");
                        }
                    });
                    bottomSheetView.findViewById(R.id.hate_speech_layout).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            reportAnswer(bottomSheetDialog, queryPostId, postUserid, "Bullying or hate words or symbols");
                        }
                    });
                    bottomSheetView.findViewById(R.id.irrelevant_layout).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            reportAnswer(bottomSheetDialog, queryPostId, postUserid, "Irrelevant or violent");
                        }
                    });
                    bottomSheetView.findViewById(R.id.emergency_message_layout).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bottomSheetDialog.dismiss();
                            Intent emergencyActivity = new Intent(AnswerActivity.this, EmergencyReportActivity.class);
                            startActivity(emergencyActivity);
                        }
                    });
                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                            String imageUrl = task.getResult().getString("image_url");
                            Boolean isSolved = task.getResult().getBoolean("is_solved");
                            queryPostUserId = task.getResult().getString("user_id");

                            titleView.setText(title);
                            bodyView.setText(body);

                            if (isSolved.equals(true)) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    isQuerySolvedIv.setImageDrawable(getApplication().getDrawable(R.drawable.ic_baseline_check_circle_24));
                                }
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    isQuerySolvedIv.setImageDrawable(getApplication().getDrawable(R.drawable.ic_outline_check_circle_24_white));
                                }
                            }

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
                                                RequestOptions requestOptions = new RequestOptions();
                                                requestOptions.placeholder(R.drawable.ic_baseline_image_24);
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

    public void reportAnswer(final BottomSheetDialog bottomSheetDialog,
                             String queryPostId, String user_id, String message) {
        Map<String, Object> reportAnswer = new HashMap<>();
        reportAnswer.put("query_post_id", queryPostId);
        reportAnswer.put("user_id", user_id);
        reportAnswer.put("report_message", message);

        firebaseFirestore.collection("reported_query_posts").add(reportAnswer)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            bottomSheetDialog.dismiss();
                            Toast.makeText(AnswerActivity.this, "Thanks for reporting, you'll receive status of this report soon.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            bottomSheetDialog.dismiss();
                            Toast.makeText(AnswerActivity.this, "Failed to report, check your internet", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}