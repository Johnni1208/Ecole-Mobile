package com.johnlouisjacobs.ecolemobile.Adpaters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.johnlouisjacobs.ecolemobile.Fragments.FragmentVertretung;
import com.johnlouisjacobs.ecolemobile.R;

/**
 * The VertretungAdapter is an ArrayAdpater that can provide the layout for each item
 * based on a data source, which is a list of Entry objects.
 */

public class VertretungAdapter extends RecyclerView.Adapter<VertretungAdapter.VertretungsViewHolder> {
    private int mResourceId;

    /* The context to use for utility purposes */
    private final Context mContext;

    /*
     * Below we've defined an interface to handle clicks on items within this Adapter. In the
     * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
     * said interface. We store that instance in this variable to call the onClick method whenever
     * an item is clicked in the list.
     */
    private final VertretungsAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives the onClick messages.
     */
    public interface VertretungsAdapterOnClickHandler {
        void onClick(int id);
    }

    private Cursor mCursor;

    /**
     * Creates a VertretungsAdapter.
     *
     * @param context      Used to talk to the UI and app rescources
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public VertretungAdapter(Context context,int resource, VertretungsAdapterOnClickHandler clickHandler) {
        mContext = context;
        mResourceId = resource;
        mClickHandler = clickHandler;
    }

    @Override
    public VertretungsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(mResourceId, parent, false);
        view.setFocusable(true);
        return new VertretungsViewHolder(view);
    }

    /**
     * Is called by the RecyclerView to display the data at the specified position.
     * One after another it fills up the visible childviews with the data given by the database.
     *
     * @param vertretungsViewHolder The Viewholder which should be updated to represent
     *                              the contents of the item at the given position in the data set.
     * @param position              The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(VertretungsViewHolder vertretungsViewHolder, int position) {
        mCursor.moveToPosition(position);

        /** Klasse **/
        String klasse = mCursor.getString(FragmentVertretung.INDEX_VERTRETUNG_KLASSE);

        /* Resizes the KlassenView accordingly to the length of tht gotten Klassen String.
         * Per exemple if the gotten string is "9a-9d/ Wil", it resizes the textsize to 10.
         */
        if (klasse.length() == 3) {
            vertretungsViewHolder.klassenView.setPadding(0, 0, 0, 0);
            vertretungsViewHolder.klassenView.setTextSize(18);
        }
        if (klasse.length() > 3) vertretungsViewHolder.klassenView.setTextSize(9);
        if (klasse.length() < 3) vertretungsViewHolder.klassenView.setTextSize(24);

        vertretungsViewHolder.klassenView.setText(klasse);


        /** Stunde **/
        String stunde = mCursor.getString(FragmentVertretung.INDEX_VERTRETUNG_STUNDE);
        vertretungsViewHolder.stundenView.setText(stunde);

        /** Fach **/
        String fach = mCursor.getString(FragmentVertretung.INDEX_VERTRETUNG_FACH);
        vertretungsViewHolder.fachView.setText(fach);

        /** Lehrer **/
        String lehrer = mCursor.getString(FragmentVertretung.INDEX_VERTRETUNG_LEHRER);
        vertretungsViewHolder.lehrerView.setText(lehrer);

        /** Raum **/
        String raum = mCursor.getString(FragmentVertretung.INDEX_VERTRETUNG_RAUM);
        vertretungsViewHolder.raumView.setText(raum);

        /** Information **/
        String information = mCursor.getString(FragmentVertretung.INDEX_VERTRETUNG_INFORMATION);
        vertretungsViewHolder.informationView.setText(information);

    }


    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our views and for animations.
     *
     * @return the number of items availble in our forecast
     */
    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    /**
     * Swaps the cursor used by the VertretungsAdapter for its Vertretungs data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the Vertretungs data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as VertretungsAdapter's data source
     */
    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * Caches the child views for a list item. It also sets the OnClickListener, since it has access to the adapter
     * and the view.
     */
    class VertretungsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView klassenView;
        final TextView stundenView;
        final TextView fachView;
        final TextView lehrerView;
        final TextView raumView;
        final TextView informationView;

        VertretungsViewHolder(View view) {
            super(view);

            klassenView = view.findViewById(R.id.klasse);
            stundenView = view.findViewById(R.id.stunde);
            fachView = view.findViewById(R.id.fach);
            lehrerView = view.findViewById(R.id.lehrer);
            raumView = view.findViewById(R.id.raum);
            informationView = view.findViewById(R.id.info);

            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click. We fetch the normalized_id that has been
         * selected, and then call the onClick handler registered with this adapter, passing that
         * id.
         *
         * @param v the view that was clicked.
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onClick(adapterPosition);
        }

    }
}
