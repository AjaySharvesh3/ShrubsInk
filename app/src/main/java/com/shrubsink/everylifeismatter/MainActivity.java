package com.shrubsink.everylifeismatter;

import androidx.annotation.NonNull;
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
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    public static void startActivity(Context context, String username) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("username", username);
        context.startActivity(intent);
    }

    FirebaseAuth mFirebaseAuth;
    GoogleSignInClient googleSignInClient;
    QueryFragment queryFragment;
    ActivityFragment activityFragment;
    ProductListFragment productListFragment;
    BottomNavigationView mBottomNavigationView;
    FloatingActionButton mPostQueryFAB;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        mBottomNavigationView = findViewById(R.id.mainBottomNav);
        mPostQueryFAB = findViewById(R.id.post_query_fab);

        queryFragment = new QueryFragment();
        activityFragment = new ActivityFragment();
        productListFragment = new ProductListFragment();

       replaceFragment(queryFragment);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.bottom_action_home : replaceFragment(queryFragment); return true;
                    case R.id.bottom_action_activity : replaceFragment(activityFragment);
                    case R.id.bottom_action_product_list : replaceFragment(productListFragment);
                    default: return true;
                }
            }
        });

        mPostQueryFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent postQueryIntent = new Intent(MainActivity.this, PostQueryActivity.class);
                startActivity(postQueryIntent);
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
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