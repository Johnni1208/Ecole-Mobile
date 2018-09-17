package com.johnlouisjacobs.ecolemobile.Data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This class serves as the ContentProvider of all Vertretungsplan's data. This class allows us to
 * bulkInsert data, query data and delete data.
 * Created by John on 11.04.2018.
 */

public class VertretungsProvider extends ContentProvider {
    /* Constants for match URIs with a UriMatcher */
    public static final int CODE_VERTRETUNG = 100;
    public static final int CODE_VERTRETUNG_WITH_ID = 101;
    public static final int CODE_VERTRETUNG_WITH_KLASSE = 102;

    /* The URI matcher used by this content provider. */
    public static final UriMatcher sUriMatcher = buildUriMatcher();

    private VertretungDbHelper mOpenHelper;

    /**
     * Creates the UriMatcher that will match each URI to the CODE_VERTRETUNG and
     * CODE_VERTRETUNG_WITH_ID constants defined above.
     *
     * @return A UriMatcher that correctly matches the constants for CODE_VERTRETUNG and CODE_VERTRETUNG_WITH_ID
     */
    private static UriMatcher buildUriMatcher() {
        /* Create new matcher with NO_MATCH as constructor parameters.
         * NO_MATCH means that the UriMatcher returns for the root URI.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = VertretungContract.CONTENT_AUTHORITY;

        /* This URI is content://com.johnlouisjacobs.ecolemobile/vertretung/ */
        matcher.addURI(authority, VertretungContract.PATH_VERTRETUNG, CODE_VERTRETUNG);

        /* This URI is something like content://com.johnlouisjacobs.ecolemobile/vertretung/12
         * The "/#" signifies to the UriMatcher that if PATH_VERTRETUNG is follow by ANY number,
         * that it should return the CODE_VERTRETUNG_WITH_ID code
         */
        matcher.addURI(authority, VertretungContract.PATH_VERTRETUNG + "/#", CODE_VERTRETUNG_WITH_ID);

        /* This URI is something like content://com.johnlouisjacobs.ecolemobile/vertretung/klasse/9a
         * The "/#" signifies to the UriMatcher that if PATH_KLASSE is follow by ANY number/string,
         * that it should return the CODE_VERTRETUNG_WITH_KLASSE code
         */
        matcher.addURI(authority, VertretungContract.PATH_VERTRETUNG + "/" + VertretungContract.PATH_KLASSE + "/*", CODE_VERTRETUNG_WITH_KLASSE);

        return matcher;
    }

    /**
     * In onCreate, we initialize our content provider on startup. This method is called for all
     * registered content providers on the application main thread at application launch time.
     * It must not perform lengthy operations, or application startup will be delayed.
     *
     * @return true if the provider was successfully loaded, false otherwise
     */
    @Override
    public boolean onCreate() {
        /* Initialise the Database Helper.
         * We can do this here since this is not an hard operation and can be done on the main thread.
         */
        mOpenHelper = new VertretungDbHelper(getContext());
        return true;
    }

    /**
     * Handles request to insert a set of new rows.
     *
     * @param uri    The content:// URI of the insertion request (can only be CODE_VERTRETUNG)
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be null.
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        //  Gets a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Switch-block to test the given uri with our UriMatcher
        switch (sUriMatcher.match(uri)) {
            case CODE_VERTRETUNG:
                db.beginTransaction();
                // Holds number of inserted rows
                int rowsInserted = 0;

                try {
                    // Goes through all contentvalues
                    for (ContentValues value : values) {
                        // and inserts them one after another
                        long _id = db.insert(VertretungContract.VertretungDbEntry.TABLE_NAME, null, value);
                        // If it was successful add a row
                        if (_id != 1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                // If rows were added notify the content resolver that something had changed
                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                // Then return the inserted rows
                return rowsInserted;

            // Triggers when the uri does not match CODE_VERTRETUNG, which cannot be the case.
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {

            case CODE_VERTRETUNG_WITH_ID: {
                String id = uri.getLastPathSegment();

                String[] selectionArgumentsId = new String[]{id};

                cursor = mOpenHelper.getReadableDatabase().query(
                        VertretungContract.VertretungDbEntry.TABLE_NAME,
                        projection,
                        VertretungContract.VertretungDbEntry._ID + " = ? ",
                        selectionArgumentsId,
                        null,
                        null,
                        sortOrder);

                break;
            }

            case CODE_VERTRETUNG_WITH_KLASSE: {
                String klasse = uri.getLastPathSegment();

                String[] selectionArgumentsKlasse = new String[]{"%" + klasse + "%"};

                cursor = mOpenHelper.getReadableDatabase().query(
                        VertretungContract.VertretungDbEntry.TABLE_NAME,
                        projection,
                        VertretungContract.VertretungDbEntry.COLUMN_KLASSE + " LIKE ? ",
                        selectionArgumentsKlasse,
                        null,
                        null,
                        sortOrder);
                break;
            }

            case CODE_VERTRETUNG: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        VertretungContract.VertretungDbEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Deletes data at a given URI with optional arguments for more fine tuned deletions.
     *
     * @param uri               The full URI to query
     * @param selection         An optional restriction to apply to rows when deleting.
     * @param selectionArgs     Used in conjunction with the selection statement
     * @return The number of rows deleted
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        /* deleted rows */
        int numRowsDeleted;

        /* If the selection is null, it would normally delete the whole table.
         * Instead we replace it with 1 which, accordingly to the SQLite documentation, deletes all rows and
         * returns the number of deleted rows. (This it what the user of this method expects.)
         */
        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)){
            case CODE_VERTRETUNG:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        VertretungContract.VertretungDbEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
            }
            /* If we actually deleted any rows, notify that a change has occurred to this URI */
            if (numRowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    /** We are not implementing any other method since we don't need them. **/

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Not implementing getType.");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new RuntimeException("Use bulkInsert instead!");
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new RuntimeException("Not implementing update.");
    }
}
