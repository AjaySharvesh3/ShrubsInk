package com.shrubsink.everylifeismatter;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class QueryFragment extends Fragment implements View.OnClickListener  {

    CircleImageView mProfilePicture;
    TextView mUsernameTv;
    FirebaseAuth mFirebaseAuth;
    GoogleSignInClient googleSignInClient;

    public QueryFragment() {

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_query, container, false);

        mProfilePicture = view.findViewById(R.id.profile_image);
        mUsernameTv = view.findViewById(R.id.username_tv);

        view.findViewById(R.id.profile_image).setOnClickListener(this);

        googleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), GoogleSignInOptions.DEFAULT_SIGN_IN);
        mFirebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(Objects.requireNonNull(getActivity()));
        if (acct != null) {
            String personName = acct.getDisplayName();
            Uri personPhoto = acct.getPhotoUrl();

            mUsernameTv.setText(personName);
            Glide.with(this).load(personPhoto).placeholder(R.drawable.profile_placeholder).into(mProfilePicture);
        }

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_image:
                goToProfile();
                break;
        }
    }

    private void goToProfile() {
        Intent profileActivity = new Intent(getActivity(), ProfileActivity.class);
        startActivity(profileActivity);
    }
}