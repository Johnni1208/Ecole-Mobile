package com.johnlouisjacobs.ecolemobile.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/** Utility class for Network purposes */
public class NetworkUtils {

    /**
     * Checks the Internet connection
     **/
    public static boolean isConnectedOrConnecting(Context context) {
        // Check for Connectivity Status
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        // Create boolean for telling if the device is connected to the internet
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Checks if the device can accses the internet, by calling the URL
     * isConnctedOrConnecting just checks for the Network state
     * NOTE:    There is a bug. Whenever the URL's Servers are down this returns also false,
     * even thought the device has access to the internet
     *
     * @return true if it can connect, false if not
     */
    public static boolean isOnline(String url) {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress(url, 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
