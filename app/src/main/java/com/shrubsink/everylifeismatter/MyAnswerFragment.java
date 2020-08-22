package com.shrubsink.everylifeismatter;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.shrubsink.everylifeismatter.adapter.MyAnswerAdapter;
import com.shrubsink.everylifeismatter.adapter.QueryAnswerRecyclerAdapter;
import com.shrubsink.everylifeismatter.model.QueryAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyAnswerFragment extends Fragment {

    RecyclerView answerRecyclerView;
    MyAnswerAdapter myAnswerAdapter;
    List<QueryAnswer> queryAnswerList;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    ImageView noDataFoundIv;

    String queryPostId;
    String postUserId;
    String currentUserId;
    String queryPostUserId;

    public MyAnswerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_answer, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid();

        answerRecyclerView = view.findViewById(R.id.answer_list);
        noDataFoundIv = view.findViewById(R.id.no_data_found_iv);

        /*fetchQueryPost();*/

        //RecyclerView Firebase List
        queryAnswerList = new ArrayList<>();
        myAnswerAdapter = new MyAnswerAdapter(queryAnswerList);
        answerRecyclerView.setHasFixedSize(true);
        answerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        answerRecyclerView.setAdapter(myAnswerAdapter);

        try {
            firebaseFirestore.collection("user_bio/" + currentUserId + "/answer_list/")
                    .whereEqualTo("user_id", currentUserId)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                            try {
                                if (!documentSnapshots.isEmpty()) {
                                    noDataFoundIv.setVisibility(View.GONE);
                                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                            String answerId = doc.getDocument().getId();
                                            QueryAnswer queryAnswer = doc.getDocument().toObject(QueryAnswer.class).withId(answerId);
                                            queryAnswerList.add(queryAnswer);
                                            myAnswerAdapter.notifyDataSetChanged();
                                        }
                                    }
                                } else {
                                    noDataFoundIv.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception er) {
                                er.printStackTrace();
                            }
                        }
                    });
        } catch (Exception er) {
            er.printStackTrace();
        }
        return view;
    }
}