package com.shrubsink.everylifeismatter;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class CreditsFragment extends Fragment {

    Button viewCreditBtn;
    TextView totalCreditPointTv, questionCreditTv, answerCreditTv, solvedCreditTv, codeOfConductTv;
    TextView totalQuestionCreditDetailTv, totalAnswerCreditDetailTv, totalCorrectAnswerCreditDetailTv;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirebaseFirestore;
    String currentUserId;
    int questionCount, answerCount, solvedAnswerCount;

    public CreditsFragment() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_credits, container, false);

        viewCreditBtn = view.findViewById(R.id.view_credit_btn);
        totalCreditPointTv = view.findViewById(R.id.total_credits_point_tv);
        questionCreditTv = view.findViewById(R.id.question_credit_btn);
        answerCreditTv = view.findViewById(R.id.total_answer_credit_btn);
        solvedCreditTv = view.findViewById(R.id.total_correct_answer_credit_btn);
        totalQuestionCreditDetailTv = view.findViewById(R.id.question_credit_detail_tv);
        totalAnswerCreditDetailTv = view.findViewById(R.id.total_answer_credit_detail_tv);
        totalCorrectAnswerCreditDetailTv = view.findViewById(R.id.total_correct_answer_credit_details_tv);
        codeOfConductTv = view.findViewById(R.id.code_of_conduct_tv);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        currentUserId = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();

        viewCreditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        requireContext(), R.style.BottomSheetDialogTheme
                );
                final View bottomSheetView = LayoutInflater.from(requireActivity().getApplicationContext())
                        .inflate(
                                R.layout.credits_view_bottom_sheet,
                                (ScrollView) view.findViewById(R.id.bottom_sheet_container_credits)
                        );
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

        codeOfConductTv.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                Intent codeOfConduct = new Intent(getContext(), CodeOfConductActivity.class);
                startActivity(codeOfConduct, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        countOfQuestionCredit();
        countOfAnswerCredit();
        countOfSolvedAnswerCredit();

        countOfAllCredit();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        /*overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);*/
    }

    public void countOfQuestionCredit() {
        mFirebaseFirestore.collection("query_posts")
                .whereEqualTo("user_id", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {
                                questionCount = documentSnapshots.size();
                                questionCreditTv.setText(questionCount*10 + " Credits");
                                totalQuestionCreditDetailTv.setText("10 credits * " + questionCount + " questions");
                            } else {
                                questionCreditTv.setText(0 + " Credits");
                                totalQuestionCreditDetailTv.setText("10 credits * " + 0 + " question");
                            }
                        } catch (Exception ex) {
                            Log.d("Logout Error", "Error: " + ex);
                        }
                    }
                });
    }

    public void countOfAnswerCredit() {
        mFirebaseFirestore.collection("user_bio/" + currentUserId + "/answer_list/")
                .whereEqualTo("user_id", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {
                                answerCount = documentSnapshots.size();
                                answerCreditTv.setText(answerCount*20 + " Credits");
                                totalAnswerCreditDetailTv.setText("20 credits * " + answerCount + " answers");
                            } else {
                                answerCreditTv.setText(0 + " Credits");
                                totalAnswerCreditDetailTv.setText("20 credits * " + 0 + " answer");
                            }
                        } catch (Exception ex) {
                            Log.d("Logout Error", "Error: " + ex);
                        }
                    }
                });
    }

    public void countOfSolvedAnswerCredit() {
        mFirebaseFirestore.collection("user_bio/" + currentUserId + "/your_solutions/")
                .whereEqualTo("user_id", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {
                                solvedAnswerCount = documentSnapshots.size();
                                solvedCreditTv.setText(solvedAnswerCount * 50 + " Credits");
                                totalCorrectAnswerCreditDetailTv.setText("50 credits * " + solvedAnswerCount + " solved answers");
                            } else {
                                solvedCreditTv.setText(0 + " Credits");
                                totalCorrectAnswerCreditDetailTv.setText("50 credits * " + 0 + " solved answer");
                            }
                        } catch (Exception ex) {
                            Log.d("Logout Error", "Error: " + ex);
                        }
                    }
                });
    }

    public void countOfAllCredit() {
        mFirebaseFirestore.collection("query_posts")
                .whereEqualTo("user_id", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {
                                questionCount = documentSnapshots.size();
                                questionCount *= 10;
                                totalCreditPointTv.setText(questionCount + " Credits");

                                mFirebaseFirestore.collection("user_bio/" + currentUserId + "/answer_list/")
                                        .whereEqualTo("user_id", currentUserId)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                                try {
                                                    if (!documentSnapshots.isEmpty()) {
                                                        answerCount = documentSnapshots.size();
                                                        answerCount *= 20;
                                                        answerCount += questionCount;
                                                        totalCreditPointTv.setText(answerCount + " Credits");

                                                        mFirebaseFirestore.collection("user_bio/" + currentUserId + "/your_solutions/")
                                                                .whereEqualTo("user_id", currentUserId)
                                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                    @SuppressLint("SetTextI18n")
                                                                    @Override
                                                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                                                        try {
                                                                            if (!documentSnapshots.isEmpty()) {
                                                                                solvedAnswerCount = documentSnapshots.size();
                                                                                solvedAnswerCount *= 50;
                                                                                solvedAnswerCount += answerCount;
                                                                                totalCreditPointTv.setText(solvedAnswerCount +
                                                                                        " Credits");
                                                                            } else {
                                                                                totalCreditPointTv.setText(answerCount + " Credits");
                                                                            }
                                                                        } catch (Exception ex) {
                                                                            Log.d("Logout Error", "Error: " + ex);
                                                                        }
                                                                    }
                                                                });

                                                    } else {
                                                        totalCreditPointTv.setText(questionCount + " Credits");
                                                    }
                                                } catch (Exception ex) {
                                                    Log.d("Logout Error", "Error: " + ex);
                                                }
                                            }
                                        });

                            } else {
                                totalCreditPointTv.setText(0 + " Credits");
                            }
                        } catch (Exception ex) {
                            Log.d("Logout Error", "Error: " + ex);
                        }
                    }
                });
    }
}