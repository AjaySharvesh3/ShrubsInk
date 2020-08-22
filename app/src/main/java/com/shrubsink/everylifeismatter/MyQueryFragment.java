package com.shrubsink.everylifeismatter;

import android.app.ProgressDialog;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.shrubsink.everylifeismatter.adapter.MyQueryPostAdapter;
import com.shrubsink.everylifeismatter.adapter.QueryPostRecyclerAdapter;
import com.shrubsink.everylifeismatter.model.QueryPost;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.supercharge.shimmerlayout.ShimmerLayout;


public class MyQueryFragment extends Fragment {

    FirebaseAuth mFirebaseAuth;
    GoogleSignInClient googleSignInClient;
    String currentUserId;

    private RecyclerView query_list_view;
    private List<QueryPost> query_list;
    static ProgressDialog mProgressDialog;
    ImageView noDataFoundIv;
    private FirebaseFirestore firebaseFirestore;
    private MyQueryPostAdapter myQueryPostAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    ShimmerLayout shimmerLayout;


    public MyQueryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_query, container, false);

        /*shimmerLayout = view.findViewById(R.id.shimmer_layout);*/

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN);
        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUserId = mFirebaseAuth.getCurrentUser().getUid();

        query_list = new ArrayList<>();
        query_list_view = view.findViewById(R.id.query_list_view);
        noDataFoundIv = view.findViewById(R.id.no_data_found_iv);

        myQueryPostAdapter = new MyQueryPostAdapter(query_list);
        query_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        query_list_view.setAdapter(myQueryPostAdapter);
        query_list_view.setHasFixedSize(true);

        listQueries();

        return view;
    }

    public void listQueries() {
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

            try {
                new Thread(new Runnable() {
                    public void run() {
                        /*shimmerLayout.startShimmerAnimation();*/
                        Query firstQuery = firebaseFirestore.collection("query_posts")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .whereEqualTo("user_id", currentUserId);
                        firstQuery.addSnapshotListener(requireActivity(), new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                try {
                                    if (!documentSnapshots.isEmpty()) {
                                        noDataFoundIv.setVisibility(View.GONE);
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
                                                myQueryPostAdapter.notifyDataSetChanged();
                                            }
                                        }
                                        isFirstPageFirstLoad = false;
                                    } else {
                                        noDataFoundIv.setVisibility(View.VISIBLE);
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
            } catch(Exception er) {
                er.printStackTrace();
            }
        }

    }

    public void loadMoreQueries() {
        /*shimmerLayout.startShimmerAnimation();*/
        if (mFirebaseAuth.getCurrentUser() != null) {
            Query nextQuery = firebaseFirestore.collection("query_posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .whereEqualTo("user_id", currentUserId)
                    .startAfter(lastVisible);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    try {
                        if (!documentSnapshots.isEmpty()) {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                if (documentSnapshots != null) {
                                    noDataFoundIv.setVisibility(View.GONE);
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        /*shimmerLayout.stopShimmerAnimation();
                                        shimmerLayout.setVisibility(View.GONE);*/
                                        String queryPostId = doc.getDocument().getId();
                                        QueryPost queryPost = doc.getDocument().toObject(QueryPost.class).withId(queryPostId);
                                        query_list.add(queryPost);
                                        myQueryPostAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        } else {
                            noDataFoundIv.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception ex) {
                        Log.d("Logout Error", "Error: " + ex);
                    }
                }
            });
        }
    }
}