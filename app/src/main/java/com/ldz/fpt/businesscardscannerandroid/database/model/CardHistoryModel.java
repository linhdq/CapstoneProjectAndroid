package com.ldz.fpt.businesscardscannerandroid.database.model;

/**
 * Created by linhdq on 4/20/17.
 */

public class CardHistoryModel {
    private int id;
    private String contactName;
    private String phoneNumber;
    private String emailAddress;
    private String cardImageBase64;
    private String cardUri;
    private String htmlText;

    public CardHistoryModel(String contactName, String phoneNumber, String emailAddress, String cardImageBase64, String cardUri, String htmlText) {
        this.id = -1;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.cardImageBase64 = cardImageBase64;
        this.cardUri = cardUri;
        this.htmlText = htmlText;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getCardImageBase64() {
        return cardImageBase64;
    }

    public void setCardImageBase64(String cardImageBase64) {
        this.cardImageBase64 = cardImageBase64;
    }

    public String getCardUri() {
        return cardUri;
    }

    public void setCardUri(String cardUri) {
        this.cardUri = cardUri;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
