package com.johnlouisjacobs.ecolemobile.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by John on 11.04.2018.
 */

public class VertretungDbHelper extends SQLiteOpenHelper {
    /* Name of the database */

    public static final String DATABASE_NAME = "ecolemobilevplan.db";

    /* Version number of the database
    *  IMPORTANT INCREASE WHEN CHANGING SOMETHING OF THE DATABASE
    */
    private static final int DATABASE_VERSION = 1;

    /* Constructor of this class */
    public VertretungDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the table should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        /* String Command for executing the sql-command */
        final String SQL_CREATE_VERTRETUNG_TABLE =

                /* Create new database */
                "CREATE TABLE " + VertretungContract.VertretungDbEntry.TABLE_NAME + "(" +

                        /* Create all columns */
                        VertretungContract.VertretungDbEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        VertretungContract.VertretungDbEntry.COLUMN_STUNDE + " TEXT NOT NULL, " +

                        VertretungContract.VertretungDbEntry.COLUMN_KLASSE + " TEXT NOT NULL, " +

                        VertretungContract.VertretungDbEntry.COLUMN_FACH + " TEXT NOT NULL, " +

                        VertretungContract.VertretungDbEntry.COLUMN_LEHRER + " TEXT NOT NULL, " +

                        VertretungContract.VertretungDbEntry.COLUMN_INFORMATION + " TEXT NOT NULL, " +

                        VertretungContract.VertretungDbEntry.COLUMN_RAUM + " TEXT NOT NULL);";

        /* After we created the command execute it */
        sqLiteDatabase.execSQL(SQL_CREATE_VERTRETUNG_TABLE);
    }

    /** This method gets called when changing the version number.
     * It just discards the old database.
     * @param sqLiteDatabase    Database that is being used
     * @param oldVersion        The old database version
     * @param newVersion        The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + VertretungContract.VertretungDbEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
