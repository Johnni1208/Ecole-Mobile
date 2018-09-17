package com.johnlouisjacobs.ecolemobile.Sync;

import android.app.IntentService;
import android.content.Intent;

/**
 * An IntentService subclass for handling asynchronous task request in
 * a service on a separate handler thread.
 * Created by John on 21.04.2018.
 */

public class VertretungSyncIntentService extends IntentService {

    public VertretungSyncIntentService() {
        super("VertretungSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        com.johnlouisjacobs.ecolemobile.Sync.VertretungSyncTask.syncVertretung(this);
    }
}
