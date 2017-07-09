package com.ldz.fpt.businesscardscannerandroid.database.db_context;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ldz.fpt.businesscardscannerandroid.activity.ocr_language_activity.OcrLanguage;
import com.ldz.fpt.businesscardscannerandroid.database.model.CardHistoryModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linhdq on 6/27/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "business_card_scanner";

    // Table name
    private static final String TABLE_OCR_LANGUAGE = "ocr_language";
    private static final String TABLE_CARD_HISTORY = "card_history";

    // Ocr language table columns names
    private static final String L_CODE = "l_code";
    private static final String L_DISPLAY = "l_display";
    private static final String INSTALLED = "installed";
    private static final String SIZE = "size";
    private static final String IS_DOWLOADING = "is_downloading";
    private static final String DOWNLOAD_ID = "download_id";

    // Card history table columns names
    private static final String ID = "id";
    private static final String CONTACT_NAME = "contact_name";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String EMAIL = "email";
    private static final String CARD_BASE64 = "card_base64";
    private static final String CARD_URI = "card_uri";
    private static final String HTML_TEXT = "html_text";

    // Ocr language table create statement
    private static final String CREATE_OCR_LANGUAGE_TABLE = "CREATE TABLE " + TABLE_OCR_LANGUAGE + "("
            + L_CODE + " TEXT PRIMARY KEY," + L_DISPLAY + " TEXT," + INSTALLED + " INTEGER DEFAULT 0, "
            + SIZE + " LONG DEFAULT 0, " + IS_DOWLOADING + " INTEGER DEFAULT 0, " + DOWNLOAD_ID + " LONG DEFAULT 0)";

    // Card history table create statement
    private static final String CREATE_CARD_HISTORY_TABLE = "CREATE TABLE " + TABLE_CARD_HISTORY + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CONTACT_NAME + " TEXT, " + PHONE_NUMBER
            + " TEXT," + EMAIL + " TEXT, " + CARD_BASE64 + " TEXT, "
            + CARD_URI + " TEXT, " + HTML_TEXT + " TEXT)";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_OCR_LANGUAGE_TABLE);
        db.execSQL(CREATE_CARD_HISTORY_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OCR_LANGUAGE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARD_HISTORY);
        // Create tables again
        onCreate(db);
    }

    /*
    * OCR Language Table
    * */

    public void addOCRLanguage(OcrLanguage model) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(L_CODE, model.getLanguageCode());
        values.put(L_DISPLAY, model.getDisplayText());
        values.put(INSTALLED, model.isInstalled() ? 1 : 0);
        values.put(SIZE, model.getSize());
        values.put(IS_DOWLOADING, model.isDownloading() ? 1 : 0);
        values.put(DOWNLOAD_ID, model.getDownloadId());

        // Inserting Row
        db.insert(TABLE_OCR_LANGUAGE, null, values);
        db.close(); // Closing database connection
    }

    public List<OcrLanguage> getAllOCRLanguage() {
        List<OcrLanguage> list = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_OCR_LANGUAGE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                OcrLanguage model = new OcrLanguage(cursor.getString(0), cursor.getString(1),
                        cursor.getInt(2) == 1, cursor.getLong(3));
                model.setDownloading(cursor.getInt(4) == 1);
                model.setDownloadId(cursor.getLong(5));
                // Adding model to list
                list.add(model);
            } while (cursor.moveToNext());
        }
        // return list
        return list;
    }

    public int updateOCRLanguageFull(OcrLanguage model) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(L_CODE, model.getLanguageCode());
        values.put(L_DISPLAY, model.getDisplayText());
        values.put(INSTALLED, model.isInstalled() ? 1 : 0);
        values.put(SIZE, model.getSize());
        values.put(IS_DOWLOADING, model.isDownloading() ? 1 : 0);
        values.put(DOWNLOAD_ID, model.getDownloadId());

        // updating row
        return db.update(TABLE_OCR_LANGUAGE, values, L_CODE + " = ?",
                new String[]{model.getLanguageCode()});
    }

    public int updateOCRLanguage(OcrLanguage model) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(L_DISPLAY, model.getDisplayText());
        values.put(INSTALLED, model.isInstalled() ? 1 : 0);
        values.put(SIZE, model.getSize());

        // updating row
        return db.update(TABLE_OCR_LANGUAGE, values, L_CODE + " = ?",
                new String[]{model.getLanguageCode()});
    }

    public boolean checkLanguageCodeIsExists(String code) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_OCR_LANGUAGE, new String[]{L_CODE}, L_CODE + "= ?",
                new String[]{code}, null, null, null, null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            return true;
        }

        return false;
    }

    public OcrLanguage getOCRLanguageByCode(String code) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_OCR_LANGUAGE, new String[]{L_CODE,
                        L_DISPLAY, INSTALLED, SIZE, IS_DOWLOADING, DOWNLOAD_ID}, L_CODE + "= ?",
                new String[]{code}, null, null, null, null);
        OcrLanguage model = null;
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            model = new OcrLanguage(cursor.getString(0), cursor.getString(1),
                    cursor.getInt(2) == 1, cursor.getLong(3));
            model.setDownloading(cursor.getInt(4) == 1);
            model.setDownloadId(cursor.getLong(5));
        }

        return model;
    }

    /*
    * Card History Table
    * */

    public void addCardHistory(CardHistoryModel model) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CONTACT_NAME, model.getContactName());
        values.put(PHONE_NUMBER, model.getPhoneNumber());
        values.put(EMAIL, model.getEmailAddress());
        values.put(CARD_BASE64, model.getCardImageBase64());
        values.put(CARD_URI, model.getCardUri());
        values.put(HTML_TEXT, model.getHtmlText());

        // Inserting Row
        db.insert(TABLE_CARD_HISTORY, null, values);
        db.close(); // Closing database connection
    }

    public List<CardHistoryModel> getAllCardHistory() {
        List<CardHistoryModel> list = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CARD_HISTORY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        int id;
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
                CardHistoryModel model = new CardHistoryModel(cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
                model.setId(id);
                // Adding model to list
                list.add(model);
            } while (cursor.moveToNext());
        }
        // return list
        return list;
    }

    public void deleteCardHistoryById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CARD_HISTORY, ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public int updateCardHistory(CardHistoryModel model) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CONTACT_NAME, model.getContactName());
        values.put(PHONE_NUMBER, model.getPhoneNumber());
        values.put(EMAIL, model.getEmailAddress());
        values.put(CARD_BASE64, model.getCardImageBase64());
        values.put(CARD_URI, model.getCardUri());
        values.put(HTML_TEXT, model.getHtmlText());

        // updating row
        return db.update(TABLE_CARD_HISTORY, values, ID + " = ?",
                new String[]{String.valueOf(model.getId())});
    }

    public boolean checkCardHistoryIsExists(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CARD_HISTORY, new String[]{ID}, ID + "= ?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            return true;
        }

        return false;
    }
}
