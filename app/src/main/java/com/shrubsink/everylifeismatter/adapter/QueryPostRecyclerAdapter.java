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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.shrubsink.everylifeismatter.QueryFragment;
import com.shrubsink.everylifeismatter.R;
import com.shrubsink.everylifeismatter.model.QueryPost;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class QueryPostRecyclerAdapter extends RecyclerView.Adapter<QueryPostRecyclerAdapter.ViewHolder> {

    public List<QueryPost> query_list;
    public Context context;

    private FragmentActivity myContext;
    private QueryFragment thisFragment;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private MainActivity thisActivity;

    public QueryPostRecyclerAdapter(List<QueryPost> query_list) {
        this.query_list = query_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.query_list_item, parent, false);
        thisActivity = new MainActivity();
        thisFragment = new QueryFragment();
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        final String queryPostId = query_list.get(position).QueryPostId;
        final int credits = query_list.get(position).getCredits();
        final Boolean is_solved = query_list.get(position).getIs_solved();
        final String issue_location = query_list.get(position).getIssue_location();
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        final String title = query_list.get(position).getTitle();
        holder.setTitleText(title);

        final String body = query_list.get(position).getBody();
        holder.setBodyText(body);

        final String issueLocation = query_list.get(position).getIssue_location();
        holder.setIssueLocationText(issueLocation);

        final String tags = query_list.get(position).getTags();
        holder.setTagsText(tags);

        final String image_url = query_list.get(position).getImage_url();
        final String thumbUri = query_list.get(position).getImage_thumb();
        if (thumbUri != null && image_url != null) {
            /*holder.queryPostImageView.setVisibility(View.VISIBLE);*/
            holder.setQueryPostImage(image_url, thumbUri);
        } else {
            /* holder.queryPostImageView.setVisibility(View.INVISIBLE);*/
        }

        final String user_id = query_list.get(position).getUser_id();

        //User Data will be retrieved here...
        firebaseFirestore.collection("user_bio").document(user_id)
                .collection("personal").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try {
                            if (task.isSuccessful()) {
                                String userName = task.getResult().getString("name");
                                String userImage = task.getResult().getString("profile");
                                String shortBio = task.getResult().getString("short_bio");
                                holder.setUserData(userName, shortBio, userImage);
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
            long millisecond = query_list.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("dd MMM yyyy\nhh:mm a", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Get Comment Count
        firebaseFirestore.collection("query_posts/" + queryPostId + "/answers")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {
                                int count = documentSnapshots.size();
                                holder.updateCommentsCount(count);
                            } else {
                                holder.updateCommentsCount(0);
                            }
                        } catch (Exception ex) {
                            Log.d("Error", "Error: " + ex);
                        }
                    }
                });

        //Get Likes
        new Thread(new Runnable() {
            public void run() {
                firebaseFirestore.collection("query_posts/" + queryPostId + "/likes").document(currentUserId)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                try {
                                    if (documentSnapshot.exists()) {
                                        holder.queryPostLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_favorite_24));
                                    } else {
                                        holder.queryPostLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_favorite_border_24));
                                    }
                                } catch (Exception ex) {
                                    Log.d("Error", "Error: " + ex);
                                }
                            }
                        });
            }
        }).start();

        //Likes Feature
        new Thread(new Runnable() {
            public void run() {
                holder.queryPostLikeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (user_id.equals(currentUserId)) {
                            holder.queryPostLikeLayout.setEnabled(false);
                            Snackbar.make(holder.queryPostLikeLayout, "Sorry! You're not allowed to like your own query", Snackbar.LENGTH_LONG)
                                    .show();
                        } else {
                            firebaseFirestore.collection("query_posts/" + queryPostId + "/likes")
                                    .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    try {
                                        if (!task.getResult().exists()) {
                                            Map<String, Object> likesMap = new HashMap<>();
                                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                                            likesMap.put("user_id", currentUserId);
                                            firebaseFirestore.collection("query_posts/" + queryPostId + "/likes")
                                                    .document(currentUserId).set(likesMap);

                                            FirebaseUser acct = firebaseAuth.getCurrentUser();
                                            Map<String, Object> notificationMessage = new HashMap<>();
                                            notificationMessage.put("timestamp", FieldValue.serverTimestamp());
                                            notificationMessage.put("message", "Liked your question");
                                            notificationMessage.put("user_name", acct.getDisplayName());
                                            notificationMessage.put("from", currentUserId);
                                            try {
                                            firebaseFirestore.collection("user_bio/" + user_id + "/notifications/")
                                                    .add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                }
                                            });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            firebaseFirestore.collection("query_posts/" + queryPostId + "/likes")
                                                    .document(currentUserId).delete();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }).start();

        //Get Likes Count
        new Thread(new Runnable() {
            public void run() {
                firebaseFirestore.collection("query_posts/" + queryPostId + "/likes")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                try {
                                    if (!documentSnapshots.isEmpty()) {
                                        int count = documentSnapshots.size();
                                        holder.updateLikesCount(count);
                                    } else {
                                        holder.updateLikesCount(0);
                                    }
                                } catch (Exception ex) {
                                    Log.d("Logout Error", "Error: " + ex);
                                }
                            }
                        });
            }
        }).start();


        holder.queryPostAnswersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent answerIntent = new Intent(context, AnswerActivity.class);
                answerIntent.putExtra("query_post_id", queryPostId);
                answerIntent.putExtra("user_id", user_id);
                answerIntent.putExtra("title", title);
                answerIntent.putExtra("body", body);
                answerIntent.putExtra("tags", tags);
                answerIntent.putExtra("image_url", image_url);
                answerIntent.putExtra("image_thumb", thumbUri);
                answerIntent.putExtra("credits", credits);
                answerIntent.putExtra("is_solved", is_solved);
                answerIntent.putExtra("issue_location", issue_location);
                context.startActivity(answerIntent);
            }
        });


        if (is_solved.equals(true)) {
            holder.isSolvedQueryItemIv.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_check_circle_24));
            holder.isSolvedTv.setText("Solution Found");
        } else {
            holder.isSolvedQueryItemIv.setImageDrawable(context.getDrawable(R.drawable.ic_outline_check_circle_24));
            holder.isSolvedTv.setText("Not yet solved");
        }
    }


    @Override
    public int getItemCount() {
        return query_list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView titleView, bodyView, issueLocationView, tagsView;
        private ImageView queryPostImageView;
        private TextView queryPostDate;

        private TextView queryPostUserName;
        private CircleImageView queryPostUserImage;

        private ImageView queryPostLikeBtn;
        private LinearLayout queryPostLikeLayout, queryPostAnswersLayout;
        private TextView queryPostLikeCount;
        private TextView queryPostAnswersCount;
        private TextView isSolvedTv;
        private ImageView isSolvedQueryItemIv;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            queryPostLikeBtn = mView.findViewById(R.id.likes_icon);
            queryPostLikeCount = mView.findViewById(R.id.like_count_tv);
            queryPostLikeLayout = mView.findViewById(R.id.like_layout);
            queryPostAnswersLayout = mView.findViewById(R.id.answer_layout);
            isSolvedTv = mView.findViewById(R.id.is_solved_tv);
            isSolvedQueryItemIv = mView.findViewById(R.id.is_solved_query_item_iv);
        }

        public void setTitleText(String titleText) {
            titleView = mView.findViewById(R.id.title_tv);
            titleView.setText(titleText);
        }

        public void setBodyText(String bodyText) {
            bodyView = mView.findViewById(R.id.body_tv);
            bodyView.setText(bodyText);
        }

        public void setIssueLocationText(String issueLocationText) {
            issueLocationView = mView.findViewById(R.id.issue_location_tv);
            issueLocationView.setText(issueLocationText);
        }

        public void setTagsText(String tagsText) {
            String replacedTags = tagsText.replace(", ", " • ");
            tagsView = mView.findViewById(R.id.tags_tv);
            tagsView.setText(replacedTags);
        }

        @SuppressLint("CheckResult")
        public void setQueryPostImage(String downloadUri, String thumbUri) {

            queryPostImageView = mView.findViewById(R.id.query_image_iv);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.ic_baseline_image_24);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(
                    Glide.with(context).load(thumbUri)
            ).into(queryPostImageView);
        }

        public void setTime(String date) {
            queryPostDate = mView.findViewById(R.id.timestamp_tv);
            queryPostDate.setText(date);
        }

        @SuppressLint("CheckResult")
        public void setUserData(String name, String shortBio, String image) {
            queryPostUserImage = mView.findViewById(R.id.profile_image);

            queryPostUserName = mView.findViewById(R.id.username_tv);
            queryPostUserName.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(queryPostUserImage);
        }

        @SuppressLint("SetTextI18n")
        public void updateLikesCount(int count) {
            queryPostLikeCount = mView.findViewById(R.id.like_count_tv);
            queryPostLikeCount.setText(count + " likes");
        }

        @SuppressLint("SetTextI18n")
        public void updateCommentsCount(int count) {
            queryPostAnswersCount = mView.findViewById(R.id.answer_count_tv);
            queryPostAnswersCount.setText(count + " answers");
        }
    }

}
