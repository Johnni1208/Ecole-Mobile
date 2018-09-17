package com.johnlouisjacobs.ecolemobile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.johnlouisjacobs.ecolemobile.Adpaters.ViewPageAdapter;
import com.johnlouisjacobs.ecolemobile.Fragments.FragmentEssen;
import com.johnlouisjacobs.ecolemobile.Fragments.FragmentHome;
import com.johnlouisjacobs.ecolemobile.Fragments.FragmentVertretung;
import com.johnlouisjacobs.ecolemobile.Utils.BottomNavigationViewUtils;

public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    BottomNavigationViewEx bottomNavigationView;

    MenuItem prevMenuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        bottomNavigationView = findViewById(R.id.bottom_nav_bar);

        // Setup the bottomnavbar
        BottomNavigationViewUtils.setupNavbar(bottomNavigationView);

        /* When a menu item is clicked in the bottom navigation bar,
           change the viewpagers tab to the corresponding menu item */
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_essen:
                        viewPager.setCurrentItem(0);
                        return true;

                    case R.id.nav_home:
                        viewPager.setCurrentItem(1);
                        return true;

                    case R.id.nav_vplan:
                        viewPager.setCurrentItem(2);
                        return true;
                }
                return false;
            }
        });

        /* Triggers when the page gets swiped or changed */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                /* Changes the color corresponding to the current tab */
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });

        // Sets up viewpager
        setupViewPager(viewPager);
        // Sets the current tab to one, to make sure the main page is shown first
        viewPager.setCurrentItem(1);
    }

    /**
     * Sets up the viewpager with all needed Fragments
     *
     * @param viewPager to use
     */
    private void setupViewPager(ViewPager viewPager) {
        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
        Fragment leftFragment = new FragmentEssen();
        Fragment rightFragment = new FragmentVertretung();
        Fragment homeFragment = new FragmentHome();
        viewPageAdapter.addFragment(leftFragment);
        viewPageAdapter.addFragment(homeFragment);
        viewPageAdapter.addFragment(rightFragment);
        viewPager.setAdapter(viewPageAdapter);

    }


    /**
     * Overwriting onBackPressed() so you can't get back to the splashscreen.
     * Also when in FragmentEssen trigger the webViewGoBack() method of the FragmentEssen Class
     */
    @Override
    public void onBackPressed() {
        int currentPage = viewPager.getCurrentItem();
        switch (currentPage){
            case 0:
                FragmentEssen.webViewGoBack();
                break;
            case 1:
                FragmentHome.hideMenu();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FragmentEssen.webViewLogOut(getApplicationContext());
    }
}
