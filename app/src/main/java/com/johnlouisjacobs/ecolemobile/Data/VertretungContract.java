package com.johnlouisjacobs.ecolemobile.Data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines tables and columns for easy access. This class is not necessary, but keeps the code organized
 * Created by John on 11.04.2018.
 */
public class VertretungContract {
    /* Name of the entire content provider */
    public static final String CONTENT_AUTHORITY = "com.johnlouisjacobs.ecolemobile";

    /* Uri for using in content provider */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /* Path for adding to the uri
     * Used when wanting to get a specific item of the database.
     */
    public static final String PATH_VERTRETUNG = "vertretung";

    /* Path for adding to the uri
     * Used when wanting to get a specific "Klasse".
     * Will look like this content://com.johnlouisjacobs.ecolemobile/vertretung/klasse/#
     * The # will be the class we want to get.
     */
    public static final String PATH_KLASSE = "klasse";

    /* Inner class which defines the table content */
    public static final class VertretungDbEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Vertretung table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_VERTRETUNG)
                .build();

        /* Name of the Vertretungs Table */
        public static final String TABLE_NAME = "vertretung";

        /* Name of the Columns */
        public static final String COLUMN_STUNDE = "stunde";
        public static final String COLUMN_KLASSE = "klasse";
        public static final String COLUMN_FACH = "fach";
        public static final String COLUMN_LEHRER = "lehrer";
        public static final String COLUMN_INFORMATION = "information";
        public static final String COLUMN_RAUM = "raum";

        /**
         * Builds a URI that adds the id of the vertretungs view.
         * This is used to query details about a single vertretungs entry.
         *
         * @param id of the selected vertretungs view
         * @return Uri to query details about a single vertretungs entry
         */
        public static Uri buildVertretungsUriWithId(int id){
            return CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(id + 1))
                    .build();
        }

        /**
         * Builds a URI that adds the Klasse, which got selected in the login menu.
         * This is used to query informations about a specific Klasse.
         *
         * @param klasse of the wanted database item
         * @return Uri to query information about
         */
        public static Uri buildVertretungsUriWithKlasse(String klasse){
            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_KLASSE)
                    .appendPath(klasse)
                    .build();
        }
    }
}
