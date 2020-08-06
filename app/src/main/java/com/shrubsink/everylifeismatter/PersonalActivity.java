package com.shrubsink.everylifeismatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PersonalActivity extends AppCompatActivity {

    ImageView mCloseActivityIv;
    TextInputLayout mGenderTIL, mAgeTIL, mPhoneTIL, mShortBioTIL, mDesignationTIL;
    TextInputEditText mGenderEt, mAgeEt, mPhoneEt, mShortBioEt, mDesignationEt;
    Button savePersonalBtn;
    static ProgressDialog mProgressDialog;
    FirebaseFirestore mFirebaseFirestore;
    FirebaseAuth mFirebaseAuth;
    String mUserId;
    String phone, age, gender, short_bio, designation;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        /*Toolbar toolbar = findViewById(R.id.general_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);*/

        mGenderTIL = findViewById(R.id.gender_et);
        mAgeTIL = findViewById(R.id.age_et);
        mPhoneTIL = findViewById(R.id.phone_et);
        mShortBioTIL = findViewById(R.id.short_bio_et);
        mDesignationTIL = findViewById(R.id.designation_et);
        savePersonalBtn = findViewById(R.id.save_general_button);
        mCloseActivityIv = findViewById(R.id.close_activity);

        mCloseActivityIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(PersonalActivity.this, ProfileActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });

        mGenderEt = findViewById(R.id.gender_tiet);
        mAgeEt = findViewById(R.id.age_tiet);
        mPhoneEt = findViewById(R.id.phone_tiet);
        mShortBioEt = findViewById(R.id.short_bio_tiet);
        mDesignationEt = findViewById(R.id.designation_tiet);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        fetchExistingPersonal();
        savePersonalBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                postPersonal();
            }
        });
    }

    public void fetchExistingPersonal() {
        gender = getIntent().getStringExtra("gender");
        age = getIntent().getStringExtra("age");
        phone = getIntent().getStringExtra("phone");
        short_bio = getIntent().getStringExtra("short_bio");
        designation = getIntent().getStringExtra("designation");

        mGenderTIL.setHintAnimationEnabled(false);
        mGenderEt.setText(gender);
        mAgeTIL.setHintAnimationEnabled(false);
        mAgeEt.setText(age);
        mPhoneTIL.setHintAnimationEnabled(false);
        mPhoneEt.setText(phone);
        mShortBioTIL.setHintAnimationEnabled(false);
        mShortBioEt.setText(short_bio);
        mDesignationTIL.setHintAnimationEnabled(false);
        mDesignationEt.setText(designation);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void postPersonal() {
        showProgressDialog(this, "Loading...", "Saving your general..", false);

        String gender = Objects.requireNonNull(mGenderTIL.getEditText()).getText().toString();
        String age = Objects.requireNonNull(mAgeTIL.getEditText()).getText().toString();
        String phone = Objects.requireNonNull(mPhoneTIL.getEditText()).getText().toString();
        String short_bio = Objects.requireNonNull(mShortBioTIL.getEditText()).getText().toString();
        String designation = Objects.requireNonNull(mDesignationTIL.getEditText()).getText().toString();

        if (short_bio.length() > 40) {
            mShortBioEt.setError("Short bio should be in 30 characters only");
            removeProgressDialog();
            return;
        }

        if (phone.length() > 10) {
            mPhoneEt.setError("Invaild Phone Number");
            removeProgressDialog();
            return;
        }

        if (age.length() > 2) {
            mAgeEt.setError("Invaild Age");
            removeProgressDialog();
            return;
        }

        mUserId = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();
        DocumentReference mDocumentreference = mFirebaseFirestore.collection("user_bio").document(mUserId).collection("personal").document(mUserId);
        Map<String, Object> user_bio = new HashMap<>();
        user_bio.put("gender", gender);
        user_bio.put("age", age);
        user_bio.put("phone", phone);
        user_bio.put("short_bio", short_bio);
        user_bio.put("designation", designation);

        mDocumentreference.set(user_bio).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                removeProgressDialog();

                Toast.makeText(PersonalActivity.this,
                        "You're done. Saved your general.",
                        Toast.LENGTH_LONG)
                        .show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                removeProgressDialog();

                Toast.makeText(PersonalActivity.this,
                        "Failed to save general, please check network connection",
                        Toast.LENGTH_LONG)
                        .show();
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
}