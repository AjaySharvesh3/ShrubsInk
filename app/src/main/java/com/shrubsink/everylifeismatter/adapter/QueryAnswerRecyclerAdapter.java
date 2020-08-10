package com.shrubsink.everylifeismatter.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shrubsink.everylifeismatter.AnswerActivity;
import com.shrubsink.everylifeismatter.R;
import com.shrubsink.everylifeismatter.model.QueryAnswer;

import java.util.List;

public class QueryAnswerRecyclerAdapter extends RecyclerView.Adapter<QueryAnswerRecyclerAdapter.ViewHolder> {

    public AnswerActivity answerActivity;
    public List<QueryAnswer> answerList;
    public Context context;

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
        String answerMessage = answerList.get(position).getAnswer();
        holder.setComment_message(answerMessage);
        /*holder.username.setText(answerList.get(position).getUserName());*/
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

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
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

    }
}
