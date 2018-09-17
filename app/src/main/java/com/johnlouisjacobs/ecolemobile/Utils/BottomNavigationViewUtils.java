package com.johnlouisjacobs.ecolemobile.Utils;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/**
 * Utility class for seting up bottomnavigation views
 * Created by John on 22.03.2018.
 */

public class BottomNavigationViewUtils {
    /**
     * Disables all animations, per example shifting or resizing.
     * Also it disables text.
     * @param bar to use
     */
    public static void setupNavbar(BottomNavigationViewEx bar){
        bar.enableShiftingMode(false);
        bar.enableItemShiftingMode(false);
        bar.enableAnimation(false);
        bar.setTextVisibility(false);
    }
}
