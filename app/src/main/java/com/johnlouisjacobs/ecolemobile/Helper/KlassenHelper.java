package com.johnlouisjacobs.ecolemobile.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.johnlouisjacobs.ecolemobile.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for the Klassen variables
 * Created by John on 18.03.2018.
 */

public class KlassenHelper {
    private List<String> klassenList = new ArrayList<>();

    /**
     * Constructor
     */
    public KlassenHelper() {
        setUpKlassenList();
    }

    /**
     * Sets up Klassen list
     */
    private void setUpKlassenList(){
        // Adding all Klassen
        klassenList.add("Klasse...");
        klassenList.add("5a");
        klassenList.add("5b");
        klassenList.add("5c");
        klassenList.add("5d");
        klassenList.add("6a");
        klassenList.add("6b");
        klassenList.add("6c");
        klassenList.add("6d");
        klassenList.add("7a");
        klassenList.add("7b");
        klassenList.add("7c");
        klassenList.add("7d");
        klassenList.add("8a");
        klassenList.add("8b");
        klassenList.add("8c");
        klassenList.add("8d");
        klassenList.add("9a");
        klassenList.add("9b");
        klassenList.add("9c");
        klassenList.add("9d");
        klassenList.add("10a");
        klassenList.add("10b");
        klassenList.add("10c");
        klassenList.add("10d");
        klassenList.add("11a");
        klassenList.add("11b");
        klassenList.add("11c");
        klassenList.add("12a");
        klassenList.add("12b");
        klassenList.add("12c");
    }
    /**
     * Gets all Klassen
     *
     * @return List of Klassen strings
     */
    public List<String> getKlassenList() {
        return klassenList;
    }

    /**
     * Gets one specific member of the List Klassen
     * @param position position in the list
     * @return String of list item
     */
    public String getListItem(int position){
        return klassenList.get(position);
    }

    /**
     * Gets the Klasse which got selected in the login screen.
     * @param context context which is used by sharedpreferences and getString method
     * @return String which contains the Klasse
     */
    public static String getSelectedKlasse(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.pref_schueler_klasse_key),context.getString(R.string.pref_schueler_klasse_key));
    }
}
