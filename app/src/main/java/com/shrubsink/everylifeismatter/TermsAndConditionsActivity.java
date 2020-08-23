package com.shrubsink.everylifeismatter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class TermsAndConditionsActivity extends AppCompatActivity {

    TextView siteTv;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Terms and Conditions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        siteTv = findViewById(R.id.site_tv);
        String tnc = "These terms and conditions outline the rules and regulations for the use of Shrubsink's Website, located at www.shrubsink.com and Shrubsink App";
        SpannableString spannableString = new SpannableString(tnc);

        ClickableSpan site = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                String url = "https://app-shrubsink.web.app";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        };

        spannableString.setSpan(site, 109, 125, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        siteTv.setText(spannableString);
        siteTv.setMovementMethod(LinkMovementMethod.getInstance());
    }
}