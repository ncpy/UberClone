package com.example.uberclone;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("DASGPC1NCdptJRYyxSedttPQTLG6AbxYOY3wtz4q")
                .clientKey("hOO3JELy2THXjS0DCVhQojNrRQV3BkrgafyHWyI2")
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}
