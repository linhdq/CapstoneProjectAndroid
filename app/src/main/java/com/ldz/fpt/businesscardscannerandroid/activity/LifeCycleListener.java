package com.ldz.fpt.businesscardscannerandroid.activity;

/**
 * Created by linhdq on 6/15/17.
 */

public interface LifeCycleListener {
    void onActivityCreated(MonitorActivity activity);

    void onActivityDestroyed(MonitorActivity activity);

    void onActivityPaused(MonitorActivity activity);

    void onActivityResumed(MonitorActivity activity);

    void onActivityStarted(MonitorActivity activity);

    void onActivityStopped(MonitorActivity activity);
}
