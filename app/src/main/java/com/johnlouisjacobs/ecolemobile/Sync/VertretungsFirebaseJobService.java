package com.johnlouisjacobs.ecolemobile.Sync;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Service for getting new Vertretungsplan.
 * Created by John on 20.04.2018.
 */

public class VertretungsFirebaseJobService extends JobService {
    private AsyncTask<Void, Void, Void> mFetchVertretungsplanTask;

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     *
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        mFetchVertretungsplanTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Context context = getApplicationContext();
                com.johnlouisjacobs.ecolemobile.Sync.VertretungSyncTask.syncVertretung(context);
                jobFinished(jobParameters, false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParameters, false);
            }
        };
        mFetchVertretungsplanTask.execute();
        return true;
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return whether the job should be retried
     */
    @Override
    public boolean onStopJob(JobParameters job) {
        if (mFetchVertretungsplanTask != null){
            mFetchVertretungsplanTask.cancel(true);
        }
        return true;
    }
}
