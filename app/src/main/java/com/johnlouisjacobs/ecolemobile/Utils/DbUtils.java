package com.johnlouisjacobs.ecolemobile.Utils;

import android.content.ContentValues;

import com.johnlouisjacobs.ecolemobile.Data.VertretungContract;
import com.johnlouisjacobs.ecolemobile.Parsers.VertretungsplanParser;

import java.util.ArrayList;

/**
 * Utility method for Database uses.
 * Created by John on 20.04.2018.
 */

public class DbUtils {

    /**
     * Makes Content Values out of an Array List of Entrys.
     * @param entries which get transferred into Content Values
     * @return an array of content values
     */
    public static ContentValues[] getValuesFromEntries(ArrayList<VertretungsplanParser.Entry> entries){
        ContentValues[] vertretungsContentValues = new ContentValues[entries.size()];
        /* Iterates through all entries and
         * makes Content Values out of them.
         */
        for (int i = 0; i <= entries.size()-1; i++){
            String klasse = entries.get(i).getKlasse();
            String stunde = entries.get(i).getStunde();
            String fach = entries.get(i).getFach();
            String lehrer = entries.get(i).getLehrer();
            String information = entries.get(i).getInformation();
            String raum = entries.get(i).getRaum();

            ContentValues values = new ContentValues();
            values.put(VertretungContract.VertretungDbEntry.COLUMN_KLASSE, klasse);
            values.put(VertretungContract.VertretungDbEntry.COLUMN_STUNDE, stunde);
            values.put(VertretungContract.VertretungDbEntry.COLUMN_FACH, fach);
            values.put(VertretungContract.VertretungDbEntry.COLUMN_LEHRER, lehrer);
            values.put(VertretungContract.VertretungDbEntry.COLUMN_INFORMATION, information);
            values.put(VertretungContract.VertretungDbEntry.COLUMN_RAUM, raum);

            vertretungsContentValues[i] = values;

        }

        return vertretungsContentValues;
    }
}
