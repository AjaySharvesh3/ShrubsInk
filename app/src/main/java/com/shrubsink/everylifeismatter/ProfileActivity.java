package com.shrubsink.everylifeismatter;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileActivity extends AppCompatActivity {

    TextView mUsername;
    TextView mEmail;
    CircleImageView mProfilePicture;
    Button mAddAddressBtn, mAddGeneralButton;
    ImageView mEditAddressIv, mEditGeneralIv, mCloseActivityIv;
    TextView mAddressLineTv, mCityPincodeTv, mStateCountryTv;
    TextView mShortBioTv, mDesignationTv, mPhoneTv, mGenderAgeTv;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirebaseFirestore;
    String mUserId;
    static ProgressDialog mProgressDialog;
    String address_line, city, pincode, state, country;
    String gender, age, phone, short_bio, designation;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        showProgressDialog(this, "Loading...","Collecting your profile..",false);

        /*Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);*/

        mUsername = findViewById(R.id.username_et);
        mEmail = findViewById(R.id.email_et);
        mProfilePicture = findViewById(R.id.profile_image);
        mAddAddressBtn = findViewById(R.id.add_address_btn);
        mAddGeneralButton = findViewById(R.id.add_personal_btn);
        mEditAddressIv = findViewById(R.id.edit_address_iv);
        mEditGeneralIv = findViewById(R.id.edit_general_iv);

        mAddressLineTv = findViewById(R.id.address_line_tv);
        mCityPincodeTv = findViewById(R.id.city_pincode_tv);
        mStateCountryTv = findViewById(R.id.state_country_tv);

        mShortBioTv = findViewById(R.id.short_bio_tv);
        mDesignationTv = findViewById(R.id.designation_tv);
        mPhoneTv = findViewById(R.id.phone_tv);
        mGenderAgeTv = findViewById(R.id.gender_age_tv);

        mCloseActivityIv = findViewById(R.id.profile_close_activity);

        mCloseActivityIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(ProfileActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();

            mUsername.setText(personName);
            mEmail.setText(personEmail);
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        mUserId = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();

        FirebaseUser account = mFirebaseAuth.getCurrentUser();
        if (account != null){
            String personImage = Objects.requireNonNull(account.getPhotoUrl()).toString();
            Glide.with(this).load(personImage).placeholder(R.drawable.profile_placeholder).into(mProfilePicture);
        }

        fetchAddress();
        fetchGeneral();

        addAddress();
        editAddress();
        addGeneral();
        editGeneral();
    }

    public void fetchAddress() {
        DocumentReference mDocumentReference = mFirebaseFirestore.collection("user_bio").document(mUserId).collection("address").document(mUserId);
        mDocumentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                removeProgressDialog();
                mAddAddressBtn.setVisibility(View.GONE);
                mEditAddressIv.setVisibility(View.VISIBLE);

                mAddressLineTv.setText(value.getString("address_line"));
                mCityPincodeTv.setText(value.getString("city") + ", " + value.getString("pincode"));
                mStateCountryTv.setText(value.getString("state") + ", " + value.getString("country"));

                address_line = value.getString("address_line");
                city = value.getString("city");
                pincode = value.getString("pincode");
                state = value.getString("state");
                country = value.getString("country");

                if (value.getString("address_line") == null) {
                    removeProgressDialog();
                    mAddAddressBtn.setVisibility(View.VISIBLE);

                    mAddressLineTv.setVisibility(View.GONE);
                    mCityPincodeTv.setVisibility(View.GONE);
                    mStateCountryTv.setVisibility(View.GONE);
                    mEditAddressIv.setVisibility(View.GONE);
                }
            }
        });
    }

    public void fetchGeneral() {
        DocumentReference mDocumentReference = mFirebaseFirestore.collection("user_bio").document(mUserId).collection("personal").document(mUserId);
        mDocumentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                removeProgressDialog();
                mAddGeneralButton.setVisibility(View.GONE);
                mEditGeneralIv.setVisibility(View.VISIBLE);

                mShortBioTv.setText(value.getString("short_bio"));
                mDesignationTv.setText(value.getString("designation"));
                mPhoneTv.setText(value.getString("phone"));
                mGenderAgeTv.setText(value.getString("age") + ", " + value.getString("gender"));

                phone = value.getString("phone");
                age = value.getString("age");
                gender = value.getString("gender");
                short_bio = value.getString("short_bio");
                designation = value.getString("designation");

                if (value.getString("phone") == null) {
                    removeProgressDialog();
                    mAddGeneralButton.setVisibility(View.VISIBLE);

                    mShortBioTv.setVisibility(View.GONE);
                    mPhoneTv.setVisibility(View.GONE);
                    mDesignationTv.setVisibility(View.GONE);
                    mGenderAgeTv.setVisibility(View.GONE);
                    mEditGeneralIv.setVisibility(View.GONE);
                }
            }
        });
    }


    public static void showProgressDialog(Context context, String title,
                                          String msg, boolean isCancelable) {
        try {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(context, title, msg);
                mProgressDialog.setCancelable(isCancelable);
            }

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }

        } catch (IllegalArgumentException ie) {
            ie.printStackTrace();
        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void removeProgressDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
        } catch (IllegalArgumentException ie) {
            ie.printStackTrace();

        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addAddress() {
        mAddAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addAddressActivity = new Intent(ProfileActivity.this, AddressActivity.class);
                startActivity(addAddressActivity);
            }
        });
    }

    public void editAddress() {
        mEditAddressIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editAddressActivity = new Intent(ProfileActivity.this, AddressActivity.class);
                editAddressActivity.putExtra("address_line", address_line);
                editAddressActivity.putExtra("city", city);
                editAddressActivity.putExtra("pincode", pincode);
                editAddressActivity.putExtra("state", state);
                editAddressActivity.putExtra("country", country);
                startActivity(editAddressActivity);
            }
        });
    }

    public void addGeneral() {
        mAddGeneralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addGeneralActivity = new Intent(ProfileActivity.this, PersonalActivity.class);
                startActivity(addGeneralActivity);
            }
        });
    }

    public void editGeneral() {
        mEditGeneralIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editGeneralActivity = new Intent(ProfileActivity.this, PersonalActivity.class);
                editGeneralActivity.putExtra("gender", gender);
                editGeneralActivity.putExtra("age", age);
                editGeneralActivity.putExtra("phone", phone);
                editGeneralActivity.putExtra("short_bio", short_bio);
                editGeneralActivity.putExtra("designation", designation);
                startActivity(editGeneralActivity);
            }
        });
    }

}