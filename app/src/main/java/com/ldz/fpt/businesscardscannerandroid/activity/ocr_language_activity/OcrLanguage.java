package com.ldz.fpt.businesscardscannerandroid.activity.ocr_language_activity;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by linhdq on 6/6/17.
 */

public class OcrLanguage implements Parcelable {
    private boolean downloading;
    private long downloadId;
    private String languageCode;
    private String displayText;
    private InstallStatus installStatus;

    public OcrLanguage() {
    }

    public OcrLanguage(final String languageCode, final String displayText, boolean installed, long size) {
        this.installStatus = new InstallStatus(installed, size);
        this.languageCode = languageCode;
        this.displayText = displayText;
    }

    protected OcrLanguage(Parcel in) {
        downloading = in.readByte() != 0;
        languageCode = in.readString();
        displayText = in.readString();
        installStatus = (InstallStatus) in.readValue(InstallStatus.class.getClassLoader());
    }

    public static final Creator<OcrLanguage> CREATOR = new Creator<OcrLanguage>() {
        @Override
        public OcrLanguage createFromParcel(Parcel in) {
            return new OcrLanguage(in);
        }

        @Override
        public OcrLanguage[] newArray(int size) {
            return new OcrLanguage[size];
        }
    };

    public Uri getDownloadUri() {
        String extension = ".traineddata";
        String rootUrl = "https://raw.githubusercontent.com/tesseract-ocr/tessdata/master/";
        return Uri.parse(rootUrl + getLanguageCode() + extension);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (downloading ? 1 : 0));
        dest.writeString(languageCode);
        dest.writeString(displayText);
        dest.writeValue(installStatus);
    }

    @Override
    public String toString() {
        return getDisplayText();
    }

    public boolean isInstalled() {
        return installStatus.isInstalled();
    }

    public boolean isNew() {
        return installStatus.isNew();
    }

    public void setUninstalled() {
        installStatus = new InstallStatus(false, 0);
    }

    public void setInstallStatus(InstallStatus installStatus) {
        this.installStatus = installStatus;
    }

    public long getSize() {
        return installStatus.getInstalledSize();
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getDisplayText() {
        return displayText;
    }

    public boolean isDownloading() {
        return downloading;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }
}
