package com.shrubsink.everylifeismatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    public static void startActivity(Context context, String username) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("username", username);
        context.startActivity(intent);
    }

    FirebaseAuth mFirebaseAuth;
    GoogleSignInClient googleSignInClient;
    QueryFragment queryFragment;
    ProductListFragment productListFragment;
    ProfileFragment profileFragment;
    NotificationFragment notificationFragment;
    CreditsFragment creditsFragment;
    BottomNavigationView mBottomNavigationView;
    /*ExtendedFloatingActionButton mPostQueryFAB;*/

    FirebaseFirestore mFirebaseFirestore;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        mBottomNavigationView = findViewById(R.id.mainBottomNav);
        /*mPostQueryFAB = findViewById(R.id.post_query_fab);*/

        queryFragment = new QueryFragment();
        productListFragment = new ProductListFragment();
        profileFragment = new ProfileFragment();
        notificationFragment = new NotificationFragment();
        creditsFragment = new CreditsFragment();

        replaceFragment(queryFragment);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.bottom_action_home:
                        replaceFragment(queryFragment);
                        return true;
                    /*case R.id.bottom_action_products:
                        replaceFragment(productListFragment);
                        return true;*/
                    case R.id.bottom_action_rewards:
                        replaceFragment(creditsFragment);
                        return true;
                    /*case R.id.bottom_action_notifications:
                        replaceFragment(notificationFragment);
                        return true;*/
                    case R.id.bottom_action_profile:
                        replaceFragment(profileFragment);
                        return true;
                    default:
                        return true;
                }
            }
        });

        /*mPostQueryFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent postQueryIntent = new Intent(MainActivity.this, PostQueryActivity.class);
                startActivity(postQueryIntent);
            }
        });*/
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Intent i = new Intent(MainActivity.this, GoogleSignInActivity.class);
                startActivity(i);
            } else {
                String currentUserId = mFirebaseAuth.getCurrentUser().getUid();
                mFirebaseFirestore.collection("user_bio").document(currentUserId)
                        .collection("personal").document(currentUserId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().exists()) {
                                        ProfileFragment fragment = new ProfileFragment();
                                        mBottomNavigationView.setSelectedItemId(R.id.bottom_action_profile);
                                        replaceFragment(fragment);
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, R.string.fui_no_internet, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commitNow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out: {
                signOut();
                break;
            }
            case R.id.action_my_activities: {
                Intent myActivities = new Intent(MainActivity.this, MyActivitiesActivity.class);
                startActivity(myActivities);
                /*overridePendingTransition(R.anim.slide_out_up, R.anim.slide_in_up);*/
                break;
            }
            case R.id.action_post_query: {
                Intent postQueryIntent = new Intent(MainActivity.this, PostQueryActivity.class);
                startActivity(postQueryIntent);
                /*overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);*/
                break;
            }
        }
        return true;
    }

    private void signOut() {
        mFirebaseAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent googleSignInActivity = new Intent(MainActivity.this, GoogleSignInActivity.class);
                        startActivity(googleSignInActivity);
                    }
                });
    }
}