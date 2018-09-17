package com.johnlouisjacobs.ecolemobile.Parsers;

import android.util.Base64;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by John on 24.11.2017.
 * This class parses XML from stundenplan24.de
 * Given an InputStream representation of a VERTRETUNG, it returns a List of entries,
 * where each list element represents a single <aktion> in the XML feed.
 */
public class VertretungsplanParser {
    //We don't use namespaces
    private static final String ns = null;

    private final String basicAuth = "Basic " + Base64.encodeToString("schueler:xxxx".getBytes(), Base64.NO_WRAP); // I will not show passwords online


    // This class represents a single aktion (Vertretungs element) in the XML feed.
    // It includes the data members "klasse", "fach" and "lehrer"

    public static class Entry {
        private final String stunde;
        private final String klasse;
        private final String fach;
        private final String lehrer;
        private final String information;
        private final String raum;

        /**
         * Create a new Entry object
         *
         * @param stunde      Hour of the Vertretung
         * @param fach        Course of the Vertretung which replaces the normal course
         * @param lehrer      Teacher which does the vertretung
         * @param information provided information
         */
        public Entry(String stunde, String klasse, String fach, String lehrer, String information, String raum) {
            this.stunde = stunde;
            this.klasse = klasse;
            this.fach = fach;
            this.lehrer = lehrer;
            this.information = information;
            this.raum = raum;
        }

        /**
         * Get the hour of the Entry
         */
        public String getStunde() {
            return stunde;
        }

        /**
         * Get the klasse of the Entry
         */
        public String getKlasse() {
            return klasse;
        }

        /**
         * Get the Fach of the Entry
         */
        public String getFach() {
            return fach;
        }

        /**
         * Get the Lehrer of the Entry
         */
        public String getLehrer() {
            return lehrer;
        }

        /**
         * Get the Information of the Entry
         */
        public String getInformation() {
            return information;
        }

        /**
         * Get the Raum of the Entry
         */
        public String getRaum() {
            return raum;
        }
    }

    private ArrayList<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readVP(parser);

            //Close input stream
        } finally {
            in.close();
        }
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", basicAuth);
        conn.setDoInput(true);

        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    //Uploads XML from stundenplan24.de, parses it and pasts it into the Arraylist to be set into a recycler view
    public ArrayList<Entry> loadXMLFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        VertretungsplanParser vertretungsPlanParser = new VertretungsplanParser();
        ArrayList<VertretungsplanParser.Entry> entries;

        try {
            stream = vertretungsPlanParser.downloadUrl(urlString);
            entries = vertretungsPlanParser.parse(stream);


            //Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return entries;
    }

    private ArrayList<Entry> readVP(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Entry> entries = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, ns, "vp");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the action tag
            if (name.equals("haupt")) {
                parser.nextTag();
                entries = readAktion(parser);

            } else {
                skip(parser);
            }

        }

        return entries;
    }


    private ArrayList<Entry> readAktion(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Entry> list = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, ns, "aktion");
        String stunde = null;
        String klasse = null;
        String fach = null;
        String lehrer = null;
        String information = null;
        String raum = null;
        Entry entry = new Entry(stunde, klasse, fach, lehrer, information, raum);
        while (parser.next() != XmlPullParser.END_TAG) {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                switch (name) {
                    case "stunde":
                        stunde = readInfo(parser, "stunde");
                        break;
                    case "fach":
                        fach = readInfo(parser, "fach");
                        break;
                    case "klasse":
                        klasse = readInfo(parser, "klasse");
                        break;
                    case "lehrer":
                        lehrer = readInfo(parser, "lehrer");
                        break;
                    case "info":
                        information = readInfo(parser, "info");
                        break;
                    case "raum":
                        raum = readInfo(parser, "raum");
                        break;
                    default:
                        skip(parser);
                        break;
                }
                entry = new Entry(stunde + ". Stunde", klasse, fach, lehrer, information, raum);

            }
            list.add(entry);
            parser.next();
        }
        return list;
    }


    //Processes the information of the given name
    private String readInfo(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String information = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return information;
    }

    //Extracts text values
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
