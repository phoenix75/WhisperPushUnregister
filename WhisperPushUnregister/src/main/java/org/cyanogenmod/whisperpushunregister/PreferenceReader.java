package org.cyanogenmod.whisperpushunregister;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PreferenceReader {
    private static final String TAG = PreferenceReader.class.getSimpleName();

    public static final String PREF_XML_PATH = "/data/data/org.whispersystems.whisperpush/shared_prefs/org.whispersystems.whisperpush_preferences.xml";
    private static final String PREF_PUSH_PASSWORD = "pref_push_password";
    private static final String PREF_REGISTERED_NUMBER = "pref_registered_number";

    private HashMap<String, String> preferenceValues = new HashMap<String, String>();

    public PreferenceReader() throws PreferenceReadException {
        try {
            String xml = readPreferenceXml();
            InputStream xmlInputStream = new ByteArrayInputStream(xml.getBytes());
            Document document = getDocument(xmlInputStream);
            parseDocument(document);
        } catch (Exception e) {
            throw new PreferenceReadException(e);
        }
    }

    private static Document getDocument(InputStream inputStream) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            return document;
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "ParserConfigurationException", e);
        } catch (SAXException e) {
            Log.e(TAG, "SAXException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }

        return null;
    }

    private void parseDocument(Document document) {
        Node mapNode = document.getFirstChild();
        if (mapNode.getNodeType() == Node.ELEMENT_NODE) {
            NodeList childNodes = mapNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Node attribute = node.getAttributes().getNamedItem("name");

                    String key = attribute.getNodeValue();
                    String value = getString(node);

                    if (key != null && value != null) {
                        preferenceValues.put(key, value);
                    }
                }
            }
        }
    }

    private static String getString(Node node) {
        NodeList nodes = node.getChildNodes();
        if (nodes != null && nodes.getLength() > 0) {
            return nodes.item(0).getNodeValue();
        } else {
            return null;
        }
    }

    private String readPreferenceXml() {
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/su -c /system/bin/sh");
            OutputStream stdin = process.getOutputStream();

            stdin.write(("cat " + PREF_XML_PATH + "\n").getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();

            StringBuilder builder = new StringBuilder();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null && line.length() > 0) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
            throw new RuntimeException(e);
        }
    }

    public String getPassword() {
        return preferenceValues.get(PREF_PUSH_PASSWORD);
    }

    public String getRegisteredNumber() {
        return preferenceValues.get(PREF_REGISTERED_NUMBER);
    }
}
