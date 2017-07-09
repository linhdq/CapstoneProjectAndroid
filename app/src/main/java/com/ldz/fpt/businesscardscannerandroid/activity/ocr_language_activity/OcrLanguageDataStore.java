package com.ldz.fpt.businesscardscannerandroid.activity.ocr_language_activity;

import android.content.Context;

import com.ldz.fpt.businesscardscannerandroid.R;
import com.ldz.fpt.businesscardscannerandroid.utils.Util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linhdq on 6/6/17.
 */

public class OcrLanguageDataStore {
    private static final File[] EMPTY_FILE_ARRAY = new File[0];

    public static List<OcrLanguage> getInstalledOCRLanguages(Context appContext) {
        final List<OcrLanguage> ocrLanguages = getAvailableOcrLanguages(appContext);
        final List<OcrLanguage> result = new ArrayList<>();
        for (OcrLanguage lang : ocrLanguages) {
            if (lang.isInstalled()) {
                result.add(lang);
            }
        }
        return result;
    }

    public static String getInstalledOCRLanguagesCode(Context context) {
        String languageCodes = "";
        List<OcrLanguage> ocrLanguages = getAvailableOcrLanguages(context);
        for (OcrLanguage language : ocrLanguages) {
            if (language.isInstalled()) {
                if (languageCodes.equals("")) {
                    languageCodes += language.getLanguageCode();
                } else {
                    languageCodes += "+" + language.getLanguageCode();
                }
            }
        }
        return languageCodes;
    }

    public static List<OcrLanguage> getAvailableOcrLanguages(Context context) {
        List<OcrLanguage> languages = new ArrayList<>();
        // actual values uses by tesseract
        final String[] languageValues = context.getResources().getStringArray(R.array.ocr_languages);
        // values shown to the user
        final String[] languageDisplayValues = new String[languageValues.length];
        for (int i = 0; i < languageValues.length; i++) {
            final String val = languageValues[i];
            final int firstSpace = val.indexOf(' ');
            languageDisplayValues[i] = languageValues[i].substring(firstSpace + 1, languageValues[i].length());
            languageValues[i] = languageValues[i].substring(0, firstSpace);
        }
        for (int i = 0; i < languageValues.length; i++) {
            final InstallStatus installStatus = isLanguageInstalled(languageValues[i], context);
            OcrLanguage language = new OcrLanguage(languageValues[i], languageDisplayValues[i], installStatus.isInstalled(), installStatus.getInstalledSize());
            languages.add(language);
        }
        return languages;
    }


    public static InstallStatus isLanguageInstalled(final String ocrLang, Context context) {
        final File[] languageFiles = getAllFilesFor(ocrLang, context);
        if (languageFiles.length == 0) {
            return new InstallStatus(false, 0);
        }

        final boolean isInstalled = languageFiles.length >= 1;

        return new InstallStatus(isInstalled, sumFileSizes(languageFiles));

    }

    private static File[] getAllFilesFor(final String ocrLang, Context context) {
        final File tessDir = Util.getTrainingDataDir(context);
        if (!tessDir.exists()) {
            return EMPTY_FILE_ARRAY;
        }

        final File[] files = tessDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return isLanguageFileFor(pathname, ocrLang);
            }
        });
        if (files == null) {
            return EMPTY_FILE_ARRAY;
        } else {
            return files;
        }
    }

    private static long sumFileSizes(File[] languageFiles) {
        if (languageFiles == null) {
            return 0;
        }
        long sum = 0;
        for (File f : languageFiles) {
            sum += f.length();
        }
        return sum;
    }

    private static boolean isLanguageFileFor(File pathname, String ocrLang) {
        return pathname.getName().startsWith(ocrLang + ".") && pathname.isFile();
    }

    public static boolean deleteLanguage(OcrLanguage language, Context context) {
        final File[] languageFiles = getAllFilesFor(language.getLanguageCode(), context);
        if (languageFiles.length == 0) {
            language.setUninstalled();
            return false;
        }

        boolean success = true;
        boolean atLeastOneDeleted = false;

        for (File file : languageFiles) {
            final boolean deleted = file.delete();
            success &= deleted;
            atLeastOneDeleted |= deleted;
        }
        if (atLeastOneDeleted) {
            language.setUninstalled();
        }
        return success;
    }
}
