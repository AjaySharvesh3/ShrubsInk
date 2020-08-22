package com.shrubsink.everylifeismatter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.shrubsink.everylifeismatter.adapter.NotificationAdapter;
import com.shrubsink.everylifeismatter.adapter.QueryAnswerRecyclerAdapter;
import com.shrubsink.everylifeismatter.adapter.QueryPostRecyclerAdapter;
import com.shrubsink.everylifeismatter.model.Notification;
import com.shrubsink.everylifeismatter.model.QueryAnswer;
import com.shrubsink.everylifeismatter.model.QueryPost;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.supercharge.shimmerlayout.ShimmerLayout;


public class NotificationFragment extends Fragment {

    FirebaseAuth mFirebaseAuth;
    RecyclerView notificationRecyclerView;
    List<Notification> notificationList;
    static ProgressDialog mProgressDialog;
    String currentUserId;
    private FirebaseFirestore firebaseFirestore;
    private NotificationAdapter notificationAdapter;
    private ImageView noNotificationIv;

    ShimmerLayout shimmerLayout;

    public NotificationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUserId = mFirebaseAuth.getCurrentUser().getUid();

        notificationRecyclerView = view.findViewById(R.id.notification_list);
        noNotificationIv = view.findViewById(R.id.no_notification_iv);

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList, this);
        notificationRecyclerView.setHasFixedSize(true);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationRecyclerView.setAdapter(notificationAdapter);

        Log.d("notificationsssss", "yessssssssssss");

        firebaseFirestore.collection("user_bio/" + currentUserId + "/notifications")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {
                                noNotificationIv.setVisibility(View.GONE);
                                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        String notificationId = doc.getDocument().getId();
                                        Notification notification = doc.getDocument().toObject(Notification.class).withId(notificationId);
                                        Log.d("notificationsssss", notification + "");
                                        notificationList.add(notification);
                                        Log.d("notificationsssssSS", notificationList + "");
                                        notificationAdapter.notifyDataSetChanged();
                                    }
                                }
                                Log.d("NOTIFY", notificationList + "");
                            } else {
                                noNotificationIv.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception er) {
                            er.printStackTrace();
                        }
                    }
                });

        return view;
    }

    public void clearAll() {
        /*firebaseFirestore.collection("user_bio/" + currentUserId + "/notifications/")
                .whereEqualTo("user_id", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {
                                clearAllNotificationTv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        clearAllNotificationTv.setEnabled(true);

                                    }
                                });
                            } else {
                                clearAllNotificationTv.setEnabled(false);
                                clearAllNotificationTv.setTextColor(R.color.grey);
                            }
                        } catch (Exception ex) {
                            Log.d("Logout Error", "Error: " + ex);
                        }
                    }
                });*/
    }
}