package com.johnlouisjacobs.ecolemobile.Helper;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

/**
 * Helper Class for Spinners
 * Created by John on 18.03.2018.
 */

public class SpinnerHelper implements AdapterView.OnItemSelectedListener {
    Context mContext;

    Spinner mSpinner;

    /**
     * Constructor
     *
     * @param mContext
     */
    public SpinnerHelper(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Sets Up given Spinner with a given List of Strings
     *
     * @param spinner  to setup
     * @param ItemList to fill spinner with
     */
    public void setUpAdapter(Spinner spinner, List<String> ItemList) {
        mSpinner = spinner;

        // Add Click Listener
        mSpinner.setOnItemSelectedListener(this);

        // Create adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, ItemList);

        // Add Dropdown View
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Add Adapter to given spinner
        mSpinner.setAdapter(dataAdapter);
    }

    /**
     * Triggers when a Spinner Item gets clicked
     *
     * @param parent   AdapterView -> AdapterList which got added to the spinner
     * @param view     which got clicked
     * @param position position in the arraylist
     * @param id       ...
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSpinner.setTag(position);
    }

    /**
     * Triggers when noting gets selected
     *
     * @param parent AdapterView -> AdapterList which got added to the spinner
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mSpinner.setTag(-1);
    }
}
