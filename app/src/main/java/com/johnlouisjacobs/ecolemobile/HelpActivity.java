package com.johnlouisjacobs.ecolemobile;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.johnlouisjacobs.ecolemobile.Fragments.FragmentEssen;

public class HelpActivity extends AppCompatActivity {
    TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.text_help));

        email = findViewById(R.id.email);
        email.setPaintFlags(email.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uriText =
                        "mailto:"+email.getText().toString();
                Uri uri = Uri.parse(uriText);
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(uri);
                startActivity(Intent.createChooser(emailIntent, getString(R.string.text_email_intent)));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FragmentEssen.webViewLogOut(getApplicationContext());
    }
}
