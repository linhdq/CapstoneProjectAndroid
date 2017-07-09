package com.ldz.fpt.businesscardscannerandroid;

public class PermissionGrantedEvent {
    private final String mPermission;

    public PermissionGrantedEvent(String permission) {
        mPermission = permission;
    }

    public String getPermission() {
        return mPermission;
    }
}
