package com.johnlouisjacobs.ecolemobile.Sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.johnlouisjacobs.ecolemobile.Data.VertretungContract;
import com.johnlouisjacobs.ecolemobile.Parsers.AdditionallyInformationParser;
import com.johnlouisjacobs.ecolemobile.Parsers.DateParser;
import com.johnlouisjacobs.ecolemobile.Parsers.VertretungsplanParser;
import com.johnlouisjacobs.ecolemobile.R;
import com.johnlouisjacobs.ecolemobile.Utils.DbUtils;

import java.util.ArrayList;

/**
 * Loads a list of Vertretungen
 */

public class VertretungSyncTask {

    /* URL of Vertretungsplan */
    private static String mUrl =
            "http://www.stundenplan24.de/20173526/vplan/vdaten/VplanKl.xml?_=1511542862569";

    /**
     * Perfoms the network request for updated vertretungs data. The gotten Entry values get inserted in the
     * database by our Content resolver.
     *
     * @param context Used to access our Content resolver.
     */
    synchronized public static void syncVertretung(Context context) {
        try {
            // Get the SharedPreference Editor
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();

            loadDate(context, editor);

            loadAdditionals(context, editor);


            // Creates an arraylist of Entrys to hold our entries, we got from the network request.
            ArrayList<VertretungsplanParser.Entry> entries;
            VertretungsplanParser vertretungsplanParser = new VertretungsplanParser();
            // Performs network request
            entries = vertretungsplanParser.loadXMLFromNetwork(mUrl);

            // Gets Content values from the given entries, for bulk insert them into the database
            ContentValues[] vertretungsValues = DbUtils.getValuesFromEntries(entries);

            /* In case of an network error (Server not accessable or something like this)
             * no entries are given. Also no Content Values are made. So vertretungsValues is null.
             * We check this here.
             */
            if (vertretungsValues != null && vertretungsValues.length != 0) {
                ContentResolver vertretungsResolver = context.getContentResolver();

                // Delete the old vertretungsplan data because we want the new vertretungsplan
                vertretungsResolver.delete(
                        VertretungContract.VertretungDbEntry.CONTENT_URI,
                        null,
                        null);

                // Insert all Content Values into the database with out made bulk Insert method.
                vertretungsResolver.bulkInsert(
                        VertretungContract.VertretungDbEntry.CONTENT_URI,
                        vertretungsValues);

                //TODO: Notificytion?
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadAdditionals(Context context, SharedPreferences.Editor editor) {
        try {
            // Load the Additional Information into the SharedPreferences
            AdditionallyInformationParser additionallyInformationParser = new AdditionallyInformationParser();
            ArrayList<String> additionals = additionallyInformationParser.loadXMLFromNetwork(mUrl);
            // If the additionals ArrayList is empty set the SharedPreference to ""
            if (additionals.size() == 0 || additionals == null) {
                editor.putString(context.getString(R.string.pref_additional_key), "");
                return;
            }
            // Extracts every element from the Arraylist
            StringBuilder additionalFullTextBuilder = new StringBuilder();
            for (int i = 0; i <= additionals.size(); i++) {
                if (i == additionals.size() - 1) {
                    additionalFullTextBuilder.append("- ").append(additionals.get(i));
                    break;
                }
                additionalFullTextBuilder.append("- ").append(additionals.get(i)).append("\n\n");
            }
            // And builds a single string
            String additionalFullText = additionalFullTextBuilder.toString();
            // Saves it in the SharedPreferences for offline uses
            editor.putString(context.getString(R.string.pref_additional_key), additionalFullText);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadDate(Context context, SharedPreferences.Editor editor) {
        try {
            // Load the Date into the SharedPreferences
            DateParser dateParser = new DateParser();
            String date = dateParser.loadXMLFromNetwork(mUrl);
            editor.putString(context.getString(R.string.pref_date_key), date);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
