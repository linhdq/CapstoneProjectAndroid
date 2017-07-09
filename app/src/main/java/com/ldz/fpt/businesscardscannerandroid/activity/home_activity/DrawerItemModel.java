package com.ldz.fpt.businesscardscannerandroid.activity.home_activity;

/**
 * Created by linhdq on 4/18/17.
 */

public class DrawerItemModel {
    private int icon;
    private String title;

    public DrawerItemModel(int icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
