package com.johnlouisjacobs.ecolemobile.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.johnlouisjacobs.ecolemobile.R;
import com.johnlouisjacobs.ecolemobile.Utils.NetworkUtils;

import static android.view.View.GONE;

/**
 * Essen-Bestellen Fragement to order food
 * Created by John on 22.03.2018.
 */

public class FragmentEssen extends android.support.v4.app.Fragment {
    ProgressBar mPBar;
    TextView mNetworkErrorTextView;
    /**
     * The SwipeRefreshLayout
     **/
    private SwipeRefreshLayout refreshLayout;

    /**
     * Webview in this Fragment
     **/
    public static WebView wV;

    /**
     * URL of the website to order food
     **/
    private final String MAIN_URL = "https://mpibs.de/skitbs/";

    /**
     * 2. URL of the website to order food
     **/
    private final String SECOND_MAIN_URL = "https://mpibs.de/skitbs/public/login.xhtml";

    String mUsername;
    String mPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_essen, container, false);
    }

    /**
     * Is triggered when the View got created
     **/
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mPBar = view.findViewById(R.id.progress_bar_essen);
        mNetworkErrorTextView = view.findViewById(R.id.essen_network_error);
        // Gets the refreshlayout
        refreshLayout = getView().findViewById(R.id.refresh_layout);
        // and adds a OnRefreshListener for listening to a swipe down which triggers the reload animation
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // then just reload the page
                wV.reload();
            }
        });

        // Get SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Gets the in the login screen set variables of "Essen-Kundennummer" and "Essen-Kundennummer-Passwort"
        mUsername = preferences.getString(getContext().getResources().getString(R.string.pref_essen_kundennummer_key), getContext().getResources().getString(R.string.pref_essen_kundennummer_default));
        mPassword = preferences.getString(getContext().getResources().getString(R.string.pref_essen_passwort_key), getContext().getResources().getString(R.string.pref_essen_passwort_default));

        // Gets the webview in our layout
        wV = getView().findViewById(R.id.webview);

        /* If the device is online start loading the page.
         * Else show an error message.
         */
        if (NetworkUtils.isConnectedOrConnecting(getContext())) {
            loadWebpage();
        } else {
            mPBar.setVisibility(GONE);
            mNetworkErrorTextView.setVisibility(View.VISIBLE);
            ConnectivityAsyncTask connectivityTask = new ConnectivityAsyncTask();
            connectivityTask.execute();
        }
    }

    private void loadWebpage() {
        if (mPBar.getVisibility() == GONE) {
            mPBar.setVisibility(View.VISIBLE);
        }
        // Lets the Webview load the main url
        wV.loadUrl(MAIN_URL);
        /* Activate JavaScript for later use.
         * We can use JavascriptEnabled eventhought there are some Javascript
         * vulnerabilities, because we know that the Website we load won't inject any Javascript
         * to hurt the user.
         */
        wV.getSettings().setJavaScriptEnabled(true);
        // Now sets the WebView to a custom WebViewClient
        wV.setWebViewClient(new WebViewClient() {
            /* Triggers when the WebView has gotten the DOM and now starts to display it.
             * We use this method instead of onPageFinished to hide the ProgressBar, because
             * in onPageFinished the ProgressBar is shown until it's FULLY loaded. So it's visible even
             * if we see something appear. We don't want that.
             */
            @Override
            public void onPageCommitVisible(WebView view, String url) {
                /* Hide progressbar since the website has now loaded */
                if (mPBar.getVisibility() == View.VISIBLE) {
                    mPBar.setVisibility(GONE);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // If any error occurs try to reload the page
                loadWebpage();
            }

            // Triggers when the page has finished loading
            @Override
            public void onPageFinished(WebView view, String url) {
                /* If the refreshlayout currently shows the reload animation,
                stop it since we don't load anything anymore */
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
                /* If the SDK Version is above KITKAT and the webview is currently in one of
                  the two websites trigger a javascript injection to automatically fill in the username
                  and the password and then press the login button, so the user don't has to click anything. */
                if (Build.VERSION.SDK_INT >= 19 && (url.equals(MAIN_URL) || url.equals(SECOND_MAIN_URL))) {

                    /* Test if the user has entered a Password and a Username in the Login activity */
                    if (!mUsername.isEmpty() && !mPassword.isEmpty()) {
                        // For this we use a simple javascript call.
                        String js = "javascript:document.getElementById('j_idt6:UserName').value = '" + mUsername + "';"
                                + "document.getElementById('j_idt6:Password').value = '" + mPassword + "';"
                                + "document.getElementById('j_idt6:_loginButton').click()";
                        view.evaluateJavascript(js, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                // do nothing, since we don't receive any values
                            }
                        });
                    }
                }

            }

            /**
             * Custom WebViewClient for overwriting the onFormResubmission method, since we can't
             * accept a form resubmission inside the webview. It accepts the resubmission by itself.
             */
            @Override
            public void onFormResubmission(WebView view, Message dontResend, Message resend) {
                resend.sendToTarget();
            }
        });
    }


    /**
     * If the webview can go back a page, then go back. Else just stay where it is.
     */
    public static void webViewGoBack() {
        if (wV.canGoBack()) {
            wV.goBack();
        }
    }

    public static void webViewLogOut(Context context) {
        wV.clearCache(true);
        wV.clearHistory();
        clearCookies(context);
    }

    /**
     * This method clears all cookies.
     * We use this method when a user logsout of the app, so the website of the Essensdienst doesn't log into the old account.
     */
    public static void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }


    /**
     * This method is only used when there is no network connection.
     * AsyncTask for Constantly checking if there is a connection to the internet.
     * When an connection could be established it loads the content into the RecyclerView and
     * loads the correct title.
     **/
    private class ConnectivityAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            while (!NetworkUtils.isConnectedOrConnecting(getContext()) && !NetworkUtils.isOnline(MAIN_URL)) {
                // nothing here
            }
            // if it has a connection
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNetworkErrorTextView.setVisibility(View.GONE);
                    loadWebpage();
                }
            });

            return true;
        }
    }


}
