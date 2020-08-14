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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.shrubsink.everylifeismatter.AnswerActivity;
import com.shrubsink.everylifeismatter.EmergencyReportActivity;
import com.shrubsink.everylifeismatter.R;
import com.shrubsink.everylifeismatter.model.QueryAnswer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryAnswerRecyclerAdapter extends RecyclerView.Adapter<QueryAnswerRecyclerAdapter.ViewHolder> {

    public AnswerActivity answerActivity;
    public List<QueryAnswer> answerList;
    public Context context;
    public int upVoteCount;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    public QueryAnswerRecyclerAdapter(List<QueryAnswer> answerList, AnswerActivity answerActivity){
        this.answerList = answerList;
        this.answerActivity = answerActivity;
    }

    @Override
    public QueryAnswerRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.answer_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new QueryAnswerRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final QueryAnswerRecyclerAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        final String queryAnswerId = answerList.get(position).QueryAnswerId;
        final String queryPostId = answerActivity.getIntent().getStringExtra("query_post_id");;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String answerMessage = answerList.get(position).getAnswer();
        final String user_id = answerList.get(position).getUser_id();
        holder.setComment_message(answerMessage);

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
                            Toast.makeText(answerActivity,
                                    context.getString(R.string.check_internet_connection),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

        try {
            long millisecond = answerList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("dd MMM yyyy • hh:mm a", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {
            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //Get UpVote
        new Thread(new Runnable() {
            public void run() {
                firebaseFirestore.collection("query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/upvotes")
                        .document(currentUserId)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                try {
                                    if (documentSnapshot.exists()) {
                                        holder.upVoteBtn.setImageDrawable(context.getDrawable(R.drawable.upvote_active));
                                    } else {
                                        holder.upVoteBtn.setImageDrawable(context.getDrawable(R.drawable.upvote));
                                    }
                                } catch (Exception er) {
                                    er.printStackTrace();
                                }
                            }
                        });
            }
        }).start();

        //UpVote Feature
        new Thread(new Runnable() {
            public void run() {
                holder.upVoteLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firebaseFirestore.collection("query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/upvotes")
                                .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (!task.getResult().exists()) {
                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());
                                    likesMap.put("user_id", currentUserId);
                                    firebaseFirestore.collection(
                                            "query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/upvotes")
                                            .document(currentUserId).set(likesMap);
                                    firebaseFirestore.collection(
                                            "query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/downvotes")
                                            .document(currentUserId).delete();
                                } else {
                                    firebaseFirestore.collection(
                                            "query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/upvotes")
                                            .document(currentUserId).delete();
                                }
                            }
                        });
                    }
                });
            }
        }).start();

        //Get UpVote Count
        new Thread(new Runnable() {
            public void run() {
                firebaseFirestore.collection("query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/upvotes")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                try {
                                    if (!documentSnapshots.isEmpty()) {
                                        int count = documentSnapshots.size();
                                        Log.d("count", "" + count);
                                        holder.updateVoteCount(count);
                                    } else {
                                        holder.updateVoteCount(0);
                                    }
                                } catch (Exception er) {
                                    er.printStackTrace();
                                }
                            }
                        });
            }
        }).start();



        //Get DownVote
        new Thread(new Runnable() {
            public void run() {
                firebaseFirestore.collection("query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/downvotes")
                        .document(currentUserId)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                try {
                                    if (documentSnapshot.exists()) {
                                        holder.downVoteBtn.setImageDrawable(context.getDrawable(R.drawable.downvote_active));
                                    } else {
                                        holder.downVoteBtn.setImageDrawable(context.getDrawable(R.drawable.downvote));
                                    }
                                } catch (Exception er) {
                                    er.printStackTrace();
                                }
                            }
                        });
            }
        }).start();

        //DownVote Feature
        new Thread(new Runnable() {
            public void run() {
                holder.downVoteLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firebaseFirestore.collection("query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/downvotes")
                                .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (!task.getResult().exists()) {
                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());
                                    likesMap.put("user_id", currentUserId);
                                    firebaseFirestore.collection(
                                            "query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/downvotes")
                                            .document(currentUserId).set(likesMap);
                                    firebaseFirestore.collection(
                                            "query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/upvotes")
                                            .document(currentUserId).delete();
                                } else {
                                    firebaseFirestore.collection(
                                            "query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/downvotes")
                                            .document(currentUserId).delete();
                                }
                            }
                        });
                    }
                });
            }
        }).start();

        //Get DownVote Count
        new Thread(new Runnable() {
            public void run() {
                firebaseFirestore.collection("query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/downvotes")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                try {
                                    if (!documentSnapshots.isEmpty()) {
                                        int count = documentSnapshots.size();
                                        holder.updateDownVoteCount(count);
                                    } else {
                                        holder.updateDownVoteCount(0);
                                    }
                                } catch (Exception er) {
                                    er.printStackTrace();
                                }
                            }
                        });
            }
        }).start();

        holder.reportOptionIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        answerActivity, R.style.BottomSheetDialogTheme
                );
                final View bottomSheetView = LayoutInflater.from(context.getApplicationContext())
                        .inflate(
                                R.layout.answer_activity_bottom_sheet,
                                (LinearLayout) view.findViewById(R.id.bottom_sheet_container)
                        );
                bottomSheetView.findViewById(R.id.sexual_activity_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reportAnswer(bottomSheetDialog, queryPostId, queryAnswerId, user_id, "Sexual activity");
                    }
                });
                bottomSheetView.findViewById(R.id.scam_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reportAnswer(bottomSheetDialog, queryPostId, queryAnswerId, user_id, "Scam or fraud");
                    }
                });
                bottomSheetView.findViewById(R.id.hate_speech_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reportAnswer(bottomSheetDialog, queryPostId, queryAnswerId, user_id, "Bullying or hate words or symbols");
                    }
                });
                bottomSheetView.findViewById(R.id.irrelevant_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reportAnswer(bottomSheetDialog, queryPostId, queryAnswerId, user_id, "Irrelevant or violent");
                    }
                });
                bottomSheetView.findViewById(R.id.emergency_message_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                        Intent emergencyActivity = new Intent(context, EmergencyReportActivity.class);
                        context.startActivity(emergencyActivity);
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
    }

    public void reportAnswer(final BottomSheetDialog bottomSheetDialog, String queryPostId, String queryAnswerId, String user_id, String message) {
        Map<String, Object> reportAnswer = new HashMap<>();
        reportAnswer.put("query_post_id", queryPostId);
        reportAnswer.put("answer_id", queryAnswerId);
        reportAnswer.put("user_id", user_id);
        reportAnswer.put("report_message", message);

        firebaseFirestore.collection("reported_answers").add(reportAnswer)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            bottomSheetDialog.dismiss();
                            Toast.makeText(context, "Thanks for reporting", Toast.LENGTH_LONG).show();
                        } else {
                            bottomSheetDialog.dismiss();
                            Toast.makeText(context, "Failed to report, check your internet", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        if(answerList != null) {
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

        ImageView upVoteBtn, downVoteBtn, reportOptionIv;
        LinearLayout upVoteLayout, downVoteLayout;
        TextView voteCountTv, downVoteCountTv;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            upVoteBtn = mView.findViewById(R.id.upvote_iv);
            upVoteLayout = mView.findViewById(R.id.upvote_layout);
            downVoteBtn = mView.findViewById(R.id.downvote_iv);
            downVoteLayout = mView.findViewById(R.id.downvote_layout);
            voteCountTv = mView.findViewById(R.id.vote_count_tv);
            downVoteCountTv = mView.findViewById(R.id.downvote_count_tv);
            reportOptionIv = mView.findViewById(R.id.report_option_iv);
        }

        public void setComment_message(String answer){
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

        @SuppressLint("SetTextI18n")
        public void updateVoteCount(int vote) {
            voteCountTv = mView.findViewById(R.id.vote_count_tv);
            voteCountTv.setText(vote + "");
        }

        @SuppressLint("SetTextI18n")
        public void updateDownVoteCount(int vote) {
            downVoteCountTv = mView.findViewById(R.id.downvote_count_tv);
            downVoteCountTv.setText(vote + "");
        }
    }
}
