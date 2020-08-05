package com.shrubsink.everylifeismatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

public class AddressActivity extends AppCompatActivity {

    ImageView mCloseActivityIv;
    TextInputLayout mAddressLineTIL, mCityTIL, mPincodeTIL, mStateTIL, mCountryTIL;
    TextInputEditText mAddressLineEt, mCityEt, mPincodeEt, mStateEt, mCountryEt;
    Button saveAddressBtn;
    static ProgressDialog mProgressDialog;
    FirebaseFirestore mFirebaseFirestore;
    FirebaseAuth mFirebaseAuth;
    String mUserId;
    String address_line, city, pincode, state, country, phone, age, gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        /*showProgressDialog(this, "Loading...","Collecting your existing profile..",false);*/

        mCloseActivityIv = findViewById(R.id.close_activity_iv);
        closeActivity();

        mAddressLineTIL = findViewById(R.id.address_line_et);
        mCityTIL = findViewById(R.id.city_et);
        mPincodeTIL = findViewById(R.id.pincode_et);
        mStateTIL = findViewById(R.id.state_et);
        mCountryTIL = findViewById(R.id.country_et);
        saveAddressBtn = findViewById(R.id.save_address_button);

        mAddressLineEt = findViewById(R.id.address_line_tiet);
        mCityEt = findViewById(R.id.city_tiet);
        mPincodeEt = findViewById(R.id.pincode_tiet);
        mStateEt = findViewById(R.id.state_tiet);
        mCountryEt = findViewById(R.id.country_tiet);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        saveAddressBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                postUserDataAndAddress();
            }
        });

        fetchExistingAddress();
    }

    public void fetchExistingAddress() {
        address_line = getIntent().getStringExtra("address_line");
        city = getIntent().getStringExtra("city");
        pincode = getIntent().getStringExtra("pincode");
        state = getIntent().getStringExtra("state");
        country = getIntent().getStringExtra("country");

        mAddressLineTIL.setHintAnimationEnabled(false);
        mAddressLineEt.setText(address_line);
        mCityTIL.setHintAnimationEnabled(false);
        mCityEt.setText(city);
        mPincodeTIL.setHintAnimationEnabled(false);
        mPincodeEt.setText(pincode);
        mStateTIL.setHintAnimationEnabled(false);
        mStateEt.setText(state);
        mCountryTIL.setHintAnimationEnabled(false);
        mCountryEt.setText(country);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void postUserDataAndAddress() {
        showProgressDialog(this, "Loading...","Saving your address..",false);

        FirebaseUser account = mFirebaseAuth.getCurrentUser();
        assert account != null;
        String name = Objects.requireNonNull(account.getDisplayName());
        String email = Objects.requireNonNull(account.getEmail());

        String addressLine = Objects.requireNonNull(mAddressLineTIL.getEditText()).getText().toString();
        String city = Objects.requireNonNull(mCityTIL.getEditText()).getText().toString();
        String pincode = Objects.requireNonNull(mPincodeTIL.getEditText()).getText().toString();
        String state = Objects.requireNonNull(mStateTIL.getEditText()).getText().toString();
        String country = Objects.requireNonNull(mCountryTIL.getEditText()).getText().toString();

        mUserId = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();
        DocumentReference mDocumentreference = mFirebaseFirestore.collection("user_bio").document(mUserId).collection("address").document(mUserId);
        Map<String, Object> user_bio = new HashMap<>();
        user_bio.put("name", name);
        user_bio.put("email", email);
        user_bio.put("address_line", addressLine);
        user_bio.put("city", city);
        user_bio.put("pincode", pincode);
        user_bio.put("state", state);
        user_bio.put("country", country);

        mDocumentreference.set(user_bio).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                removeProgressDialog();

                Toast.makeText(AddressActivity.this,
                        "You're done. Saved your address.",
                        Toast.LENGTH_LONG)
                        .show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                removeProgressDialog();

                Toast.makeText(AddressActivity.this,
                        "Failed to save address, please check network connection",
                        Toast.LENGTH_LONG)
                        .show();
            }
        });
    }


    public void closeActivity() {
        mCloseActivityIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent closeActivity = new Intent(AddressActivity.this, ProfileActivity.class);
                startActivity(closeActivity);
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