package com.example.ft_hangout;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Locale;

public class Global extends Application {

    public static String toolbarColor = "#4CAF50";
    public static String appLang = Locale.getDefault().getLanguage();

}
