package com.ldz.fpt.businesscardscannerandroid.utils.xml_parser;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by linhdq on 7/9/17.
 */

public class XMLParser {
    private static final String ITEM_NAME = "line";
    private static final String ELEMENT_TEXT = "text";
    private static final String ELEMENT_FONT_SIZE = "font_size";

    private static XMLParser inst;

    public static XMLParser getInst() {
        if (inst == null) {
            inst = new XMLParser();
        }
        return inst;
    }

    private Document getDomElement(String xml) {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);

        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        return doc;
    }

    private String getValue(Element item, String str) {
        NodeList n = item.getElementsByTagName(str);
        return this.getElementValue(n.item(0));
    }

    private final String getElementValue(Node elem) {
        Node child;
        if (elem != null) {
            if (elem.hasChildNodes()) {
                for (child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child.getNodeType() == Node.TEXT_NODE) {
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }

    public List<LineModel> getListLineFromXml(String xml) {
        List<LineModel> lineModels = new ArrayList<>();
        if (xml == null) {
            return lineModels;
        }
        Document doc = getDomElement(xml);
        NodeList nl = doc.getElementsByTagName(ITEM_NAME);
        int fontSize;
        String text;
        Element element;
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                element = (Element) nl.item(i);
                fontSize = 0;
                try {
                    fontSize = Integer.parseInt(getValue(element, ELEMENT_FONT_SIZE));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                text = getValue(element, ELEMENT_TEXT);
                if (fontSize > 10 && text != null && text.replaceAll(" ", "").toCharArray().length > 5) {
                    lineModels.add(new LineModel(fontSize, text));
                }
            }
        }
        return lineModels;
    }
}
