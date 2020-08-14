package com.shrubsink.everylifeismatter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import androidx.annotation.Nullable;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.shrubsink.everylifeismatter.adapter.QueryPostRecyclerAdapter;
import com.shrubsink.everylifeismatter.model.QueryPost;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.disposables.CompositeDisposable;
import io.supercharge.shimmerlayout.ShimmerLayout;


public class QueryFragment extends Fragment implements View.OnClickListener {

    CircleImageView mProfilePicture;
    TextView mUsernameTv;
    FirebaseAuth mFirebaseAuth;
    GoogleSignInClient googleSignInClient;

    private RecyclerView query_list_view;
    private List<QueryPost> query_list;
    static ProgressDialog mProgressDialog;

    private FirebaseFirestore firebaseFirestore;
    private QueryPostRecyclerAdapter queryPostRecyclerAdapter;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    ShimmerLayout shimmerLayout;

    public QueryFragment() {

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_query, container, false);

        mProfilePicture = view.findViewById(R.id.profile_image);
        mUsernameTv = view.findViewById(R.id.username_tv);
        /*shimmerLayout = view.findViewById(R.id.shimmer_layout);*/

        view.findViewById(R.id.profile_image).setOnClickListener(this);

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN);
        mFirebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(requireActivity());
        if (acct != null) {
            String personName = acct.getDisplayName();
            Uri personPhoto = acct.getPhotoUrl();

            mUsernameTv.setText("Hello, " + personName);
            Glide.with(this).load(personPhoto).placeholder(R.drawable.profile_placeholder).into(mProfilePicture);
        }

        query_list = new ArrayList<>();
        query_list_view = view.findViewById(R.id.query_list_view);

        queryPostRecyclerAdapter = new QueryPostRecyclerAdapter(query_list);
        query_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        query_list_view.setAdapter(queryPostRecyclerAdapter);
        query_list_view.setHasFixedSize(true);

        if (mFirebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            query_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom) {
                        loadMoreQueries();
                    }
                }
            });

            new Thread(new Runnable() {
                public void run() {
                    /*shimmerLayout.startShimmerAnimation();*/
                    Query firstQuery = firebaseFirestore.collection("query_posts")
                            .orderBy("timestamp", Query.Direction.DESCENDING);
                    firstQuery.addSnapshotListener(requireActivity(), new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                            try {
                                if (!documentSnapshots.isEmpty()) {
                                    if (isFirstPageFirstLoad) {
                                        /*shimmerLayout.stopShimmerAnimation();
                                        shimmerLayout.setVisibility(View.GONE);*/
                                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                        query_list.clear();
                                    }
                                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                        if (documentSnapshots != null) {
                                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                                String queryPostPostId = doc.getDocument().getId();
                                                QueryPost queryPost = doc.getDocument().toObject(QueryPost.class).withId(queryPostPostId);
                                                if (isFirstPageFirstLoad) {
                                                    /*shimmerLayout.stopShimmerAnimation();
                                                    shimmerLayout.setVisibility(View.GONE);*/
                                                    query_list.add(queryPost);
                                                } else {
                                                    /*shimmerLayout.stopShimmerAnimation();
                                                    shimmerLayout.setVisibility(View.GONE);*/
                                                    query_list.add(0, queryPost);
                                                }
                                            }
                                            queryPostRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    }
                                    isFirstPageFirstLoad = false;
                                }
                            } catch (Exception ex) {
                               /* shimmerLayout.stopShimmerAnimation();
                                shimmerLayout.setVisibility(View.GONE);*/
                                Log.d("Error", "Error: " + ex);
                            }

                        }

                    });
                }
            }).start();
        }
        return view;
    }

    public void loadMoreQueries() {
        /*shimmerLayout.startShimmerAnimation();*/
        if (mFirebaseAuth.getCurrentUser() != null) {
            Query nextQuery = firebaseFirestore.collection("query_posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    try {
                        if (!documentSnapshots.isEmpty()) {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                if (documentSnapshots != null) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        /*shimmerLayout.stopShimmerAnimation();
                                        shimmerLayout.setVisibility(View.GONE);*/
                                        String queryPostId = doc.getDocument().getId();
                                        QueryPost queryPost = doc.getDocument().toObject(QueryPost.class).withId(queryPostId);
                                        query_list.add(queryPost);
                                        queryPostRecyclerAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Log.d("Logout Error", "Error: " + ex);
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_image:
                /*goToProfile();*/
                break;
        }
    }

   /* private void goToProfile() {
        Intent profileActivity = new Intent(getActivity(), ProfileActivity.class);
        startActivity(profileActivity);
    }*/
}