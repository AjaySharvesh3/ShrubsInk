package com.shrubsink.everylifeismatter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class PostQueryActivity extends AppCompatActivity {

    EditText mQueryTitleEt, mQueryBodyEt, mQueryIssueLocationEt;
    ImageView mQueryImageIv;
    Button mPostQueryBtn;
    ProgressDialog mProgressDialog;

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
            @Override
            public void onClick(View v) {

                final String title = mQueryTitleEt.getText().toString();
                final String body = mQueryBodyEt.getText().toString();
                final String issueLocation = mQueryIssueLocationEt.getText().toString();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(body) && !TextUtils.isEmpty(issueLocation) && postImageUri != null) {

                    /*newPostProgress.setVisibility(View.VISIBLE);*/

                    final String randomName = UUID.randomUUID().toString();
                    File newImageFile = new File(postImageUri.getPath());

                    try {
                        compressedImageFile = new Compressor(PostQueryActivity.this)
                                .setMaxHeight(720)
                                .setMaxWidth(720)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();

                    UploadTask filePath = storageReference.child("post_images").child(randomName + ".jpg").putBytes(imageData);
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
                                                    .setMaxHeight(100)
                                                    .setMaxWidth(100)
                                                    .setQuality(1)
                                                    .compressToBitmap(newThumbFile);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] thumbData = baos.toByteArray();

                                        UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                                .child(randomName + ".jpg").putBytes(thumbData);

                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                String downloadThumbUri = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                                                Map<String, Object> postMap = new HashMap<>();
                                                postMap.put("image_url", downloadUrl);
                                                postMap.put("image_thumb", downloadThumbUri);
                                                postMap.put("title", title);
                                                postMap.put("body", body);
                                                postMap.put("issue_location", issueLocation);
                                                postMap.put("user_id", current_user_id);
                                                postMap.put("timestamp", FieldValue.serverTimestamp());

                                                firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(PostQueryActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                            Intent mainIntent = new Intent(PostQueryActivity.this, MainActivity.class);
                                                            startActivity(mainIntent);
                                                            finish();

                                                        } else {

                                                        }
                                                        /*newPostProgress.setVisibility(View.INVISIBLE);*/
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //Error handling
                                            }
                                        });
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(PostQueryActivity.this, "Please add your picture and blog..!", Toast.LENGTH_LONG).show();
                }

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
            }
        }
    }
}