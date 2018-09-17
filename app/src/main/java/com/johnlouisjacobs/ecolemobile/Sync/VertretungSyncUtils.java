package com.johnlouisjacobs.ecolemobile.Sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.johnlouisjacobs.ecolemobile.Data.VertretungContract;

import java.util.concurrent.TimeUnit;

/**
 * Created by John on 20.04.2018.
 */

public class VertretungSyncUtils {

    /* Interval at which to sync with the vertretung.*/
    private static final int SYNC_INTERVAL_HOURS = 1;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS;

    private static boolean sInitialized;

    private static final String  VERTRETUNG_SYNC_TAG = "vertretung-sync";

    /**
     * Schedules a repeating sync of Sunshines's weather data using FirebaseJobDispatcher.
     * @param context Context used to create the GooglePlayDriver that powers the
     *                FireBaseJobDispatcher
     */
    static void scheduleFirebaseJobDispatcher (final Context context){

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        /* Creates the Job to periodically sync Vertretungs */
        Job syncVertretungJob = dispatcher.newJobBuilder()
                /* The service that will be used to sync Vertretung's data*/
                .setService(VertretungsFirebaseJobService.class)
                /* UNIQUE tag to identify Job */
                .setTag(VERTRETUNG_SYNC_TAG)
                /* Network constraint on which this Job will run.
                 * ON_ANY_NETWORK means that it can run on any network, also mobile data.
                 * The Job does not load big data, so we can use the users mobile data.
                 */
                .setConstraints(Constraint.ON_ANY_NETWORK)
                /* The Job lifes forever (or util the user uninstalls the app) */
                .setLifetime(Lifetime.FOREVER)
                /* We want the Vertretungs Data to stay up-to-date, so we recur this job. */
                .setRecurring(true)
                /* The Job loads every 1-2 hours new data */
                .setTrigger(Trigger.executionWindow(SYNC_INTERVAL_SECONDS,
                        SYNC_FLEXTIME_SECONDS + SYNC_INTERVAL_SECONDS))
                /* If there already exists a job with this tag, replace it. */
                .setReplaceCurrent(true)
                /* Once the job is ready, build it to unload it. */
                .build();
        /* Schedule the Job with the dispatcher */
        dispatcher.schedule(syncVertretungJob);
    }
    /**
     * Creates periodic sync task and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     *
     * @param context Context that will be passed tp other methods and used to access the
     *                ContentResolver
     */
    synchronized public static void initialize(final Context context){
        if (sInitialized) return;

        sInitialized = true;

        scheduleFirebaseJobDispatcher(context);

        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {
                Uri vertretungsUri = VertretungContract.VertretungDbEntry.CONTENT_URI;

                String[] projectionColums = {VertretungContract.VertretungDbEntry._ID};

                Cursor cursor = context.getContentResolver().query(
                        vertretungsUri,
                        projectionColums,
                        null,
                        null,
                        null);

                if (cursor == null || cursor.getCount()== 0){
                    startImmediateSync(context);
                }

                cursor.close();
            }
        });
        checkForEmpty.start();
    }

    public static void startImmediateSync(final Context context){
        Intent intentToSyncImmediatly = new Intent(context, VertretungSyncIntentService.class);
        context.startService(intentToSyncImmediatly);
    }
}
