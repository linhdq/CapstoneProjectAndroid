package com.ldz.fpt.businesscardscannerandroid.activity.ocr_language_activity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by linhdq on 6/6/17.
 */

public class InstallStatus implements Parcelable {
    private boolean isInstalled;
    private long installedSize;

    public InstallStatus(boolean isInstalled, long installedSize) {
        this.isInstalled = isInstalled;
        this.installedSize = installedSize;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public long getInstalledSize() {
        return installedSize;
    }

    public boolean isNew() {
        return !isInstalled;
    }

    protected InstallStatus(Parcel in) {
        isInstalled = in.readByte() != 0;
        installedSize = in.readLong();
    }

    public static final Creator<InstallStatus> CREATOR = new Creator<InstallStatus>() {
        @Override
        public InstallStatus createFromParcel(Parcel in) {
            return new InstallStatus(in);
        }

        @Override
        public InstallStatus[] newArray(int size) {
            return new InstallStatus[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isInstalled ? 1 : 0));
        dest.writeLong(installedSize);
    }
}
