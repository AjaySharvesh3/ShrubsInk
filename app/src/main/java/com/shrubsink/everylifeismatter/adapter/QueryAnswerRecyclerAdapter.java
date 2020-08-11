package com.shrubsink.everylifeismatter.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.shrubsink.everylifeismatter.AnswerActivity;
import com.shrubsink.everylifeismatter.R;
import com.shrubsink.everylifeismatter.model.QueryAnswer;

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
        String user_id = answerList.get(position).getUser_id();
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

        //Get Upvote
        new Thread(new Runnable() {
            public void run() {
                firebaseFirestore.collection("query_posts/" + queryPostId + "/answers/" + queryAnswerId + "/upvotes")
                        .document(currentUserId)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                if (documentSnapshot.exists()) {
                                    holder.upVoteBtn.setImageDrawable(context.getDrawable(R.drawable.upvote_color));
                                } else {
                                    holder.upVoteBtn.setImageDrawable(context.getDrawable(R.drawable.upvote));
                                }
                            }
                        });
            }
        }).start();

        //UpVote Feature
        new Thread(new Runnable() {
            public void run() {
                holder.upVoteBtn.setOnClickListener(new View.OnClickListener() {
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
                                if (!documentSnapshots.isEmpty()) {
                                    int count = documentSnapshots.size();
                                    Log.d("count", ""+ count);
                                    holder.updateVoteCount(count);
                                } else {
                                    holder.updateVoteCount(0);
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
                                if (documentSnapshot.exists()) {
                                    holder.downVoteBtn.setImageDrawable(context.getDrawable(R.drawable.downvote_color));
                                } else {
                                    holder.downVoteBtn.setImageDrawable(context.getDrawable(R.drawable.donwvote));
                                }
                            }
                        });
            }
        }).start();

        //DownVote Feature
        new Thread(new Runnable() {
            public void run() {
                holder.downVoteBtn.setOnClickListener(new View.OnClickListener() {
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
                                if (!documentSnapshots.isEmpty()) {
                                    int count = documentSnapshots.size();
                                    holder.updateDownVoteCount(count);
                                } else {
                                    holder.updateDownVoteCount(0);
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
                View bottomSheetView = LayoutInflater.from(context.getApplicationContext())
                        .inflate(
                                R.layout.answer_activity_bottom_sheet,
                                (LinearLayout) view.findViewById(R.id.bottom_sheet_container)
                        );
                bottomSheetView.findViewById(R.id.violation_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "Violation Clicked", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
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
        public TextView username;
        public ImageView userImage;

        ImageView upVoteBtn, downVoteBtn, reportOptionIv;
        TextView voteCountTv, downVoteCountTv;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            upVoteBtn = mView.findViewById(R.id.upvote_iv);
            downVoteBtn = mView.findViewById(R.id.downvote_iv);
            voteCountTv = mView.findViewById(R.id.vote_count_tv);
            downVoteCountTv = mView.findViewById(R.id.downvote_count_tv);
            reportOptionIv = mView.findViewById(R.id.report_option_iv);
        }

        public void setComment_message(String answer){
            answerContent = mView.findViewById(R.id.answer_content_tv);
            answerContent.setText(answer);
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
