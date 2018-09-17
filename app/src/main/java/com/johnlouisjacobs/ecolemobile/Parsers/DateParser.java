package com.johnlouisjacobs.ecolemobile.Parsers;

import android.util.Base64;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by John on 01.02.2018.
 * This class parses XML from stundenplan24.de
 * Given an InputStream representation of a VERTRETUNG, it returns the "<titel>" element of the data,
 * which then gets used to set the title of the appbar to the date of the VERTRETUNG
 */

public class DateParser {
    //We don't use namespaces
    private static final String ns = null;

    private final String basicAuth = "Basic " + Base64.encodeToString("schueler:xxxx".getBytes(), Base64.NO_WRAP); // I will not show passwords online


    // This class represents the titel element in the XML feed.

    private String parse(InputStream in) throws XmlPullParserException, IOException {
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

    //Uploads XML from stundenplan24.de, parses it and pasts it into the String
    public String loadXMLFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        String titel;

        try {
            stream = downloadUrl(urlString);
            titel = parse(stream);


            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return titel;
    }

    private String readVP(XmlPullParser parser) throws XmlPullParserException, IOException {
        String titel = "";
        parser.require(XmlPullParser.START_TAG, ns, "vp");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the kopf tag
            if (name.equals("kopf")) {
                titel = readTitel(parser);

            } else {
                skip(parser);
            }

        }

        return titel;
    }


    private String readTitel(XmlPullParser parser) throws XmlPullParserException, IOException {
        String titel = "";
        parser.require(XmlPullParser.START_TAG, ns, "kopf");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("titel")) {
                titel = readInfo(parser, "titel");
            } else {
                skip(parser);
            }

        }
        return titel;
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
