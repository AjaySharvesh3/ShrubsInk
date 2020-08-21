package com.shrubsink.everylifeismatter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class PostQueryActivity extends AppCompatActivity {

    EditText mQueryTitleEt, mQueryBodyEt, mQueryIssueLocationEt, mQueryTagEt;
    ImageView mQueryImageIv;
    Button mPostQueryBtn;
    static ProgressDialog mProgressDialog;

    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    String current_user_id;
    Uri postImageUri = null;
    Bitmap compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_query);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        mQueryTitleEt = findViewById(R.id.query_title_et);
        mQueryBodyEt = findViewById(R.id.query_body_et);
        mQueryIssueLocationEt = findViewById(R.id.query_issue_location_et);
        mQueryTagEt = findViewById(R.id.query_tag_et);
        mQueryImageIv = findViewById(R.id.query_image_iv);
        mPostQueryBtn = findViewById(R.id.post_query_button);

        mQueryImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(PostQueryActivity.this);
            }
        });

        mPostQueryBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                final String title = mQueryTitleEt.getText().toString();
                final String body = mQueryBodyEt.getText().toString();
                final String issueLocation = mQueryIssueLocationEt.getText().toString();
                final String tags = mQueryTagEt.getText().toString();

                if (TextUtils.isEmpty(title)) {
                    mQueryTitleEt.setError("Title should not be empty");
                    Toast.makeText(PostQueryActivity.this, "Title should not be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(body)) {
                    mQueryBodyEt.setError("Body should not be empty");
                    Toast.makeText(PostQueryActivity.this, "Body should not be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(issueLocation)) {
                    mQueryIssueLocationEt.setError("Issue Location should not be empty");
                    Toast.makeText(PostQueryActivity.this, "Issue Location should not be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(tags)) {
                    mQueryTagEt.setError("Tags should not be empty");
                    Toast.makeText(PostQueryActivity.this, "Tags should not be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                showProgressDialog(PostQueryActivity.this, "Publishing...", "Please wait until we publish your query", false);

                    final String randomName = UUID.randomUUID().toString();
                    File newImageFile = new File(Objects.requireNonNull(postImageUri.getPath()));

                    try {
                        compressedImageFile = new Compressor(PostQueryActivity.this)
                                .setMaxHeight(500)
                                .setMaxWidth(1000)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();

                    UploadTask filePath = storageReference.child("post_query_images").child(current_user_id)
                            .child(randomName + ".jpg").putBytes(imageData);
                    filePath.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            final Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            uri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();

                                    File newThumbFile = new File(postImageUri.getPath());
                                    try {
                                        compressedImageFile = new Compressor(PostQueryActivity.this)
                                                .setMaxHeight(500)
                                                .setMaxWidth(1000)
                                                .setQuality(1)
                                                .compressToBitmap(newThumbFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("image_url", downloadUrl);
                                    postMap.put("image_thumb", downloadUrl);
                                    postMap.put("title", title);
                                    postMap.put("body", body);
                                    postMap.put("issue_location", issueLocation);
                                    postMap.put("tags", tags);
                                    postMap.put("user_id", current_user_id);
                                    postMap.put("timestamp", FieldValue.serverTimestamp());
                                    postMap.put("credits", 10);
                                    postMap.put("is_solved", false);

                                    firebaseFirestore.collection("query_posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                removeProgressDialog();
                                                Toast.makeText(PostQueryActivity.this, "Published your query", Toast.LENGTH_LONG).show();
                                                Intent mainIntent = new Intent(PostQueryActivity.this, MainActivity.class);
                                                startActivity(mainIntent);
                                                finish();
                                            } else {
                                                removeProgressDialog();
                                                Toast.makeText(PostQueryActivity.this,
                                                        "Something went wrong, check your network connection",
                                                        Toast.LENGTH_LONG)
                                                        .show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                mQueryImageIv.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(PostQueryActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        }
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