package com.johnlouisjacobs.ecolemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.johnlouisjacobs.ecolemobile.Helper.KlassenHelper;
import com.johnlouisjacobs.ecolemobile.Helper.LoginPreferencesHelper;
import com.johnlouisjacobs.ecolemobile.Helper.SpinnerHelper;

public class LoginActivity extends AppCompatActivity {
    /* VIEWS */
    // Schüler Button
    AppCompatButton btnSchueler;
    // ViewGroup Spinner
    ViewGroup spinnerHolder;
    // Spinner Schüler
    Spinner spnSchueler;

    // Lehrer Button;
    AppCompatButton btnLehrer;
    // ViewGroup EditText
    ViewGroup editHolder;
    // LinearLayout Lehrer
    LinearLayout lehrerContent;
    // EditText Lehrer Hintername
    EditText editLehrer;
    // Checkbox Herr
    RadioButton radioHerr;
    // Checkbox Frau
    RadioButton radioFrau;

    // Vplan Passwort EditText
    EditText editTextVplanPasswort;
    ImageView imageCheck;
    // Essen-Kundennummer Edittext;
    EditText editTextEssenKundennummer;
    // Essen-Passwort EditText;
    EditText editTextEssenPasswort;

    // Login Button;
    AppCompatButton btnLogin;

    /* VARIABLES */
    // TODO: Remove
    public static final String mPasswort = "xxxx"; // I will not show passwords online

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SlideInInputFields();


        /* Init Views */
        btnLehrer = findViewById(R.id.btn_lehrer);
        btnSchueler = findViewById(R.id.btn_schueler);
        spinnerHolder = findViewById(R.id.spinner_container);
        spnSchueler = findViewById(R.id.spinner_schueler);
        editHolder = findViewById(R.id.edit_container);
        lehrerContent = findViewById(R.id.lehrer_content);
        editLehrer = findViewById(R.id.edit_lehrer);
        radioHerr = findViewById(R.id.radio_herr);
        radioFrau = findViewById(R.id.radio_frau);
        editTextVplanPasswort = findViewById(R.id.edit_Vplan_Passwort);
        imageCheck = findViewById(R.id.vplan_passwort_checker);
        editTextEssenKundennummer = findViewById(R.id.edit_Essen_Kundennummer);
        editTextEssenPasswort = findViewById(R.id.edit_Essen_Passwort);
        btnLogin = findViewById(R.id.btn_login);

        /* Setup OnKeyListener */
        editTextEssenPasswort.setOnKeyListener(OKLessenPasswort);

        /* Setup TextWatcher */
        editTextVplanPasswort.addTextChangedListener(TWvplanpasswort);

        /* Setup OnClickListeners */
        btnLehrer.setOnClickListener(OCLlehrerButton);
        radioHerr.setOnClickListener(OCLradioHerr);
        radioFrau.setOnClickListener(OCLradioFrau);
        btnSchueler.setOnClickListener(OCLschuelerButton);
        btnLogin.setOnClickListener(OCLloginButton);

    }

    /**
     * Method for showing the input fields in activity_login
     * either with animation or without, when the user has not the appropriate API level
     */
    private void SlideInInputFields() {
        /* Get needed Views */
        final ViewGroup transitionHolder = findViewById(R.id.transition_container);
        final LinearLayout content = findViewById(R.id.content_container);

        /* Start animation after 0.5 secs because of the animation of the ecole icon when entering the acitvity */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // If user has API LEVEL 21 or higher use an animation
                // else just show the Input Fields
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Uses a slide animation with a FastOutSlowInInterpolator
                    TransitionManager.beginDelayedTransition(transitionHolder, new Slide().setInterpolator(new FastOutSlowInInterpolator()));
                    content.setVisibility(View.VISIBLE);
                } else {
                    content.setVisibility(View.VISIBLE);
                }
            }
        }, 500);
    }

    /**
     * OnKeyListener for the last EditText.
     * When the enter button gets pressed there it automatically performs a click on the login button.
     */
    View.OnKeyListener OKLessenPasswort = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                btnLogin.performClick();
                return true;
            }
            return false;
        }
    };

    /* TextWatcher */
    /**
     * TextWatcher for the OnTextChangedListener of the editTextVplanpasswort.
     * Checks the correctness of the passwort. If its not correct you can't login and it shows a red cross.
     * If it's correct you can login and it shows a green checkmark.
     */
    TextWatcher TWvplanpasswort = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = s.toString();

            if (!text.isEmpty()) {
                imageCheck.setVisibility(View.VISIBLE);


                if (text.equals(mPasswort)) {
                    imageCheck.setBackgroundResource(R.drawable.ic_check_green);
                } else {
                    imageCheck.setBackgroundResource(R.drawable.ic_close_red);
                }
            } else {
                imageCheck.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    /* OnClickListeners */
    /**
     * Triggers when the Schüler-Button is clicked
     */
    View.OnClickListener OCLschuelerButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            activateSchueler();

            //Sets true as tag to the schueler button for later getting information if it's been clicked or not
            btnLehrer.setTag(false);
            btnSchueler.setTag(true);
        }
    };

    /**
     * Triggers when the Lehrer-Button is clicked
     */
    View.OnClickListener OCLlehrerButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "Lehrermodus noch nicht verfügbar.", Toast.LENGTH_LONG).show();
            /*
             * TODO: Add Lehrer Vplan
             * Currently has a bug, where this is clicked and then logged in the App crashes,
             * since we only implemented Schüler interaction.
             */
            //            activateLehrer();
            //
            //            //Sets true as tag to the lehrer button for later getting information if it's been clicked or not
            //            btnSchueler.setTag(false);
            //            btnLehrer.setTag(true);
        }
    };

    /**
     * Triggers when the Herr-Radiobutton is clicked
     */
    View.OnClickListener OCLradioHerr = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // When RadioButton Herr is clicked, set RadioButton Frau to false
            radioFrau.setChecked(false);
        }
    };

    /**
     * Triggers when the Frau-Button is clicked
     */
    View.OnClickListener OCLradioFrau = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // When RadioButton Frau is clicked, set RadioButton Herr to false
            radioHerr.setChecked(false);
        }
    };

    /**
     * Triggers when then the Login-Button is clicked
     */
    View.OnClickListener OCLloginButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Gets PreferenceHelper
            LoginPreferencesHelper prefrencesHelper = new LoginPreferencesHelper(LoginActivity.this);
            // Resets all Preferences
            prefrencesHelper.resetPrefs();

            // If something is missing
            if (!prefrencesHelper.setPreferences()) {
                // Trigger a toast which displays a error message
                Toast.makeText(LoginActivity.this, getApplicationContext().getResources().getString(R.string.toast_missing_argument), Toast.LENGTH_LONG).show();
                return;
            }
            /* If everything is OK, set the SharedPreference Boolean logged_in to true,
             * so we don't need to re-login every time we open up the app.
             */
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getString(R.string.pref_logged_in_key), true);
            editor.apply();

            // And then start the MainActivity
            Intent IntentMainActivity = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(IntentMainActivity);

        }
    };


    /**
     * Activates the Lehrer's choices View with all the needed methods
     */
    private void activateLehrer() {
        // Change Lehrer's buttons color to red
        colorRed(btnLehrer);
        // Hides the schuler spinner if it's shown
        setVisibilitySpinner(false);
        // And disables adapter
        spnSchueler.setAdapter(null);
        // Shows Lehrer choice view with animation
        setVisibilityLehrerContent(true);
    }

    /**
     * Activates the Schueler Spinner with all the needed methods
     */
    private void activateSchueler() {
        // Change Schueler's buttons color to red
        colorRed(btnSchueler);

        // If the spinner hasn't a spinner adapter set up Schueler Spinner
        if (spnSchueler.getAdapter() == null) {
            KlassenHelper helper = new KlassenHelper();
            new SpinnerHelper(getApplicationContext()).setUpAdapter(spnSchueler, helper.getKlassenList());
        }

        // Hides LehrerContent view if it's shown
        setVisibilityLehrerContent(false);
        // Shows Schueler Spinner with animation
        setVisibilitySpinner(true);
    }


    /**
     * Switches the colors of the two buttons "Schüler" and "Lehrer" to either one of the two
     * red with white textcolor
     *
     * @param shouldBeRedButton on which button this style should be applied
     */
    private void colorRed(Button shouldBeRedButton) {
        // Sets the textcolor of the pressed button to white
        shouldBeRedButton.setTextColor(getResources().getColor(R.color.white));

        // Switches the colors
        if (shouldBeRedButton == btnLehrer) {
            ViewCompat.setBackgroundTintList(shouldBeRedButton, getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
            ViewCompat.setBackgroundTintList(btnSchueler, getApplicationContext().getResources().getColorStateList(R.color.colorButtonMain));

            btnSchueler.setTextColor(getResources().getColor(R.color.black));
        } else if (shouldBeRedButton == btnSchueler) {
            ViewCompat.setBackgroundTintList(shouldBeRedButton, getApplicationContext().getResources().getColorStateList(R.color.colorAccent));
            ViewCompat.setBackgroundTintList(btnLehrer, getApplicationContext().getResources().getColorStateList(R.color.colorButtonMain));
            btnLehrer.setTextColor(getResources().getColor(R.color.black));
        }

    }

    /**
     * sets visibility of the spinner with animation
     *
     * @param visibility whether it should be visible or not
     */
    private void setVisibilitySpinner(boolean visibility) {
        // If it should be shown (visibility -> true) then show the Spinner with animation
        // Else (visibility -> false) hide it with animation
        if (visibility) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ends the transition of the EditText of the Lehrer
                TransitionManager.endTransitions(editHolder);
            }
            // Animation only works on devices with Android API Lollipop or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Uses a Slide animation
                Slide transition = new Slide();
                transition.setSlideEdge(Gravity.TOP);
                TransitionManager.beginDelayedTransition(spinnerHolder, transition);
                spnSchueler.setVisibility(View.VISIBLE);
            } else {
                // with devices below Android API Lollipop don't use animation
                spnSchueler.setVisibility(View.VISIBLE);
            }
        } else {
            // Animation only works on devices with Android API Lollipop or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Uses a Slide animation
                Slide transition = new Slide();
                transition.setSlideEdge(Gravity.TOP);
                TransitionManager.beginDelayedTransition(spinnerHolder, transition);
                spnSchueler.setVisibility(View.GONE);
            } else {
                // with devices below Android API Lollipop don't use animation
                spnSchueler.setVisibility(View.GONE);
            }
        }
    }

    /**
     * sets visibility of the LehrerContent with animation
     *
     * @param visibility whether it should be visible or not
     */
    private void setVisibilityLehrerContent(boolean visibility) {
        // If it should be shown (visibility -> true) then show the LehrerContent with animation
        // Else (visibility -> false) hide it with animation
        if (visibility) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ends the transition of the Spinner
                TransitionManager.endTransitions(spinnerHolder);
            }
            // Animation only works on devices with Android API Lollipop or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Uses a slide animation to slide in the EditText of the Lehrer
                Slide transition = new Slide();
                transition.setSlideEdge(Gravity.TOP);
                TransitionManager.beginDelayedTransition(editHolder, transition);
                lehrerContent.setVisibility(View.VISIBLE);
            } else {
                // with devices below Android API Lollipop don't use animation
                lehrerContent.setVisibility(View.VISIBLE);
            }
        } else {
            // Animation only works on devices with Android API Lollipop or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Uses a fade animation
                Slide transition = new Slide();
                transition.setSlideEdge(Gravity.TOP);
                TransitionManager.beginDelayedTransition(editHolder, transition);
                lehrerContent.setVisibility(View.GONE);
            } else {
                // with devices below Android API Lollipop don't use animation
                lehrerContent.setVisibility(View.GONE);
            }
            // Uncheck both Radiobuttons
            radioFrau.setChecked(false);
            radioHerr.setChecked(false);
            // Set Lehrer EditText's text to nothing ("")
            editLehrer.setText("");
        }
    }

    /**
     * Overwriting onBackPressed() so you can't get back to the splashscreen,
     * by simply returning the function when the function gets called.
     */
    @Override
    public void onBackPressed() {
        return; // do nothing
    }
}
