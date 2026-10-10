package se.lublin.mumla.app;

import static androidx.appcompat.app.AppCompatDelegate.setApplicationLocales;
import static androidx.core.os.LocaleListCompat.forLanguageTags;
import static androidx.core.os.LocaleListCompat.getEmptyLocaleList;
import static se.lublin.mumla.Settings.PREF_LANGUAGE;
import static se.lublin.mumla.Settings.PREF_THEME;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class MumlaApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener,
        Application.ActivityLifecycleCallbacks {
    private int mStartedActivities;
    private ForegroundListener mForegroundListener;

    public interface ForegroundListener {
        void onForegroundChanged(boolean foreground);
    }

    public boolean isForeground() {
        return mStartedActivities > 0;
    }

    public void setForegroundListener(@Nullable ForegroundListener listener) {
        mForegroundListener = listener;
        if (listener != null) {
            listener.onForegroundChanged(isForeground());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        applyTheme(preferences);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mStartedActivities++;
        if (mStartedActivities == 1 && mForegroundListener != null) {
            mForegroundListener.onForegroundChanged(true);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        mStartedActivities--;
        if (mStartedActivities == 0 && mForegroundListener != null) {
            mForegroundListener.onForegroundChanged(false);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, @Nullable String key) {
        if (key == null) {
            return;
        }
        switch (key) {
            case PREF_LANGUAGE:
                String language = preferences.getString(PREF_LANGUAGE, "system");
                setApplicationLocales(language.equals("system") ? getEmptyLocaleList() : forLanguageTags(language));
                break;
            case PREF_THEME:
                applyTheme(preferences);
                break;
        }
    }

    private static void applyTheme(SharedPreferences preferences) {
        // The "system" and "force*" values are new (see preference_notranslate.xml).
        // We let other (older) value result in system default theme, and write that
        // to the preference store.
        switch (preferences.getString(PREF_THEME, "system")) {
            case "forceLight":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "forceDark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                preferences.edit().putString(PREF_THEME, "system").apply();
                break;
        }
    }
}
