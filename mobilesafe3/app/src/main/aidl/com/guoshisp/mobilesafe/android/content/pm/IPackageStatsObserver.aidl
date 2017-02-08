// IPackageStatsObserver.aidl
package com.guoshisp.mobilesafe.android.content.pm;
import android.content.pm.PackageStats;
// Declare any non-default types here with import statements

 interface IPackageStatsObserver {

    void onGetStatsCompleted(in PackageStats pStats, boolean succeeded);
}

