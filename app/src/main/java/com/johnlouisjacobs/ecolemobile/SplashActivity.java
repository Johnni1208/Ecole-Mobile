package com.johnlouisjacobs.ecolemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {
    /* VIEWS */
    // Ecole Icon View
    ImageView ecoleIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Fade());
        }

        // Init Ecole Icon
        ecoleIcon = findViewById(R.id.first_screen_ecole_icon);

        /* Triggers after 0,5 seconds */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Tests if the user has logged in and has API Level 21 or higher
                if (!isLoggedIn() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Start LoginActivity while Transition-animating the Ecole-logo
                    Intent LoginActivity = new Intent(SplashActivity.this, LoginActivity.class);
                    ActivityOptionsCompat transitionOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(SplashActivity.this, ecoleIcon, ViewCompat.getTransitionName(ecoleIcon));
                    startActivity(LoginActivity, transitionOptions.toBundle());
                }else if(!isLoggedIn() && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                    /*
                     * When the SDK Version is lower than API 21 start the login screen without animation,
                     * since any API lower than 21 hasn't got Activity Transitions
                     */
                    Intent LoginActivity = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(LoginActivity);
                } else if (isLoggedIn()) {
                    /*
                     * If the user is already logged in just start the MainActivity.
                     */
                    Intent MainActivity = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(MainActivity);
                    finish();
                }
            }
        }, 500);

    }

    /**
     * Tests if the user has ever logged in
     */
    private boolean isLoggedIn() {
        // Gets SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Gets boolean, which displays if the user has ever logged in.
        // This changes to true, when the user has logged in
        return sharedPreferences.getBoolean(getResources().getString(R.string.pref_logged_in_key), getResources().getBoolean(R.bool.pref_already_logged_in_default));
    }
}

//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(getResources().getString(R.string.pref_logged_in_key), false);
//        editor.apply();
