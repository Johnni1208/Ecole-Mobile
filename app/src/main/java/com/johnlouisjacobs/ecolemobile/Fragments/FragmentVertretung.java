package com.johnlouisjacobs.ecolemobile.Fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.johnlouisjacobs.ecolemobile.Adpaters.VertretungAdapter;
import com.johnlouisjacobs.ecolemobile.Data.VertretungContract;
import com.johnlouisjacobs.ecolemobile.Parsers.AdditionallyInformationParser;
import com.johnlouisjacobs.ecolemobile.Parsers.DateParser;
import com.johnlouisjacobs.ecolemobile.R;
import com.johnlouisjacobs.ecolemobile.Sync.VertretungSyncUtils;
import com.johnlouisjacobs.ecolemobile.Utils.NetworkUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by DenisNadine on 22.03.2018.
 */

public class FragmentVertretung extends android.support.v4.app.Fragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>,
        VertretungAdapter.VertretungsAdapterOnClickHandler {
    String TAG = getClass().getSimpleName();

    /*
     * The columns of data that we are interested in displaying within our MainActivity's list of
     * Vertretungs data.
     */
    public static final String[] MAIN_VERTRETUNGS_PROJECTION = {
            VertretungContract.VertretungDbEntry.COLUMN_STUNDE,
            VertretungContract.VertretungDbEntry.COLUMN_KLASSE,
            VertretungContract.VertretungDbEntry.COLUMN_FACH,
            VertretungContract.VertretungDbEntry.COLUMN_LEHRER,
            VertretungContract.VertretungDbEntry.COLUMN_INFORMATION,
            VertretungContract.VertretungDbEntry.COLUMN_RAUM,
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be
     * able to access the data from our query. NOTE: If the order of the Strings above change, these
     * indices must change to.
     */
    public static final int INDEX_VERTRETUNG_STUNDE = 0;
    public static final int INDEX_VERTRETUNG_KLASSE = 1;
    public static final int INDEX_VERTRETUNG_FACH = 2;
    public static final int INDEX_VERTRETUNG_LEHRER = 3;
    public static final int INDEX_VERTRETUNG_INFORMATION = 4;
    public static final int INDEX_VERTRETUNG_RAUM = 5;

    /**
     * URL with data
     **/
    private static final String URL =
            "http://www.stundenplan24.de/20173526/vplan/vdaten/VplanKl.xml?_=1534104031139";

    /**
     * All needed Views
     **/
    private TextView mTitle;
    private TextView mConnectionMessage;
    private TextView mAdditionalInfo;

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    /**
     * Holds the position of the RecyclerView
     * Initiates with NO_POSITION since we want to start at the top
     */
    private int mPosition = RecyclerView.NO_POSITION;

    private VertretungAdapter mAdapter;


    /**
     * LoaderManager for starting loading data in background thread
     **/
    android.support.v4.app.LoaderManager loaderManager;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vertretung, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find a reference to the Titel in the Layout
        mTitle = view.findViewById(R.id.vertretung_title);

        // Find reference to the error message TextView.
        mConnectionMessage = view.findViewById(R.id.connection_error);

        mAdditionalInfo = view.findViewById(R.id.additional_info);
        // Find a reference to the RecyclerView in the Layout
        mRecyclerView = view.findViewById(R.id.list);

        // Find reference to the Progressbar in the layout
        mProgressBar = view.findViewById(R.id.progress_bar);
        // Change color to red
        mProgressBar.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);

        /*
         * A LinearLayoutManager to manage our orientation.
         * It could also be a grid, horizontal, ...
         * Change last parameter reverseLayout to true if you want to support reversing the layout (Useful when using horizontal layout
         * and wanting to support right-to-left)
         */
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        // Attach the layout manager to the RecyclerView
        mRecyclerView.setLayoutManager(layoutManager);

        // Adds a divider between each list item
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        // Create a new ArrayAdapter for the ListView
        mAdapter = new VertretungAdapter(getContext(), R.layout.list_item_vertretung, this);
        /* Using this setting improves performance.
         * It tells the RecyclerView that it must not calculate the heights of the list items
         * since they will all have the same height.
         */
        mRecyclerView.setHasFixedSize(true);

        // Set the adapter of Vertretungen to the RecyclerView
        mRecyclerView.setAdapter(mAdapter);

        /*
         * This if-else block checks the Network Connectivity.
         * If the users has a FUNCTIONING Network connection it starts loading the data.
         * If not it uses the already downloaded database and waits until the user got a functioning network connection.
         * If the user has one, it starts loading new data.
         */
        if (NetworkUtils.isConnectedOrConnecting(getContext())) {
            loadVertretungsplan();
        } else {
            mProgressBar.setVisibility(View.GONE);
            visibilityConnectionMessage(true);
            ConnectivityAsyncTask connectivityAsyncTask = new ConnectivityAsyncTask();
            connectivityAsyncTask.execute();
        }
        setupLoader();
    }

    /**
     * Loads the title into the title TextView and loads the data into the database.
     */
    private void loadVertretungsplan() {
        // Start an AsyncTask to download the Additional Info out of the given XML file
        AdditionalInfoAsyncTask adTask =  new AdditionalInfoAsyncTask();
        adTask.execute(URL);

        // Start an AsyncTask to download the Date out of the given XML File
        DateAsyncTask titleTask = new DateAsyncTask();
        titleTask.execute(URL);

        // Starts an immediate sync of the database with the downloaded data.
        VertretungSyncUtils.startImmediateSync(getContext());
        // Schedules, if not done already, a Job for loading new content into the database in the background.
        VertretungSyncUtils.initialize(getContext());
    }

    /**
     * Sets up the loader
     **/
    private void setupLoader() {
        // Get a reference to the LoaderManager, in order to interact with the loaders.
        loaderManager = getLoaderManager();
        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(1, null, this);
    }

    /**
     * Called by the LoaderManager when a new Loader needs to be
     * created.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return A new Loader instance that is ready to start loading.
     */
    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        Uri vertretungsQueryUri = VertretungContract.VertretungDbEntry.CONTENT_URI;

        return new android.support.v4.content.CursorLoader(getContext(),
                vertretungsQueryUri,
                MAIN_VERTRETUNGS_PROJECTION,
                null,
                null,
                null);
    }

    /**
     * Called when a Loader has finished loading its data.
     **/
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showVertretungsDataView();
    }

    /**
     * Triggers when the device gets rotated or the app gets paused.
     * <p>
     * The application sgould at this point remove any references it has to the Loader's data.
     **/
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    // TODO: Add click function
    @Override
    public void onClick(int id) {
        Uri uri = VertretungContract.VertretungDbEntry.buildVertretungsUriWithId(id);
    }

    /**
     * Swaps the visibility of the Progressbar and the RecyclerView
     */
    private void showVertretungsDataView() {
        // The Progressbar gets hidden
        mProgressBar.setVisibility(View.GONE);
        // The RecyclerView gets visible
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method is only used when there is no network connection.
     * AsyncTask for Constantly checking if there is a connection to the internet.
     * When an connection could be established it loads the content into the RecyclerView and
     * loads the correct title.
     **/
    public class ConnectivityAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            while (!NetworkUtils.isConnectedOrConnecting(getContext()) && !NetworkUtils.isOnline(URL)) {
                // nothing here
            }
            loadVertretungsplan();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    visibilityConnectionMessage(false);
                }
            });
            return true;
        }
    }


    /**
     * Either shows the layout for when there is no internet connection or when there is a internet connection.
     *
     * @param visible if the "No-Connection" layout should be visible
     */
    private void visibilityConnectionMessage(boolean visible) {
        if (visible) {
            // Set the titel to no-connection-message
            mTitle.setText(R.string.text_no_connection);
            // Set the padding of the titel to 0, since the ConnectionMessage TextView has its own 16dp margin-top.
            mTitle.setPadding(0, 0, 0, 0);

            /* Show the subtitle with a description */
            /* For this we first get the date out of the stored date variable. This variable holds
             * last downloaded date.
             */
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String date = preferences.getString(getString(R.string.pref_date_key), getString(R.string.pref_date_default));
            // Then we get the precreated Text message and add the date
            String connectionMessage = getString(R.string.text_message_no_connection) + "\n" + date;
            mConnectionMessage.setText(connectionMessage);
            mConnectionMessage.setVisibility(View.VISIBLE);

            FragmentHome.sDate.setText(date);

            // Shows the saved Additional Infos
            mAdditionalInfo.setVisibility(View.VISIBLE);
            mAdditionalInfo.setText(preferences.getString(getString(R.string.pref_additional_key), getString(R.string.pref_additional_default)));

        } else {
            // If visible is true
            // Set the titles padding to bottom 16, since we don't have the ConnectionMessage anymore.
            mTitle.setPadding(0, 0, 0, 16);
            // Hide the ConnectionMessage, since we now have a network connection
            mConnectionMessage.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the date in FragmentVertretung and FragmentHome.
     * On top it saves the date for offline functionality.
     *
     * @param date String of the date
     */
    private void showDate(String date) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.pref_date_key), date);
        editor.apply();


        mTitle.setText(date);
        FragmentHome.sDate.setText(date);
    }

    /**
     * Loads the date and sets it to the FragmentsVertretungs middle part's titel.
     * Also sets it to the FragmentHome's Subtitle, under "Deine Vertretung".
     */
    private class DateAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            DateParser parser = new DateParser();
            String date = "";

            try {
                date = parser.loadXMLFromNetwork(urls[0]);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return date;
        }

        @Override
        protected void onPostExecute(String date) {
            super.onPostExecute(date);
            showDate(date);
        }
    }



    /**
     * Shows the additional info in FragmentVertretung.
     * On top it saves the additional info for offline functionality.
     *
     * @param additionals String of the date
     */
    private void showAdditionals(ArrayList<String> additionals){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();

        // If the additionals ArrayList is empty set the SharedPreference to ""
        if (additionals == null || additionals.size() == 0){
            editor.putString(getString(R.string.pref_additional_key), "");
            return;
        }

        // Extracts every element from the Arraylist
        StringBuilder addtionalFullTextBuilder = new StringBuilder();
        for (int i =0; i <= additionals.size(); i++) {
            if (i == additionals.size() - 1){
                addtionalFullTextBuilder.append("- ").append(additionals.get(i));
                break;
            }
            addtionalFullTextBuilder.append("- ").append(additionals.get(i)).append("\n\n");
        }
        // And builds a single string
        String additionalFullText = addtionalFullTextBuilder.toString();

        // Saves it in the SharedPreferences for offline uses

        editor.putString(getString(R.string.pref_additional_key), additionalFullText);
        editor.apply();


        // Sets it to the TextView
        mAdditionalInfo.setText(additionalFullText);
        mAdditionalInfo.setVisibility(View.VISIBLE);
        mTitle.setPadding(0,0,0,0);
    }

    /**
     * Loads the additional info out of the XML file and sets it under the Title.
     */
    private class AdditionalInfoAsyncTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... urls) {

            AdditionallyInformationParser parser = new AdditionallyInformationParser();
            ArrayList<String> additionalInfos = null;

            try {
                additionalInfos = parser.loadXMLFromNetwork(urls[0]);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return additionalInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<String> additionals) {
            super.onPostExecute(additionals);
            showAdditionals(additionals);
        }
    }
}
