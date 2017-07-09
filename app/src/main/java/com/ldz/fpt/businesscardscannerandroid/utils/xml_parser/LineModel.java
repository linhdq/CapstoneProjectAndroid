package com.ldz.fpt.businesscardscannerandroid.utils.xml_parser;

/**
 * Created by linhdq on 7/9/17.
 */

public class LineModel {
    private int fontSize;
    private String text;

    public LineModel() {
    }

    public LineModel(int fontSize, String text) {
        this.fontSize = fontSize;
        this.text = text;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format("FontSize: %d: %s\n", fontSize, text);
    }
}
