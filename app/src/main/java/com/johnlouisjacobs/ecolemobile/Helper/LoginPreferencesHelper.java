package com.johnlouisjacobs.ecolemobile.Helper;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.johnlouisjacobs.ecolemobile.LoginActivity;
import com.johnlouisjacobs.ecolemobile.R;

/**
 * Helper Class for setting up the preferences
 * Created by John on 19.03.2018.
 */

public class LoginPreferencesHelper {
    private AppCompatActivity mActivity;

    private SharedPreferences preferences;

    private SharedPreferences.Editor editor;

    private Resources res;

    /**
     * Constructor
     *
     * @param activity where to get everything from
     */
    public LoginPreferencesHelper(AppCompatActivity activity) {
        this.mActivity = activity;
        this.res = mActivity.getApplicationContext().getResources();
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
    }

    /**
     * Gets called when the Login Button is clicked.
     * Then sets all needed preferences
     *
     * @return true if everything is OK and filled out, false if something is missing
     */
    public boolean setPreferences() {
        editor = preferences.edit();

        /* Setting up all views */
        Button btnLehrer = mActivity.findViewById(R.id.btn_lehrer);
        Button btnSchueler = mActivity.findViewById(R.id.btn_schueler);

        Spinner spnSchueler = mActivity.findViewById(R.id.spinner_schueler);

        EditText editLehrer = mActivity.findViewById(R.id.edit_lehrer);
        RadioButton radioHerr = mActivity.findViewById(R.id.radio_herr);
        RadioButton radioFrau = mActivity.findViewById(R.id.radio_frau);

        EditText editTextVplanPasswort = mActivity.findViewById(R.id.edit_Vplan_Passwort);
        String TextVplanPasswort = editTextVplanPasswort.getText().toString();
        EditText editTextEssenKundennummer = mActivity.findViewById(R.id.edit_Essen_Kundennummer);
        String TextEssenKundennummer = editTextEssenKundennummer.getText().toString();
        EditText editTextEssenPasswort = mActivity.findViewById(R.id.edit_Essen_Passwort);
        String TextEssenPasswort = editTextEssenPasswort.getText().toString();


        // Put values to which button got clicked "Schüler" or "Lehrer"
        if (btnSchueler.getTag() == null) return false;
        editor.putBoolean(res.getString(R.string.pref_is_schueler_key), (Boolean) btnSchueler.getTag());
        if (btnLehrer.getTag() == null) return false;
        editor.putBoolean(res.getString(R.string.pref_is_lehrer_key), (Boolean) btnLehrer.getTag());
        editor.apply();

        // Sets value for the "Klasse" if "Schüler" got clicked before
        // Returns false if something is missing
        if (!setKlassePrefs(spnSchueler)) return false;
        setKlassePrefs(spnSchueler);

        // Sets either "Frau" or "Mann"
        if (!setLehrerPrefs(editLehrer, radioFrau, radioHerr)) return false;
        setLehrerPrefs(editLehrer, radioFrau, radioHerr);

        // We need a Vertretungspasswort to get into the app security reasons
        if (TextVplanPasswort.isEmpty() || !TextVplanPasswort.equals(LoginActivity.mPasswort)) return false;

        // If the user only filled in one of the Essens fields don't continue, since we need both Passwort and Username
        if (    (!TextEssenKundennummer.isEmpty() && TextEssenPasswort.isEmpty()) ||
                (!TextEssenPasswort.isEmpty() && TextEssenKundennummer.isEmpty())) {
            return false;
        }


        // Sets values for Vplan Passwort, Essen-Kundennummer and Essen-Passwort
        editor.putString(res.getString(R.string.pref_vplan_passwort_key), TextVplanPasswort);
        editor.putString(res.getString(R.string.pref_essen_kundennummer_key), TextEssenKundennummer);
        editor.putString(res.getString(R.string.pref_essen_passwort_key), TextEssenPasswort);

        // Applys all changes
        editor.apply();

        return true;
    }

    /**
     * Helper method for setting the Klassen preference
     *
     * @param spinner where to get information from
     * @return true if everything works, false if something is missing
     */
    private boolean setKlassePrefs(Spinner spinner) {
        // If "Schüler" Button has been clicked trigger this condition
        if (preferences.getBoolean(res.getString(R.string.pref_is_schueler_key), res.getBoolean(R.bool.pref_is_schueler_default))) {
            // Gets spinners tag
            int tag = (int) spinner.getTag();
            // If the spinner's tag is not -1 (nothing selected) or 0 ("Klasse...")
            if (tag != -1 && tag != 0) {
                editor.putString(res.getString(R.string.pref_schueler_klasse_key), new KlassenHelper().getListItem((int) spinner.getTag()));
            } else {
                return false;
            }
        }
        return true;
    }


    /**
     * Helper method for setting the Lehrer preference
     *
     * @param lehrerName EditText of the Nachname
     * @param frau       RadioButton
     * @param herr       RadioButton
     * @return true if everything works, false if something is missing
     */
    private boolean setLehrerPrefs(EditText lehrerName, RadioButton frau, RadioButton herr) {
        if (preferences.getBoolean(res.getString(R.string.pref_is_lehrer_key), res.getBoolean(R.bool.pref_is_lehrer_default))) {
            if (!(lehrerName.getText().toString().isEmpty())) {
                editor.putString(res.getString(R.string.pref_lehrer_nachname_key), lehrerName.getText().toString());
                editor.apply();
            } else {
                return false;
            }
            if (frau.isChecked() || herr.isChecked()) {
                editor.putBoolean(res.getString(R.string.pref_lehrer_mann_key), herr.isChecked());
                editor.putBoolean(res.getString(R.string.pref_lehrer_frau_key), frau.isChecked());
            } else {
                return false;
            }
            // Joins together "Frau" or "Herr" with "Lehrer Nachname"
            String fullName = "";
            String nachname = preferences.getString(res.getString(R.string.pref_lehrer_nachname_key), res.getString(R.string.pref_lehrer_nachname_default));
            if (frau.isChecked()) {
                fullName = "Frau "
                        + nachname;
            }
            if (herr.isChecked()) {
                fullName = "Herr " + nachname;
            }
            editor.putString(res.getString(R.string.pref_full_lehrer_name_key), fullName);
        }
        return true;
    }

    /**
     * Resets all preferences
     */
    public void resetPrefs() {
        editor = preferences.edit();

        editor.putBoolean(res.getString(R.string.pref_is_schueler_key), res.getBoolean(R.bool.pref_is_schueler_default));
        editor.putBoolean(res.getString(R.string.pref_is_lehrer_key), res.getBoolean(R.bool.pref_is_lehrer_default));
        editor.putString(res.getString(R.string.pref_schueler_klasse_key), res.getString(R.string.pref_schueler_klasse_default));

        editor.putString(res.getString(R.string.pref_lehrer_nachname_key), res.getString(R.string.pref_lehrer_nachname_default));
        editor.putBoolean(res.getString(R.string.pref_lehrer_mann_key), res.getBoolean(R.bool.pref_lehrer_mann_default));
        editor.putBoolean(res.getString(R.string.pref_lehrer_frau_key), res.getBoolean(R.bool.pref_lehrer_frau_default));

        editor.putString(res.getString(R.string.pref_vplan_passwort_key), res.getString(R.string.pref_vplan_passwort_default));
        editor.putString(res.getString(R.string.pref_essen_kundennummer_key), res.getString(R.string.pref_essen_kundennummer_default));
        editor.putString(res.getString(R.string.pref_essen_passwort_key), res.getString(R.string.pref_essen_passwort_default));

        editor.apply();
    }
}
