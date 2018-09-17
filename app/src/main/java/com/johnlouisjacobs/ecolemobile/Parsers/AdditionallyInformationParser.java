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

public class AdditionallyInformationParser {
    //We don't use namespaces
    private static final String ns = null;

    private final String basicAuth = "Basic " + Base64.encodeToString("schueler:xxxx".getBytes(), Base64.NO_WRAP); // I will not show passwords online


    // This class represents the titel element in the XML feed.

    private ArrayList<String> parse(InputStream in) throws XmlPullParserException, IOException {
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
    public ArrayList<String> loadXMLFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        ArrayList<String> additionals;

        try {
            stream = downloadUrl(urlString);

            additionals = parse(stream);


            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return additionals;
    }

    private ArrayList<String> readVP(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<String> additionals = new ArrayList();
        parser.require(XmlPullParser.START_TAG, ns, "vp");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the kopf tag
            if (name.equals("fuss")) {
                parser.nextTag();
                additionals = readFuss(parser);

            } else {
                skip(parser);
            }

        }

        return additionals;
    }


    private ArrayList readFuss(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<String> additionals = new ArrayList<>();
        String fussinfo = "";

        parser.require(XmlPullParser.START_TAG, ns, "fusszeile");
        while (parser.next() != XmlPullParser.END_TAG) {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("fussinfo")) {
                    fussinfo = readInfo(parser, "fussinfo");
                } else {
                    skip(parser);
                }
            }
            additionals.add(fussinfo);
            parser.next();
        }
        return additionals;
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
