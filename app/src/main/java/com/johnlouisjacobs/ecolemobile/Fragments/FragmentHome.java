package com.johnlouisjacobs.ecolemobile.Fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.johnlouisjacobs.ecolemobile.Adpaters.VertretungAdapter;
import com.johnlouisjacobs.ecolemobile.Data.VertretungContract;
import com.johnlouisjacobs.ecolemobile.HelpActivity;
import com.johnlouisjacobs.ecolemobile.Helper.KlassenHelper;
import com.johnlouisjacobs.ecolemobile.LoginActivity;
import com.johnlouisjacobs.ecolemobile.R;


/**
 * Created by John-Louis Jacobs on 22.03.2018.
 */

public class FragmentHome extends android.support.v4.app.Fragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>,
        VertretungAdapter.VertretungsAdapterOnClickHandler {

    private LinearLayout mContent;

    /* Static TextView which shows the date.
     * We specified this as "static" since we access it in another class ("FragmentVertretung").
     */
    public static TextView sDate;

    /* The RecyclerView in the layout with its current position.
     * We set it to NO_POSITION since we want to start form the top.
     */
    private RecyclerView mRecyclerView;
    public int mPosition = RecyclerView.NO_POSITION;

    /*
     * Imagebutton which gets displayed when some vertretungsviews are out of sight.
     * It scrolls down to the bottom of the RecyclerView
     */
    private ImageButton mScrollDown;

    /*
     * The titel of the Vertretung.
     * It's been used to either show that there is Vertretung available or show
     * a message which says that there is no Vertretung.
     */
    private TextView mTitel;

    /*
     * The three points in the top right corner.
     * Used to show a little options menu.
     */
    private ImageButton mMore;

    /*
     * The container of the menu.
     * We have a container for them to apply animations to it.
     */
    private static LinearLayout mMoreContainer;
    /*
     * Animation duration for the menu container mMoreContainer.
     */
    private final static int DURATION_ANIMATION_MENU = 200;

    /*
     * The logout button.
     * We handle this TextView as a button.
     */
    private TextView mTvLogout;

    /*
     * The Help button.
     * We handle this TextView as a button.
     * When the user clicks this button it redirects the user to the help and impressum activity.
     */
    private TextView mTvHelp;

    private VertretungAdapter mAdapter;

    private Cursor mCursor;

    private static Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();

        mContent = view.findViewById(R.id.content_home);
        mContent.setOnClickListener(OCLContent);

        sDate = view.findViewById(R.id.main_date);

        mTitel = view.findViewById(R.id.main_vertretung_title);

        mMore = view.findViewById(R.id.imgbtn_more);
        mMore.setOnClickListener(OCLMore);

        mMoreContainer = view.findViewById(R.id.menu_more);

        mTvLogout = view.findViewById(R.id.btn_logout);
        mTvLogout.setOnClickListener(OCLLogout);

        mTvHelp = view.findViewById(R.id.btn_help);
        mTvHelp.setOnClickListener(OCLHelp);

        mScrollDown = view.findViewById(R.id.imgbtn_scrolldown);
        mScrollDown.setOnClickListener(OCLScrollDown);

        /* Initialise the Recycler View with all settings */
        mRecyclerView = view.findViewById(R.id.recycler_main);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mAdapter = new VertretungAdapter(getContext(), R.layout.list_item_vertretung_personal, this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        /* Setup the Loader to load the data out of the database */
        setupLoader();

    }

    /**
     * Sets up the loader
     **/
    private void setupLoader() {
        // Get a reference to the LoaderManager, in order to interact with the loaders.
        android.support.v4.app.LoaderManager loaderManager = getLoaderManager();
        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(1, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /* We first get the Klasse which got set in the LoginScreen out of the SharedPreferences. */
        String klasse = KlassenHelper.getSelectedKlasse(getContext());
        /* And then build an Uri to use it with our cursor */
        Uri vertretungWithKlasseQueryUri = VertretungContract.VertretungDbEntry.buildVertretungsUriWithKlasse(klasse);


        /* Then returns a new Cursor with all the data */
        return new android.support.v4.content.CursorLoader(getContext(),
                vertretungWithKlasseQueryUri,
                FragmentVertretung.MAIN_VERTRETUNGS_PROJECTION,
                null,
                null,
                null);
    }

    /**
     * Triggers when the cursor has finished loading.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        /* We then set the cursor to a global variable. */
        mCursor = data;

        /* Then we look for any items in the cursor.
         * It can happen that the cursor has no items, per example when the chosen Klasse has no
         * Vertretung.
         */
        if (!mCursor.moveToFirst()) {
            showNoVertetung();
        } else {
            /* If the cursor has results we show them */
            showVertretung(mCursor);
        }

    }

    /**
     * Triggers when the device gets rotated or the app gets paused.
     * <p>
     * The application should at this point remove any references it has to the Loader's data.
     **/
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onClick(int id) {

    }

    /**
     * Shows that no Vertretungsplan is available.
     * It sets the titel so it says that there is no Vertretung.
     * Also it sets the padding of the date to 16 bottom so it looks
     * better.
     */
    private void showNoVertetung() {
        mTitel.setText(getString(R.string.text_no_vertretung));
        mAdapter.swapCursor(null);
        /*
         * This translates the int value to dp, the .setPadding method
         * sets the padding in pixel
         */
        int padding_in_dp = 16; // 16dp
        final float scale = getResources().getDisplayMetrics().density;
        int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

        sDate.setPadding(0, 0, 0, padding_in_px);
    }

    /**
     * Shows the Vertretungsplan which got extracted from the database.
     * Additionally it adds a OnChildAttachStateChangeListener to the RecyclerView,
     * so we can trigger the showScrollButton method.
     * Also we add an onScrollListener so we can keep track, whether the user scrolls up or down.
     * We use this for later purposes.
     *
     * @param data Cursor which holds the data.
     */
    private void showVertretung(Cursor data) {
        mTitel.setText(getString(R.string.text_titel_deine_vertretung));
        mAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);

        mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            /*
             * We use these variables and the onChildViewAttachedToWindow method
             * to fix a bug, where the ScrollButton is visible even though we see all the available
             * Vertretung.
             */
            View mView;
            Boolean getViewInitalized = false;
            Boolean setScrollButtonInvisible = false;

            @Override
            public void onChildViewAttachedToWindow(View view) {
                /*
                 * We simply test if the first attached view is the same when it gets attached again.
                 * (Out of no reason it double attaches the child views)
                 */
                if (!getViewInitalized) {
                    mView = view;
                    getViewInitalized = true;
                }
                if (mView == view && !setScrollButtonInvisible) {
                    mScrollDown.setVisibility(View.INVISIBLE);
                    setScrollButtonInvisible = true;
                }


            }

            /*
             * When there are more than ChildViews, than fitting on the screen
             * we show the ScrollDown button.
             */
            @Override
            public void onChildViewDetachedFromWindow(View view) {
                showScrollButton();
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) mPosition = 0;
                if (dy > 0) mPosition = 1;
            }
        });
    }

    /**
     * Method for whether showing or hiding the ScrollButton
     */
    private void showScrollButton() {
        if (mScrollDown.getVisibility() != View.VISIBLE) {
            mScrollDown.setVisibility(View.VISIBLE);
        }
        if (mScrollDown.getVisibility() == View.VISIBLE && mPosition == 1) {
            mScrollDown.setVisibility(View.INVISIBLE);
        }
    }


    /*************************
     * OnClickListeners
     *************************/

    /**
     * OnClickListener for the whole layout
     * Its used to hide the menu in the top right corner when the user clicks away.
     * For that it uses a scale in animation. (res/anim/menu_scale_out)
     */
    private View.OnClickListener OCLContent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideMenu();
        }
    };
    public static void hideMenu(){
        if (mMoreContainer.getVisibility() == View.VISIBLE){
            Animation scaleOut = AnimationUtils.loadAnimation(context, R.anim.menu_scale_out);
            scaleOut.setInterpolator(new FastOutSlowInInterpolator());
            scaleOut.setDuration(DURATION_ANIMATION_MENU);
            mMoreContainer.startAnimation(scaleOut);
            mMoreContainer.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * OnClickListener for the little ScrollDownButton.
     * When the button gets clicked it scrolls to the bottom of the RecyclerView
     * and makes itself invisible.
     */
    private View.OnClickListener OCLScrollDown = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPosition = mCursor.getCount();
            mRecyclerView.smoothScrollToPosition(mPosition);
            mScrollDown.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * OnClickListener for the three points in top right.
     * When the button gets clicked it shows the menu with a little scale in animation. (res/anim/menu_scale_in)
     */
    private View.OnClickListener OCLMore = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Animation scaleIn = AnimationUtils.loadAnimation(getContext(), R.anim.menu_scale_in);
            scaleIn.setInterpolator(new FastOutSlowInInterpolator());
            scaleIn.setDuration(DURATION_ANIMATION_MENU);
            mMoreContainer.startAnimation(scaleIn);
            mMoreContainer.setVisibility(View.VISIBLE);
        }
    };

    /**
     * OnClickListener for the logout button in the menu.
     * When the logout button gets clicked, it logs the user out and shows a Toast message,
     * that the user has been logged out.
     */
    private View.OnClickListener OCLLogout = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getResources().getString(R.string.pref_logged_in_key), false);
            editor.apply();
            FragmentEssen.webViewLogOut(context);
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            Toast.makeText(getContext(), R.string.text_toast_logged_out, Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener OCLHelp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent helpActivity = new Intent(getContext(), HelpActivity.class);
            startActivity(helpActivity);
        }
    };
}
